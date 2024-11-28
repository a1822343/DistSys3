package member;

import enums.MessageTypes;
import message.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

public class MemberImpl implements Member {
    // This is purely for testing purposes
    // Hardcodes a predefined latency into the Member's response time
    // Sleep thread for "latency" milliseconds before responding
    AtomicInteger latency;

    // The one-based voting preference of Member e.g. M1 has voting preference 1, M9 has 0.
    // A 0 preference means the member has no preference,
    // with a 50/50 chance of voting for the proposed member
    int preference;

    // Used to check if proposal n from proposer is less than any proposal accepted before
    // Should always be max(n, proposalID)
    AtomicInteger proposalID;

    // The currently proposed candidate
    AtomicInteger proposedNominee;

    // the most recently accepted proposalID
    AtomicInteger acceptedID;

    // the most recently accepted proposed nominee
    AtomicInteger acceptedNominee;

    // The port number of the node
    int port;

    int membersCount;
    // The number of members considered a majority
    int majority;

    int acceptors;
    int votes;

    // represents the availability of this member
    boolean available;

    List<Socket> members;

    List<Integer> decisions;

    // THREE CONSTRUCTORS
    // this constructs a member with no preferences, with port "_port"
    public MemberImpl(int _port, int _membersCount) {
        init(_port, _membersCount, 0, 0);
    }

    // this constructs a member with a preference for "_preference", with port "_port"
    public MemberImpl(int _port, int _membersCount, int _preference) {
        init(_port, _membersCount, _preference, 0);
    }

    // this is a constructor for a test member with a preference, with an intentional latency
    public MemberImpl(int _port, int _membersCount, int _preference, int _latency) {
        init(_port, _membersCount, _preference, _latency);
    }

    // sets up the Member as part of the peer network
    public void init(int _port, int _membersCount, int _preference, int _latency) {
        this.latency = new AtomicInteger(_latency);
        this.preference = _preference;
        this.proposalID = new AtomicInteger(-1);
        this.proposedNominee = new AtomicInteger(0);
        this.acceptedID = new AtomicInteger(-1);
        this.acceptedNominee = new AtomicInteger(0);
        this.port = _port;
        this.membersCount = _membersCount;
        this.majority = membersCount / 2 + 1;
        this.members = new ArrayList<>();
        this.decisions = new ArrayList<>();
        available = true;
        startMember();
    }

    public void startMember() {
        Thread memberThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                // while the server is available for communication
                while (available) {
                    Socket member = serverSocket.accept();
                    new Thread(() -> handleMemberConnection(member)).start();
                }
                serverSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        });
        memberThread.start();
    }

    // connects this Member to all other Members
    public void establishConnections() throws IOException {
        for (int i = 0; i < membersCount; i++) {
            int _port = 2048 + i;
            if (_port != port) {
                Socket socket = new Socket("localhost", _port);
                members.add(socket);
            }
        }
        //System.out.print("Established connections with: ");
        //for (Socket member : members) System.out.print("Member, ");
        //System.out.println();
    }

    public void handleMemberConnection(Socket member) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(member.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(member.getOutputStream()))
        ) {
            String message = readMessage(in);
            System.out.println(port + " " + message);
            Message response = actOnMessage(message);
            //System.out.println(response);
            if (response != null) sendMessage(out, response);
            out.flush();
            member.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void propagateMessage(Message message) {
        for (Socket member : members) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(member.getOutputStream()))) {
                sendMessage(bw, message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // needed a separate function that could be called by itself to retry communicating with a member after a failure
    public void sendMessage(BufferedWriter member, Message message) {
        try {
            //System.out.println(message.asString());
            message.send(member);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            sendMessage(member, message);
        }
    }

    public String readMessage(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();

        String line = "";
        while ((line = in.readLine()) != null) {
            if (line.equals("EOM")) break;
            response.append(line + "\n");
        }

        return response.toString();
    }

    public Message actOnMessage(String message) {
        //System.out.println(port + "\n" + message);
        // Grab the status from the messages first line
        String header = message.split("\n")[0];
        int type = Integer.parseInt(header.split(" ")[1]);
        JSONParser p = new JSONParser();
        JSONObject body = null;
        int proposedID = 0;
        int nominee = 0;
        try {
            // this should support bodies like { "key": value } and
            // {
            //   "key": value
            // }
            body = (JSONObject) p.parse(message.split(header + "\n")[1]);
            if (!body.isEmpty()) {
                proposedID = Integer.parseInt(body.get("n").toString());
                nominee = Integer.parseInt(body.get("nominee").toString());
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }

        Message response;
        switch (type) {
            // PROPOSE
            case 100:
                response = respondPromise(proposedID);
                acceptedID.set(proposedID);
                acceptedNominee.set(nominee);
                break;
            // PREPARE-OK
            case 101:
                System.out.println("Got promises");
                acceptors++;
                if (acceptors == majority) {
                    System.out.println("Gathering acceptance");
                    propagateAcceptRequest();
                }
                return null;
            // NACK
            case 102:
                return null;
            // ACCEPT-REQUEST
            case 200:
                response = respondAcceptDecision();
                break;
            // ACCEPT-OK
            case 201:
                votes++;
                if (votes == majority) propagateDecide(proposedNominee.intValue());
                return null;
            // ACCEPT-REJECT
            case 202:
                return null;
            // DECIDE
            case 300:
                decide(nominee);
                return null;
            default:
                return null;
        }

        if (acceptors - votes >= majority) return null;

        return response;
    }

    // this node becomes the leader for the (proposalCount + 1)th proposal
    @Override
    public Message propagatePropose(int _nominee) {
        acceptors = 0;
        votes = 0;
        if (preference == 0) proposedNominee.set(_nominee);
        else proposedNominee.set(preference);

        JSONObject payload = new JSONObject();
        payload.put("n", proposalID.incrementAndGet());
        payload.put("nominee", proposedNominee.intValue());

        Message proposal = new Message(MessageTypes.PROPOSE, payload);

        propagateMessage(proposal);

        return proposal;
    }

    // this node responds to the proposal leader with a promise
    @Override
    public Message respondPromise(int _proposalID) {
        JSONObject payload = new JSONObject();
        // if this member has accepted any proposals before
        if (acceptedID.intValue() != 0 && acceptedNominee.intValue() != 0) {
            payload.put("n", acceptedID.intValue());
            payload.put("nominee", acceptedNominee.intValue());
        }


        Message promise;
        if (_proposalID <= acceptedID.intValue()) {
            promise = new Message(MessageTypes.NACK, payload);
        } else {
            promise = new Message(MessageTypes.PREPARE_OK, payload);
            System.out.println("Promising...");
        }

        // artificial latency
        try {
            sleep(latency.longValue());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return promise;
    }


    @Override
    public Message propagateAcceptRequest() {
        Message acceptRequest = new Message(MessageTypes.ACCEPT_REQUEST, null);

        // artificial latency
        try {
            sleep(latency.longValue());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        propagateMessage(acceptRequest);
        return acceptRequest;
    }

    @Override
    public Message respondAcceptDecision() {
        Message acceptDecision;
        // if member has a preference, vote according to that preference
        if (preference != 0) {
            if (proposedNominee.intValue() == preference) {
                acceptDecision = new Message(MessageTypes.ACCEPT_OK, null);
                System.out.println("Accepting...");
            } else {
                acceptDecision = new Message(MessageTypes.ACCEPT_REJECT, null);
                System.out.println("Rejecting...");
            }
        } else {
            // if member has no preference, accept randomly
            if ((int) (Math.random() * 2) == 0) {
                acceptDecision = new Message(MessageTypes.ACCEPT_OK, null);
            } else {
                acceptDecision = new Message(MessageTypes.ACCEPT_REJECT, null);
            }
        }


        // artificial latency
        try {
            sleep(latency.longValue());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        return acceptDecision;
    }

    @Override
    public Message propagateDecide(int nominee) {
        JSONObject payload = new JSONObject();
        payload.put("nominee", nominee);
        Message decision = new Message(MessageTypes.DECIDE, payload);

        // artificial latency
        try {
            sleep(latency.longValue());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        propagateMessage(decision);
        decide(nominee);
        return decision;
    }

    public void decide(int nominee) {
        decisions.add(nominee);
    }

    // like a self-destruct button
    @Override
    public void forceStop() {
        available = false;
    }

    public List<Integer> getDecisions() {
        return decisions;
    }
}

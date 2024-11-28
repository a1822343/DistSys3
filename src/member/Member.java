package member;

import message.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public interface Member {
    void init(int _port, int _membersCount, int _preference, int _latency);
    void establishConnections() throws IOException;
    void handleMemberConnection(Socket member);
    Message propagatePropose(int _nominee);
    Message respondPromise(int _proposalID);
    Message propagateAcceptRequest();
    Message respondAcceptDecision();
    Message propagateDecide();
    void decide(int nominee);

    void sendMessage(Socket member, Message message);
    String readMessage(BufferedReader member) throws IOException;
    boolean actOnMessage(String message, BufferedWriter out);

    // Mostly for testing purposes
    // Forces Member to go offline
    void forceStop();

    List<Integer> getDecisions();
}

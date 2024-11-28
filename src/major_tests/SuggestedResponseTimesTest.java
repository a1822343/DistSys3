package major_tests;

import member.Member;
import member.MemberImpl;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuggestedResponseTimesTest {
    int MEMBERS_COUNT = 9;
    @Test
    public void M4M9Immediate() {

    }

    @Test
    public void M4M9Staggered() {

    }

    @Test
    public void M2ProposeDisconnect() {
        Member[] members = new Member[MEMBERS_COUNT];

        members[0] = new MemberImpl(2048, MEMBERS_COUNT, 1, 100);
        members[1] = new MemberImpl(2049, MEMBERS_COUNT, 2, 100);
        members[2] = new MemberImpl(2050, MEMBERS_COUNT, 3, 100);
        for (int i = 3; i < MEMBERS_COUNT; i++) {
            members[i] = new MemberImpl(2048 + i, MEMBERS_COUNT, 100);
        }

        // Once all serverSockets have been initialised
        for (Member member : members) {
            try {
                member.establishConnections();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        members[1].propagatePropose(2);
        members[1].forceStop();
        List<Integer> expectedDecisions = new ArrayList<>();
        expectedDecisions.add(1);
        System.out.println("Expecting: " + expectedDecisions);
        System.out.println("Received: ");
        for (int i = 0; i < MEMBERS_COUNT; i++) {
            System.out.println(members[i].getDecisions());
        }
        for (int i = 0; i < MEMBERS_COUNT; i++) {
            assertEquals(expectedDecisions, members[i].getDecisions());
        }
    }
}

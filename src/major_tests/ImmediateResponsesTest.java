package major_tests;

import member.Member;
import member.MemberImpl;
import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class ImmediateResponsesTest {
    final int MEMBERS_COUNT = 9;
    @Test
    public void M1Propose() {
        Member[] members = new Member[MEMBERS_COUNT];

        members[0] = new MemberImpl(2048, MEMBERS_COUNT, 1);
        members[1] = new MemberImpl(2049, MEMBERS_COUNT, 2);
        members[2] = new MemberImpl(2050, MEMBERS_COUNT, 3);
        for (int i = 3; i < MEMBERS_COUNT; i++) {
            members[i] = new MemberImpl(2048 + i, MEMBERS_COUNT);
        }

        // Once all serverSockets have been initialised
        for (Member member : members) {
            try {
                member.establishConnections();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        members[0].propagatePropose(1);
        List<Integer> expectedDecisions = new ArrayList<>();
        expectedDecisions.add(1);
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Expecting: " + expectedDecisions);
        System.out.println("Received: ");
        for (int i = 0; i < MEMBERS_COUNT; i++) {
            System.out.println(members[i].getDecisions());
        }
    }

    public void M1M2Propose() {
        Member[] members = new Member[MEMBERS_COUNT];

        members[0] = new MemberImpl(2048, MEMBERS_COUNT, 1);
        members[1] = new MemberImpl(2049, MEMBERS_COUNT, 2);
        members[2] = new MemberImpl(2050, MEMBERS_COUNT, 3);
        for (int i = 3; i < MEMBERS_COUNT; i++) {
            members[i] = new MemberImpl(2048 + i, MEMBERS_COUNT);
        }

        // Once all serverSockets have been initialised
        for (Member member : members) {
            try {
                member.establishConnections();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        members[0].propagatePropose(1);
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

    public void M4M5Propose() {
        Member[] members = new Member[MEMBERS_COUNT];

        members[0] = new MemberImpl(2048, MEMBERS_COUNT, 1);
        members[1] = new MemberImpl(2049, MEMBERS_COUNT, 2);
        members[2] = new MemberImpl(2050, MEMBERS_COUNT, 3);
        for (int i = 3; i < MEMBERS_COUNT; i++) {
            members[i] = new MemberImpl(2048 + i, MEMBERS_COUNT);
        }

        // Once all serverSockets have been initialised
        for (Member member : members) {
            try {
                member.establishConnections();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        members[3].propagatePropose(1);
        members[4].propagatePropose(2);
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

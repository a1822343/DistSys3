package major_tests;


import member.Member;
import member.MemberImpl;
import org.junit.*;

import java.io.IOException;

public class DualProposeTest {
    final int MEMBERS_COUNT = 9;
    @Test
    public void M1M2Propose() {
        Member[] members = new Member[MEMBERS_COUNT];

        members[0] = new MemberImpl(2048, MEMBERS_COUNT, 1);
        members[1] = new MemberImpl(2049, MEMBERS_COUNT, 2);
        members[2] = new MemberImpl(2050, MEMBERS_COUNT, 3);
        for (int i = 3; i < MEMBERS_COUNT; i++) {
            members[i] = new MemberImpl(2048 + i, MEMBERS_COUNT);
        }

        for (Member member : members) {
            try {
                member.establishConnections();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        members[0].propagatePropose(1);
        members[1].propagatePropose(2);
    }
}

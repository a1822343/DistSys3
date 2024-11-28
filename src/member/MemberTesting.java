package member;

import org.junit.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MemberTesting {
    @Test
    public void TwoMembers() {
        Member[] members = new Member[3];
        for (int i = 0; i < 3; i++) {
            members[i] = new MemberImpl(2048 + i, 3);
        }

        for (int i = 0; i < 3; i++) {
            try {
                members[i].establishConnections();
            } catch (IOException e) {
                System.out.println("In Test: " + e.getMessage());
            }
        }

        members[0].propagatePropose(1);

        List<Integer> expectedDecisions = new ArrayList<>();
        expectedDecisions.add(1);
        for (Member member : members) {
            assertEquals(expectedDecisions, member.getDecisions());
        }
    }
}

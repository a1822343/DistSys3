package member;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MemberTests {
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

    @Test
    public void EmptyBody() {
        String empty = new JSONObject().toJSONString();
        JSONParser p = new JSONParser();
        JSONObject body = null;
        try {
            body = (JSONObject) p.parse(empty);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(body.isEmpty());
    }

    @Test
    public void AccessNonExistentEntry() {
        String empty = new JSONObject().toJSONString();
        JSONParser p = new JSONParser();
        JSONObject body = null;
        try {
            body = (JSONObject) p.parse(empty);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        System.out.println(body.get("halogen"));
    }
}

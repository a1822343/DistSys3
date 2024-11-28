package message;

import enums.MessageTypes;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public record Message(MessageTypes statusCode, JSONObject payload) {
    public String asString() {
        StringBuilder message = new StringBuilder();
        switch (statusCode) {
            case PROPOSE:
                message.append("DS3/1.0 100 PROPOSE");
                break;
            case PREPARE_OK:
                message.append("DS3/1.0 101 PREPARE-OK");
                break;
            case NACK:
                message.append("DS3/1.0 102 NACK");
                break;
            case ACCEPT_REQUEST:
                message.append("DS3/1.0 200 ACCEPT-REQUEST");
                break;
            case ACCEPT_OK:
                message.append("DS3/1.0 201 ACCEPT-OK");
                break;
            case ACCEPT_REJECT:
                message.append("DS3/1.0 202 ACCEPT-REJECT");
                break;
            case DECIDE:
                message.append("DS3/1.0 300 DECIDE");
                break;
            default:
                break;
        }
        message.append('\n').append(payload.toJSONString()).append("\nEOM");
        return message.toString();
    }

    // Send message
    public void send(BufferedWriter bw) throws IOException {
        bw.write(this.asString());
        bw.newLine();
        bw.flush();
    }
}

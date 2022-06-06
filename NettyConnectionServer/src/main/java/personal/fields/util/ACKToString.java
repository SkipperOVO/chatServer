package personal.fields.util;

import personal.fields.protocol.ChatProtocol;

public class ACKToString {

    public static String ackString(ChatProtocol.ChatProtoPack ack) {
        return "{ ACK: " + ack.getAck().getAck() + ", SEQ: " + ack.getAck().getSeq() + " }";
    }
}

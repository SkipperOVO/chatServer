package personal.fields.util;

import personal.fields.protocol.ChatProtocol;

import static personal.fields.constant.Constant.PROTO_VERSION;

public class MessageHelper {

    public static ChatProtocol.ChatProtoPack buildS2CNotifyMsg(
            Integer version,
            Integer fromId,
            Integer toId,
            String msg,
            Integer msgId,
            Integer seq) {

        return ChatProtocol.ChatProtoPack.newBuilder()
                .setVersion(version)
                .setS2CNotifyMsg(ChatProtocol.S2CNotifyMsg.newBuilder()
                        .setVersion(version)
                        .setFromId(fromId)
                        .setToId(toId)
                        .setMsg(msg)
                        .setSeq(seq)
                        .setMsgId(msgId).build()).build();
    }


    public static ChatProtocol.ChatProtoPack buildS2CNotifyMsg(ChatProtocol.C2CSendReq msg, Integer msgId, Integer seq ) {

        return ChatProtocol.ChatProtoPack.newBuilder()
                .setVersion(PROTO_VERSION)
                .setS2CNotifyMsg(ChatProtocol.S2CNotifyMsg.newBuilder()
                        .setVersion(PROTO_VERSION)
                        .setFromId(msg.getFromId())
                        .setToId(msg.getToId())
                        .setMsg(msg.getMsg())
                        .setSeq(seq)
                        .setMsgId(msgId).build()).build();
    }

    public static ChatProtocol.ChatProtoPack buildACK( Integer seq) {
        return ChatProtocol.ChatProtoPack.newBuilder()
                .setVersion(PROTO_VERSION)
                .setAck(ChatProtocol.ACK.newBuilder()
                .setAck(seq+1)
                .setSeq(Seq.generate()).build()).build();
    }
}

package personal.fields.util;

import personal.fields.protocol.C2CSendReq;
import personal.fields.protocol.ChatProtocol;
import personal.fields.protocol.NotifyMsgText;

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


    public static NotifyMsgText buildS2CNotifyText(C2CSendReq c2cSendReq, Integer msgId, Integer seq) {
        NotifyMsgText notifyMsgText = new NotifyMsgText();
        notifyMsgText.setFromId(c2cSendReq.getFromId());
        notifyMsgText.setToId(c2cSendReq.getToId());
        notifyMsgText.setMsg(c2cSendReq.getMsg());
        notifyMsgText.setMsgId(msgId);
        notifyMsgText.setSeq(seq);
        notifyMsgText.setVersion(1);
        return notifyMsgText;
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

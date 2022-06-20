package personal.fields.util;

import personal.fields.protocol.ChatProtocol;
import personal.fields.protocol.logicProtocol.C2CSendReq;

public class ProtoToProto {

    public static C2CSendReq C2CSendGoogleToC2C(ChatProtocol.C2CSendReq c2cSendReq) {
        C2CSendReq res = new C2CSendReq();
        res.setFromId(c2cSendReq.getFromId());
        res.setToId(c2cSendReq.getToId());
        res.setMsg(c2cSendReq.getMsg());
        res.setSeq(c2cSendReq.getSeq());
        res.setVersion(c2cSendReq.getVersion());
        return res;
    }
}

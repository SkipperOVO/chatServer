package personal.fields.protocol.logicProtocol;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class NotifyMsgText {

    private Integer version;

    private Integer fromId;

    private Integer toId;

    private String msg;

    private Integer msgId;

    private Integer seq;

    private Integer type;


    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }


    @Override
    public boolean equals(Object o) {
        NotifyMsgText obj = (NotifyMsgText) o;
        return obj.version == this.version
                && obj.fromId == this.fromId
                && obj.toId == this.toId
                && obj.msg.equals(this.msg)
                && obj.msgId == this.msgId
                && obj.seq == this.seq
                && obj.type == this.type;
    }

    @Override
    public int hashCode() {
        return (fromId << 16 & toId) & msg.hashCode() & msgId & seq & type;
    }

}

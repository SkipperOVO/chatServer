package personal.fields.protocol.logicProtocol;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class NotifyPendingPackText {

    private NotifyMsgText notifyPack;

    private long sendTime;

    private int retryTimes = 0;

    public NotifyPendingPackText(NotifyMsgText notifyPack, long mills) {
        this.notifyPack = notifyPack;
        this.sendTime = mills;
    }


    public void incrementRetry() {
        this.retryTimes++;
    }

    @Override
    public String toString() {
        return notifyPack.toString() + " " + JSON.toJSONString(this);
    }
}

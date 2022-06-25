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

    @Override
    public boolean equals(Object o) {
        NotifyPendingPackText obj = (NotifyPendingPackText) o;
        return this.notifyPack.equals(obj.getNotifyPack())
                && this.sendTime == obj.sendTime
                && this.retryTimes == obj.retryTimes;
    }

    @Override
    public int hashCode() {
        return ((int) sendTime + retryTimes) & notifyPack.hashCode();
    }
}

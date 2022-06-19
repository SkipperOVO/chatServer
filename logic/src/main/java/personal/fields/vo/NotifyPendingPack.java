package personal.fields.vo;

import personal.fields.protocol.ChatProtocol;

public class NotifyPendingPack {

    private ChatProtocol.ChatProtoPack notifyPack;

    private long sendTime;

    private int retryTimes = 0;

    public NotifyPendingPack(ChatProtocol.ChatProtoPack notifyPack, long mills) {
        this.notifyPack = notifyPack;
        this.sendTime = mills;
    }


    public long getSendTime() {
        return sendTime;
    }

    public ChatProtocol.ChatProtoPack getNotifyPack() {
        return notifyPack;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setSendTime(long now) {
        this.sendTime = now;
    }

    public int getRetryTimes() {
        return retryTimes;
    }


    public void incrementRetry() {
        this.retryTimes++;
    }
}


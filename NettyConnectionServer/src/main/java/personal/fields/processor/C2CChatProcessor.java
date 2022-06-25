package personal.fields.processor;

import com.alibaba.fastjson.JSON;
import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.channelMap.ChannelMap;
import personal.fields.channelMap.SimpleHashMap;
import personal.fields.constant.AttrbuteSet;
import personal.fields.infrastructure.ioc.IOC.SpringIOC;
import personal.fields.protocol.ChatProtocol;
import personal.fields.util.ACKToString;
import personal.fields.util.MessageHelper;
import personal.fields.util.ProtoToProto;
import personal.fields.util.Seq;
import personal.fields.util.concurrent.DelayExecutorGroup;
import personal.fields.vo.NotifyPendingPack;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Predicate;

import static personal.fields.constant.AttrbuteSet.USER_ID;

/*

    Customer to Customer chat Processor

 */

public class C2CChatProcessor extends BaseProcessor {

    private static final Logger logger = LoggerFactory.getLogger(C2CChatProcessor.class);

    private ScheduledThreadPoolExecutor ackDaemon = new ScheduledThreadPoolExecutor(1);

    private Channel ch;

    public C2CChatProcessor(DelayExecutorGroup executor, ProcessorContainer container) {
        super(executor, container);

        // 启动定时任务检查队列中超时 notifyPack
//        this.ackDaemon.scheduleAtFixedRate(
//                new CheckTimeoutAndRetry2(container.getNotifyQueue()), 0, 1, TimeUnit.SECONDS);
        // 持续匹配 ACK 和 notify
//        this.ackDaemon.execute(
//                new ACKFetcher(container.getNotifyQueue(), container.getAckQueue()));
    }

    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {

        try {
            this.ch = ctx.channel();
            // Todo 发送落库MQ消息给 logic
            DefaultMQProducer mqProducer = this.container.getMqProducer();
            ChatProtocol.C2CSendReq sendReq = msg.getC2CSendReq();
            String reqJson = JSON.toJSONString(ProtoToProto.C2CSendGoogleToC2C(sendReq));
            Message message = new Message("C2CTopic_send", "TAG:c2csend", reqJson.getBytes(RemotingHelper.DEFAULT_CHARSET));
            SendResult sendResult = mqProducer.send(message);
            // 投递 send 消息到 MQ 成功, 返回客户端 ACK
            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                ChatProtocol.ChatProtoPack ack = ChatProtocol.ChatProtoPack.newBuilder()
                        .setVersion(1)
                        .setAck(ChatProtocol.ACK.newBuilder()
                                .setSeq(Seq.generate())
                                .setAck(msg.getC2CSendReq().getSeq() + 1).build()).build();
                ch.writeAndFlush(ack);
                logger.info("send 消息投递到 mq 成功！");
            } else {
                // MQ 出现拥挤或者故障，不返回 ACK，使系统不可用
                // 啥也不做
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送 send 消息到 logic 失败！");
        }


        // 以下代码 在 MQ 消息监听器中执行
        // 发送给客户端 ACK
//        ChatProtocol.ChatProtoPack ack = ChatProtocol.ChatProtoPack.newBuilder()
//                .setVersion(1)
//                .setAck(ChatProtocol.ACK.newBuilder()
//                        .setSeq(Seq.generate())
//                        .setAck(msg.getC2CSendReq().getSeq() + 1).build()).build();
//        ch.writeAndFlush(ack);
//
//
//        ChatProtocol.C2CSendReq sendReq = msg.getC2CSendReq();
//        Cache cache = new JedisCache();
//        // 查看消息接收方 clientB 是否在线
//        if (cache.get(ch.attr(USER_ID).get().toString()) != null) {
//            logger.info("用户B在线！给他推送消息了！");
//            // 接收方在线,启动推送消息流程
//            ChatProtocol.ChatProtoPack notifyMsg = MessageHelper.buildS2CNotifyMsg(sendReq, /*从消息队列获取的落库消息id msgId*/999, Seq.generate());
//            ChannelMap<Integer, Channel> channelIdMap = (SimpleHashMap) SpringIOC.getBean("getChannelIdMap");
//            Channel peerCh = channelIdMap.get(sendReq.getToId());
//            this.addNotifyPack(notifyMsg);
//            peerCh.writeAndFlush(notifyMsg);
//            logger.info("notify SEQ: " + notifyMsg.getS2CNotifyMsg().getSeq());
//
//            //  设置定时任务发送notify并接收 clientB 的 ACK
////            ACKFetch ackFetchTask = new ACKFetch(peerCh, ch, notifyMsg);
////            this.scheduleWithFixedDelay(ackFetchTask, 0, 1, TimeUnit.SECONDS);
//
//        } else {
//            logger.info("用户B不在线！启动离线消息过程");
//            //Todo 用户离线,启动离线消息过程,发送离线mq消息给 logic
//            // 需要监听返回结果，不成功需要不断重试
//
//        }

    }

    public void addNotifyPack(ChatProtocol.ChatProtoPack notifyMsg) {

        this.execute(() -> {
            try {
                // 并发容器 put 线程安全, 可能会阻塞，所以使用业务线程放入
                BlockingQueue<NotifyPendingPack> notify_queue = ch.attr(AttrbuteSet.NOTIFY_QUEUE).get();
                notify_queue.put(new NotifyPendingPack(notifyMsg, System.currentTimeMillis()));
            } catch (InterruptedException ine) {
                ine.printStackTrace();
                // 重试放入两次
            }
        });

    }


//    private class ACKFetcher implements Runnable {
//
//        private BlockingQueue<NotifyPendingPack> nQueue;
//
//        private BlockingQueue<ChatProtocol.ChatProtoPack> ackQueue;
//
//
//        public ACKFetcher(BlockingQueue<NotifyPendingPack> nQueue, BlockingQueue<ChatProtocol.ChatProtoPack> ackQueue) {
//            this.nQueue = nQueue;
//            this.ackQueue = ackQueue;
//        }
//
//        @Override
//        public void run() {
//
//            while (true) {
//                try {
//                    // 拉取出一个 ack 消息
//                    ChatProtocol.ChatProtoPack ack = ackQueue.take();
//                    // 从 notify 队列中找到这个ack对应的 notify
//                    int size = nQueue.size();
//                    for (int i = 0; i < size; ++i) {
//                        NotifyPendingPack notify = nQueue.peek();
//                        // ACK 匹配 notify
//                        if (notify.getNotifyPack().getS2CNotifyMsg().getSeq() == ack.getAck().getAck() + 1) {
//                            nQueue.poll();
//                            // 发送给 clientB ack 消息
//                            Channel peerCh = (Channel) ((SimpleHashMap)SpringIOC.getBean("getChannelIdMap"))
//                                    .get(notify.getNotifyPack().getS2CNotifyMsg().getToId());
//                            peerCh.writeAndFlush(ack.getAck().getSeq() + 1);
//                            logger.info("收到客户端对notify的ACK：");
//                            logger.info(ACKToString.ackString(ack));
//                            logger.info("推送消息成功！clientB已接收到");
//                        } else {
//                            // ACK 不匹配,ACK 可能乱序，所以抛弃
//                            logger.info("丢弃ACK消息" + ACKToString.ackString(ack));
//                            break;
//                        }
//                    }
//                } catch (InterruptedException ine) {
//                    ine.printStackTrace();
//                    // 处理
//                }
//
//            }
//        }
//    }
//
//    public class CheckTimeoutAndRetry2 implements Runnable {
//
//        private BlockingQueue<NotifyPendingPack> nQueue;
//
//        public CheckTimeoutAndRetry2(BlockingQueue<NotifyPendingPack> nQueue) {
//            this.nQueue = nQueue;
//        }
//
//        @Override
//        public void run() {
//            logger.info("check~~~~timeout");
//
//            if (nQueue.isEmpty())
//                return ;
//
//            SimpleHashMap<Integer, Channel> channelIdMap = (SimpleHashMap) SpringIOC.getBean("getChannelIdMap");
//            long now = System.currentTimeMillis();
//
//            nQueue.removeIf(new Predicate<NotifyPendingPack>() {
//                @Override
//                public boolean test(NotifyPendingPack nPending) {
//                    try {
//                        // 超时
//                        if ((now - nPending.getSendTime()) / 1000.0 >= 1) {
//                            // 重试次数小于 2,重新发送
//                            if (nPending.getRetryTimes() < 1) {
//                                int peerId = nPending.getNotifyPack().getS2CNotifyMsg().getToId();
//                                channelIdMap.get(peerId).writeAndFlush(nPending.getNotifyPack());
//                                nPending.incrementRetry();
//                                nPending.setSendTime(now);
//                                nQueue.put(nPending);
//                                logger.info("notify 消息超时，重新发送notify：SEQ:" + nPending.getNotifyPack().getS2CNotifyMsg().getSeq());
//                            } else {
//                                // Todo 启动离线消息过程
//                                logger.info("notify重试次数到达上限，推送消息失败！");
//                            }
//                            return true;
//                        } else
//                            return false;
//                    } catch (InterruptedException ine) {
//                        ine.printStackTrace();
//                        return false;
//                    }
//                }
//            });
//        }
//    }

//    private class ACKFetch implements Runnable {
//
//        private Channel ch;
//
//        private Channel peerCh;
//
//        private ChatProtocol.ChatProtoPack notifyMsg;
//
//        private volatile boolean isSend = false;
//
//        private volatile int retryTimes = 0;
//
//        public ACKFetch(Channel peerCh ,Channel ch, ChatProtocol.ChatProtoPack notifyMsg) {
//            this.peerCh = peerCh;
//            this.ch = ch;
//            this.notifyMsg = notifyMsg;
//        }
//
//        @Override
//        public void run() {
//
//            if (isSend == false) {
//                peerCh.writeAndFlush(notifyMsg);
//                isSend = true;
//            } else {
////                Attribute<Integer> ack = ch.attr(AttrbuteSet.)
//            }
//        }
//    }


}

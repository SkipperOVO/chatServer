package personal.fields.processor;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import personal.fields.channelMap.ChannelMap;
import personal.fields.constant.AttrbuteSet;
import personal.fields.infrastructure.ioc.IOC.SpringIOC;
import personal.fields.protocol.ChatProtocol;
import personal.fields.protocol.logicProtocol.NotifyPendingPackText;
import personal.fields.util.ProtoToProto;
import personal.fields.util.concurrent.DelayExecutorGroup;
import personal.fields.vo.NotifyPendingPack;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ProcessorContainer {

    private static final Logger logger = LoggerFactory.getLogger(ProcessorContainer.class);

    // 线程安全，只会初始化一次，不会发生更改
    private final HashMap<String /* DataBodyCase */, Processor> handlers = new HashMap<>();

//    private LinkedBlockingQueue<NotifyPendingPackText> notifyQueue = new LinkedBlockingQueue<>(64);

    private LinkedBlockingQueue<ChatProtocol.ChatProtoPack> ackQueue = new LinkedBlockingQueue<>(64);

//    private final ThreadPoolExecutor businessThreads = new ThreadPoolExecutor(
//            8, 256, 3*60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(64));

    private DelayExecutorGroup executors = new DelayExecutorGroup(4, 32);


    @Value("${servername}")
    private String serverName;


    @Value("${rocketmq.nameserver}")
    private String nameserver;


    private DefaultMQProducer mqProducer;


    private DefaultMQPushConsumer mqConsumer;


    public ProcessorContainer() {

    }

    @PostConstruct
    public void init() {
        // Todo 初始化 processor 应该从配置文件读或者 spring 上下文中读，省略
        this.addProcessor("HEARTBEATREQ", new HeartBeatProcessor(this.executor(), this));
        this.addProcessor("C2CSENDREQ", new C2CChatProcessor(this.executor(), this));
        this.addProcessor("ACK", new ACKProcessor(this.executor(), this));

        logger.info(serverName);
        logger.info(nameserver);
        this.initMq();
    }


    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {
        this.handlers.get(msg.getDataBodyCase().toString()).process(ctx, msg);
    }


    public void addProcessor(String processorName, Processor processor) {
        this.handlers.put(processorName, processor);
    }

    public DelayExecutorGroup executor() {
        return this.executors;
    }

//    public LinkedBlockingQueue<NotifyPendingPackText> getNotifyQueue() {
//        return this.notifyQueue;
//    }

    public LinkedBlockingQueue<ChatProtocol.ChatProtoPack> getAckQueue() {
        return ackQueue;
    }

    public void initMq() {
        try {

            this.mqProducer = new DefaultMQProducer("producer_of_" + serverName);
            this.mqConsumer = new DefaultMQPushConsumer("consumer_of_" + serverName);
            this.mqProducer.setNamesrvAddr(nameserver);
            this.mqConsumer.setNamesrvAddr(nameserver);
//            mqProducer.setRetryTimesWhenSendFailed(3);
            mqConsumer.subscribe("C2CTopic_notify", "TAG:notify:" + serverName);
            final ProcessorContainer that = this;
            // 消费者收到 notify 消息直接返回消费成功，然后消息向用户的推送由netty端负责。如果推送期间崩溃，将产生错误的离线消息，通过补偿手段进行补偿。
            mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    logger.info("[notify] 消息被 netty server 接受[成功]！");
                    try {
                        ChannelMap<Integer, Channel> channelMap = (ChannelMap) SpringIOC.getBean("getChannelIdMap");
                        for (MessageExt msg : list) {

                            logger.info(new String(msg.getBody()));

                            NotifyPendingPackText pack = JSON.parseObject(new String(msg.getBody()), NotifyPendingPackText.class);
                            Channel toChannel = channelMap.get(pack.getNotifyPack().getToId());
                            final BlockingQueue<NotifyPendingPackText> notifyQueue = toChannel.attr(AttrbuteSet.NOTIFY_QUEUE).get();
                            // 使用业务线程执行
                            that.executors.execute(() -> {
                                try {
                                    ChatProtocol.ChatProtoPack notifyMsg = ProtoToProto.NotifyToNotifyGoogle(pack.getNotifyPack());
                                    // 给指定客户端推送 notify 消息
                                    toChannel.writeAndFlush(notifyMsg);
                                    notifyQueue.put(pack);
                                    logger.info("[notify] 消息投放到 notify 队列[成功]！ " + pack);
                                    logger.info("notify 队列长度-------:" + notifyQueue.size());

                                    // 设置定时任务
                                    Runnable task = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                int retry = pack.getRetryTimes();
                                                logger.info("[定时任务] 执行，重试次数：" + retry);
                                                // 如果 notify 队列中有这个notify消息，那么判断是否要重试或者重试超过3次，设置离线消息
                                                if (notifyQueue.contains(pack)) {
                                                    if (retry < 3) {
                                                        toChannel.writeAndFlush(notifyMsg);
                                                        notifyQueue.put(pack);
                                                        logger.info("[notify] 消息投放到 notify 队列[成功]！ " + pack);
                                                        logger.info("notify 队列长度-------:" + notifyQueue.size());
                                                        pack.incrementRetry();
                                                        that.executors.scheduleWithFixedDelay(this, 1000,
                                                                TimeUnit.MILLISECONDS, pack.getNotifyPack().getFromId());
                                                    } else {
                                                        logger.warn("[notify] 消息[超时]！");
                                                        // Todo 启动离线消息过程
                                                    }
                                                }
                                            } catch (InterruptedException ine) {
                                                ine.printStackTrace();
                                                logger.info("[notify] 消息重试投放到 notify 队列[失败]！");
                                            }
                                        }
                                    };
                                    that.executors.scheduleWithFixedDelay(task, 1000, TimeUnit.MILLISECONDS, pack.getNotifyPack().getFromId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    logger.info("[notify] 消息投放到 notify 队列[失败]！");
                                }
                            }, pack.getNotifyPack().getFromId());
                        }

                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
            mqProducer.start();
            mqConsumer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DefaultMQProducer getMqProducer() {
        return this.mqProducer;
    }
}

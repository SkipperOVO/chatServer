package personal.fields.processor;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import personal.fields.protocol.ChatProtocol;
import personal.fields.vo.NotifyPendingPack;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.*;

@Component
public class ProcessorContainer {

    // 线程安全，只会初始化一次，不会发生更改
    private final HashMap<String /* DataBodyCase */, Processor> handlers = new HashMap<>();

    private LinkedBlockingQueue<NotifyPendingPack> notifyQueue = new LinkedBlockingQueue<>(64);

    private LinkedBlockingQueue<ChatProtocol.ChatProtoPack> ackQueue = new LinkedBlockingQueue<>(64);

    private final ThreadPoolExecutor businessThreads = new ThreadPoolExecutor(
            8, 256, 3*60, TimeUnit.SECONDS, new LinkedBlockingDeque<>(64));

    @Value("servername")
    private int serverName;

    @Value("rocketmq.nameserver")
    private String nameserver;

    private DefaultMQProducer mqProducer;

    private DefaultMQPushConsumer mqConsumer;

    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {
        this.handlers.get(msg.getDataBodyCase().toString()).process(ctx, msg);
    }


    public void addProcessor(String processorName, Processor processor) {
        this.handlers.put(processorName, processor);
    }

    public ThreadPoolExecutor getThreadPool()  {
        return this.businessThreads;
    }

    public LinkedBlockingQueue<NotifyPendingPack> getNotifyQueue() {
        return this.notifyQueue;
    }

    public LinkedBlockingQueue<ChatProtocol.ChatProtoPack> getAckQueue() {
        return ackQueue;
    }

    protected void initMq() {
        try {

            this.mqProducer = new DefaultMQProducer("producer_of_" + serverName);
            this.mqConsumer = new DefaultMQPushConsumer("consumer_of_" + serverName);
            this.mqProducer.setNamesrvAddr(nameserver);
            this.mqConsumer.setNamesrvAddr(nameserver);
            mqConsumer.subscribe("C2CTopic", "TAG:notify:" + serverName);
            final ProcessorContainer that = this;
            mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    final BlockingQueue<NotifyPendingPack> notifyQueue = that.getNotifyQueue();
                    for (MessageExt msg : list) {
                        NotifyPendingPack pack = JSON.parseObject(new String(msg.getBody()), NotifyPendingPack.class);
                        notifyQueue.put(pack);
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

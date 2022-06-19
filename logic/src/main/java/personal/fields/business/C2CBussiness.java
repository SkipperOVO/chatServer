package personal.fields.business;

import VO.ServerInfo;
import com.alibaba.fastjson.JSON;
import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import infrastructure.cache.JedisPoolUtil;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import personal.fields.protocol.ChatProtocol;
import personal.fields.util.MessageHelper;
import personal.fields.util.Seq;
import personal.fields.vo.NotifyPendingPack;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class C2CBussiness {

    private final static Logger logger = LoggerFactory.getLogger(C2CBussiness.class);

    private DefaultMQProducer mqProducer;

    private DefaultMQPushConsumer mqConsumer;

    @Value("rocketmq.nameserver")
    private String namesrv;

    @Value("servername")
    private String servername;

    @Value("logicId")
    private Integer lid;

    private final Cache cache = new JedisCache();

    public C2CBussiness() {
        try {

            mqProducer = new DefaultMQProducer("producer_of_" + servername);
            mqConsumer = new DefaultMQPushConsumer("consumer_of_" + servername);
            mqConsumer.subscribe("C2CTopic", "TAG:c2csend");
            // 消费不需要有序
            mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    // 默认每次消费一个消息，一个消息由一个线程消费
                    try {
                        for (MessageExt msg : list) {
                            logger.info("收到 send 消息：" + new String(msg.getBody()));
                            ChatProtocol.C2CSendReq c2cReq = JSON.parseObject(new String(msg.getBody()), ChatProtocol.C2CSendReq.class);
                            // Todo 落库 ,落库后可以获得 msgId
                            ChatProtocol.ChatProtoPack notifyMsg = MessageHelper.buildS2CNotifyMsg(c2cReq, /*从数据库返回的msgId*/999, Seq.generate());
                            NotifyPendingPack notifyPending = new NotifyPendingPack(notifyMsg, System.currentTimeMillis());
                            String nettyServername = ((ServerInfo) cache.get(new Integer(c2cReq.getToId()).toString())).getName();
                            mqProducer.send(new Message("C2CTopic", "TAG:notify:" + nettyServername,
                                    JSON.toJSONString(notifyPending).getBytes(RemotingHelper.DEFAULT_CHARSET)));
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    } finally {
                        return null;
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MQ 启动失败！");
        }
    }



}

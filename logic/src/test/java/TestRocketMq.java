import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestRocketMq {

    public static void main(String[] args) {

        try {

            DefaultMQProducer producer = new DefaultMQProducer("producer01");
            producer.setNamesrvAddr("localhost:9876");
            producer.start();

            new Thread(()->{

                try {

                    for (int i = 0; i < 10; ++i) {
                        Message msg = new Message("TopicTest", "tagTest", ("this is test msg : " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                        SendResult res = producer.send(msg);
                        System.out.println(res);
                        Thread.sleep(1000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();


            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer01");
            consumer.setNamesrvAddr("localhost:9876");
            consumer.subscribe("TopicTest", "*");
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    for (int i = 0; i < list.size(); ++i) {

//                        System.out.printf("%s Receive New Messages: ", Thread.currentThread().getName());
                        System.out.println(new String(list.get(i).getBody()) + " batchId : " + i);
                    }
                    // 标记该消息已经被成功消费
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
//
//            new Thread(()->{
//
//                try {
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package ForTest;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

public class DeleteAllMessage {

    public static void main(String[] args) {
        try {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_of_nettyServer-01");
//            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("consumer_of_logic-01");
            consumer.setNamesrvAddr("localhost:9876");
            consumer.subscribe("*", "*");
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    System.out.println("消费成功一条！");
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            consumer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

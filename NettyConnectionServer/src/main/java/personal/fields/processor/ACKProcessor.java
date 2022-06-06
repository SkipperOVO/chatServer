package personal.fields.processor;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.protocol.ChatProtocol;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ACKProcessor extends BaseProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ACKProcessor.class);

    public ACKProcessor(ThreadPoolExecutor threadPool, ProcessorContainer container) {
        super(threadPool, container);
    }


    @Override
    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {
        // put 会阻塞线程，使用业务线程组避免 netty worker 线程
        this.container.getThreadPool().execute(()->{
            try {
                this.container.getAckQueue().put(msg);
                logger.info("放入一个 ACK 到 ACK 队列了");
                logger.info("{ ACK: " + msg.getAck().getAck() + ", SEQ: " + msg.getAck().getSeq() + "}");
            } catch (InterruptedException ine) {
                ine.printStackTrace();
            }
        });

    }
}

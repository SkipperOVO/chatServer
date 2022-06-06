package personal.fields.processor;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.channelHandler.HeartBeatHandler;
import personal.fields.protocol.ChatProtocol;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;


public class HeartBeatProcessor extends BaseProcessor {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);


    public HeartBeatProcessor(ThreadPoolExecutor threadPool, ProcessorContainer container) {
        super(threadPool, container);
    }


    public void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {

        // Dispatcher 只负责转发，具体业务逻辑由业务线程组完成。避免占用 Netty worker 线程组影响 channel 读写响应
        Runnable task = ()-> {
                logger.info("客户端 " + msg.getHeartBeatReq().getUserId() + " 发来心跳");

                ChatProtocol.ChatProtoPack.Builder builder = ChatProtocol.ChatProtoPack.newBuilder()
                        .setVersion(1)
                        .setHeartBeatResp(ChatProtocol.HeartBeatResp.newBuilder()
                                .setVersion(1).build());
                ChatProtocol.ChatProtoPack response = builder.build();
                ctx.channel().writeAndFlush(response);
        };

        this.execute(task);

    }
}

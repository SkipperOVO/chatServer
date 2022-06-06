package personal.fields.channelHandler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import personal.fields.processor.C2CChatProcessor;
import personal.fields.processor.HeartBeatProcessor;
import personal.fields.processor.Processor;
import personal.fields.processor.ProcessorContainer;
import personal.fields.protocol.ChatProtocol;
import personal.fields.vo.NotifyPendingPack;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.*;

@ChannelHandler.Sharable
@Component
public class DispatchMessageHandler extends SimpleChannelInboundHandler<ChatProtocol.ChatProtoPack> {

    private static final Logger logger = LoggerFactory.getLogger(DispatchMessageHandler.class);

    @Autowired
    @Qualifier("getProcessorContainer")
    private ProcessorContainer proContainer;


    @Override
    public void channelRead0(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg) {

        logger.info("收到消息: " + msg.getDataBodyCase().toString());

        this.proContainer.process(ctx, msg);

    }

}

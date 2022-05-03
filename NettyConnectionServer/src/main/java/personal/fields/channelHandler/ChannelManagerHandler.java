package personal.fields.channelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.protocol.C2CSendRequest;

public class ChannelManagerHandler extends SimpleChannelInboundHandler<C2CSendRequest.C2CSendReq> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelManagerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        logger.info("客户端建立连接");

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, C2CSendRequest.C2CSendReq msg) {

        logger.info("客户端" + msg.getFromId() + "发送 send 请求 To " + msg.getToId() + " [" + msg.getMsg() + "]");

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        logger.info("客户端xx下线");
        ctx.channel().close();

    }
}

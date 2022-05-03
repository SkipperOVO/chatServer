package personal.fields.channelHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import personal.fields.protocol.ChatProtocol;
import personal.fields.protocol.HeartBeatRequest;
import personal.fields.protocol.HeartBeatResponse;

public class HeartBeatHandler extends SimpleChannelInboundHandler<ChatProtocol.ChatProtoPack> {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    // 当有心跳请求到达时，返回一个心跳响应
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack request) {

        logger.info("客户端 " + request.getHeartBeatReq().getUserId() + " 发来心跳");

        ChatProtocol.ChatProtoPack.Builder builder = ChatProtocol.ChatProtoPack.newBuilder()
                .setDataType(ChatProtocol.ChatProtoPack.DataType.HeartBeatRespType)
                            .setHeartBeatResp(ChatProtocol.HeartBeatResp.newBuilder()
                                .setVersion(1).build());
        ChatProtocol.ChatProtoPack response = builder.build();
        ctx.channel().writeAndFlush(response);
    }

    // IdleStateHandler 会管理与客户端的读写超时，当客户端超时未有任何心跳和数据时，利用 ctx.firxxxx(evt) 触发超时事件
    // ctx 会将事件传递给下一个 ctx，直到有一个 ctx 的 handler 可以处理这个事件
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {

        logger.info("用户心跳超时，已断连");

        ctx.channel().close();

        // 移出 redis 集群中的 userId，channel 映射关系
    }

}

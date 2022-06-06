package personal.fields.channelHandler;

import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import personal.fields.protocol.C2CSendRequest;

import java.util.concurrent.atomic.AtomicInteger;

@ChannelHandler.Sharable
@Component
public class ChannelManagerHandler extends SimpleChannelInboundHandler<C2CSendRequest.C2CSendReq> {

    private static final Logger logger = LoggerFactory.getLogger(ChannelManagerHandler.class);

    private static AtomicInteger connectionCount = new AtomicInteger(0);

    private Cache jedisCache;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        logger.info("客户端建立连接");

        // Todo redis 集群 {userId,channel} 路由更新


    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, C2CSendRequest.C2CSendReq msg) {

        // Todo 连接保存和消息处理切面
        logger.info(ctx.channel().attr(AttributeKey.valueOf("token")).get().toString());

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

        logger.info("客户端xx下线");

        // Todo redis 集群 {userId,channel} 路由更新
        ctx.channel().close();

    }
}

package personal.fields.channelHandler;

import VO.ServerInfo;
import exception.TokenParseErrorException;
import fields.personal.infrastructure.JwtToken;
import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import personal.fields.channelMap.ChannelMap;
import personal.fields.channelMap.SimpleHashMap;
import personal.fields.constant.AttrbuteSet;
import personal.fields.infrastructure.ioc.IOC.SpringIOC;


import javax.swing.text.AttributeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static personal.fields.constant.Constant.ONLINE_COUNT;


public class NioWebsocketHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(NioWebsocketHandler.class);

    private WebSocketServerHandshaker handshaker;


    private void channelActive(ChannelHandlerContext ctx, FullHttpRequest msg) {
        if (handleHttpRequest(ctx,  msg)) {
            Cache cache = new JedisCache();
            Channel ch = ctx.channel();
            try {

                String tokenStr = msg.uri().split("=")[1];
                // 鉴权
                JwtToken token = new JwtToken(tokenStr);
                // Todo 保存用户路由信息
                // cache.set(token.getUserId(), serverInfo);
                // 将 userId 和 channel 绑定
                Attribute<Integer> userIdAttr = ch.attr(AttrbuteSet.USER_ID);
                userIdAttr.set(token.getUserId());
                ChannelMap channelIdMap = (SimpleHashMap) SpringIOC.getBean("getChannelIdMap");
                channelIdMap.put(token.getUserId(), ch);
                // 保存用户和 nettyServer 的映射
                cache.set(token.getUserId().toString(), new ServerInfo());


                cache.incr(ONLINE_COUNT);
                logger.info("当前在线用户数：" + cache.get(ONLINE_COUNT));

                // channel 添加ACK queue绑定
                Attribute<BlockingQueue> notify_queue = ch.attr(AttrbuteSet.NOTIFY_QUEUE);
                notify_queue.set(new LinkedBlockingDeque(16));
                Attribute<BlockingQueue> ack_queue = ch.attr(AttrbuteSet.ACK_QUEUE);
                ack_queue.set(new LinkedBlockingDeque(32));

            } catch (TokenParseErrorException tke) {
                logger.warn("token 非法");
                ctx.channel().close();
            } catch (Exception e) {
                logger.error(e.toString());
                e.printStackTrace();
            } finally {
                cache.release();
            }
        }
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (msg instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame((((PingWebSocketFrame) msg).content()).retain()));
            return;
        }

        // 协议升级请求同时鉴权
        if (msg instanceof FullHttpRequest) {
            channelActive(ctx, (FullHttpRequest) msg);
            return;
        }

        if (msg instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), ((CloseWebSocketFrame) msg).retain());
            return;
        }

        // for debug
//                handlerWebSocketFrame(ctx, (WebSocketFrame) msg);
//                ByteBuf buf = ((WebSocketFrame) msg).content();
//                byte[] bytes = new byte[10];
//                buf.readBytes(bytes);

        // 业务消息包
        // 提取发送的二进制数据
        ByteBuf byteBuf = ((WebSocketFrame) msg).content();
        // 增加 byteBuf 的引用
        ReferenceCountUtil.retain(byteBuf);
        // 传递给下一个 handler 处理
        ctx.fireChannelRead(byteBuf);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            this.offline(ctx.channel());
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) {


    }


    private void offline(Channel ch) {

        Attribute<Integer> userId = ch.attr(AttributeKey.valueOf("userId"));

        logger.info("用户 " + userId + " 心跳超时,即将下线");

        ChannelMap channelIdMap = (ChannelMap) SpringIOC.getBean("getChannelIdMap");
        channelIdMap.remove(userId.get());

        Cache cache = new JedisCache();

        cache.decr(ONLINE_COUNT);
        logger.info("当前在线用户数：" + cache.get(ONLINE_COUNT));

        // 删除cache中用户和 serverInfo 的映射
        cache.del(userId.get().toString());

        ch.close();
        cache.release();
    }


    /**
     * 唯一的一次http请求，用于创建websocket
     */
    private boolean handleHttpRequest(ChannelHandlerContext ctx,
                                   FullHttpRequest req) {
        if (!isUpgrateReq(req)) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return false;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:7888", null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory
                    .sendUnsupportedVersionResponse(ctx.channel());
            return false;
        } else {
            handshaker.handshake(ctx.channel(), req);

        }
        return true;
    }



    private boolean isUpgrateReq(FullHttpRequest req) {
        //要求Upgrade为websocket，过滤掉get/Post
        return req.decoderResult().isSuccess()
                || "websocket".equals(req.headers().get("Upgrade"));
        //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
    }





    /**
     * 拒绝不合法的请求，并返回错误信息
     */
    private static void sendHttpResponse(ChannelHandlerContext ctx,
                                         FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(),
                    CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        // 如果是非Keep-Alive，关闭连接
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }




    // no use
    private void handlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
//        if (!(frame instanceof TextWebSocketFrame)) {
//            logger.debug("本例程仅支持文本消息，不支持二进制消息");
//            throw new UnsupportedOperationException(String.format(
//                    "%s frame types not supported", frame.getClass().getName()));
//        }
        // 返回应答消息
//        String request = ((TextWebSocketFrame) frame).text();
//        logger.debug("服务端收到：" + request);
//        TextWebSocketFrame tws = new TextWebSocketFrame(new Date().toString()
//                + ctx.channel().id() + " 服务端收到请求：" + request);

//        ctx.channel().writeAndFlush(tws);
        // 群发
        // Channel Manager
        // ChannelSupervise.send2All(tws);
        // 返回【谁发的发给谁】
        // ctx.channel().writeAndFlush(tws);
    }
}
package personal.fields.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;
import personal.fields.channelHandler.*;
import personal.fields.infrastructure.ioc.IOC.SpringIOC;
import personal.fields.protocol.ChatProtocol;


@Component
public class NettyChatServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch)  {
        // inbound
        ch.pipeline()
                //30 秒没有向客户端发送消息就发生心跳
                .addLast(new IdleStateHandler(60*30, 60*30, 0))
                // http 编解器
                .addLast(new HttpServerCodec())
                // http 消息聚合器
                .addLast(new HttpObjectAggregator(65536))
                // 支持异步发送大的码流(大的文件传输),但不占用过多的内存，防止java内存溢出
//                .addLast(new ChunkedWriteHandler())
                // 处理 websocket 协议升级和握手
//                .addLast(new GlobalVarInjectHandler())
                .addLast(new NioWebsocketHandler())
                // 管理连接
//                .addLast(new ChannelManagerHandler())
                // google Protobuf 编解码
                .addLast(new ProtobufDecoder(ChatProtocol.ChatProtoPack.getDefaultInstance()))
//                // 心跳
//                .addLast(new HeartBeatHandler())
                // 分派器
                .addLast((SimpleChannelInboundHandler<ChatProtocol.ChatProtoPack>) SpringIOC.getBean("getDispatcher"));

        // outbound
        ch.pipeline()
                // 将内存中 java 对象编码为 protobuf 协议包，并再次打包为 BinaryWebSocketFrame,满足 websocket 协议
                .addLast(new WebSocketEncoder());
    }
}

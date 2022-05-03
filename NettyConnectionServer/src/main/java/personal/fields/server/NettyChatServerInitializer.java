package personal.fields.server;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import personal.fields.channelHandler.ChannelManagerHandler;
import personal.fields.channelHandler.HeartBeatHandler;
import personal.fields.channelHandler.NioWebsocketHandler;
import personal.fields.protocol.ChatProtocol;
import personal.fields.protocol.HeartBeatRequest;

import java.util.List;

import static io.netty.buffer.Unpooled.wrappedBuffer;

public class NettyChatServerInitializer extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch)  {
//        // inbound

        ch.pipeline()
                //30 秒没有向客户端发送消息就发生心跳
                .addLast(new IdleStateHandler(30, 0, 0))
//                .addLast(new ChannelManagerHandler())
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                .addLast(new NioWebsocketHandler())
                // google Protobuf 编解码
                .addLast(new ProtobufVarint32FrameDecoder())
                .addLast(new ProtobufDecoder(ChatProtocol.ChatProtoPack.getDefaultInstance()))
                .addLast(new HeartBeatHandler());
//
//
//        // outbound
        ch.pipeline()
                .addLast(new ProtobufVarint32LengthFieldPrepender())
                .addLast(new ProtobufEncoder());


    }
}

package personal.fields.channelHandler;

import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageLiteOrBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

// 融合 protobuf 的编码过程和 websocket 的 WebScoketFrame 过程到一个编码器。因为似乎不能多个编码器或者解码器串联
@ChannelHandler.Sharable
public class WebSocketEncoder extends MessageToMessageEncoder<MessageLiteOrBuilder> {

    protected void encode(ChannelHandlerContext ctx, MessageLiteOrBuilder msg, List<Object> out) throws Exception {
        if (msg instanceof MessageLite) {
            out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(((MessageLite)msg).toByteArray())));
        } else {
            if (msg instanceof MessageLite.Builder) {
                out.add(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(((MessageLite.Builder)msg).build().toByteArray())));
            }

        }
    }

}

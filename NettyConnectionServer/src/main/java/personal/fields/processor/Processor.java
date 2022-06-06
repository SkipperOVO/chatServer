package personal.fields.processor;

import io.netty.channel.ChannelHandlerContext;
import personal.fields.protocol.ChatProtocol;

public interface Processor {

    void process(ChannelHandlerContext ctx, ChatProtocol.ChatProtoPack msg);

}

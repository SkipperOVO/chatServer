package personal.fields.channelHandler;

import infrastructure.ioc.SpringIOC;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import personal.fields.channelMap.ChannelMap;
import personal.fields.channelMap.SimpleHashMap;
import personal.fields.constant.AttrbuteSet;

/*

    注入 Netty 级别上下文全局变量

 */

@ChannelHandler.Sharable
@Component
public class GlobalVarInjectHandler extends SimpleChannelInboundHandler<Object> {

    /*
        Channel 没有实现 Serilizable 接口无法序列化到 redis 保存，所以使用 Map 形式保存
     */
    @Autowired
    ChannelMap<Integer, Channel> channelIdMap;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {

        ctx.fireChannelActive();
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object obj) {
        ReferenceCountUtil.retain(obj);
        ctx.fireChannelRead(obj);
    }


}

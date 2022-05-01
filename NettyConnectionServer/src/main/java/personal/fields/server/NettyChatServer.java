package personal.fields.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import personal.fields.NettyServerBootStrap;

import java.net.InetSocketAddress;

@Component
public class NettyChatServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyChatServer.class);

    private EventLoopGroup bossGrop = new NioEventLoopGroup(200);

    private EventLoopGroup workerGrop = new NioEventLoopGroup(200);

    @Value("${netty.port}")
    private int nettyPort;

    public void start() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .group(bossGrop, workerGrop)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(nettyPort))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new NettyChatServerInitializer());
        ChannelFuture future = serverBootstrap.bind().sync();

        if (future.isSuccess()) {

        }

    }
}

package nzhenry;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryDecoder;
import nzhenry.netty.DatagramDnsResponseEncoder;
import nzhenry.netty.DnsNameResolver;
import nzhenry.netty.DnsNameResolverBuilder;

public class Application {

    static final int PORT = 53;

    public static void main(String[] args) throws Exception {
        start();
//        new EchoServer(PORT).run();
    }

    public static void start() {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        DnsNameResolver resolver = new DnsNameResolverBuilder(eventLoopGroup.next())
                .channelType(NioDatagramChannel.class)
                .build();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel nioDatagramChannel) throws InterruptedException {
//                            nioDatagramChannel.pipeline().addLast(new DnsServerMessageHandler());
                            nioDatagramChannel.pipeline().addLast(new DatagramDnsQueryDecoder());
                            nioDatagramChannel.pipeline().addLast(new DatagramDnsResponseEncoder());
                            nioDatagramChannel.pipeline().addLast(new DnsProxy(resolver));
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true);

            ChannelFuture future = bootstrap.bind(PORT);
            future.sync().channel().closeFuture().sync();
        } catch (Exception ex) {
            System.err.println(ex);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
}

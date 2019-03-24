package nzhenry;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class DnsClient {

    private final Bootstrap bootstrap;
    private final EventLoopGroup group;
    private final DnsClientMessageHandler handler;
    private Channel channel;

    public DnsClient() {
        this.bootstrap = new Bootstrap();
        this.group = new NioEventLoopGroup();
        this.handler = new DnsClientMessageHandler();

        try {
            bootstrap.channel(NioDatagramChannel.class).group(group).handler(handler);
            bootstrap.option(ChannelOption.SO_BROADCAST, true).option(ChannelOption.SO_REUSEADDR, false);
            this.channel = bootstrap.bind(0).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public final void sendNettyMessage(ByteBuf buf, InetSocketAddress address) {
        DatagramPacket packet = new DatagramPacket(buf, address);
        channel.writeAndFlush(packet);
    }

    /**
     * Shuts down the client for good, once this is called the client can no
     * longer connect to servers.
     */
    public final void shutdown() {
        // Close channel
        channel.close();
        group.shutdownGracefully();
    }
}
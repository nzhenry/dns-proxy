package nzhenry;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

public class DnsServerMessageHandler extends ChannelInboundHandlerAdapter {

    private final DnsClient dnsClient = new DnsClient();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket packet = (DatagramPacket)msg;
        ByteBuf buf = packet.content();
        ByteBuf copy = buf.copy();
        dnsClient.sendNettyMessage(copy, new InetSocketAddress("1.1.1.1", 53));
//        ctx.writeAndFlush()
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}

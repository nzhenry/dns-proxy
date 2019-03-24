package nzhenry;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;

public class DnsClientMessageHandler extends ChannelInboundHandlerAdapter {
    public DnsClientMessageHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        DatagramPacket packet = (DatagramPacket)msg;
        ByteBuf buf = packet.content();
        ByteBuf copy = buf.copy();
        packet.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

}
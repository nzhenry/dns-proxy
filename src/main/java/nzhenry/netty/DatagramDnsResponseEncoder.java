//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecordEncoder;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.util.internal.ObjectUtil;

import java.net.InetSocketAddress;
import java.util.List;

@Sharable
public class DatagramDnsResponseEncoder extends MessageToMessageEncoder<AddressedEnvelope<DnsResponse, InetSocketAddress>> {

    private final DnsRecordEncoder recordEncoder;

    public DatagramDnsResponseEncoder() {
        this(DnsRecordEncoder.DEFAULT);
    }

    public DatagramDnsResponseEncoder(DnsRecordEncoder recordEncoder) {
        this.recordEncoder = (DnsRecordEncoder) ObjectUtil.checkNotNull(recordEncoder, "recordEncoder");
    }

    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<DnsResponse, InetSocketAddress> in, List<Object> out) throws Exception {
        InetSocketAddress recipient = in.recipient();
        DatagramDnsResponse response = (DatagramDnsResponse)in.content();

        ByteBuf buf = response.getPacket().content().retainedDuplicate();
//        writeOutBytes(buf);
        buf.readerIndex(0);
        buf.writerIndex(0);
        int responseId = response.id();
        buf.writeShort(responseId);
        buf.resetWriterIndex();

        out.add(new DatagramPacket(buf, recipient, null));


//        ByteBuf buf = this.allocateBuffer(ctx, in);
//        boolean success = false;
//
//        try {
//            encodeHeader(response, buf);
//            this.encodeQuestions(response, buf);
//            this.encodeRecords(response, DnsSection.ANSWER, buf);
//            this.encodeRecords(response, DnsSection.AUTHORITY, buf);
//            this.encodeRecords(response, DnsSection.ADDITIONAL, buf);
//            success = true;
//        } finally {
//            if (!success) {
//                buf.release();
//            }
//
//        }

//        writeOutBytes(buf);


//        out.add(new DatagramPacket(buf, recipient, (InetSocketAddress)null));
    }

    private void writeOutBytes(ByteBuf buf) {
        buf = buf.retainedDuplicate();
        buf.readerIndex(0);
        int readableBytes = buf.readableBytes();
        byte[] bytes = new byte[readableBytes];
        buf.readBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        System.out.println(sb);
    }

    protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, AddressedEnvelope<DnsResponse, InetSocketAddress> msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }

    private static void encodeHeader(DnsResponse response, ByteBuf buf) {
        buf.writeShort(response.id());
        int flags = '耀';
        flags = flags | (response.opCode().byteValue() & 255) << 11;
        if (response.isAuthoritativeAnswer()) {
            flags |= 1024;
        }

        if (response.isTruncated()) {
            flags |= 512;
        }

        if (response.isRecursionDesired()) {
            flags |= 256;
        }

        if (response.isRecursionAvailable()) {
            flags |= 128;
        }

        flags |= response.z() << 4;
        flags |= response.code().intValue();
        buf.writeShort(flags);
        buf.writeShort(response.count(DnsSection.QUESTION));
        buf.writeShort(response.count(DnsSection.ANSWER));
        buf.writeShort(response.count(DnsSection.AUTHORITY));
        buf.writeShort(response.count(DnsSection.ADDITIONAL));
    }

    private void encodeQuestions(DnsResponse response, ByteBuf buf) throws Exception {
        int count = response.count(DnsSection.QUESTION);

        for(int i = 0; i < count; ++i) {
            this.recordEncoder.encodeQuestion((DnsQuestion)response.recordAt(DnsSection.QUESTION, i), buf);
        }

    }

    private void encodeRecords(DnsResponse response, DnsSection section, ByteBuf buf) throws Exception {
        int count = response.count(section);

        for(int i = 0; i < count; ++i) {
            this.recordEncoder.encodeRecord(response.recordAt(section, i), buf);
        }

    }
}

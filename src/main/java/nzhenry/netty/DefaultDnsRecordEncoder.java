//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.handler.codec.UnsupportedMessageTypeException;
import io.netty.handler.codec.dns.*;
import io.netty.util.internal.StringUtil;

public class DefaultDnsRecordEncoder implements DnsRecordEncoder {
    private static final int PREFIX_MASK = 7;

    public DefaultDnsRecordEncoder() {
    }

    public final void encodeQuestion(DnsQuestion question, ByteBuf out) throws Exception {
        this.encodeName(question.name(), out);
        out.writeShort(question.type().intValue());
        out.writeShort(question.dnsClass());
    }

    public void encodeRecord(DnsRecord record, ByteBuf out) throws Exception {
//        ByteBuf in = ((DefaultDnsRawRecord)record).content();
//        in.readerIndex(0);
//        out.writeBytes(in);


        if (record instanceof DnsQuestion) {
            this.encodeQuestion((DnsQuestion)record, out);
        } else if (record instanceof DnsPtrRecord) {
            this.encodePtrRecord((DnsPtrRecord)record, out);
        } else if (record instanceof DnsOptEcsRecord) {
            this.encodeOptEcsRecord((DnsOptEcsRecord)record, out);
        } else if (record instanceof DnsOptPseudoRecord) {
            this.encodeOptPseudoRecord((DnsOptPseudoRecord)record, out);
        } else {
            if (!(record instanceof DnsRawRecord)) {
                throw new UnsupportedMessageTypeException(StringUtil.simpleClassName(record));
            }

            this.encodeRawRecord((DnsRawRecord)record, out);
        }

    }

    private void encodeRecord0(DnsRecord record, ByteBuf out) throws Exception {
        this.encodeName(record.name(), out);
        out.writeShort(record.type().intValue());
        out.writeShort(record.dnsClass());
        out.writeInt((int)record.timeToLive());
    }

    private void encodePtrRecord(DnsPtrRecord record, ByteBuf out) throws Exception {
        this.encodeRecord0(record, out);
        this.encodeName(record.hostname(), out);
    }

    private void encodeOptPseudoRecord(DnsOptPseudoRecord record, ByteBuf out) throws Exception {
        this.encodeRecord0(record, out);
        out.writeShort(0);
    }

    private void encodeOptEcsRecord(DnsOptEcsRecord record, ByteBuf out) throws Exception {
        this.encodeRecord0(record, out);
        int sourcePrefixLength = record.sourcePrefixLength();
        int scopePrefixLength = record.scopePrefixLength();
        int lowOrderBitsToPreserve = sourcePrefixLength & 7;
        byte[] bytes = record.address();
        int addressBits = bytes.length << 3;
        if (addressBits >= sourcePrefixLength && sourcePrefixLength >= 0) {
            short addressNumber = (short)(bytes.length == 4 ? InternetProtocolFamily.IPv4.addressNumber() : InternetProtocolFamily.IPv6.addressNumber());
            int payloadLength = calculateEcsAddressLength(sourcePrefixLength, lowOrderBitsToPreserve);
            int fullPayloadLength = 8 + payloadLength;
            out.writeShort(fullPayloadLength);
            out.writeShort(8);
            out.writeShort(fullPayloadLength - 4);
            out.writeShort(addressNumber);
            out.writeByte(sourcePrefixLength);
            out.writeByte(scopePrefixLength);
            if (lowOrderBitsToPreserve > 0) {
                int bytesLength = payloadLength - 1;
                out.writeBytes(bytes, 0, bytesLength);
                out.writeByte(padWithZeros(bytes[bytesLength], lowOrderBitsToPreserve));
            } else {
                out.writeBytes(bytes, 0, payloadLength);
            }

        } else {
            throw new IllegalArgumentException(sourcePrefixLength + ": " + sourcePrefixLength + " (expected: 0 >= " + addressBits + ')');
        }
    }

    static int calculateEcsAddressLength(int sourcePrefixLength, int lowOrderBitsToPreserve) {
        return (sourcePrefixLength >>> 3) + (lowOrderBitsToPreserve != 0 ? 1 : 0);
    }

    private void encodeRawRecord(DnsRawRecord record, ByteBuf out) throws Exception {
        this.encodeRecord0(record, out);
        ByteBuf content = record.content();
        int contentLen = content.readableBytes();
        out.writeShort(contentLen);
        out.writeBytes(content, content.readerIndex(), contentLen);
    }

    protected void encodeName(String name, ByteBuf buf) throws Exception {
        if (".".equals(name)) {
            buf.writeByte(0);
        } else {
            String[] labels = name.split("\\.");
            String[] var4 = labels;
            int var5 = labels.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String label = var4[var6];
                int labelLen = label.length();
                if (labelLen == 0) {
                    break;
                }

                buf.writeByte(labelLen);
                ByteBufUtil.writeAscii(buf, label);
            }

            buf.writeByte(0);
        }
    }

    private static byte padWithZeros(byte b, int lowOrderBitsToPreserve) {
        switch(lowOrderBitsToPreserve) {
            case 0:
                return 0;
            case 1:
                return (byte)(128 & b);
            case 2:
                return (byte)(192 & b);
            case 3:
                return (byte)(224 & b);
            case 4:
                return (byte)(240 & b);
            case 5:
                return (byte)(248 & b);
            case 6:
                return (byte)(252 & b);
            case 7:
                return (byte)(254 & b);
            case 8:
                return b;
            default:
                throw new IllegalArgumentException("lowOrderBitsToPreserve: " + lowOrderBitsToPreserve);
        }
    }
}

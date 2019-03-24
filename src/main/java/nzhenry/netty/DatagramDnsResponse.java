//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.dns.DnsOpCode;
import io.netty.handler.codec.dns.DnsResponseCode;

import java.net.InetSocketAddress;

public class DatagramDnsResponse extends io.netty.handler.codec.dns.DatagramDnsResponse {

    public DatagramDnsResponse(DatagramPacket packet, InetSocketAddress sender, InetSocketAddress recipient, int id, DnsOpCode opCode, DnsResponseCode responseCode) {
        super(sender, recipient, id, opCode, responseCode);
        this.packet = packet;
    }

    private final DatagramPacket packet;

    public DatagramPacket getPacket() {
        return packet;
    }
}

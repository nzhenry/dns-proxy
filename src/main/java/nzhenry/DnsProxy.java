package nzhenry;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import nzhenry.netty.DatagramDnsResponse;
import nzhenry.netty.DnsNameResolver;

import java.net.InetSocketAddress;

public class DnsProxy extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    final DnsNameResolver resolver;

    public DnsProxy(DnsNameResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramDnsQuery query) {
        DnsQuestion question = query.recordAt(DnsSection.QUESTION, 0);

        resolver.query(question).addListener(f -> {
            AddressedEnvelope<DnsResponse, InetSocketAddress> envelope;
            envelope = (AddressedEnvelope<DnsResponse, InetSocketAddress>)f.getNow();
            context.writeAndFlush(getResponse((DatagramDnsResponse) envelope.content(), query));
        });
    }

    private DnsResponse getResponse(DatagramDnsResponse serverResponse, DatagramDnsQuery query) {
        DnsResponse response = new DatagramDnsResponse(serverResponse.getPacket(), query.recipient(), query.sender(), query.id());
        copySections(serverResponse, response);
        return response;
    }

    private void copySections(DnsResponse r1, DnsResponse r2) {
	    for(DnsSection section : DnsSection.values()) {
            copySection(r1, r2, section);
        }
    }

    private void copySection(DnsResponse r1, DnsResponse r2, DnsSection section) {
        for (int i = 0; i < r1.count(section); i++) {
            DnsRecord record = r1.recordAt(section, i);
            r2.addRecord(section, record);
        }
    }
}

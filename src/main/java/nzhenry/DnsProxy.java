package nzhenry;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import io.netty.resolver.dns.DnsNameResolver;

import java.net.InetSocketAddress;
import java.util.function.Consumer;

public class DnsProxy extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    final DnsNameResolver resolver;

    public DnsProxy(DnsNameResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramDnsQuery query) {
        for(int i = 0; i < query.count(DnsSection.QUESTION); i++) {
            DnsQuestion question = query.recordAt(DnsSection.QUESTION, i);
            askQuestion(question, r -> {
                final DnsResponse response = getResponse(r, query);
                context.writeAndFlush(response);
            });
        }
    }

    private void askQuestion(DnsQuestion question, Consumer<DnsResponse> callback) {
        resolver.query(question).addListener(f -> {
            AddressedEnvelope<DnsResponse, InetSocketAddress> envelope;
            envelope = (AddressedEnvelope<DnsResponse, InetSocketAddress>)f.getNow();
            DnsResponse response = envelope.content();
            callback.accept(response);
        });
    }

    private DnsResponse getResponse(DnsResponse serverResponse, DatagramDnsQuery query) {
        DnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        if(serverResponse == null) {
            return response;
        }
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
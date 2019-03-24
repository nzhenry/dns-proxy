package nzhenry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import nzhenry.netty.DnsNameResolver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

public class DnsProxy extends SimpleChannelInboundHandler<DatagramDnsQuery> {

    final DnsNameResolver resolver;

    public DnsProxy(DnsNameResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, DatagramDnsQuery query) {
        DnsQuestion question = query.recordAt(DnsSection.QUESTION, 0);
//        resolver.resolveAll(question).addListener(f -> {
//            List<DnsRecord> records = (List<DnsRecord>)f.getNow();
//            context.writeAndFlush(getResponse(records, query));
//        });

        resolver.query(question).addListener(f -> {
            AddressedEnvelope<DnsResponse, InetSocketAddress> envelope;
            envelope = (AddressedEnvelope<DnsResponse, InetSocketAddress>)f.getNow();
            context.writeAndFlush(getResponse(envelope.content(), query));
        });
    }

    private DnsResponse getResponse(DnsResponse serverResponse, DatagramDnsQuery query) {
        DnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
        copySections(serverResponse, response);
        return response;
    }

//    private DnsResponse getResponse(List<DnsRecord> records, DatagramDnsQuery query) {
//        DnsResponse response = new DatagramDnsResponse(query.recipient(), query.sender(), query.id());
//        for(DnsRecord record : records) {
//            response.addRecord(DnsSection.ANSWER, record);
//        }
//        return response;
//    }

    private void copySections(DnsResponse r1, DnsResponse r2) {
	    for(DnsSection section : DnsSection.values()) {
            copySection(r1, r2, section);
        }
    }

    private void copySection(DnsResponse r1, DnsResponse r2, DnsSection section) {
        for (int i = 0; i < r1.count(section); i++) {
            DnsRecord record = r1.recordAt(section, i);
            if(record.type() == DnsRecordType.CNAME) {
                continue;
            }
            r2.addRecord(section, record);
        }
    }
}

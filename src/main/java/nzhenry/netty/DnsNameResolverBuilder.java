//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;

public final class DnsNameResolverBuilder {
    private EventLoop eventLoop;
    private ChannelFactory<? extends DatagramChannel> channelFactory;
    private long queryTimeoutMillis = 5000L;
    private ResolvedAddressTypes resolvedAddressTypes;
    private boolean recursionDesired;
    private int maxQueriesPerResolve;
    private int maxPayloadSize;
    private boolean optResourceEnabled;
    private HostsFileEntriesResolver hostsFileEntriesResolver;
    private DnsServerAddressStreamProvider dnsServerAddressStreamProvider;
    private int ndots;
    private boolean decodeIdn;

    public DnsNameResolverBuilder(EventLoop eventLoop) {
        this.resolvedAddressTypes = DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = true;
        this.maxQueriesPerResolve = 16;
        this.maxPayloadSize = 4096;
        this.optResourceEnabled = true;
        this.hostsFileEntriesResolver = HostsFileEntriesResolver.DEFAULT;
        this.dnsServerAddressStreamProvider = DnsServerAddressStreamProviders.platformDefault();
        this.ndots = -1;
        this.decodeIdn = true;
        this.eventLoop(eventLoop);
    }

    public DnsNameResolverBuilder eventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        return this;
    }

    public DnsNameResolverBuilder channelFactory(ChannelFactory<? extends DatagramChannel> channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    public DnsNameResolverBuilder channelType(Class<? extends DatagramChannel> channelType) {
        return this.channelFactory(new ReflectiveChannelFactory(channelType));
    }

    public DnsNameResolver build() {
        if (this.eventLoop == null) {
            throw new IllegalStateException("eventLoop should be specified to build a DnsNameResolver.");
        } else {
            return new DnsNameResolver(this.eventLoop, this.channelFactory, this.queryTimeoutMillis, this.resolvedAddressTypes, this.recursionDesired, this.maxQueriesPerResolve, false, this.maxPayloadSize, this.optResourceEnabled, this.hostsFileEntriesResolver, this.dnsServerAddressStreamProvider, null, this.ndots, this.decodeIdn);
        }
    }
}

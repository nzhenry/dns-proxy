//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.DnsQueryLifecycleObserverFactory;
import io.netty.resolver.dns.DnsServerAddressStreamProvider;
import io.netty.resolver.dns.DnsServerAddressStreamProviders;
import io.netty.resolver.dns.NoopDnsQueryLifecycleObserverFactory;
import io.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public final class DnsNameResolverBuilder {
    private EventLoop eventLoop;
    private ChannelFactory<? extends DatagramChannel> channelFactory;
    private Integer minTtl;
    private Integer maxTtl;
    private Integer negativeTtl;
    private long queryTimeoutMillis = 5000L;
    private ResolvedAddressTypes resolvedAddressTypes;
    private boolean recursionDesired;
    private int maxQueriesPerResolve;
    private boolean traceEnabled;
    private int maxPayloadSize;
    private boolean optResourceEnabled;
    private HostsFileEntriesResolver hostsFileEntriesResolver;
    private DnsServerAddressStreamProvider dnsServerAddressStreamProvider;
    private DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory;
    private String[] searchDomains;
    private int ndots;
    private boolean decodeIdn;

    public DnsNameResolverBuilder() {
        this.resolvedAddressTypes = DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = true;
        this.maxQueriesPerResolve = 16;
        this.maxPayloadSize = 4096;
        this.optResourceEnabled = true;
        this.hostsFileEntriesResolver = HostsFileEntriesResolver.DEFAULT;
        this.dnsServerAddressStreamProvider = DnsServerAddressStreamProviders.platformDefault();
        this.dnsQueryLifecycleObserverFactory = NoopDnsQueryLifecycleObserverFactory.INSTANCE;
        this.ndots = -1;
        this.decodeIdn = true;
    }

    public DnsNameResolverBuilder(EventLoop eventLoop) {
        this.resolvedAddressTypes = DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = true;
        this.maxQueriesPerResolve = 16;
        this.maxPayloadSize = 4096;
        this.optResourceEnabled = true;
        this.hostsFileEntriesResolver = HostsFileEntriesResolver.DEFAULT;
        this.dnsServerAddressStreamProvider = DnsServerAddressStreamProviders.platformDefault();
        this.dnsQueryLifecycleObserverFactory = NoopDnsQueryLifecycleObserverFactory.INSTANCE;
        this.ndots = -1;
        this.decodeIdn = true;
        this.eventLoop(eventLoop);
    }

    public DnsNameResolverBuilder eventLoop(EventLoop eventLoop) {
        this.eventLoop = eventLoop;
        return this;
    }

    protected ChannelFactory<? extends DatagramChannel> channelFactory() {
        return this.channelFactory;
    }

    public DnsNameResolverBuilder channelFactory(ChannelFactory<? extends DatagramChannel> channelFactory) {
        this.channelFactory = channelFactory;
        return this;
    }

    public DnsNameResolverBuilder channelType(Class<? extends DatagramChannel> channelType) {
        return this.channelFactory(new ReflectiveChannelFactory(channelType));
    }

    public DnsNameResolverBuilder dnsQueryLifecycleObserverFactory(DnsQueryLifecycleObserverFactory lifecycleObserverFactory) {
        this.dnsQueryLifecycleObserverFactory = (DnsQueryLifecycleObserverFactory)ObjectUtil.checkNotNull(lifecycleObserverFactory, "lifecycleObserverFactory");
        return this;
    }

    public DnsNameResolverBuilder ttl(int minTtl, int maxTtl) {
        this.maxTtl = maxTtl;
        this.minTtl = minTtl;
        return this;
    }

    public DnsNameResolverBuilder negativeTtl(int negativeTtl) {
        this.negativeTtl = negativeTtl;
        return this;
    }

    public DnsNameResolverBuilder queryTimeoutMillis(long queryTimeoutMillis) {
        this.queryTimeoutMillis = queryTimeoutMillis;
        return this;
    }

    public static ResolvedAddressTypes computeResolvedAddressTypes(InternetProtocolFamily... internetProtocolFamilies) {
        if (internetProtocolFamilies != null && internetProtocolFamilies.length != 0) {
            if (internetProtocolFamilies.length > 2) {
                throw new IllegalArgumentException("No more than 2 InternetProtocolFamilies");
            } else {
                switch(internetProtocolFamilies[0]) {
                    case IPv4:
                        return internetProtocolFamilies.length >= 2 && internetProtocolFamilies[1] == InternetProtocolFamily.IPv6 ? ResolvedAddressTypes.IPV4_PREFERRED : ResolvedAddressTypes.IPV4_ONLY;
                    case IPv6:
                        return internetProtocolFamilies.length >= 2 && internetProtocolFamilies[1] == InternetProtocolFamily.IPv4 ? ResolvedAddressTypes.IPV6_PREFERRED : ResolvedAddressTypes.IPV6_ONLY;
                    default:
                        throw new IllegalArgumentException("Couldn't resolve ResolvedAddressTypes from InternetProtocolFamily array");
                }
            }
        } else {
            return DnsNameResolver.DEFAULT_RESOLVE_ADDRESS_TYPES;
        }
    }

    public DnsNameResolverBuilder resolvedAddressTypes(ResolvedAddressTypes resolvedAddressTypes) {
        this.resolvedAddressTypes = resolvedAddressTypes;
        return this;
    }

    public DnsNameResolverBuilder recursionDesired(boolean recursionDesired) {
        this.recursionDesired = recursionDesired;
        return this;
    }

    public DnsNameResolverBuilder maxQueriesPerResolve(int maxQueriesPerResolve) {
        this.maxQueriesPerResolve = maxQueriesPerResolve;
        return this;
    }

    public DnsNameResolverBuilder traceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        return this;
    }

    public DnsNameResolverBuilder maxPayloadSize(int maxPayloadSize) {
        this.maxPayloadSize = maxPayloadSize;
        return this;
    }

    public DnsNameResolverBuilder optResourceEnabled(boolean optResourceEnabled) {
        this.optResourceEnabled = optResourceEnabled;
        return this;
    }

    public DnsNameResolverBuilder hostsFileEntriesResolver(HostsFileEntriesResolver hostsFileEntriesResolver) {
        this.hostsFileEntriesResolver = hostsFileEntriesResolver;
        return this;
    }

    protected DnsServerAddressStreamProvider nameServerProvider() {
        return this.dnsServerAddressStreamProvider;
    }

    public DnsNameResolverBuilder nameServerProvider(DnsServerAddressStreamProvider dnsServerAddressStreamProvider) {
        this.dnsServerAddressStreamProvider = (DnsServerAddressStreamProvider)ObjectUtil.checkNotNull(dnsServerAddressStreamProvider, "dnsServerAddressStreamProvider");
        return this;
    }

    public DnsNameResolverBuilder searchDomains(Iterable<String> searchDomains) {
        ObjectUtil.checkNotNull(searchDomains, "searchDomains");
        List<String> list = new ArrayList(4);
        Iterator var3 = searchDomains.iterator();

        while(var3.hasNext()) {
            String f = (String)var3.next();
            if (f == null) {
                break;
            }

            if (!list.contains(f)) {
                list.add(f);
            }
        }

        this.searchDomains = (String[])list.toArray(new String[0]);
        return this;
    }

    public DnsNameResolverBuilder ndots(int ndots) {
        this.ndots = ndots;
        return this;
    }

    public DnsNameResolverBuilder decodeIdn(boolean decodeIdn) {
        this.decodeIdn = decodeIdn;
        return this;
    }

    public DnsNameResolver build() {
        if (this.eventLoop == null) {
            throw new IllegalStateException("eventLoop should be specified to build a DnsNameResolver.");
        } else {
            return new DnsNameResolver(this.eventLoop, this.channelFactory, this.dnsQueryLifecycleObserverFactory, this.queryTimeoutMillis, this.resolvedAddressTypes, this.recursionDesired, this.maxQueriesPerResolve, this.traceEnabled, this.maxPayloadSize, this.optResourceEnabled, this.hostsFileEntriesResolver, this.dnsServerAddressStreamProvider, this.searchDomains, this.ndots, this.decodeIdn);
        }
    }

    public DnsNameResolverBuilder copy() {
        DnsNameResolverBuilder copiedBuilder = new DnsNameResolverBuilder();
        if (this.eventLoop != null) {
            copiedBuilder.eventLoop(this.eventLoop);
        }

        if (this.channelFactory != null) {
            copiedBuilder.channelFactory(this.channelFactory);
        }

        if (this.maxTtl != null && this.minTtl != null) {
            copiedBuilder.ttl(this.minTtl, this.maxTtl);
        }

        if (this.negativeTtl != null) {
            copiedBuilder.negativeTtl(this.negativeTtl);
        }

        if (this.dnsQueryLifecycleObserverFactory != null) {
            copiedBuilder.dnsQueryLifecycleObserverFactory(this.dnsQueryLifecycleObserverFactory);
        }

        copiedBuilder.queryTimeoutMillis(this.queryTimeoutMillis);
        copiedBuilder.resolvedAddressTypes(this.resolvedAddressTypes);
        copiedBuilder.recursionDesired(this.recursionDesired);
        copiedBuilder.maxQueriesPerResolve(this.maxQueriesPerResolve);
        copiedBuilder.traceEnabled(this.traceEnabled);
        copiedBuilder.maxPayloadSize(this.maxPayloadSize);
        copiedBuilder.optResourceEnabled(this.optResourceEnabled);
        copiedBuilder.hostsFileEntriesResolver(this.hostsFileEntriesResolver);
        if (this.dnsServerAddressStreamProvider != null) {
            copiedBuilder.nameServerProvider(this.dnsServerAddressStreamProvider);
        }

        if (this.searchDomains != null) {
            copiedBuilder.searchDomains(Arrays.asList(this.searchDomains));
        }

        copiedBuilder.ndots(this.ndots);
        copiedBuilder.decodeIdn(this.decodeIdn);
        return copiedBuilder;
    }
}

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponse;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRawRecord;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsRecordType;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.resolver.HostsFileEntriesResolver;
import io.netty.resolver.ResolvedAddressTypes;
import io.netty.resolver.dns.*;
import io.netty.util.NetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.EmptyArrays;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.IDN;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DnsNameResolver {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsNameResolver.class);
    private static final InetAddress LOCALHOST_ADDRESS;
    private static final DnsRecord[] EMPTY_ADDITIONALS = new DnsRecord[0];
    private static final DnsRecordType[] IPV4_ONLY_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV6_ONLY_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
    private static final DnsRecordType[] IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
    private static final InternetProtocolFamily[] IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
    static final ResolvedAddressTypes DEFAULT_RESOLVE_ADDRESS_TYPES;
    static final String[] DEFAULT_SEARCH_DOMAINS;
    private static final int DEFAULT_NDOTS;
    private static final DatagramDnsResponseDecoder DECODER;
    private static final DatagramDnsQueryEncoder ENCODER;
    final Future<Channel> channelFuture;
    final Channel ch;
    final DnsQueryContextManager queryContextManager;
    private final FastThreadLocal<DnsServerAddressStream> nameServerAddrStream;
    private final long queryTimeoutMillis;
    private final int maxQueriesPerResolve;
    private final ResolvedAddressTypes resolvedAddressTypes;
    private final InternetProtocolFamily[] resolvedInternetProtocolFamilies;
    private final boolean recursionDesired;
    private final int maxPayloadSize;
    private final boolean optResourceEnabled;
    private final HostsFileEntriesResolver hostsFileEntriesResolver;
    private final DnsServerAddressStreamProvider dnsServerAddressStreamProvider;
    private final String[] searchDomains;
    private final int ndots;
    private final boolean supportsAAAARecords;
    private final boolean supportsARecords;
    private final InternetProtocolFamily preferredAddressType;
    private final DnsRecordType[] resolveRecordTypes;
    private final boolean decodeIdn;
    private final EventLoop executor;

    DnsNameResolver(EventLoop executor, ChannelFactory<? extends DatagramChannel> channelFactory, DnsQueryLifecycleObserverFactory dnsQueryLifecycleObserverFactory, long queryTimeoutMillis, ResolvedAddressTypes resolvedAddressTypes, boolean recursionDesired, int maxQueriesPerResolve, boolean traceEnabled, int maxPayloadSize, boolean optResourceEnabled, HostsFileEntriesResolver hostsFileEntriesResolver, DnsServerAddressStreamProvider dnsServerAddressStreamProvider, String[] searchDomains, int ndots, boolean decodeIdn) {
        this.executor = executor;
        this.queryContextManager = new DnsQueryContextManager();
        this.nameServerAddrStream = new FastThreadLocal<DnsServerAddressStream>() {
            protected DnsServerAddressStream initialValue() {
                return DnsNameResolver.this.dnsServerAddressStreamProvider.nameServerAddressStream("");
            }
        };
        this.queryTimeoutMillis = ObjectUtil.checkPositive(queryTimeoutMillis, "queryTimeoutMillis");
        this.resolvedAddressTypes = resolvedAddressTypes != null ? resolvedAddressTypes : DEFAULT_RESOLVE_ADDRESS_TYPES;
        this.recursionDesired = recursionDesired;
        this.maxQueriesPerResolve = ObjectUtil.checkPositive(maxQueriesPerResolve, "maxQueriesPerResolve");
        this.maxPayloadSize = ObjectUtil.checkPositive(maxPayloadSize, "maxPayloadSize");
        this.optResourceEnabled = optResourceEnabled;
        this.hostsFileEntriesResolver = (HostsFileEntriesResolver)ObjectUtil.checkNotNull(hostsFileEntriesResolver, "hostsFileEntriesResolver");
        this.dnsServerAddressStreamProvider = (DnsServerAddressStreamProvider)ObjectUtil.checkNotNull(dnsServerAddressStreamProvider, "dnsServerAddressStreamProvider");
        this.searchDomains = searchDomains != null ? (String[])searchDomains.clone() : DEFAULT_SEARCH_DOMAINS;
        this.ndots = ndots >= 0 ? ndots : DEFAULT_NDOTS;
        this.decodeIdn = decodeIdn;
        switch(this.resolvedAddressTypes) {
            case IPV4_ONLY:
                this.supportsAAAARecords = false;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV4_ONLY_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES;
                break;
            case IPV4_PREFERRED:
                this.supportsAAAARecords = true;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV4_PREFERRED_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
                break;
            case IPV6_ONLY:
                this.supportsAAAARecords = true;
                this.supportsARecords = false;
                this.resolveRecordTypes = IPV6_ONLY_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES;
                break;
            case IPV6_PREFERRED:
                this.supportsAAAARecords = true;
                this.supportsARecords = true;
                this.resolveRecordTypes = IPV6_PREFERRED_RESOLVED_RECORD_TYPES;
                this.resolvedInternetProtocolFamilies = IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES;
                break;
            default:
                throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
        }

        this.preferredAddressType = preferredAddressType(this.resolvedAddressTypes);
        Bootstrap b = new Bootstrap();
        b.group(this.executor());
        b.channelFactory(channelFactory);
        b.option(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, true);
        final DnsNameResolver.DnsResponseHandler responseHandler = new DnsNameResolver.DnsResponseHandler(this.executor().newPromise());
        b.handler(new ChannelInitializer<DatagramChannel>() {
            protected void initChannel(DatagramChannel ch) throws Exception {
                ch.pipeline().addLast(new ChannelHandler[]{DnsNameResolver.DECODER, DnsNameResolver.ENCODER, responseHandler});
//                ch.pipeline().addLast(new ChannelHandler[]{DnsNameResolver.ENCODER, responseHandler});
            }
        });
        this.channelFuture = responseHandler.channelActivePromise;
        ChannelFuture future = b.register();
        Throwable cause = future.cause();
        if (cause != null) {
            if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else if (cause instanceof Error) {
                throw (Error)cause;
            } else {
                throw new IllegalStateException("Unable to create / register Channel", cause);
            }
        } else {
            this.ch = future.channel();
            this.ch.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(maxPayloadSize));
            this.ch.closeFuture();
        }
    }

    static InternetProtocolFamily preferredAddressType(ResolvedAddressTypes resolvedAddressTypes) {
        switch(resolvedAddressTypes) {
            case IPV4_ONLY:
            case IPV4_PREFERRED:
                return InternetProtocolFamily.IPv4;
            case IPV6_ONLY:
            case IPV6_PREFERRED:
                return InternetProtocolFamily.IPv6;
            default:
                throw new IllegalArgumentException("Unknown ResolvedAddressTypes " + resolvedAddressTypes);
        }
    }

    InetSocketAddress newRedirectServerAddress(InetAddress server) {
        return new InetSocketAddress(server, 53);
    }

    public long queryTimeoutMillis() {
        return this.queryTimeoutMillis;
    }

    public ResolvedAddressTypes resolvedAddressTypes() {
        return this.resolvedAddressTypes;
    }

    InternetProtocolFamily[] resolvedInternetProtocolFamiliesUnsafe() {
        return this.resolvedInternetProtocolFamilies;
    }

    final String[] searchDomains() {
        return this.searchDomains;
    }

    final int ndots() {
        return this.ndots;
    }

    final boolean supportsAAAARecords() {
        return this.supportsAAAARecords;
    }

    final boolean supportsARecords() {
        return this.supportsARecords;
    }

    final InternetProtocolFamily preferredAddressType() {
        return this.preferredAddressType;
    }

    final DnsRecordType[] resolveRecordTypes() {
        return this.resolveRecordTypes;
    }

    final boolean isDecodeIdn() {
        return this.decodeIdn;
    }

    public boolean isRecursionDesired() {
        return this.recursionDesired;
    }

    public int maxQueriesPerResolve() {
        return this.maxQueriesPerResolve;
    }

    public int maxPayloadSize() {
        return this.maxPayloadSize;
    }

    public boolean isOptResourceEnabled() {
        return this.optResourceEnabled;
    }

    public HostsFileEntriesResolver hostsFileEntriesResolver() {
        return this.hostsFileEntriesResolver;
    }

    public void close() {
        if (this.ch.isOpen()) {
            this.ch.close();
        }

    }

    protected EventLoop executor() {
        return executor;
    }

    private InetAddress resolveHostsFileEntry(String hostname) {
        if (this.hostsFileEntriesResolver == null) {
            return null;
        } else {
            InetAddress address = this.hostsFileEntriesResolver.address(hostname, this.resolvedAddressTypes);
            return address == null && PlatformDependent.isWindows() && "localhost".equalsIgnoreCase(hostname) ? LOCALHOST_ADDRESS : address;
        }
    }

    private static DnsRecord[] toArray(Iterable<DnsRecord> additionals, boolean validateType) {
        ObjectUtil.checkNotNull(additionals, "additionals");
        DnsRecord r;
        if (additionals instanceof Collection) {
            Collection<DnsRecord> records = (Collection)additionals;
            Iterator var6 = additionals.iterator();

            while(var6.hasNext()) {
                r = (DnsRecord)var6.next();
                validateAdditional(r, validateType);
            }

            return (DnsRecord[])records.toArray(new DnsRecord[records.size()]);
        } else {
            Iterator<DnsRecord> additionalsIt = additionals.iterator();
            if (!additionalsIt.hasNext()) {
                return EMPTY_ADDITIONALS;
            } else {
                ArrayList records = new ArrayList();

                do {
                    r = (DnsRecord)additionalsIt.next();
                    validateAdditional(r, validateType);
                    records.add(r);
                } while(additionalsIt.hasNext());

                return (DnsRecord[])records.toArray(new DnsRecord[records.size()]);
            }
        }
    }

    private static void validateAdditional(DnsRecord record, boolean validateType) {
        ObjectUtil.checkNotNull(record, "record");
        if (validateType && record instanceof DnsRawRecord) {
            throw new IllegalArgumentException("DnsRawRecord implementations not allowed: " + record);
        }
    }

    private InetAddress loopbackAddress() {
        return this.preferredAddressType().localhost();
    }

    private boolean doResolveCached(String hostname, DnsRecord[] additionals, Promise<InetAddress> promise, DnsCache resolveCache) {
        List<? extends DnsCacheEntry> cachedEntries = resolveCache.get(hostname, additionals);
        if (cachedEntries != null && !cachedEntries.isEmpty()) {
            Throwable cause = ((DnsCacheEntry)cachedEntries.get(0)).cause();
            if (cause != null) {
                tryFailure(promise, cause);
                return true;
            } else {
                int numEntries = cachedEntries.size();
                InternetProtocolFamily[] var8 = this.resolvedInternetProtocolFamilies;
                int var9 = var8.length;

                for(int var10 = 0; var10 < var9; ++var10) {
                    InternetProtocolFamily f = var8[var10];

                    for(int i = 0; i < numEntries; ++i) {
                        DnsCacheEntry e = (DnsCacheEntry)cachedEntries.get(i);
                        if (f.addressType().isInstance(e.address())) {
                            trySuccess(promise, e.address());
                            return true;
                        }
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    static <T> void trySuccess(Promise<T> promise, T result) {
        if (!promise.trySuccess(result)) {
            logger.warn("Failed to notify success ({}) to a promise: {}", result, promise);
        }

    }

    private static void tryFailure(Promise<?> promise, Throwable cause) {
        if (!promise.tryFailure(cause)) {
            logger.warn("Failed to notify failure to a promise: {}", promise, cause);
        }

    }

    private static String hostname(String inetHost) {
        String hostname = IDN.toASCII(inetHost);
        if (StringUtil.endsWith(inetHost, '.') && !StringUtil.endsWith(hostname, '.')) {
            hostname = hostname + ".";
        }

        return hostname;
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question) {
        return this.query(this.nextNameServerAddress(), question);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Iterable<DnsRecord> additionals) {
        return this.query(this.nextNameServerAddress(), question, additionals);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query(this.nextNameServerAddress(), question, Collections.emptyList(), promise);
    }

    private InetSocketAddress nextNameServerAddress() {
        return ((DnsServerAddressStream)this.nameServerAddrStream.get()).next();
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question) {
        return this.query0(nameServerAddr, question, EMPTY_ADDITIONALS, true, this.ch.newPromise(), this.ch.eventLoop().newPromise());
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals) {
        return this.query0(nameServerAddr, question, toArray(additionals, false), true, this.ch.newPromise(), this.ch.eventLoop().newPromise());
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query0(nameServerAddr, question, EMPTY_ADDITIONALS, true, this.ch.newPromise(), promise);
    }

    public Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query(InetSocketAddress nameServerAddr, DnsQuestion question, Iterable<DnsRecord> additionals, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        return this.query0(nameServerAddr, question, toArray(additionals, false), true, this.ch.newPromise(), promise);
    }

    public static boolean isTransportOrTimeoutError(Throwable cause) {
        return cause != null && cause.getCause() instanceof DnsNameResolverException;
    }

    public static boolean isTimeoutError(Throwable cause) {
        return cause != null && cause.getCause() instanceof DnsNameResolverTimeoutException;
    }

    final void flushQueries() {
        this.ch.flush();
    }

    final Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> query0(InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, boolean flush, ChannelPromise writePromise, Promise<AddressedEnvelope<? extends DnsResponse, InetSocketAddress>> promise) {
        assert !writePromise.isVoid();

        Promise castPromise = (Promise)ObjectUtil.checkNotNull(promise, "promise");

        try {
            (new DnsQueryContext(this, nameServerAddr, question, additionals, castPromise)).query(flush, writePromise);
            return castPromise;
        } catch (Exception var9) {
            return castPromise.setFailure(var9);
        }
    }

    final DnsServerAddressStream newNameServerAddressStream(String hostname) {
        return this.dnsServerAddressStreamProvider.nameServerAddressStream(hostname);
    }

    static {
        IPV4_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.A};
        IPV4_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv4};
        IPV4_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.A, DnsRecordType.AAAA};
        IPV4_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv4, InternetProtocolFamily.IPv6};
        IPV6_ONLY_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.AAAA};
        IPV6_ONLY_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv6};
        IPV6_PREFERRED_RESOLVED_RECORD_TYPES = new DnsRecordType[]{DnsRecordType.AAAA, DnsRecordType.A};
        IPV6_PREFERRED_RESOLVED_PROTOCOL_FAMILIES = new InternetProtocolFamily[]{InternetProtocolFamily.IPv6, InternetProtocolFamily.IPv4};
        if (NetUtil.isIpV4StackPreferred()) {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_ONLY;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
        } else if (NetUtil.isIpV6AddressesPreferred()) {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV6_PREFERRED;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST6;
        } else {
            DEFAULT_RESOLVE_ADDRESS_TYPES = ResolvedAddressTypes.IPV4_PREFERRED;
            LOCALHOST_ADDRESS = NetUtil.LOCALHOST4;
        }

        String[] searchDomains = EmptyArrays.EMPTY_STRINGS;
//        try {
//            List<String> list = PlatformDependent.isWindows() ? getSearchDomainsHack() : UnixResolverDnsServerAddressStreamProvider.parseEtcResolverSearchDomains();
//            searchDomains = (String[])list.toArray(new String[0]);
//        } catch (Exception var4) {
//            searchDomains = EmptyArrays.EMPTY_STRINGS;
//        }

        DEFAULT_SEARCH_DOMAINS = searchDomains;

        int ndots = 1;
//        try {
//            ndots = UnixResolverDnsServerAddressStreamProvider.parseEtcResolverFirstNdots();
//        } catch (Exception var3) {
//            ndots = 1;
//        }

        DEFAULT_NDOTS = ndots;
        DECODER = new DatagramDnsResponseDecoder();
        ENCODER = new DatagramDnsQueryEncoder();
    }

    private final class DnsResponseHandler extends ChannelInboundHandlerAdapter {
        private final Promise<Channel> channelActivePromise;

        DnsResponseHandler(Promise<Channel> channelActivePromise) {
            this.channelActivePromise = channelActivePromise;
        }

        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
//                DatagramPacket packet = (DatagramPacket)msg;
                DatagramDnsResponse res = (DatagramDnsResponse)msg;
                int queryId = res.id();
                if (DnsNameResolver.logger.isDebugEnabled()) {
                    DnsNameResolver.logger.debug("{} RECEIVED: [{}: {}], {}", new Object[]{DnsNameResolver.this.ch, queryId, res.sender(), res});
                }

                DnsQueryContext qCtx = DnsNameResolver.this.queryContextManager.get(res.sender(), queryId);
                if (qCtx != null) {
                    qCtx.finish(res);
                    return;
                }

                DnsNameResolver.logger.warn("{} Received a DNS response with an unknown ID: {}", DnsNameResolver.this.ch, queryId);
            } finally {
                ReferenceCountUtil.safeRelease(msg);
            }

        }

        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            this.channelActivePromise.setSuccess(ctx.channel());
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            DnsNameResolver.logger.warn("{} Unexpected exception: ", DnsNameResolver.this.ch, cause);
        }
    }
}

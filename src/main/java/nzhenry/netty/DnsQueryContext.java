//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nzhenry.netty;

import io.netty.channel.AddressedEnvelope;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.dns.AbstractDnsOptPseudoRrRecord;
import io.netty.handler.codec.dns.DatagramDnsQuery;
import io.netty.handler.codec.dns.DnsQuery;
import io.netty.handler.codec.dns.DnsQuestion;
import io.netty.handler.codec.dns.DnsRecord;
import io.netty.handler.codec.dns.DnsResponse;
import io.netty.handler.codec.dns.DnsSection;
import io.netty.resolver.dns.DnsNameResolverException;
import io.netty.resolver.dns.DnsNameResolverTimeoutException;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

final class DnsQueryContext implements FutureListener<AddressedEnvelope<DnsResponse, InetSocketAddress>> {
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DnsQueryContext.class);
    private final DnsNameResolver parent;
    private final Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise;
    private final int id;
    private final DnsQuestion question;
    private final DnsRecord[] additionals;
    private final DnsRecord optResource;
    private final InetSocketAddress nameServerAddr;
    private final boolean recursionDesired;
    private volatile ScheduledFuture<?> timeoutFuture;

    DnsQueryContext(DnsNameResolver parent, InetSocketAddress nameServerAddr, DnsQuestion question, DnsRecord[] additionals, Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise) {
        this.parent = (DnsNameResolver)ObjectUtil.checkNotNull(parent, "parent");
        this.nameServerAddr = (InetSocketAddress)ObjectUtil.checkNotNull(nameServerAddr, "nameServerAddr");
        this.question = (DnsQuestion)ObjectUtil.checkNotNull(question, "question");
        this.additionals = (DnsRecord[])ObjectUtil.checkNotNull(additionals, "additionals");
        this.promise = (Promise)ObjectUtil.checkNotNull(promise, "promise");
        this.recursionDesired = parent.isRecursionDesired();
        this.id = parent.queryContextManager.add(this);
        promise.addListener(this);
        if (parent.isOptResourceEnabled()) {
            this.optResource = new AbstractDnsOptPseudoRrRecord(parent.maxPayloadSize(), 0, 0) {
            };
        } else {
            this.optResource = null;
        }

    }

    InetSocketAddress nameServerAddr() {
        return this.nameServerAddr;
    }

    DnsQuestion question() {
        return this.question;
    }

    void query(boolean flush, ChannelPromise writePromise) {
        DnsQuestion question = this.question();
        InetSocketAddress nameServerAddr = this.nameServerAddr();
        DatagramDnsQuery query = new DatagramDnsQuery((InetSocketAddress)null, nameServerAddr, this.id);
        query.setRecursionDesired(this.recursionDesired);
        query.addRecord(DnsSection.QUESTION, question);
        DnsRecord[] var6 = this.additionals;
        int var7 = var6.length;

        for(int var8 = 0; var8 < var7; ++var8) {
            DnsRecord record = var6[var8];
            query.addRecord(DnsSection.ADDITIONAL, record);
        }

        if (this.optResource != null) {
            query.addRecord(DnsSection.ADDITIONAL, this.optResource);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("{} WRITE: [{}: {}], {}", new Object[]{this.parent.ch, this.id, nameServerAddr, question});
        }

        this.sendQuery(query, flush, writePromise);
    }

    private void sendQuery(final DnsQuery query, boolean flush, final ChannelPromise writePromise) {
        if (this.parent.channelFuture.isDone()) {
            this.writeQuery(query, flush, writePromise);
        } else {
            this.parent.channelFuture.addListener(new GenericFutureListener<Future<? super Channel>>() {
                public void operationComplete(Future<? super Channel> future) {
                    if (future.isSuccess()) {
                        DnsQueryContext.this.writeQuery(query, true, writePromise);
                    } else {
                        Throwable cause = future.cause();
                        DnsQueryContext.this.promise.tryFailure(cause);
                        writePromise.setFailure(cause);
                    }

                }
            });
        }

    }

    private void writeQuery(DnsQuery query, boolean flush, ChannelPromise writePromise) {
        final ChannelFuture writeFuture = flush ? this.parent.ch.writeAndFlush(query, writePromise) : this.parent.ch.write(query, writePromise);
        if (writeFuture.isDone()) {
            this.onQueryWriteCompletion(writeFuture);
        } else {
            writeFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) {
                    DnsQueryContext.this.onQueryWriteCompletion(writeFuture);
                }
            });
        }

    }

    private void onQueryWriteCompletion(ChannelFuture writeFuture) {
        if (!writeFuture.isSuccess()) {
            this.setFailure("failed to send a query", writeFuture.cause());
        } else {
            final long queryTimeoutMillis = this.parent.queryTimeoutMillis();
            if (queryTimeoutMillis > 0L) {
                this.timeoutFuture = this.parent.ch.eventLoop().schedule(new Runnable() {
                    public void run() {
                        if (!DnsQueryContext.this.promise.isDone()) {
                            DnsQueryContext.this.setFailure("query timed out after " + queryTimeoutMillis + " milliseconds", (Throwable)null);
                        }
                    }
                }, queryTimeoutMillis, TimeUnit.MILLISECONDS);
            }

        }
    }

    void finish(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
        DnsResponse res = (DnsResponse)envelope.content();
        if (res.count(DnsSection.QUESTION) != 1) {
            logger.warn("Received a DNS response with invalid number of questions: {}", envelope);
        } else if (!this.question().equals(res.recordAt(DnsSection.QUESTION))) {
            logger.warn("Received a mismatching DNS response: {}", envelope);
        } else {
            this.setSuccess(envelope);
        }
    }

    private void setSuccess(AddressedEnvelope<? extends DnsResponse, InetSocketAddress> envelope) {
        Promise<AddressedEnvelope<DnsResponse, InetSocketAddress>> promise = this.promise;
        AddressedEnvelope<DnsResponse, InetSocketAddress> castResponse = (AddressedEnvelope<DnsResponse, InetSocketAddress>)envelope.retain();
        if (!promise.trySuccess(castResponse)) {
            envelope.release();
        }

    }

    private void setFailure(String message, Throwable cause) {
        InetSocketAddress nameServerAddr = this.nameServerAddr();
        StringBuilder buf = new StringBuilder(message.length() + 64);
        buf.append('[').append(nameServerAddr).append("] ").append(message).append(" (no stack trace available)");
        Object e;
        if (cause == null) {
            e = new DnsNameResolverTimeoutException(nameServerAddr, this.question(), buf.toString());
        } else {
            e = new DnsNameResolverException(nameServerAddr, this.question(), buf.toString(), cause);
        }

        this.promise.tryFailure((Throwable)e);
    }

    public void operationComplete(Future<AddressedEnvelope<DnsResponse, InetSocketAddress>> future) {
        ScheduledFuture<?> timeoutFuture = this.timeoutFuture;
        if (timeoutFuture != null) {
            this.timeoutFuture = null;
            timeoutFuture.cancel(false);
        }

        this.parent.queryContextManager.remove(this.nameServerAddr, this.id);
    }
}

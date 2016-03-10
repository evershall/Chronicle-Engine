package net.openhft.chronicle.engine.server.internal;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.core.annotation.UsedViaReflection;
import net.openhft.chronicle.core.threads.EventLoop;
import net.openhft.chronicle.engine.api.tree.Asset;
import net.openhft.chronicle.engine.api.tree.RequestContext;
import net.openhft.chronicle.engine.map.CMap2EngineReplicator;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptAppender;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static net.openhft.chronicle.network.HeaderTcpHandler.toHeader;

/**
 * Created by Rob Austin
 */
public class QueueReplicationHandler extends CspTcpHander<EngineWireNetworkContext> implements
        Demarshallable, WriteMarshallable {

    private static final Logger LOG = LoggerFactory.getLogger(QueueReplicationHandler.class);

    private RequestContext requestContext;
    private EventLoop eventLoop;
    private Asset rootAsset;
    private volatile boolean closed;
    private ChronicleQueue queue;
    private boolean isSource;

    @UsedViaReflection
    private QueueReplicationHandler(@NotNull WireIn wire) {
        final WireType wireType = wire.read(() -> "wireType").object(WireType.class);
        wireType(wireType);
    }

    public QueueReplicationHandler(boolean isSource) {
        this.isSource = isSource;
    }

    @Override
    public void writeMarshallable(@NotNull WireOut wire) {
        final WireType value = wireType();
        wire.write(() -> "wireType").object(value);
        wire.write(() -> "isSource").bool(isSource);
    }

    @Override
    public void nc(EngineWireNetworkContext nc) {
        super.nc(nc);
        isAcceptor(nc.isAcceptor());
        rootAsset = nc.rootAsset();
        publisher(nc.wireOutPublisher());

        this.eventLoop = rootAsset.findOrCreateView(EventLoop.class);
        eventLoop.start();

        if (nc.isAcceptor())
            // reflect the header back to the client
            nc.wireOutPublisher().put("",
                    toHeader(new QueueReplicationHandler(isSource)));
    }

    final ThreadLocal<CMap2EngineReplicator.VanillaReplicatedEntry> vre = ThreadLocal.withInitial(CMap2EngineReplicator.VanillaReplicatedEntry::new);

    public enum EventId implements ParameterizeWireKey {
        replicationEvent,
        bootstrap,
        lastUpdateIndex,
        identifierReply,
        identifier;

        private final WireKey[] params;

        @SafeVarargs
        <P extends WireKey> EventId(P... params) {
            this.params = params;
        }

        @NotNull
        public <P extends WireKey> P[] params() {
            //noinspection unchecked
            return (P[]) this.params;
        }
    }

    @Override
    public void close() {
        super.close();
        closed = true;
    }

    class Q {
        ExcerptAppender appender;
        ExcerptTailer tailer;

        public Q(ExcerptAppender appender, ExcerptTailer tailer) {
            this.appender = appender;
            this.tailer = tailer;
        }
    }


    ConcurrentHashMap<String, Q> qs = new ConcurrentHashMap();
    Q q;


    @Override
    protected void process(@NotNull WireIn inWire, @NotNull WireOut outWire) {

    /*    LOG.info("isAcceptor=" + nc().isAcceptor());
        LOG.info("wire-in=" + Wires.fromSizePrefixedBlobs(inWire.bytes()));

        try (final DocumentContext dc = inWire.readingDocument()) {

            if (!dc.isPresent())
                return;

            if (dc.isMetaData()) {
                readCsp(inWire);

                if (hasCspChanged(cspText)) {

                    q = qs.computeIfAbsent(cspText.toString(), key -> {
                        requestContext = requestContextInterner.intern(cspText);
                        final Asset asset = rootAsset.acquireAsset(requestContext.fullName());
                        queue = asset.acquireView(ChronicleQueue.class, requestContext);

                        final ExcerptAppender appender = queue.createAppender();
                        final ExcerptTailer tailer = queue.createTailer();
                        return new Q(appender, tailer);
                    });
                }
                return;
            }

            StringBuilder eventName = Wires.acquireStringBuilder();

            // eventName.setLength(0);
            final ValueIn valueIn = inWire.readEventName(eventName);

            // receives replication events
            if (EventId.lastUpdateIndex.contentEquals(eventName)) {
                // Thread.sleep(100);
                if (Jvm.isDebug())
                    LOG.info("server : received lastUpdateTime");
                final long index = valueIn.int64();
                *//*if (isSource)
                    q.
*//*

                return;
            }

            // receives replication events
            if (replicationEvent.contentEquals(eventName)) {

                if (Jvm.isDebug() && LOG.isDebugEnabled())
                    LOG.debug("server : received replicationEvent");
                CMap2EngineReplicator.VanillaReplicatedEntry replicatedEntry = vre.get();
                valueIn.marshallable(replicatedEntry);

                if (Jvm.isDebug() && LOG.isDebugEnabled())
                    LOG.debug("*****\t\t\t\t ->  RECEIVED : SERVER : replication latency=" + (System
                            .currentTimeMillis() - replicatedEntry.timestamp()) + "ms  ");

                replication.applyReplication(replicatedEntry);
                return;
            }

            if (bootstrap.contentEquals(eventName)) {
                //      Thread.sleep(100);
                // receive bootstrap
                final long timestamp = valueIn.int64();

                assert localIdentifier != remoteIdentifier;

                final ModificationIterator mi = replication.acquireModificationIterator(remoteIdentifier);


                if (mi != null)
                    mi.dirtyEntries(timestamp);

                if (isAcceptor()) {

                    outWire.writeDocument(true, d -> d.write(CoreFields.csp).text(cspText)
                            .write(CoreFields.cid).int64(cid()));
                    outWire.writeDocument(false, d -> outWire.write(bootstrap)
                            .int64(replication.lastModificationTime(remoteIdentifier))
                            .writeComment("server: localIdentifier=" + localIdentifier + ",remoteIdentifier=" + remoteIdentifier));

                    logYaml(outWire);
                }

                if (mi == null)
                    return;

                // sends replication events back to the remote client
                mi.setModificationNotifier(eventLoop::unpause);

                if (isAcceptor())
                    LOG.info("adding handler");

                if (!eventLoop.isAlive() && !eventLoop.isClosed())
                    throw new IllegalStateException("the event loop is not yet running !");

                eventLoop.addHandler(true, new ReplicationEventHandler(mi, remoteIdentifier));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

  /*  private class ReplicationEventHandler implements EventHandler, Closeable {

        private final ModificationIterator mi;
        private final byte id;
        boolean hasSentLastUpdateTime;
        long lastUpdateTime;
        boolean hasLogged;
        int count;
        long startBufferFullTimeStamp;

        public ReplicationEventHandler(ModificationIterator mi, byte id) {
            this.mi = mi;
            this.id = id;
            lastUpdateTime = 0;
            hasLogged = false;
            count = 0;
            startBufferFullTimeStamp = 0;
        }

        @NotNull
        @Override
        public HandlerPriority priority() {
            return HandlerPriority.REPLICATION;
        }

        @Override
        public boolean action() throws InvalidEventHandlerException {


            if (closed || nc().connectionClosed())
                throw new InvalidEventHandlerException();

            final WireOutPublisher publisher = nc().wireOutPublisher();

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (publisher) {

                assert !closed;

                if (publisher.isClosed())
                    throw new InvalidEventHandlerException("publisher is closed");

                // given the sending an event to the publish hold the chronicle map lock
                // we will send only one at a time

                if (!publisher.canTakeMoreData()) {
                    if (startBufferFullTimeStamp == 0) {
                        startBufferFullTimeStamp = System.currentTimeMillis();
                    }
                    return false;
                }

                if (!mi.hasNext()) {

                    if (startBufferFullTimeStamp != 0) {
                        long timetaken = System.currentTimeMillis() - startBufferFullTimeStamp;
                        if (timetaken > 100)
                            LOG.info("blocked - outbound buffer full=" + timetaken + "ms");
                        startBufferFullTimeStamp = 0;
                    }

                    // because events arrive in a bitset ( aka random ) order ( not necessary in
                    // time order ) we can only be assured that the latest time of
                    // the last event is really the latest time, once all the events
                    // have been received, we know when we have received all events
                    // when there are no more events to process.
                    if (!hasSentLastUpdateTime && lastUpdateTime > 0) {

                        publisher.put(null, w -> {
                            w.writeDocument(true, d -> d.write(CoreFields.cid).int64(cid()));
                            w.writeDocument(false, d -> {
                                        d.writeEventName(CoreFields.lastUpdateTime).int64(lastUpdateTime);
                                        d.write(() -> "id").int8(id);
                                    }
                            );
                        });

                        hasSentLastUpdateTime = true;

                        if (!hasLogged) {
                            LOG.info("received ALL replication the EVENTS for " +
                                    "id=" + id);
                            hasLogged = true;
                        }

                    }
                    return false;
                }

                mi.nextEntry(e -> {


                            publisher.put(null, w -> {

                                assert e.remoteIdentifier() != localIdentifier;


                                long newlastUpdateTime = Math.max(lastUpdateTime, e.timestamp());

                                if (newlastUpdateTime > lastUpdateTime) {
                                    hasSentLastUpdateTime = false;
                                    lastUpdateTime = newlastUpdateTime;
                                }

                                if (LOG.isDebugEnabled())
                                    LOG.debug("publish from server response from iterator " +
                                            "localIdentifier=" + localIdentifier + " ,remoteIdentifier=" +
                                            id + " event=" + e);

                                w.writeDocument(true, d -> d.write(CoreFields.cid).int64(cid()));
                                w.writeDocument(false,
                                        d -> {
                                            d.writeEventName(replicationEvent).typedMarshallable(e);
                                            d.writeComment("isAcceptor=" + nc().isAcceptor());
                                        }
                                );

                            });
                        }
                );
            }
            return true;
        }

        @Override
        public String toString() {
            return "ReplicationEventHandler{" +
                    "id=" + id + ",connectionClosed=" + nc().connectionClosed() +
                    '}';
        }

        @Override
        public void close() {
            QueueReplicationHandler.this.close();
        }
    }*/

    @Override
    public void process(@NotNull Bytes in, @NotNull Bytes out) {
        if (YamlLogging.showServerReads && !in.isEmpty())
            LOG.info("read:\n" + Wires.fromSizePrefixedBlobs(in));
        super.process(in, out);
    }
}
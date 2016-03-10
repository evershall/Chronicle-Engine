package net.openhft.chronicle.engine.api.pubsub;

import net.openhft.chronicle.engine.api.EngineReplication.ModificationIterator;
import net.openhft.chronicle.engine.api.EngineReplication.ReplicationEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Rob Austin
 */
public interface QueueReplication {

    /**
     * removes or puts the entry into the map
     */
    void bootstrap(@NotNull ReplicationEntry replicatedEntry);

    @Nullable
    ModificationIterator acquireModificationIterator(byte id);

    //  long lastModificationTime(byte id);

    // void setLastModificationTime(final byte identifier, final long timestamp);
}
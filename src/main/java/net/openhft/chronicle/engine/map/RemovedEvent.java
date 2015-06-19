/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.chronicle.engine.map;

import net.openhft.chronicle.engine.api.map.MapEvent;
import net.openhft.chronicle.engine.api.map.MapEventListener;
import net.openhft.chronicle.wire.WireIn;
import net.openhft.chronicle.wire.WireOut;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by peter on 22/05/15.
 */
public class RemovedEvent<K, V> implements MapEvent<K, V> {
    private String assetName;
    private K key;
    private V oldValue;

    private RemovedEvent(String assetName, K key, V oldValue) {
        this.assetName = assetName;
        this.key = key;
        this.oldValue = oldValue;
    }

    @Override
    public String assetName() {
        return assetName;
    }

    @NotNull
    public static <K, V> RemovedEvent<K, V> of(String assetName, K key, V value) {
        return new RemovedEvent<>(assetName, key, value);
    }

    @NotNull
    @Override
    public <K2, V2> MapEvent<K2, V2> translate(@NotNull Function<K, K2> keyFunction, @NotNull Function<V, V2> valueFunction) {
        return new RemovedEvent<>(assetName, keyFunction.apply(key), valueFunction.apply(oldValue));
    }

    @Override
    public <K2, V2> MapEvent<K2, V2> translate(BiFunction<K, K2, K2> keyFunction, BiFunction<V, V2, V2> valueFunction) {
        return new RemovedEvent<>(assetName, keyFunction.apply(key, null), valueFunction.apply(oldValue, null));
    }

    public K key() {
        return key;
    }

    @Override
    public V oldValue() {
        return oldValue;
    }

    @Nullable
    public V value() {
        return null;
    }

    @Override
    public void apply(@NotNull MapEventListener<K, V> listener) {
        listener.remove(key, oldValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash("removed", key, oldValue);
    }

    @Override
    public boolean equals(Object obj) {
        return Optional.ofNullable(obj)
                .filter(o -> o instanceof RemovedEvent)
                .map(o -> (RemovedEvent<K, V>) o)
                .filter(e -> Objects.equals(assetName, e.assetName))
                .filter(e -> Objects.equals(key, e.key))
                .filter(e -> Objects.equals(oldValue, e.oldValue))
                .isPresent();
    }

    @Override
    public String toString() {
        return "RemovedEvent{" +
                "assetName='" + assetName + '\'' +
                ", key=" + key +
                ", oldValue=" + oldValue +
                '}';
    }


    @Override
    public void readMarshallable(WireIn wire) throws IllegalStateException {
        wire.read(MapEventFields.assetName).text(s -> assetName = s);
        key = (K) wire.read(MapEventFields.key).object(Object.class);
        oldValue = (V) wire.read(MapEventFields.oldValue).object(Object.class);
    }

    @Override
    public void writeMarshallable(WireOut wire) {
        wire.write(MapEventFields.assetName).text(assetName);
        wire.write(MapEventFields.key).object(key);
        wire.write(MapEventFields.oldValue).object(oldValue);
    }
}

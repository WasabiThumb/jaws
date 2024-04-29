package xyz.wasabicodes.jaws.util;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import org.slf4j.Logger;
import xyz.wasabicodes.jaws.Jaws;

import java.util.*;
import java.util.concurrent.locks.StampedLock;

public class DistributedConcurrentHashMap<K, V> implements Map<K, V> {

    private final int bits;
    private final int mask;
    private final Object[] entries;
    public DistributedConcurrentHashMap(int bits) throws IllegalArgumentException {
        if (bits < 0 || bits > 16) throw new IllegalStateException("Number of parallel bits (" + bits + ") not supported");
        this.bits = bits;
        int size = 1 << bits;
        this.mask = (1 << bits) - 1;
        this.entries = new Object[size];
        for (int i=0; i < this.entries.length; i++) this.entries[i] = new Entry<V>(new StampedLock(), new Int2ObjectLinkedOpenHashMap<>());
    }

    public void report() {
        Logger l = Jaws.getLogger();
        l.debug("= DCH Map (" + this.bits + ") =" + System.lineSeparator());

        Entry<?> entry;
        for (int i=0; i < this.entries.length; i++) {
            entry = (Entry<?>) this.entries[i];

            String state = "IDLE";
            boolean wrLocked = entry.lock.isWriteLocked();
            if (wrLocked) {
                state = "WRLOCKED";
            } else if (entry.lock.isReadLocked()) {
                state = "RDLOCKED x" + entry.lock.getReadLockCount();
            }
            l.debug("[" + i + "] Sub-map: " + state);

            if (!wrLocked) {
                final long stamp = entry.lock.readLock();
                try {
                    int size = entry.map.size();
                    Iterator<Integer> iter = entry.map.keySet().iterator();

                    int z = 0;
                    Integer next;
                    while (iter.hasNext() && (z++) < 3) {
                        next = iter.next();
                        l.debug("| " + next + " = " + entry.map.get(next.intValue()));
                        size--;
                    }

                    if (size > 0) {
                        l.debug("-- " + size + " more...");
                    } else {
                        l.debug("-- END");
                    }
                } finally {
                    entry.lock.unlock(stamp);
                }
            }
            l.debug(" ");
        }
    }

    @Override
    public int size() {
        int ret = 0;
        Entry<?> entry;
        long stamp;
        for (Object ob : this.entries) {
            entry = (Entry<?>) ob;
            stamp = entry.lock.readLock();
            try {
                ret += entry.map.size();
            } finally {
                entry.lock.unlock(stamp);
            }
        }
        return ret;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        if (o == null) return false;
        return this.open(o, (Entry<V> entry, int index) -> {
            final long stamp = entry.lock.readLock();
            try {
                return entry.map.containsKey(index);
            } finally {
                entry.lock.unlock(stamp);
            }
        });
    }

    @Override
    public boolean containsValue(Object o) {
        Entry<?> entry;
        long stamp;
        for (Object ob : this.entries) {
            entry = (Entry<?>) ob;
            stamp = entry.lock.readLock();
            try {
                if (entry.map.containsValue(o)) return true;
            } finally {
                entry.lock.unlock(stamp);
            }
        }
        return false;
    }

    @Override
    public V get(Object o) {
        if (o == null) return null;
        return this.open(o, (Entry<V> entry, int index) -> {
            final long stamp = entry.lock.readLock();
            try {
                return entry.map.get(index);
            } finally {
                entry.lock.unlock(stamp);
            }
        });
    }

    @Override
    public V put(K k, V v) {
        Objects.requireNonNull(k);
        return this.open(k, (Entry<V> entry, int index) -> {
            final long stamp = entry.lock.writeLock();
            try {
                return entry.map.put(index, v);
            } finally {
                entry.lock.unlock(stamp);
            }
        });
    }

    @Override
    public V remove(Object o) {
        if (o == null) return null;
        return this.open(o, (Entry<V> entry, int index) -> {
            final long stamp = entry.lock.writeLock();
            try {
                return entry.map.remove(index);
            } finally {
                entry.lock.unlock(stamp);
            }
        });
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) this.put(entry.getKey(), entry.getValue());
    }

    @Override
    public void clear() {
        Entry<?> entry;
        long stamp;
        for (Object ob : this.entries) {
            entry = (Entry<?>) ob;
            stamp = entry.lock.readLock();
            try {
                if (!entry.map.isEmpty()) {
                    stamp = entry.lock.tryConvertToWriteLock(stamp);
                    if (stamp != 0L) entry.map.clear();
                }
            } finally {
                entry.lock.unlock(stamp);
            }
        }
    }

    @Override
    public Set<K> keySet() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    //

    @SuppressWarnings("unchecked")
    private <R> R open(Object key, Fn1<V, R> fn) {
        int hash = Objects.hashCode(key);
        Entry<V> entry = (Entry<V>) this.entries[hash & this.mask];
        return fn.apply(entry, hash >> this.bits);
    }

    @SuppressWarnings("unchecked")
    private boolean open(Object key, Fn2<V> fn) {
        int hash = Objects.hashCode(key);
        Entry<V> entry = (Entry<V>) this.entries[hash & this.mask];
        return fn.apply(entry, hash >> this.bits);
    }

    private record Entry<V>(StampedLock lock, Int2ObjectLinkedOpenHashMap<V> map) { }

    @FunctionalInterface
    private interface Fn1<T, R> {
        R apply(Entry<T> entry, int index);
    }

    @FunctionalInterface
    private interface Fn2<T> {
        boolean apply(Entry<T> entry, int index);
    }

}

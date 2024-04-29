package xyz.wasabicodes.jaws.util;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Append-only int-to-object map. Entries are expected to have a lifetime within the map.
 */
public class AOInt2ObjectMap<V> implements Int2ObjectMap<V> {

    private final Class<V> valueClass;
    protected Object[] arr;
    protected int keyOffset;
    protected int size;
    private boolean shouldAttemptShrink;
    private V drv = null;
    public AOInt2ObjectMap(Class<V> valueClass, int initialCapacity) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Initial capacity is less than 0 (" + initialCapacity + ")");
        this.valueClass = valueClass;
        this.arr = new Object[initialCapacity];
        this.keyOffset = 0;
        this.size = 0;
        this.shouldAttemptShrink = false;
    }

    public AOInt2ObjectMap(Class<V> valueClass) {
        this(valueClass, 16);
    }

    //

    @Override
    public void clear() {
        this.arr = new Object[0];
        this.keyOffset = 0;
        this.size = 0;
        this.shouldAttemptShrink = false;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public V get(int key) {
        key -= this.keyOffset;
        if (key < 0 || key >= this.arr.length) return this.drv;
        Object ret = this.arr[key];
        if (ret == null) return this.drv;
        return this.valueClass.cast(ret);
    }

    @Override
    public V put(int key, V value) {
        int index = this.preparePut(key);
        Object ret = this.arr[index];
        this.arr[index] = value;
        if (ret == null) {
            this.size++;
            return this.drv;
        }
        return this.valueClass.cast(ret);
    }

    @Override
    public V remove(int key) {
        int inArray = key - this.keyOffset;
        if (inArray < 0 || inArray >= this.arr.length) return this.drv;

        Object old = this.arr[inArray];
        if (old == null) return this.drv;
        this.arr[inArray] = null;
        this.size--;

        this.shouldAttemptShrink |= (inArray == 0);
        if (this.shouldAttemptShrink) this.attemptShrink();

        return this.valueClass.cast(old);
    }

    public V removeFirst() {
        int inArray = 0;
        Object old;
        while (inArray < this.arr.length) {
            old = this.arr[inArray];
            if (old == null) {
                inArray++;
                continue;
            }
            this.arr[inArray] = null;
            this.size--;
            this.shouldAttemptShrink = true;
            this.attemptShrink();
            return this.valueClass.cast(old);
        }
        return null;
    }

    //

    @Override
    public void defaultReturnValue(V rv) {
        this.drv = rv;
    }

    @Override
    public V defaultReturnValue() {
        return this.drv;
    }

    @Override
    public ObjectSet<Entry<V>> int2ObjectEntrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntSet keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ObjectCollection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsKey(int key) {
        key -= this.keyOffset;
        if (key < 0 || key >= this.arr.length) return false;
        return this.arr[key] != null;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsValue(Object o) {
        if (o != null && (!this.valueClass.isInstance(o))) return false;
        for (Object object : this.arr) {
            if (Objects.equals(object, o)) return true;
        }
        return false;
    }

    @Override
    public void putAll(Map<? extends Integer, ? extends V> map) {
        Integer k;
        for (Map.Entry<? extends Integer, ? extends V> entry : map.entrySet()) {
            k = entry.getKey();
            if (k == null) continue;
            this.put(k.intValue(), entry.getValue());
        }
    }

    public void removeIf(Predicate<V> predicate) {
        IntList removals = new IntArrayList(Math.max(this.arr.length >> 2, 4));
        Object next;
        for (int i=0; i < this.arr.length; i++) {
            next = this.arr[i];
            if (next == null) continue;
            if (predicate.test(this.valueClass.cast(next))) removals.add(i);
        }
        final int len = removals.size();
        for (int i=0; i < len; i++) {
            this.remove(removals.getInt(i));
        }
    }

    //

    private void attemptShrink() {
        int start;
        for (start = 0; start < this.arr.length; start++) {
            if (this.arr[start] != null) break;
        }
        if (start < ((this.arr.length - 1) >> 1)) return;

        final int newLen = this.arr.length - start;
        Object[] cpy = new Object[newLen];
        System.arraycopy(this.arr, start, cpy, 0, newLen);
        this.arr = cpy;
        this.keyOffset += start;
        this.shouldAttemptShrink = false;
    }

    private int preparePut(int key) {
        if (this.size == 0) {
            this.keyOffset = key;
            key = 0;
        } else if (key < this.keyOffset) {
            // Destructive!
            this.clear();
            this.keyOffset = key;
            key = 0;
        } else {
            key -= this.keyOffset;
        }
        final int requiredLen = key + 1;
        int newLen = this.arr.length;
        if (requiredLen <= newLen) return key;
        if (newLen < 1) newLen = 2;
        while (newLen < requiredLen) {
            newLen <<= 1;
        }
        Object[] cpy = new Object[newLen];
        System.arraycopy(this.arr, 0, cpy, 0, this.arr.length);
        this.arr = cpy;
        return key;
    }

}

package xyz.wasabicodes.jaws.util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * <p>
 * As a quirk of how this works, {@link #poll()} and {@link #remove()} may reveal elements that
 * exceeded the limit but have not had a chance to get collected yet. If a stack with capacity 4 is created,
 * 5 elements are added, and one is removed: the stack may have a size of 4.
 * </p><p>
 * For the stack's current use case, this is either benign or desirable. If 100 chat messages are sent with a history
 * size of 50, and 50 messages are removed, 50 messages may still be available in the history.
 * </p>
 */
public class CircularStack<E> implements Queue<E>, RandomAccess {

    private final int capacity;
    private final int capacity2;
    private final Object[] lo;
    private final Object[] hi;
    private int head;
    private boolean loFull;

    public CircularStack(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("Cannot create stack with capacity less than 1");
        this.capacity = capacity;
        this.capacity2 = capacity << 1;
        this.lo = new Object[capacity];
        this.hi = new Object[capacity];
        this.head = 0;
        this.loFull = false;
    }

    public CircularStack(Collection<? extends E> coll) {
        this(coll.size());
        this.addAll(coll);
    }

    // HELPERS

    private void assertNonNull(E element) {
        if (element == null) throw new NullPointerException("Elements in stack may not be null");
    }

    @SuppressWarnings("unchecked")
    private E trustMeBro(Object object) {
        return (E) object;
    }

    private void decrementHead() {
        this.head--;
        if (this.loFull) this.loFull = (this.head >= this.capacity);
    }

    // NATIVE METHODS

    public int maxSize() {
        return this.capacity;
    }

    public E get(int index) throws NoSuchElementException {
        if (index < 0) throw new NoSuchElementException("Cannot retrieve element at negative index " + index);
        if (this.loFull) {
            int effIndex = index - this.capacity;
            if (effIndex >= 0) throw new NoSuchElementException("No element at index " + index);
            effIndex += this.head;
            if (effIndex < this.capacity) return this.trustMeBro(this.lo[effIndex]);
            return this.trustMeBro(this.hi[effIndex - this.capacity]);
        } else {
            if (index >= this.head) throw new NoSuchElementException("No element at index " + index);
            return this.trustMeBro(this.lo[index]);
        }
    }

    // QUEUE METHODS

    @Override
    public boolean add(E element) {
        this.assertNonNull(element);
        if (this.loFull) {
            this.hi[(this.head++) - this.capacity] = element;
            if (this.head >= this.capacity2) {
                System.arraycopy(this.hi, 0, this.lo, 0, this.capacity);
                Arrays.fill(this.hi, null); // Not actually necessary, but let's give the GC a head start.
                this.head = this.capacity;
            }
        } else {
            this.lo[this.head++] = element;
            this.loFull = (this.head >= this.capacity);
        }
        return true;
    }

    @Override
    public boolean offer(E e) {
        return this.add(e);
    }

    @Override
    public E peek() {
        if (this.loFull) {
            if (this.head == this.capacity) {
                return this.trustMeBro(this.lo[this.capacity - 1]);
            } else {
                return this.trustMeBro(this.hi[this.head - this.capacity - 1]);
            }
        } else {
            if (this.head == 0) return null;
            return this.trustMeBro(this.lo[this.head - 1]);
        }
    }

    @Override
    public E poll() {
        E value = this.peek();
        if (value == null) return null;
        this.decrementHead();
        return value;
    }

    @Override
    public E element() throws NoSuchElementException {
        E value = this.peek();
        if (value == null) throw new NoSuchElementException("Stack is empty");
        return value;
    }

    @Override
    public E remove() throws NoSuchElementException {
        E value = this.peek();
        if (value == null) throw new NoSuchElementException("Stack is empty");
        this.decrementHead();
        return value;
    }

    // COLLECTION METHODS

    @Override
    public int size() {
        return this.loFull ? this.capacity : this.head;
    }

    @Override
    public boolean isEmpty() {
        return this.head == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) return false;
        for (E e : this) {
            if (e == o) return true;
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        if (this.loFull) {
            Stream<E> a = Arrays.stream(this.lo, this.head - this.capacity, this.capacity)
                    .map(this::trustMeBro);
            if (this.head == this.capacity) return a.iterator();
            Stream<E> b = Arrays.stream(this.hi, 0, this.head - this.capacity)
                    .map(this::trustMeBro);
            return Stream.concat(a, b).iterator();
        } else {
            return Arrays.stream(this.lo, 0, this.head)
                    .map(this::trustMeBro)
                    .iterator();
        }
    }

    @SuppressWarnings("SuspiciousSystemArraycopy")
    private <T> void copyIntoSized(Object array) {
        if (this.loFull) {
            final int off = this.head - this.capacity;
            final int rem = this.capacity2 - this.head;
            System.arraycopy(this.lo, off, array, 0, rem);
            System.arraycopy(this.hi, 0, array, rem, off);
        } else {
            System.arraycopy(this.lo, 0, array, 0, this.head);
        }
    }

    @Override
    public Object[] toArray() {
        final int size = this.size();
        Object[] ret = new Object[size];
        this.copyIntoSized(ret);
        return ret;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] ts) {
        final int size = this.size();
        if (ts.length == size) {
            this.copyIntoSized(ts);
            return ts;
        }

        Class<?> componentType = ts.getClass().getComponentType();
        Object array = Array.newInstance(componentType, size);
        this.copyIntoSized(array);
        return (T[]) array;
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        T[] array = generator.apply(this.size());
        this.copyIntoSized(array);
        return array;
    }

    @Override
    public boolean remove(Object o) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot remove arbitrary elements from stack");
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object c : collection) {
            if (!this.contains(c)) return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean any = false;
        for (E c : collection) {
            any |= this.add(c);
        }
        return any;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Cannot remove arbitrary elements from stack");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Cannot remove arbitrary elements from stack");
    }

    @Override
    public void clear() {
        Arrays.fill(this.hi, null);
        System.arraycopy(this.hi, 0, this.lo, 0, this.capacity);
        this.head = 0;
        this.loFull = false;
    }

}

package de.fisch37.datastructures.mi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

/**
 * Custom queue implementation for constant-time addition and removal at arbitrary* positions.
 * Note that this class doesn't implement the full Queue interface, only a portion of it.
 *
 *
 * @param <T> The content of the queue. Must be an MINode subclass.
 */
public class MIQueue<T extends MINode> implements Iterable<T>{
    @Nullable
    private T first, last;

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    @Override
    public MIIterator<T> iterator() {
        return new MIIterator<>(this);
    }

    public boolean add(@NotNull T o) {
        return offer(o);
    }
    public boolean offer(@NotNull T o){
        o.setQueue((MIQueue<MINode>) this);
        if (this.last != null)
            this.last.next = o;
        this.last = o;
        return true;
    }

    @Nullable
    public T poll() {
        try {
            return this.remove();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void remove(@NotNull T o) {
        if (o.getQueue() != this) {
            throw new NoSuchElementException("Object is not in MIQueue");
        }
        o.dropout();
    }

    @NotNull
    public T remove() {
        T removed = this.first;
        if (removed == null)
            throw new NoSuchElementException("MIQueue is empty");
        this.first = (T) removed.next;
        removed.setQueue(null);
        return removed;
    }

    @Nullable
    public T peek() {
        return this.first;
    }

    @NotNull
    public T element() {
        T result = peek();
        if (result == null)
            throw new NoSuchElementException("MIQueue is empty");
        return result;
    }

    public T end() {
        return this.last;
    }

    public void validate() {
        for (T element : this) {
            element.validate();
            if (element.getQueue() != this)
                throw new IllegalStateException("Foreign nodes in queue!");
        }
    }
}

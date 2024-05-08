package de.fisch37.datastructures.mi;

import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MIIterator<T extends MINode> implements Iterator<T> {
    @Nullable
    private T head;
    @Nullable
    private T previous;

    protected MIIterator(MIQueue<T> queue) {
        this.head = queue.peek();
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return this.head != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public T next() {
        if (head == null)
            throw new NoSuchElementException();
        previous = head;
        head = (T) this.head.getNext();
        return previous;
    }

    /**
     * Removes from the underlying collection the last element returned
     * by this iterator.  This method can be called
     * only once per call to {@link #next}.
     * <p>
     * The behavior of an iterator is unspecified if the underlying collection
     * is modified while the iteration is in progress in any way other than by
     * calling this method, unless an overriding class has specified a
     * concurrent modification policy.
     * <p>
     * The behavior of an iterator is unspecified if this method is called
     * after a call to the {@link #forEachRemaining forEachRemaining} method.
     *
     * @throws IllegalStateException         if the {@code next} method has not
     *                                       yet been called, or the {@code remove} method has already
     *                                       been called after the last call to the {@code next}
     *                                       method
     * @implSpec The default implementation throws an instance of
     * {@link UnsupportedOperationException} and performs no other action.
     */
    @Override
    public void remove() {
        if (previous == null)
            throw new IllegalStateException("Cannot remove element before iteration");
        previous.dropout();
        previous = null;
    }
}

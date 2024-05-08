package de.fisch37.datastructures.mi;

import org.jetbrains.annotations.Nullable;

public abstract class MINode {
    @Nullable
    protected MINode previous, next;
    @Nullable
    private MIQueue<MINode> queue;

    protected void setQueue(@Nullable MIQueue<MINode> queue) {
        if (!this.isOrphan())
            throw new IllegalStateException("MINode is already part of another queue");
        this.queue = queue;
    }
    protected @Nullable MIQueue<MINode> getQueue() {
        return this.queue;
    }

    public void dropout() {
        if (this.previous != null)
            this.previous.next = this.next;
        if (this.next != null)
            this.next.previous = this.previous;
        this.previous = this.next = null;
    }

    public void insert(MINode node) {
        if (!(node.isAlone() && node.isOrphan()))
            throw new IllegalArgumentException("Cannot insert node that is not dropped out");
        node.next = this;
        node.previous = this.previous;
        this.previous = node;
        node.setQueue(this.queue);
    }

    public @Nullable MINode getPrevious() {
        return this.previous;
    }
    public @Nullable MINode getNext() {
        return this.next;
    }

    public boolean isAlone() {
        return this.previous == null && this.next == null;
    }
    public boolean isFirst() {
        return this.previous == null;
    }
    public boolean isLast() {
        return this.next == null;
    }
    public boolean isOrphan() {
        return this.queue == null;
    }

    public void validate() throws IllegalStateException {
        if ((this.previous != null && this.previous.next != this)
                || (this.next != null && this.next.previous != this)){
            throw new IllegalStateException("MIQueue has desynchronized. Failed to pass validation.");
        }
    }
}

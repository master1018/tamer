public abstract class Reference<T> {
    volatile T referent;
    @SuppressWarnings("unchecked")
    volatile ReferenceQueue queue;
    @SuppressWarnings("unchecked")
    volatile Reference queueNext;
    @SuppressWarnings("unused")
    volatile private int vmData;
    Reference() {
        super();
    }
    public void clear() {
        referent = null;
    }
    @SuppressWarnings("unchecked")
    private synchronized boolean enqueueInternal() {
        if (queue != null && queueNext == null) {
            queue.enqueue(this);
            queue = null;
            return true;
        }
        return false;
    }
    public boolean enqueue() {
        return enqueueInternal();
    }
    public T get() {
        return referent;
    }
    public boolean isEnqueued() {
        return queueNext != null;
    }
}

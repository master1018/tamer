public class ReferenceQueue<T> {
    private Reference<? extends T> head;
    public ReferenceQueue() {
        super();
    }
    @SuppressWarnings("unchecked")
    public synchronized Reference<? extends T> poll() {
        if (head == null) {
            return null;
        }
        Reference<? extends T> ret;
        ret = head;
        if (head == head.queueNext) {
            head = null;
        } else {
            head = head.queueNext;
        }
        ret.queueNext = null;
        return ret;
    }
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0L);
    }
    public synchronized Reference<? extends T> remove(long timeout) throws IllegalArgumentException,
            InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }
        if (timeout == 0L) {
            while (head == null) {
                wait(0L);
            }
        } else {
            long now = System.currentTimeMillis();
            long wakeupTime = now + timeout + 1L;
            while (head == null && now < wakeupTime) {
                wait(wakeupTime - now);
                now = System.currentTimeMillis();
            }
        }
        return poll();
    }
    synchronized void enqueue(Reference<? extends T> reference) {
        if (head == null) {
            reference.queueNext = reference;
        } else {
            reference.queueNext = head;
        }
        head = reference;
        notify();
    }
}

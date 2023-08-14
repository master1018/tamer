public class Barrier {
    private Object go = new Object();
    private int count;
    private int waiters = 0;
    public Barrier(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException();
        }
        this.count = count;
    }
    public void set(int count) {
        if (waiters != 0) {
            throw new IllegalArgumentException();
        }
        this.count = count;
        this.waiters = 0;
    }
    public void await() {
        synchronized (go) {
            waiters++;
            while (count > 0) {
                try {
                    go.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new InternalError();
                }
            }
            waiters--;
        }
    }
    public void signal() {
        synchronized (go) {
            count--;
            go.notifyAll();
        }
    }
    public int getWaiterCount() {
        synchronized (go) {
            return waiters;
        }
    }
}

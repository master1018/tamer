class NativeThreadSet {
    private long[] elts;
    private int used = 0;
    private boolean waitingToEmpty;
    NativeThreadSet(int n) {
        elts = new long[n];
    }
    int add() {
        long th = NativeThread.current();
        if (th == -1)
            return -1;
        synchronized (this) {
            int start = 0;
            if (used >= elts.length) {
                int on = elts.length;
                int nn = on * 2;
                long[] nelts = new long[nn];
                System.arraycopy(elts, 0, nelts, 0, on);
                elts = nelts;
                start = on;
            }
            for (int i = start; i < elts.length; i++) {
                if (elts[i] == 0) {
                    elts[i] = th;
                    used++;
                    return i;
                }
            }
            assert false;
            return -1;
        }
    }
    void remove(int i) {
        if (i < 0)
            return;
        synchronized (this) {
            elts[i] = 0;
            used--;
            if (used == 0 && waitingToEmpty)
                notifyAll();
        }
    }
    void signalAndWait() {
        synchronized (this) {
            int u = used;
            int n = elts.length;
            for (int i = 0; i < n; i++) {
                long th = elts[i];
                if (th == 0)
                    continue;
                NativeThread.signal(th);
                if (--u == 0)
                    break;
            }
            waitingToEmpty = true;
            boolean interrupted = false;
            while (used > 0) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted)
                Thread.currentThread().interrupt();
        }
    }
}

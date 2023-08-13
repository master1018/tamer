final class Finalizer extends FinalReference { 
    static native void invokeFinalizeMethod(Object o) throws Throwable;
    static private ReferenceQueue queue = new ReferenceQueue();
    static private Finalizer unfinalized = null;
    static private Object lock = new Object();
    private Finalizer
        next = null,
        prev = null;
    private boolean hasBeenFinalized() {
        return (next == this);
    }
    private void add() {
        synchronized (lock) {
            if (unfinalized != null) {
                this.next = unfinalized;
                unfinalized.prev = this;
            }
            unfinalized = this;
        }
    }
    private void remove() {
        synchronized (lock) {
            if (unfinalized == this) {
                if (this.next != null) {
                    unfinalized = this.next;
                } else {
                    unfinalized = this.prev;
                }
            }
            if (this.next != null) {
                this.next.prev = this.prev;
            }
            if (this.prev != null) {
                this.prev.next = this.next;
            }
            this.next = this;   
            this.prev = this;
        }
    }
    private Finalizer(Object finalizee) {
        super(finalizee, queue);
        add();
    }
    static void register(Object finalizee) {
        new Finalizer(finalizee);
    }
    private void runFinalizer() {
        synchronized (this) {
            if (hasBeenFinalized()) return;
            remove();
        }
        try {
            Object finalizee = this.get();
            if (finalizee != null && !(finalizee instanceof java.lang.Enum)) {
                invokeFinalizeMethod(finalizee);
                finalizee = null;
            }
        } catch (Throwable x) { }
        super.clear();
    }
    private static void forkSecondaryFinalizer(final Runnable proc) {
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                public Void run() {
                ThreadGroup tg = Thread.currentThread().getThreadGroup();
                for (ThreadGroup tgn = tg;
                     tgn != null;
                     tg = tgn, tgn = tg.getParent());
                Thread sft = new Thread(tg, proc, "Secondary finalizer");
                sft.start();
                try {
                    sft.join();
                } catch (InterruptedException x) {
                }
                return null;
                }});
    }
    static void runFinalization() {
        forkSecondaryFinalizer(new Runnable() {
            public void run() {
                for (;;) {
                    Finalizer f = (Finalizer)queue.poll();
                    if (f == null) break;
                    f.runFinalizer();
                }
            }
        });
    }
    static void runAllFinalizers() {
        forkSecondaryFinalizer(new Runnable() {
            public void run() {
                for (;;) {
                    Finalizer f;
                    synchronized (lock) {
                        f = unfinalized;
                        if (f == null) break;
                        unfinalized = f.next;
                    }
                    f.runFinalizer();
                }}});
    }
    private static class FinalizerThread extends Thread {
        FinalizerThread(ThreadGroup g) {
            super(g, "Finalizer");
        }
        public void run() {
            for (;;) {
                try {
                    Finalizer f = (Finalizer)queue.remove();
                    f.runFinalizer();
                } catch (InterruptedException x) {
                    continue;
                }
            }
        }
    }
    static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        Thread finalizer = new FinalizerThread(tg);
        finalizer.setPriority(Thread.MAX_PRIORITY - 2);
        finalizer.setDaemon(true);
        finalizer.start();
    }
}

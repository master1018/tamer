public class FinThreads {
    static Thread mainThread;
    static Object lock = new Object();    
    static Thread finalizerThread = null;
    static Thread finalizedBy = null;
    static class Foo {
        boolean catchFinalizer = false;
        static public void create(final boolean catchFinalizer)
            throws InterruptedException
        {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    new Foo(catchFinalizer);
                }});
            t.start();
            t.join();
        }
        public Foo(boolean catchFinalizer) {
            this.catchFinalizer = catchFinalizer;
        }
        public void finalize() throws InterruptedException {
            if (catchFinalizer) {
                boolean gotFinalizer = false;
                synchronized (lock) {
                    if (finalizerThread == null) {
                        finalizerThread = Thread.currentThread();
                        gotFinalizer = true;
                    }
                }
                if (gotFinalizer) {
                    System.err.println("Caught finalizer thread; sleeping...");
                    Thread.sleep(Long.MAX_VALUE);
                }
            } else {
                synchronized (lock) {
                    finalizedBy = Thread.currentThread();
                }
                System.err.println("Test object finalized by " + finalizedBy);
            }
        }
    }
    static void alarm(final Thread sleeper, final long delay)
        throws InterruptedException
    {
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(delay);
                    System.err.println("Waking " + sleeper);
                    sleeper.interrupt();
                } catch (InterruptedException x) { }
            }});
        t.setDaemon(true);
        t.start();
    }
    public static void main(String[] args) throws Exception {
        mainThread = Thread.currentThread();
        for (;;) {
            Foo.create(true);
            System.gc();
            synchronized (lock) {
                if (finalizerThread != null) break;
            }
        }
        alarm(finalizerThread, 5000);
        Foo.create(false);
        for (;;) {
            System.gc();
            System.runFinalization();
            synchronized (lock) {
                if (finalizedBy != null) break;
            }
        }
        if (finalizedBy == mainThread)
            throw new Exception("Finalizer run in main thread");
    }
}

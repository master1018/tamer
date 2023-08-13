class Good implements Serializable {
    static {
        try { Thread.sleep(1000); } catch (InterruptedException ex) {}
    }
}
class Bad implements Serializable {
    private static final long serialVersionUID = 0xBAD;
    static {
        try { Thread.sleep(1000); } catch (InterruptedException ex) {}
        if ("foo".equals("foo")) {
            throw new RuntimeException();
        }
    }
}
class SuccessfulLookup extends Thread {
    Class cl;
    long suid;
    Object barrier;
    boolean ok;
    SuccessfulLookup(Class cl, long suid, Object barrier) {
        this.cl = cl;
        this.suid = suid;
        this.barrier = barrier;
    }
    public void run() {
        synchronized (barrier) {
            try { barrier.wait(); } catch (InterruptedException ex) {}
        }
        for (int i = 0; i < 100; i++) {
            if (ObjectStreamClass.lookup(cl).getSerialVersionUID() != suid) {
                return;
            }
        }
        ok = true;
    }
}
class FailingLookup extends Thread {
    Class cl;
    Object barrier;
    boolean ok;
    FailingLookup(Class cl, Object barrier) {
        this.cl = cl;
        this.barrier = barrier;
    }
    public void run() {
        synchronized (barrier) {
            try { barrier.wait(); } catch (InterruptedException ex) {}
        }
        for (int i = 0; i < 100; i++) {
            try {
                ObjectStreamClass.lookup(cl);
                return;
            } catch (Throwable th) {
            }
        }
        ok = true;
    }
}
public class ConcurrentClassDescLookup {
    public static void main(String[] args) throws Exception {
        ClassLoader loader = ConcurrentClassDescLookup.class.getClassLoader();
        Class cl = Class.forName("Good", false, loader);
        Object barrier = new Object();
        SuccessfulLookup[] slookups = new SuccessfulLookup[50];
        for (int i = 0; i < slookups.length; i++) {
            slookups[i] =
                new SuccessfulLookup(cl, 6319710844400051132L, barrier);
            slookups[i].start();
        }
        Thread.sleep(1000);
        synchronized (barrier) {
            barrier.notifyAll();
        }
        for (int i = 0; i < slookups.length; i++) {
            slookups[i].join();
            if (!slookups[i].ok) {
                throw new Error();
            }
        }
        cl = Class.forName("Bad", false, loader);
        FailingLookup[] flookups = new FailingLookup[50];
        for (int i = 0; i < flookups.length; i++) {
            flookups[i] = new FailingLookup(cl, barrier);
            flookups[i].start();
        }
        Thread.sleep(1000);
        synchronized (barrier) {
            barrier.notifyAll();
        }
        for (int i = 0; i < slookups.length; i++) {
            flookups[i].join();
            if (!flookups[i].ok) {
                throw new Error();
            }
        }
    }
}

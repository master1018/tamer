public class GC {
    private GC() { }            
    private static final long NO_TARGET = Long.MAX_VALUE;
    private static long latencyTarget = NO_TARGET;
    private static Thread daemon = null;
    private static class LatencyLock extends Object { };
    private static Object lock = new LatencyLock();
    public static native long maxObjectInspectionAge();
    private static class Daemon extends Thread {
        public void run() {
            for (;;) {
                long l;
                synchronized (lock) {
                    l = latencyTarget;
                    if (l == NO_TARGET) {
                        GC.daemon = null;
                        return;
                    }
                    long d = maxObjectInspectionAge();
                    if (d >= l) {
                        System.gc();
                        d = 0;
                    }
                    try {
                        lock.wait(l - d);
                    } catch (InterruptedException x) {
                        continue;
                    }
                }
            }
        }
        private Daemon(ThreadGroup tg) {
            super(tg, "GC Daemon");
        }
        public static void create() {
            PrivilegedAction<Void> pa = new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup tg = Thread.currentThread().getThreadGroup();
                    for (ThreadGroup tgn = tg;
                         tgn != null;
                         tg = tgn, tgn = tg.getParent());
                    Daemon d = new Daemon(tg);
                    d.setDaemon(true);
                    d.setPriority(Thread.MIN_PRIORITY + 1);
                    d.start();
                    GC.daemon = d;
                    return null;
                }};
            AccessController.doPrivileged(pa);
        }
    }
    private static void setLatencyTarget(long ms) {
        latencyTarget = ms;
        if (daemon == null) {
            Daemon.create();
        } else {
            lock.notify();
        }
    }
    public static class LatencyRequest
        implements Comparable<LatencyRequest> {
        private static long counter = 0;
        private static SortedSet<LatencyRequest> requests = null;
        private static void adjustLatencyIfNeeded() {
            if ((requests == null) || requests.isEmpty()) {
                if (latencyTarget != NO_TARGET) {
                    setLatencyTarget(NO_TARGET);
                }
            } else {
                LatencyRequest r = requests.first();
                if (r.latency != latencyTarget) {
                    setLatencyTarget(r.latency);
                }
            }
        }
        private long latency;
        private long id;
        private LatencyRequest(long ms) {
            if (ms <= 0) {
                throw new IllegalArgumentException("Non-positive latency: "
                                                   + ms);
            }
            this.latency = ms;
            synchronized (lock) {
                this.id = ++counter;
                if (requests == null) {
                    requests = new TreeSet<LatencyRequest>();
                }
                requests.add(this);
                adjustLatencyIfNeeded();
            }
        }
        public void cancel() {
            synchronized (lock) {
                if (this.latency == NO_TARGET) {
                    throw new IllegalStateException("Request already"
                                                    + " cancelled");
                }
                if (!requests.remove(this)) {
                    throw new InternalError("Latency request "
                                            + this + " not found");
                }
                if (requests.isEmpty()) requests = null;
                this.latency = NO_TARGET;
                adjustLatencyIfNeeded();
            }
        }
        public int compareTo(LatencyRequest r) {
            long d = this.latency - r.latency;
            if (d == 0) d = this.id - r.id;
            return (d < 0) ? -1 : ((d > 0) ? +1 : 0);
        }
        public String toString() {
            return (LatencyRequest.class.getName()
                    + "[" + latency + "," + id + "]");
        }
    }
    public static LatencyRequest requestLatency(long latency) {
        return new LatencyRequest(latency);
    }
    public static long currentLatencyTarget() {
        long t = latencyTarget;
        return (t == NO_TARGET) ? 0 : t;
    }
}

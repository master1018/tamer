class KeepAliveStream extends MeteredStream implements Hurryable {
    HttpClient hc;
    boolean hurried;
    protected boolean queuedForCleanup = false;
    private static final KeepAliveStreamCleaner queue = new KeepAliveStreamCleaner();
    private static Thread cleanerThread; 
    public KeepAliveStream(InputStream is, ProgressSource pi, long expected, HttpClient hc)  {
        super(is, pi, expected);
        this.hc = hc;
    }
    public void close() throws IOException  {
        if (closed) {
            return;
        }
        if (queuedForCleanup) {
            return;
        }
        try {
            if (expected > count) {
                long nskip = (long) (expected - count);
                if (nskip <= available()) {
                    long n = 0;
                    while (n < nskip) {
                        nskip = nskip - n;
                        n = skip(nskip);
                    }
                } else if (expected <= KeepAliveStreamCleaner.MAX_DATA_REMAINING && !hurried) {
                    queueForCleanup(new KeepAliveCleanerEntry(this, hc));
                } else {
                    hc.closeServer();
                }
            }
            if (!closed && !hurried && !queuedForCleanup) {
                hc.finished();
            }
        } finally {
            if (pi != null)
                pi.finishTracking();
            if (!queuedForCleanup) {
                in = null;
                hc = null;
                closed = true;
            }
        }
    }
    public boolean markSupported()  {
        return false;
    }
    public void mark(int limit) {}
    public void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }
    public synchronized boolean hurry() {
        try {
            if (closed || count >= expected) {
                return false;
            } else if (in.available() < (expected - count)) {
                return false;
            } else {
                int size = (int) (expected - count);
                byte[] buf = new byte[size];
                DataInputStream dis = new DataInputStream(in);
                dis.readFully(buf);
                in = new ByteArrayInputStream(buf);
                hurried = true;
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }
    private static void queueForCleanup(KeepAliveCleanerEntry kace) {
        synchronized(queue) {
            if(!kace.getQueuedForCleanup()) {
                if (!queue.offer(kace)) {
                    kace.getHttpClient().closeServer();
                    return;
                }
                kace.setQueuedForCleanup();
                queue.notifyAll();
            }
            boolean startCleanupThread = (cleanerThread == null);
            if (!startCleanupThread) {
                if (!cleanerThread.isAlive()) {
                    startCleanupThread = true;
                }
            }
            if (startCleanupThread) {
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<Void>() {
                    public Void run() {
                        ThreadGroup grp = Thread.currentThread().getThreadGroup();
                        ThreadGroup parent = null;
                        while ((parent = grp.getParent()) != null) {
                            grp = parent;
                        }
                        cleanerThread = new Thread(grp, queue, "Keep-Alive-SocketCleaner");
                        cleanerThread.setDaemon(true);
                        cleanerThread.setPriority(Thread.MAX_PRIORITY - 2);
                        cleanerThread.setContextClassLoader(null);
                        cleanerThread.start();
                        return null;
                    }
                });
            }
        } 
    }
    protected long remainingToRead() {
        return expected - count;
    }
    protected void setClosed() {
        in = null;
        hc = null;
        closed = true;
    }
}
class KeepAliveCleanerEntry
{
    KeepAliveStream kas;
    HttpClient hc;
    public KeepAliveCleanerEntry(KeepAliveStream kas, HttpClient hc) {
        this.kas = kas;
        this.hc = hc;
    }
    protected KeepAliveStream getKeepAliveStream() {
        return kas;
    }
    protected HttpClient getHttpClient() {
        return hc;
    }
    protected void setQueuedForCleanup() {
        kas.queuedForCleanup = true;
    }
    protected boolean getQueuedForCleanup() {
        return kas.queuedForCleanup;
    }
}

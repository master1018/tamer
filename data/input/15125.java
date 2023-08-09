class KeepAliveStreamCleaner
    extends LinkedList<KeepAliveCleanerEntry>
    implements Runnable
{
    protected static int MAX_DATA_REMAINING = 512;
    protected static int MAX_CAPACITY = 10;
    protected static final int TIMEOUT = 5000;
    private static final int MAX_RETRIES = 5;
    static {
        final String maxDataKey = "http.KeepAlive.remainingData";
        int maxData = AccessController.doPrivileged(
            new PrivilegedAction<Integer>() {
                public Integer run() {
                    return NetProperties.getInteger(maxDataKey, MAX_DATA_REMAINING);
                }}).intValue() * 1024;
        MAX_DATA_REMAINING = maxData;
        final String maxCapacityKey = "http.KeepAlive.queuedConnections";
        int maxCapacity = AccessController.doPrivileged(
            new PrivilegedAction<Integer>() {
                public Integer run() {
                    return NetProperties.getInteger(maxCapacityKey, MAX_CAPACITY);
                }}).intValue();
        MAX_CAPACITY = maxCapacity;
    }
    @Override
    public boolean offer(KeepAliveCleanerEntry e) {
        if (size() >= MAX_CAPACITY)
            return false;
        return super.offer(e);
    }
    @Override
    public void run()
    {
        KeepAliveCleanerEntry kace = null;
        do {
            try {
                synchronized(this) {
                    long before = System.currentTimeMillis();
                    long timeout = TIMEOUT;
                    while ((kace = poll()) == null) {
                        this.wait(timeout);
                        long after = System.currentTimeMillis();
                        long elapsed = after - before;
                        if (elapsed > timeout) {
                            kace = poll();
                            break;
                        }
                        before = after;
                        timeout -= elapsed;
                    }
                }
                if(kace == null)
                    break;
                KeepAliveStream kas = kace.getKeepAliveStream();
                if (kas != null) {
                    synchronized(kas) {
                        HttpClient hc = kace.getHttpClient();
                        try {
                            if (hc != null && !hc.isInKeepAliveCache()) {
                                int oldTimeout = hc.getReadTimeout();
                                hc.setReadTimeout(TIMEOUT);
                                long remainingToRead = kas.remainingToRead();
                                if (remainingToRead > 0) {
                                    long n = 0;
                                    int retries = 0;
                                    while (n < remainingToRead && retries < MAX_RETRIES) {
                                        remainingToRead = remainingToRead - n;
                                        n = kas.skip(remainingToRead);
                                        if (n == 0)
                                            retries++;
                                    }
                                    remainingToRead = remainingToRead - n;
                                }
                                if (remainingToRead == 0) {
                                    hc.setReadTimeout(oldTimeout);
                                    hc.finished();
                                } else
                                    hc.closeServer();
                            }
                        } catch (IOException ioe) {
                            hc.closeServer();
                        } finally {
                            kas.setClosed();
                        }
                    }
                }
            } catch (InterruptedException ie) { }
        } while (kace != null);
    }
}

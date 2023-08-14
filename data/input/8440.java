public abstract class ServerCommunicatorAdmin {
    public ServerCommunicatorAdmin(long timeout) {
        if (logger.traceOn()) {
            logger.trace("Constructor",
                         "Creates a new ServerCommunicatorAdmin object "+
                         "with the timeout "+timeout);
        }
        this.timeout = timeout;
        timestamp = 0;
        if (timeout < Long.MAX_VALUE) {
            Runnable timeoutTask = new Timeout();
            final Thread t = new Thread(timeoutTask);
            t.setName("JMX server connection timeout " + t.getId());
            t.setDaemon(true);
            t.start();
        }
    }
    public boolean reqIncoming() {
        if (logger.traceOn()) {
            logger.trace("reqIncoming", "Receive a new request.");
        }
        synchronized(lock) {
            if (terminated) {
                logger.warning("reqIncoming",
                               "The server has decided to close " +
                               "this client connection.");
            }
            ++currentJobs;
            return terminated;
        }
    }
    public boolean rspOutgoing() {
        if (logger.traceOn()) {
            logger.trace("reqIncoming", "Finish a request.");
        }
        synchronized(lock) {
            if (--currentJobs == 0) {
                timestamp = System.currentTimeMillis();
                logtime("Admin: Timestamp=",timestamp);
                lock.notify();
            }
            return terminated;
        }
    }
    protected abstract void doStop();
    public void terminate() {
        if (logger.traceOn()) {
            logger.trace("terminate",
                         "terminate the ServerCommunicatorAdmin object.");
        }
        synchronized(lock) {
            if (terminated) {
                return;
            }
            terminated = true;
            lock.notify();
        }
    }
    private class Timeout implements Runnable {
        public void run() {
            boolean stopping = false;
            synchronized(lock) {
                if (timestamp == 0) timestamp = System.currentTimeMillis();
                logtime("Admin: timeout=",timeout);
                logtime("Admin: Timestamp=",timestamp);
                while(!terminated) {
                    try {
                        while(!terminated && currentJobs != 0) {
                            if (logger.traceOn()) {
                                logger.trace("Timeout-run",
                                             "Waiting without timeout.");
                            }
                            lock.wait();
                        }
                        if (terminated) return;
                        final long remaining =
                            timeout - (System.currentTimeMillis() - timestamp);
                        logtime("Admin: remaining timeout=",remaining);
                        if (remaining > 0) {
                            if (logger.traceOn()) {
                                logger.trace("Timeout-run",
                                             "Waiting with timeout: "+
                                             remaining + " ms remaining");
                            }
                            lock.wait(remaining);
                        }
                        if (currentJobs > 0) continue;
                        final long elapsed =
                            System.currentTimeMillis() - timestamp;
                        logtime("Admin: elapsed=",elapsed);
                        if (!terminated && elapsed > timeout) {
                            if (logger.traceOn()) {
                                logger.trace("Timeout-run",
                                             "timeout elapsed");
                            }
                            logtime("Admin: timeout elapsed! "+
                                    elapsed+">",timeout);
                            terminated = true;
                            stopping = true;
                            break;
                        }
                    } catch (InterruptedException ire) {
                        logger.warning("Timeout-run","Unexpected Exception: "+
                                       ire);
                        logger.debug("Timeout-run",ire);
                        return;
                    }
                }
            }
            if (stopping) {
                if (logger.traceOn()) {
                    logger.trace("Timeout-run", "Call the doStop.");
                }
                doStop();
            }
        }
    }
    private void logtime(String desc,long time) {
        timelogger.trace("synchro",desc+time);
    }
    private long    timestamp;
    private final int[] lock = new int[0];
    private int currentJobs = 0;
    private long timeout;
    private boolean terminated = false;
    private static final ClassLogger logger =
        new ClassLogger("javax.management.remote.misc",
                        "ServerCommunicatorAdmin");
    private static final ClassLogger timelogger =
        new ClassLogger("javax.management.remote.timeout",
                        "ServerCommunicatorAdmin");
}

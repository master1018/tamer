public class IdleConnectionHandler {
    private final Log log = LogFactory.getLog(getClass());
    private final Map<HttpConnection,TimeValues> connectionToTimes;
    public IdleConnectionHandler() {
        super();
        connectionToTimes = new HashMap<HttpConnection,TimeValues>();
    }
    public void add(HttpConnection connection, long validDuration, TimeUnit unit) {
        Long timeAdded = Long.valueOf(System.currentTimeMillis());
        if (log.isDebugEnabled()) {
            log.debug("Adding connection at: " + timeAdded);
        }
        connectionToTimes.put(connection, new TimeValues(timeAdded, validDuration, unit));
    }
    public boolean remove(HttpConnection connection) {
        TimeValues times = connectionToTimes.remove(connection);
        if(times == null) {
            log.warn("Removing a connection that never existed!");
            return true;
        } else {
            return System.currentTimeMillis() <= times.timeExpires;
        }
    }
    public void removeAll() {
        this.connectionToTimes.clear();
    }
    public void closeIdleConnections(long idleTime) {
        long idleTimeout = System.currentTimeMillis() - idleTime;
        if (log.isDebugEnabled()) {
            log.debug("Checking for connections, idleTimeout: "  + idleTimeout);
        }
        Iterator<HttpConnection> connectionIter =
            connectionToTimes.keySet().iterator();
        while (connectionIter.hasNext()) {
            HttpConnection conn = connectionIter.next();
            TimeValues times = connectionToTimes.get(conn);
            Long connectionTime = times.timeAdded;
            if (connectionTime.longValue() <= idleTimeout) {
                if (log.isDebugEnabled()) {
                    log.debug("Closing connection, connection time: "  + connectionTime);
                }
                connectionIter.remove();
                try {
                    conn.close();
                } catch (IOException ex) {
                    log.debug("I/O error closing connection", ex);
                }
            }
        }
    }
    public void closeExpiredConnections() {
        long now = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Checking for expired connections, now: "  + now);
        }
        Iterator<HttpConnection> connectionIter =
            connectionToTimes.keySet().iterator();
        while (connectionIter.hasNext()) {
            HttpConnection conn = connectionIter.next();
            TimeValues times = connectionToTimes.get(conn);
            if(times.timeExpires <= now) {
                if (log.isDebugEnabled()) {
                    log.debug("Closing connection, expired @: "  + times.timeExpires);
                }
                connectionIter.remove();
                try {
                    conn.close();
                } catch (IOException ex) {
                    log.debug("I/O error closing connection", ex);
                }
            }
        }        
    }
    private static class TimeValues {
        private final long timeAdded;
        private final long timeExpires;
        TimeValues(long now, long validDuration, TimeUnit validUnit) {
            this.timeAdded = now;
            if(validDuration > 0) {
                this.timeExpires = now + validUnit.toMillis(validDuration);
            } else {
                this.timeExpires = Long.MAX_VALUE;
            }
        }
    }
}

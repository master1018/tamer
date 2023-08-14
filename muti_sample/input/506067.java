public class BasicEofSensorWatcher implements EofSensorWatcher {
    protected ManagedClientConnection managedConn;
    protected boolean attemptReuse;
    public BasicEofSensorWatcher(ManagedClientConnection conn,
                                 boolean reuse) {
        if (conn == null)
            throw new IllegalArgumentException
                ("Connection may not be null.");
        managedConn = conn;
        attemptReuse = reuse;
    }
    public boolean eofDetected(InputStream wrapped)
        throws IOException {
        try {
            if (attemptReuse) {
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            managedConn.releaseConnection();
        }
        return false;
    }
    public boolean streamClosed(InputStream wrapped)
        throws IOException {
        try {
            if (attemptReuse) {
                wrapped.close();
                managedConn.markReusable();
            }
        } finally {
            managedConn.releaseConnection();
        }
        return false;
    }
    public boolean streamAbort(InputStream wrapped)
        throws IOException {
        managedConn.abortConnection();
        return false;
    }
} 

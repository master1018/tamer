final class ConnectionDesc {
    private final static boolean debug = Pool.debug;
    static final byte BUSY = (byte)0;
    static final byte IDLE = (byte)1;
    static final byte EXPIRED = (byte)2;
    final private PooledConnection conn;
    private byte state = IDLE;  
    private long idleSince;
    private long useCount = 0;  
    ConnectionDesc(PooledConnection conn) {
        this.conn = conn;
    }
    ConnectionDesc(PooledConnection conn, boolean use) {
        this.conn = conn;
        if (use) {
            state = BUSY;
            ++useCount;
        }
    }
    public boolean equals(Object obj) {
        return obj != null
            && obj instanceof ConnectionDesc
            && ((ConnectionDesc)obj).conn == conn;
    }
    public int hashCode() {
        return conn.hashCode();
    }
    synchronized boolean release() {
        d("release()");
        if (state == BUSY) {
            state = IDLE;
            idleSince = System.currentTimeMillis();
            return true;  
        } else {
            return false; 
        }
    }
    synchronized PooledConnection tryUse() {
        d("tryUse()");
        if (state == IDLE) {
            state = BUSY;
            ++useCount;
            return conn;
        }
        return null;
    }
    synchronized boolean expire(long threshold) {
        if (state == IDLE && idleSince < threshold) {
            d("expire(): expired");
            state = EXPIRED;
            conn.closeConnection();  
            return true;  
        } else {
            d("expire(): not expired");
            return false; 
        }
    }
    public String toString() {
        return conn.toString() + " " +
            (state == BUSY ? "busy" : (state == IDLE ? "idle" : "expired"));
    }
    int getState() {
        return state;
    }
    long getUseCount() {
        return useCount;
    }
    private void d(String msg) {
        if (debug) {
            System.err.println("ConnectionDesc." + msg + " " + toString());
        }
    }
}

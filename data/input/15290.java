final class Connections implements PoolCallback {
    private static final boolean debug = Pool.debug;
    private static final boolean trace =
        com.sun.jndi.ldap.LdapPoolManager.trace;
    private static final int DEFAULT_SIZE = 10;
    final private int maxSize;
    final private int prefSize;
    final private List conns;
    private boolean closed = false;   
    private Reference ref; 
    Connections(Object id, int initSize, int prefSize, int maxSize,
        PooledConnectionFactory factory) throws NamingException {
        this.maxSize = maxSize;
        if (maxSize > 0) {
            this.prefSize = Math.min(prefSize, maxSize);
            initSize = Math.min(initSize, maxSize);
        } else {
            this.prefSize = prefSize;
        }
        conns = new ArrayList(maxSize > 0 ? maxSize : DEFAULT_SIZE);
        ref = new SoftReference(id);
        d("init size=", initSize);
        d("max size=", maxSize);
        d("preferred size=", prefSize);
        PooledConnection conn;
        for (int i = 0; i < initSize; i++) {
            conn = factory.createPooledConnection(this);
            td("Create ", conn ,factory);
            conns.add(new ConnectionDesc(conn)); 
        }
    }
    synchronized PooledConnection get(long timeout,
        PooledConnectionFactory factory) throws NamingException {
        PooledConnection conn;
        long start = (timeout > 0 ? System.currentTimeMillis() : 0);
        long waittime = timeout;
        d("get(): before");
        while ((conn = getOrCreateConnection(factory)) == null) {
            if (timeout > 0 && waittime <= 0) {
                throw new CommunicationException(
                    "Timeout exceeded while waiting for a connection: " +
                    timeout + "ms");
            }
            try {
                d("get(): waiting");
                if (waittime > 0) {
                    wait(waittime);  
                } else {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new InterruptedNamingException(
                    "Interrupted while waiting for a connection");
            }
            if (timeout > 0) {
                long now = System.currentTimeMillis();
                waittime = timeout - (now - start);
            }
        }
        d("get(): after");
        return conn;
    }
    private PooledConnection getOrCreateConnection(
        PooledConnectionFactory factory) throws NamingException {
        int size = conns.size(); 
        PooledConnection conn = null;
        if (prefSize <= 0 || size >= prefSize) {
            ConnectionDesc entry;
            for (int i = 0; i < size; i++) {
                entry = (ConnectionDesc) conns.get(i);
                if ((conn = entry.tryUse()) != null) {
                    d("get(): use ", conn);
                    td("Use ", conn);
                    return conn;
                }
            }
        }
        if (maxSize > 0 && size >= maxSize) {
            return null;   
        }
        conn = factory.createPooledConnection(this);
        td("Create and use ", conn, factory);
        conns.add(new ConnectionDesc(conn, true)); 
        return conn;
    }
    public synchronized boolean releasePooledConnection(PooledConnection conn) {
        ConnectionDesc entry;
        int loc = conns.indexOf(entry=new ConnectionDesc(conn));
        d("release(): ", conn);
        if (loc >= 0) {
            if (closed || (prefSize > 0 && conns.size() > prefSize)) {
                d("release(): closing ", conn);
                td("Close ", conn);
                conns.remove(entry);
                conn.closeConnection();
            } else {
                d("release(): release ", conn);
                td("Release ", conn);
                entry = (ConnectionDesc) conns.get(loc);
                entry.release();
            }
            notifyAll();
            d("release(): notify");
            return true;
        } else {
            return false;
        }
    }
    public synchronized boolean removePooledConnection(PooledConnection conn) {
        if (conns.remove(new ConnectionDesc(conn))) {
            d("remove(): ", conn);
            notifyAll();
            d("remove(): notify");
            td("Remove ", conn);
            if (conns.isEmpty()) {
                ref = null;
            }
            return true;
        } else {
            d("remove(): not found ", conn);
            return false;
        }
    }
    synchronized boolean expire(long threshold) {
        Iterator iter = conns.iterator();
        ConnectionDesc entry;
        while (iter.hasNext()) {
            entry = (ConnectionDesc) iter.next();
            if (entry.expire(threshold)) {
                d("expire(): removing ", entry);
                td("Expired ", entry);
                iter.remove();  
            }
        }
        return conns.isEmpty();  
    }
    synchronized void close() {
        expire(System.currentTimeMillis());     
        closed = true;   
    }
    String getStats() {
        int idle = 0;
        int busy = 0;
        int expired = 0;
        long use = 0;
        int len;
        synchronized (this) {
            len = conns.size();
            ConnectionDesc entry;
            for (int i = 0; i < len; i++) {
                entry = (ConnectionDesc) conns.get(i);
                use += entry.getUseCount();
                switch (entry.getState()) {
                case ConnectionDesc.BUSY:
                    ++busy;
                    break;
                case ConnectionDesc.IDLE:
                    ++idle;
                    break;
                case ConnectionDesc.EXPIRED:
                    ++expired;
                }
            }
        }
        return "size=" + len + "; use=" + use + "; busy=" + busy
            + "; idle=" + idle + "; expired=" + expired;
    }
    private void d(String msg, Object o1) {
        if (debug) {
            d(msg + o1);
        }
    }
    private void d(String msg, int i) {
        if (debug) {
            d(msg + i);
        }
    }
    private void d(String msg) {
        if (debug) {
            System.err.println(this + "." + msg + "; size: " + conns.size());
        }
    }
    private void td(String msg, Object o1, Object o2) {
        if (trace) { 
            td(msg + o1 + "[" + o2 + "]");
        }
    }
    private void td(String msg, Object o1) {
        if (trace) { 
            td(msg + o1);
        }
    }
    private void td(String msg) {
        if (trace) {
            System.err.println(msg);
        }
    }
}

public class KeepAliveCache
    extends HashMap<KeepAliveKey, ClientVector>
    implements Runnable {
    private static final long serialVersionUID = -2937172892064557949L;
    static final int MAX_CONNECTIONS = 5;
    static int result = -1;
    static int getMaxConnections() {
        if (result == -1) {
            result = java.security.AccessController.doPrivileged(
                new sun.security.action.GetIntegerAction("http.maxConnections",
                                                         MAX_CONNECTIONS))
                .intValue();
            if (result <= 0)
                result = MAX_CONNECTIONS;
        }
            return result;
    }
    static final int LIFETIME = 5000;
    private Thread keepAliveTimer = null;
    public KeepAliveCache() {}
    public synchronized void put(final URL url, Object obj, HttpClient http) {
        boolean startThread = (keepAliveTimer == null);
        if (!startThread) {
            if (!keepAliveTimer.isAlive()) {
                startThread = true;
            }
        }
        if (startThread) {
            clear();
            final KeepAliveCache cache = this;
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup grp = Thread.currentThread().getThreadGroup();
                    ThreadGroup parent = null;
                    while ((parent = grp.getParent()) != null) {
                        grp = parent;
                    }
                    keepAliveTimer = new Thread(grp, cache, "Keep-Alive-Timer");
                    keepAliveTimer.setDaemon(true);
                    keepAliveTimer.setPriority(Thread.MAX_PRIORITY - 2);
                    keepAliveTimer.setContextClassLoader(null);
                    keepAliveTimer.start();
                    return null;
                }
            });
        }
        KeepAliveKey key = new KeepAliveKey(url, obj);
        ClientVector v = super.get(key);
        if (v == null) {
            int keepAliveTimeout = http.getKeepAliveTimeout();
            v = new ClientVector(keepAliveTimeout > 0?
                                 keepAliveTimeout*1000 : LIFETIME);
            v.put(http);
            super.put(key, v);
        } else {
            v.put(http);
        }
    }
    public synchronized void remove (HttpClient h, Object obj) {
        KeepAliveKey key = new KeepAliveKey(h.url, obj);
        ClientVector v = super.get(key);
        if (v != null) {
            v.remove(h);
            if (v.empty()) {
                removeVector(key);
            }
        }
    }
    synchronized void removeVector(KeepAliveKey k) {
        super.remove(k);
    }
    public synchronized HttpClient get(URL url, Object obj) {
        KeepAliveKey key = new KeepAliveKey(url, obj);
        ClientVector v = super.get(key);
        if (v == null) { 
            return null;
        }
        return v.get();
    }
    @Override
    public void run() {
        do {
            try {
                Thread.sleep(LIFETIME);
            } catch (InterruptedException e) {}
            synchronized (this) {
                long currentTime = System.currentTimeMillis();
                ArrayList<KeepAliveKey> keysToRemove
                    = new ArrayList<KeepAliveKey>();
                for (KeepAliveKey key : keySet()) {
                    ClientVector v = get(key);
                    synchronized (v) {
                        int i;
                        for (i = 0; i < v.size(); i++) {
                            KeepAliveEntry e = v.elementAt(i);
                            if ((currentTime - e.idleStartTime) > v.nap) {
                                HttpClient h = e.hc;
                                h.closeServer();
                            } else {
                                break;
                            }
                        }
                        v.subList(0, i).clear();
                        if (v.size() == 0) {
                            keysToRemove.add(key);
                        }
                    }
                }
                for (KeepAliveKey key : keysToRemove) {
                    removeVector(key);
                }
            }
        } while (size() > 0);
        return;
    }
    private void writeObject(java.io.ObjectOutputStream stream)
    throws IOException {
        throw new NotSerializableException();
    }
    private void readObject(java.io.ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }
}
class ClientVector extends java.util.Stack<KeepAliveEntry> {
    private static final long serialVersionUID = -8680532108106489459L;
    int nap;
    ClientVector (int nap) {
        this.nap = nap;
    }
    synchronized HttpClient get() {
        if (empty()) {
            return null;
        } else {
            HttpClient hc = null;
            long currentTime = System.currentTimeMillis();
            do {
                KeepAliveEntry e = pop();
                if ((currentTime - e.idleStartTime) > nap) {
                    e.hc.closeServer();
                } else {
                    hc = e.hc;
                }
            } while ((hc== null) && (!empty()));
            return hc;
        }
    }
    synchronized void put(HttpClient h) {
        if (size() >= KeepAliveCache.getMaxConnections()) {
            h.closeServer(); 
        } else {
            push(new KeepAliveEntry(h, System.currentTimeMillis()));
        }
    }
    private void writeObject(java.io.ObjectOutputStream stream)
    throws IOException {
        throw new NotSerializableException();
    }
    private void readObject(java.io.ObjectInputStream stream)
    throws IOException, ClassNotFoundException {
        throw new NotSerializableException();
    }
}
class KeepAliveKey {
    private String      protocol = null;
    private String      host = null;
    private int         port = 0;
    private Object      obj = null; 
    public KeepAliveKey(URL url, Object obj) {
        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.obj = obj;
    }
    @Override
    public boolean equals(Object obj) {
        if ((obj instanceof KeepAliveKey) == false)
            return false;
        KeepAliveKey kae = (KeepAliveKey)obj;
        return host.equals(kae.host)
            && (port == kae.port)
            && protocol.equals(kae.protocol)
            && this.obj == kae.obj;
    }
    @Override
    public int hashCode() {
        String str = protocol+host+port;
        return this.obj == null? str.hashCode() :
            str.hashCode() + this.obj.hashCode();
    }
}
class KeepAliveEntry {
    HttpClient hc;
    long idleStartTime;
    KeepAliveEntry(HttpClient hc, long idleStartTime) {
        this.hc = hc;
        this.idleStartTime = idleStartTime;
    }
}

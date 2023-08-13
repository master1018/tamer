final class SSLSessionContextImpl implements SSLSessionContext {
    private Cache sessionCache;         
    private Cache sessionHostPortCache; 
    private int cacheLimit;             
    private int timeout;                
    private static final Debug debug = Debug.getInstance("ssl");
    SSLSessionContextImpl() {
        cacheLimit = getDefaultCacheLimit();    
        timeout = 86400;                        
        sessionCache = Cache.newSoftMemoryCache(cacheLimit, timeout);
        sessionHostPortCache = Cache.newSoftMemoryCache(cacheLimit, timeout);
    }
    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("session id cannot be null");
        }
        SSLSessionImpl sess =
                (SSLSessionImpl)sessionCache.get(new SessionId(sessionId));
        if (!isTimedout(sess)) {
            return sess;
        }
        return null;
    }
    public Enumeration<byte[]> getIds() {
        SessionCacheVisitor scVisitor = new SessionCacheVisitor();
        sessionCache.accept(scVisitor);
        return scVisitor.getSessionIds();
    }
    public void setSessionTimeout(int seconds)
                 throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException();
        }
        if (timeout != seconds) {
            sessionCache.setTimeout(seconds);
            sessionHostPortCache.setTimeout(seconds);
            timeout = seconds;
        }
    }
    public int getSessionTimeout() {
        return timeout;
    }
    public void setSessionCacheSize(int size)
                 throws IllegalArgumentException {
        if (size < 0)
            throw new IllegalArgumentException();
        if (cacheLimit != size) {
            sessionCache.setCapacity(size);
            sessionHostPortCache.setCapacity(size);
            cacheLimit = size;
        }
    }
    public int getSessionCacheSize() {
        return cacheLimit;
    }
    SSLSessionImpl get(byte[] id) {
        return (SSLSessionImpl)getSession(id);
    }
    SSLSessionImpl get(String hostname, int port) {
        if (hostname == null && port == -1) {
            return null;
        }
        SSLSessionImpl sess =
            (SSLSessionImpl)sessionHostPortCache.get(getKey(hostname, port));
        if (!isTimedout(sess)) {
            return sess;
        }
        return null;
    }
    private String getKey(String hostname, int port) {
        return (hostname + ":" + String.valueOf(port)).toLowerCase();
    }
    void put(SSLSessionImpl s) {
        sessionCache.put(s.getSessionId(), s);
        if ((s.getPeerHost() != null) && (s.getPeerPort() != -1)) {
            sessionHostPortCache.put(
                getKey(s.getPeerHost(), s.getPeerPort()), s);
        }
        s.setContext(this);
    }
    void remove(SessionId key) {
        SSLSessionImpl s = (SSLSessionImpl)sessionCache.get(key);
        if (s != null) {
            sessionCache.remove(key);
            sessionHostPortCache.remove(
                        getKey(s.getPeerHost(), s.getPeerPort()));
        }
    }
    private int getDefaultCacheLimit() {
        int cacheLimit = 0;
        try {
        String s = java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                        "javax.net.ssl.sessionCacheSize");
                }
            });
            cacheLimit = (s != null) ? Integer.valueOf(s).intValue() : 0;
        } catch (Exception e) {
        }
        return (cacheLimit > 0) ? cacheLimit : 0;
    }
    boolean isTimedout(SSLSession sess) {
        if (timeout == 0) {
            return false;
        }
        if ((sess != null) && ((sess.getCreationTime() + timeout * 1000L)
                                        <= (System.currentTimeMillis()))) {
            sess.invalidate();
            return true;
        }
        return false;
    }
    final class SessionCacheVisitor
            implements sun.security.util.Cache.CacheVisitor {
        Vector<byte[]> ids = null;
        public void visit(java.util.Map<Object, Object> map) {
            ids = new Vector<byte[]>(map.size());
            for (Object key : map.keySet()) {
                SSLSessionImpl value = (SSLSessionImpl)map.get(key);
                if (!isTimedout(value)) {
                    ids.addElement(((SessionId)key).getId());
                }
            }
        }
        public Enumeration<byte[]> getSessionIds() {
            return  ids != null ? ids.elements() :
                                  new Vector<byte[]>().elements();
        }
    }
}

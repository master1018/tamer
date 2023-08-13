public class ServerSessionContext extends AbstractSessionContext {
    private final Map<ByteArray, SSLSession> sessions
            = new LinkedHashMap<ByteArray, SSLSession>() {
        @Override
        protected boolean removeEldestEntry(
                Map.Entry<ByteArray, SSLSession> eldest) {
            return maximumSize > 0 && size() > maximumSize;
        }
    };
    private final SSLServerSessionCache persistentCache;
    public ServerSessionContext(SSLParameters parameters,
            SSLServerSessionCache persistentCache) {
        super(parameters, 100, 0);
        this.persistentCache = persistentCache;
    }
    Iterator<SSLSession> sessionIterator() {
        synchronized (sessions) {
            SSLSession[] array = sessions.values().toArray(
                    new SSLSession[sessions.size()]);
            return Arrays.asList(array).iterator();
        }
    }
    void trimToSize() {
        synchronized (sessions) {
            int size = sessions.size();
            if (size > maximumSize) {
                int removals = size - maximumSize;
                Iterator<SSLSession> i = sessions.values().iterator();
                do {
                    i.next();
                    i.remove();
                } while (--removals > 0);
            }
        }
    }
    public void setSessionTimeout(int seconds)
            throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds < 0");
        }
        timeout = seconds;
    }
    public SSLSession getSession(byte[] sessionId) {
        ByteArray key = new ByteArray(sessionId);
        synchronized (sessions) {
            SSLSession session = sessions.get(key);
            if (session != null) {
                return session;
            }
        }
        if (persistentCache != null) {
            byte[] data = persistentCache.getSessionData(sessionId);
            if (data != null) {
                SSLSession session = toSession(data, null, -1);
                if (session != null) {
                    synchronized (sessions) {
                        sessions.put(key, session);
                    }
                    return session;
                }
            }
        }
        return null;
    }
    void putSession(SSLSession session) {
        ByteArray key = new ByteArray(session.getId());
        synchronized (sessions) {
            sessions.put(key, session);
        }
        if (persistentCache != null) {
            byte[] data = toBytes(session);
            if (data != null) {
                persistentCache.putSessionData(session, data);
            }
        }
    }
}

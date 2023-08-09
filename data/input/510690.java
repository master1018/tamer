public class ClientSessionContext extends AbstractSessionContext {
    final Map<HostAndPort, SSLSession> sessions
            = new LinkedHashMap<HostAndPort, SSLSession>() {
        @Override
        protected boolean removeEldestEntry(
                Map.Entry<HostAndPort, SSLSession> eldest) {
            boolean remove = maximumSize > 0 && size() > maximumSize;
            if (remove) {
                removeById(eldest.getValue());
            }
            return remove;
        }
    };
    Map<ByteArray, SSLSession> sessionsById;
    final SSLClientSessionCache persistentCache;
    public ClientSessionContext(SSLParameters parameters,
            SSLClientSessionCache persistentCache) {
        super(parameters, 10, 0);
        this.persistentCache = persistentCache;
    }
    public final void setSessionTimeout(int seconds)
            throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds < 0");
        }
        timeout = seconds;
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
                    removeById(i.next());
                    i.remove();
                } while (--removals > 0);                
            }
        }
    }
    void removeById(SSLSession session) {
        if (sessionsById != null) {
            sessionsById.remove(new ByteArray(session.getId()));
        }
    }
    public SSLSession getSession(byte[] sessionId) {
        ByteArray id = new ByteArray(sessionId);
        synchronized (sessions) {
            indexById();
            return sessionsById.get(id);
        }
    }
    private void indexById() {
        if (sessionsById == null) {
            sessionsById = new HashMap<ByteArray, SSLSession>();
            for (SSLSession session : sessions.values()) {
                sessionsById.put(new ByteArray(session.getId()), session);
            }
        }
    }
    private void indexById(SSLSession session) {
        if (sessionsById != null) {
            sessionsById.put(new ByteArray(session.getId()), session);
        }
    }
    public SSLSession getSession(String host, int port) {
        synchronized (sessions) {
            SSLSession session = sessions.get(new HostAndPort(host, port));
            if (session != null) {
                return session;
            }
        }
        if (persistentCache != null) {
            byte[] data = persistentCache.getSessionData(host, port);
            if (data != null) {
                SSLSession session = toSession(data, host, port);
                if (session != null) {
                    synchronized (sessions) {
                        sessions.put(new HostAndPort(host, port), session);
                        indexById(session);
                    }
                    return session;
                }
            }
        }
        return null;
    }
    void putSession(SSLSession session) {
        HostAndPort key = new HostAndPort(session.getPeerHost(),
                session.getPeerPort());
        synchronized (sessions) {
            sessions.put(key, session);
            indexById(session);
        }
        if (persistentCache != null) {
            byte[] data = toBytes(session);
            if (data != null) {
                persistentCache.putSessionData(session, data);
            }
        }
    }
    static class HostAndPort {
        final String host;
        final int port;
        HostAndPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
        @Override
        public int hashCode() {
            return host.hashCode() * 31 + port;
        }
        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object o) {
            HostAndPort other = (HostAndPort) o;
            return host.equals(other.host) && port == other.port;
        }
    }
}

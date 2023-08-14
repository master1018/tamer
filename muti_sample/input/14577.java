final class SessionManager {
    private final static int DEFAULT_MAX_SESSIONS = 32;
    private final static Debug debug = Debug.getInstance("pkcs11");
    private final Token token;
    private final int maxSessions;
    private int activeSessions;
    private final Pool objSessions;
    private final Pool opSessions;
    private int maxActiveSessions;
    private final long openSessionFlags;
    SessionManager(Token token) {
        long n;
        if (token.isWriteProtected()) {
            openSessionFlags = CKF_SERIAL_SESSION;
            n = token.tokenInfo.ulMaxSessionCount;
        } else {
            openSessionFlags = CKF_SERIAL_SESSION | CKF_RW_SESSION;
            n = token.tokenInfo.ulMaxRwSessionCount;
        }
        if (n == CK_EFFECTIVELY_INFINITE) {
            n = Integer.MAX_VALUE;
        } else if ((n == CK_UNAVAILABLE_INFORMATION) || (n < 0)) {
            n = DEFAULT_MAX_SESSIONS;
        }
        maxSessions = (int)Math.min(n, Integer.MAX_VALUE);
        this.token = token;
        this.objSessions = new Pool(this);
        this.opSessions = new Pool(this);
    }
    boolean lowMaxSessions() {
        return (maxSessions <= DEFAULT_MAX_SESSIONS);
    }
    synchronized Session getObjSession() throws PKCS11Exception {
        Session session = objSessions.poll();
        if (session != null) {
            return ensureValid(session);
        }
        session = opSessions.poll();
        if (session != null) {
            return ensureValid(session);
        }
        session = openSession();
        return ensureValid(session);
    }
    synchronized Session getOpSession() throws PKCS11Exception {
        Session session = opSessions.poll();
        if (session != null) {
            return ensureValid(session);
        }
        if (maxSessions == Integer.MAX_VALUE ||
                activeSessions < maxSessions) {
            session = openSession();
            return ensureValid(session);
        }
        session = objSessions.poll();
        if (session != null) {
            return ensureValid(session);
        }
        throw new ProviderException("Could not obtain session");
    }
    private Session ensureValid(Session session) {
        session.id();
        return session;
    }
    synchronized Session killSession(Session session) {
        if ((session == null) || (token.isValid() == false)) {
            return null;
        }
        if (debug != null) {
            String location = new Exception().getStackTrace()[2].toString();
            System.out.println("Killing session (" + location + ") active: "
                + activeSessions);
        }
        closeSession(session);
        return null;
    }
    synchronized Session releaseSession(Session session) {
        if ((session == null) || (token.isValid() == false)) {
            return null;
        }
        if (session.hasObjects()) {
            objSessions.release(session);
        } else {
            opSessions.release(session);
        }
        return null;
    }
    synchronized void demoteObjSession(Session session) {
        if (token.isValid() == false) {
            return;
        }
        if (debug != null) {
            System.out.println("Demoting session, active: " +
                activeSessions);
        }
        boolean present = objSessions.remove(session);
        if (present == false) {
            return;
        }
        opSessions.release(session);
    }
    private Session openSession() throws PKCS11Exception {
        if ((maxSessions != Integer.MAX_VALUE) &&
                (activeSessions >= maxSessions)) {
            throw new ProviderException("No more sessions available");
        }
        long id = token.p11.C_OpenSession
                    (token.provider.slotID, openSessionFlags, null, null);
        Session session = new Session(token, id);
        activeSessions++;
        if (debug != null) {
            if (activeSessions > maxActiveSessions) {
                maxActiveSessions = activeSessions;
                if (maxActiveSessions % 10 == 0) {
                    System.out.println("Open sessions: " + maxActiveSessions);
                }
            }
        }
        return session;
    }
    private void closeSession(Session session) {
        session.close();
        activeSessions--;
    }
    private static final class Pool {
        private final SessionManager mgr;
        private final List<Session> pool;
        Pool(SessionManager mgr) {
            this.mgr = mgr;
            pool = new ArrayList<Session>();
        }
        boolean remove(Session session) {
            return pool.remove(session);
        }
        Session poll() {
            int n = pool.size();
            if (n == 0) {
                return null;
            }
            Session session = pool.remove(n - 1);
            return session;
        }
        void release(Session session) {
            pool.add(session);
            if (session.hasObjects()) {
                return;
            }
            int n = pool.size();
            if (n < 5) {
                return;
            }
            Session oldestSession = pool.get(0);
            long time = System.currentTimeMillis();
            if (session.isLive(time) && oldestSession.isLive(time)) {
                return;
            }
            Collections.sort(pool);
            int i = 0;
            while (i < n - 1) { 
                oldestSession = pool.get(i);
                if (oldestSession.isLive(time)) {
                    break;
                }
                i++;
                mgr.closeSession(oldestSession);
            }
            if (debug != null) {
                System.out.println("Closing " + i + " idle sessions, active: "
                        + mgr.activeSessions);
            }
            List<Session> subList = pool.subList(0, i);
            subList.clear();
        }
    }
}

public final class LdapPoolManager {
    private static final String DEBUG =
        "com.sun.jndi.ldap.connect.pool.debug";
    public static final boolean debug =
        "all".equalsIgnoreCase(getProperty(DEBUG, null));
    public static final boolean trace = debug ||
        "fine".equalsIgnoreCase(getProperty(DEBUG, null));
    private static final String POOL_AUTH =
        "com.sun.jndi.ldap.connect.pool.authentication";
    private static final String POOL_PROTOCOL =
        "com.sun.jndi.ldap.connect.pool.protocol";
    private static final String MAX_POOL_SIZE =
        "com.sun.jndi.ldap.connect.pool.maxsize";
    private static final String PREF_POOL_SIZE =
        "com.sun.jndi.ldap.connect.pool.prefsize";
    private static final String INIT_POOL_SIZE =
        "com.sun.jndi.ldap.connect.pool.initsize";
    private static final String POOL_TIMEOUT =
        "com.sun.jndi.ldap.connect.pool.timeout";
    private static final String SASL_CALLBACK =
        "java.naming.security.sasl.callback";
    private static final int DEFAULT_MAX_POOL_SIZE = 0;
    private static final int DEFAULT_PREF_POOL_SIZE = 0;
    private static final int DEFAULT_INIT_POOL_SIZE = 1;
    private static final int DEFAULT_TIMEOUT = 0;    
    private static final String DEFAULT_AUTH_MECHS = "none simple";
    private static final String DEFAULT_PROTOCOLS = "plain";
    private static final int NONE = 0;    
    private static final int SIMPLE = 1;
    private static final int DIGEST = 2;
    private static final long idleTimeout;
    private static final int maxSize;     
    private static final int prefSize;    
    private static final int initSize;    
    private static boolean supportPlainProtocol = false;
    private static boolean supportSslProtocol = false;
    private static final Pool[] pools = new Pool[3];
    static {
        maxSize = getInteger(MAX_POOL_SIZE, DEFAULT_MAX_POOL_SIZE);
        prefSize = getInteger(PREF_POOL_SIZE, DEFAULT_PREF_POOL_SIZE);
        initSize = getInteger(INIT_POOL_SIZE, DEFAULT_INIT_POOL_SIZE);
        idleTimeout = getLong(POOL_TIMEOUT, DEFAULT_TIMEOUT);
        String str = getProperty(POOL_AUTH, DEFAULT_AUTH_MECHS);
        StringTokenizer parser = new StringTokenizer(str);
        int count = parser.countTokens();
        String mech;
        int p;
        for (int i = 0; i < count; i++) {
            mech = parser.nextToken().toLowerCase();
            if (mech.equals("anonymous")) {
                mech = "none";
            }
            p = findPool(mech);
            if (p >= 0 && pools[p] == null) {
                pools[p] = new Pool(initSize, prefSize, maxSize);
            }
        }
        str= getProperty(POOL_PROTOCOL, DEFAULT_PROTOCOLS);
        parser = new StringTokenizer(str);
        count = parser.countTokens();
        String proto;
        for (int i = 0; i < count; i++) {
            proto = parser.nextToken();
            if ("plain".equalsIgnoreCase(proto)) {
                supportPlainProtocol = true;
            } else if ("ssl".equalsIgnoreCase(proto)) {
                supportSslProtocol = true;
            } else {
            }
        }
        if (idleTimeout > 0) {
            new PoolCleaner(idleTimeout, pools).start();
        }
        if (debug) {
            showStats(System.err);
        }
    }
    private LdapPoolManager() {
    }
    private static int findPool(String mech) {
        if ("none".equalsIgnoreCase(mech)) {
            return NONE;
        } else if ("simple".equalsIgnoreCase(mech)) {
            return SIMPLE;
        } else if ("digest-md5".equalsIgnoreCase(mech)) {
            return DIGEST;
        }
        return -1;
    }
    static boolean isPoolingAllowed(String socketFactory, OutputStream trace,
        String authMech, String protocol, Hashtable env)
                throws NamingException {
        if (trace != null && !debug
                || (protocol == null && !supportPlainProtocol)
                || ("ssl".equalsIgnoreCase(protocol) && !supportSslProtocol)) {
            d("Pooling disallowed due to tracing or unsupported pooling of protocol");
            return false;
        }
        String COMPARATOR = "java.util.Comparator";
        boolean foundSockCmp = false;
        if ((socketFactory != null) &&
             !socketFactory.equals(LdapCtx.DEFAULT_SSL_FACTORY)) {
            try {
                Class socketFactoryClass = Obj.helper.loadClass(socketFactory);
                Class[] interfaces = socketFactoryClass.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (interfaces[i].getCanonicalName().equals(COMPARATOR)) {
                        foundSockCmp = true;
                    }
                }
            } catch (Exception e) {
                CommunicationException ce =
                    new CommunicationException("Loading the socket factory");
                ce.setRootCause(e);
                throw ce;
            }
            if (!foundSockCmp) {
                return false;
            }
        }
        int p = findPool(authMech);
        if (p < 0 || pools[p] == null) {
            d("authmech not found: ", authMech);
            return false;
        }
        d("using authmech: ", authMech);
        switch (p) {
        case NONE:
        case SIMPLE:
            return true;
        case DIGEST:
            return (env == null || env.get(SASL_CALLBACK) == null);
        }
        return false;
    }
    static LdapClient getLdapClient(String host, int port, String socketFactory,
        int connTimeout, int readTimeout, OutputStream trace, int version,
        String authMech, Control[] ctls, String protocol, String user,
        Object passwd, Hashtable env) throws NamingException {
        ClientId id = null;
        Pool pool;
        int p = findPool(authMech);
        if (p < 0 || (pool=pools[p]) == null) {
            throw new IllegalArgumentException(
                "Attempting to use pooling for an unsupported mechanism: " +
                authMech);
        }
        switch (p) {
        case NONE:
            id = new ClientId(version, host, port, protocol,
                        ctls, trace, socketFactory);
            break;
        case SIMPLE:
            id = new SimpleClientId(version, host, port, protocol,
                ctls, trace, socketFactory, user, passwd);
            break;
        case DIGEST:
            id = new DigestClientId(version, host, port, protocol,
                ctls, trace, socketFactory, user, passwd, env);
            break;
        }
        return (LdapClient) pool.getPooledConnection(id, connTimeout,
            new LdapClientFactory(host, port, socketFactory, connTimeout,
                                readTimeout, trace));
    }
    public static void showStats(PrintStream out) {
        out.println("***** start *****");
        out.println("idle timeout: " + idleTimeout);
        out.println("maximum pool size: " + maxSize);
        out.println("preferred pool size: " + prefSize);
        out.println("initial pool size: " + initSize);
        out.println("protocol types: " + (supportPlainProtocol ? "plain " : "") +
            (supportSslProtocol ? "ssl" : ""));
        out.println("authentication types: " +
            (pools[NONE] != null ? "none " : "") +
            (pools[SIMPLE] != null ? "simple " : "") +
            (pools[DIGEST] != null ? "DIGEST-MD5 " : ""));
        for (int i = 0; i < pools.length; i++) {
            if (pools[i] != null) {
                out.println(
                    (i == NONE ? "anonymous pools" :
                        i == SIMPLE ? "simple auth pools" :
                        i == DIGEST ? "digest pools" : "")
                            + ":");
                pools[i].showStats(out);
            }
        }
        out.println("***** end *****");
    }
    public static void expire(long threshold) {
        for (int i = 0; i < pools.length; i++) {
            if (pools[i] != null) {
                pools[i].expire(threshold);
            }
        }
    }
    private static void d(String msg) {
        if (debug) {
            System.err.println("LdapPoolManager: " + msg);
        }
    }
    private static void d(String msg, String o) {
        if (debug) {
            System.err.println("LdapPoolManager: " + msg + o);
        }
    }
    private static final String getProperty(final String propName,
        final String defVal) {
        return (String) AccessController.doPrivileged(
            new PrivilegedAction() {
            public Object run() {
                try {
                    return System.getProperty(propName, defVal);
                } catch (SecurityException e) {
                    return defVal;
                }
            }
        });
    }
    private static final int getInteger(final String propName,
        final int defVal) {
        Integer val = (Integer) AccessController.doPrivileged(
            new PrivilegedAction() {
            public Object run() {
                try {
                    return Integer.getInteger(propName, defVal);
                } catch (SecurityException e) {
                    return new Integer(defVal);
                }
            }
        });
        return val.intValue();
    }
    private static final long getLong(final String propName,
        final long defVal) {
        Long val = (Long) AccessController.doPrivileged(
            new PrivilegedAction() {
            public Object run() {
                try {
                    return Long.getLong(propName, defVal);
                } catch (SecurityException e) {
                    return new Long(defVal);
                }
            }
        });
        return val.longValue();
    }
}

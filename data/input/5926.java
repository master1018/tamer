public class MBeanServerFactory {
    private MBeanServerFactory() {
    }
    private static MBeanServerBuilder builder = null;
    public static void releaseMBeanServer(MBeanServer mbeanServer) {
        checkPermission("releaseMBeanServer");
        removeMBeanServer(mbeanServer);
    }
    public static MBeanServer createMBeanServer() {
        return createMBeanServer(null);
    }
    public static MBeanServer createMBeanServer(String domain)  {
        checkPermission("createMBeanServer");
        final MBeanServer mBeanServer = newMBeanServer(domain);
        addMBeanServer(mBeanServer);
        return mBeanServer;
    }
    public static MBeanServer newMBeanServer() {
        return newMBeanServer(null);
    }
    public static MBeanServer newMBeanServer(String domain)  {
        checkPermission("newMBeanServer");
        final MBeanServerBuilder mbsBuilder = getNewMBeanServerBuilder();
        synchronized(mbsBuilder) {
            final MBeanServerDelegate delegate  =
                    mbsBuilder.newMBeanServerDelegate();
            if (delegate == null) {
                final String msg =
                        "MBeanServerBuilder.newMBeanServerDelegate() " +
                        "returned null";
                throw new JMRuntimeException(msg);
            }
            final MBeanServer mbeanServer =
                    mbsBuilder.newMBeanServer(domain,null,delegate);
            if (mbeanServer == null) {
                final String msg =
                        "MBeanServerBuilder.newMBeanServer() returned null";
                throw new JMRuntimeException(msg);
            }
            return mbeanServer;
        }
    }
    public synchronized static
            ArrayList<MBeanServer> findMBeanServer(String agentId) {
        checkPermission("findMBeanServer");
        if (agentId == null)
            return new ArrayList<MBeanServer>(mBeanServerList);
        ArrayList<MBeanServer> result = new ArrayList<MBeanServer>();
        for (MBeanServer mbs : mBeanServerList) {
            String name = mBeanServerId(mbs);
            if (agentId.equals(name))
                result.add(mbs);
        }
        return result;
    }
    public static ClassLoaderRepository getClassLoaderRepository(
            MBeanServer server) {
        return server.getClassLoaderRepository();
    }
    private static String mBeanServerId(MBeanServer mbs) {
        try {
            return (String) mbs.getAttribute(MBeanServerDelegate.DELEGATE_NAME,
                    "MBeanServerId");
        } catch (JMException e) {
            JmxProperties.MISC_LOGGER.finest(
                    "Ignoring exception while getting MBeanServerId: "+e);
            return null;
        }
    }
    private static void checkPermission(String action)
    throws SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Permission perm = new MBeanServerPermission(action);
            sm.checkPermission(perm);
        }
    }
    private static synchronized void addMBeanServer(MBeanServer mbs) {
        mBeanServerList.add(mbs);
    }
    private static synchronized void removeMBeanServer(MBeanServer mbs) {
        boolean removed = mBeanServerList.remove(mbs);
        if (!removed) {
            MBEANSERVER_LOGGER.logp(Level.FINER,
                    MBeanServerFactory.class.getName(),
                    "removeMBeanServer(MBeanServer)",
                    "MBeanServer was not in list!");
            throw new IllegalArgumentException("MBeanServer was not in list!");
        }
    }
    private static final ArrayList<MBeanServer> mBeanServerList =
            new ArrayList<MBeanServer>();
    private static Class<?> loadBuilderClass(String builderClassName)
    throws ClassNotFoundException {
        final ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            return loader.loadClass(builderClassName);
        }
        return Class.forName(builderClassName);
    }
    private static MBeanServerBuilder newBuilder(Class<?> builderClass) {
        try {
            final Object abuilder = builderClass.newInstance();
            return (MBeanServerBuilder)abuilder;
        } catch (RuntimeException x) {
            throw x;
        } catch (Exception x) {
            final String msg =
                    "Failed to instantiate a MBeanServerBuilder from " +
                    builderClass + ": " + x;
            throw new JMRuntimeException(msg, x);
        }
    }
    private static synchronized void checkMBeanServerBuilder() {
        try {
            GetPropertyAction act =
                    new GetPropertyAction(JMX_INITIAL_BUILDER);
            String builderClassName = AccessController.doPrivileged(act);
            try {
                final Class<?> newBuilderClass;
                if (builderClassName == null || builderClassName.length() == 0)
                    newBuilderClass = MBeanServerBuilder.class;
                else
                    newBuilderClass = loadBuilderClass(builderClassName);
                if (builder != null) {
                    final Class<?> builderClass = builder.getClass();
                    if (newBuilderClass == builderClass)
                        return; 
                }
                builder = newBuilder(newBuilderClass);
            } catch (ClassNotFoundException x) {
                final String msg =
                        "Failed to load MBeanServerBuilder class " +
                        builderClassName + ": " + x;
                throw new JMRuntimeException(msg, x);
            }
        } catch (RuntimeException x) {
            if (MBEANSERVER_LOGGER.isLoggable(Level.FINEST)) {
                StringBuilder strb = new StringBuilder()
                .append("Failed to instantiate MBeanServerBuilder: ").append(x)
                .append("\n\t\tCheck the value of the ")
                .append(JMX_INITIAL_BUILDER).append(" property.");
                MBEANSERVER_LOGGER.logp(Level.FINEST,
                        MBeanServerFactory.class.getName(),
                        "checkMBeanServerBuilder",
                        strb.toString());
            }
            throw x;
        }
    }
    private static synchronized MBeanServerBuilder getNewMBeanServerBuilder() {
        checkMBeanServerBuilder();
        return builder;
    }
}

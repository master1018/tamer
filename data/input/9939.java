public class ManagementFactory {
    private ManagementFactory() {};
    public final static String CLASS_LOADING_MXBEAN_NAME =
        "java.lang:type=ClassLoading";
    public final static String COMPILATION_MXBEAN_NAME =
        "java.lang:type=Compilation";
    public final static String MEMORY_MXBEAN_NAME =
        "java.lang:type=Memory";
    public final static String OPERATING_SYSTEM_MXBEAN_NAME =
        "java.lang:type=OperatingSystem";
    public final static String RUNTIME_MXBEAN_NAME =
        "java.lang:type=Runtime";
    public final static String THREAD_MXBEAN_NAME =
        "java.lang:type=Threading";
    public final static String GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE =
        "java.lang:type=GarbageCollector";
    public final static String MEMORY_MANAGER_MXBEAN_DOMAIN_TYPE=
        "java.lang:type=MemoryManager";
    public final static String MEMORY_POOL_MXBEAN_DOMAIN_TYPE=
        "java.lang:type=MemoryPool";
    public static ClassLoadingMXBean getClassLoadingMXBean() {
        return ManagementFactoryHelper.getClassLoadingMXBean();
    }
    public static MemoryMXBean getMemoryMXBean() {
        return ManagementFactoryHelper.getMemoryMXBean();
    }
    public static ThreadMXBean getThreadMXBean() {
        return ManagementFactoryHelper.getThreadMXBean();
    }
    public static RuntimeMXBean getRuntimeMXBean() {
        return ManagementFactoryHelper.getRuntimeMXBean();
    }
    public static CompilationMXBean getCompilationMXBean() {
        return ManagementFactoryHelper.getCompilationMXBean();
    }
    public static OperatingSystemMXBean getOperatingSystemMXBean() {
        return ManagementFactoryHelper.getOperatingSystemMXBean();
    }
    public static List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
        return ManagementFactoryHelper.getMemoryPoolMXBeans();
    }
    public static List<MemoryManagerMXBean> getMemoryManagerMXBeans() {
        return ManagementFactoryHelper.getMemoryManagerMXBeans();
    }
    public static List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
        return ManagementFactoryHelper.getGarbageCollectorMXBeans();
    }
    private static MBeanServer platformMBeanServer;
    public static synchronized MBeanServer getPlatformMBeanServer() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            Permission perm = new MBeanServerPermission("createMBeanServer");
            sm.checkPermission(perm);
        }
        if (platformMBeanServer == null) {
            platformMBeanServer = MBeanServerFactory.createMBeanServer();
            for (PlatformComponent pc : PlatformComponent.values()) {
                List<? extends PlatformManagedObject> list =
                    pc.getMXBeans(pc.getMXBeanInterface());
                for (PlatformManagedObject o : list) {
                    if (!platformMBeanServer.isRegistered(o.getObjectName())) {
                        addMXBean(platformMBeanServer, o);
                    }
                }
            }
        }
        return platformMBeanServer;
    }
    public static <T> T
        newPlatformMXBeanProxy(MBeanServerConnection connection,
                               String mxbeanName,
                               Class<T> mxbeanInterface)
            throws java.io.IOException {
        final Class interfaceClass = mxbeanInterface;
        final ClassLoader loader =
            AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                public ClassLoader run() {
                    return interfaceClass.getClassLoader();
                }
            });
        if (loader != null) {
            throw new IllegalArgumentException(mxbeanName +
                " is not a platform MXBean");
        }
        try {
            final ObjectName objName = new ObjectName(mxbeanName);
            String intfName = interfaceClass.getName();
            if (!connection.isInstanceOf(objName, intfName)) {
                throw new IllegalArgumentException(mxbeanName +
                    " is not an instance of " + interfaceClass);
            }
            final Class[] interfaces;
            boolean emitter = connection.isInstanceOf(objName, NOTIF_EMITTER);
            return JMX.newMXBeanProxy(connection, objName, mxbeanInterface,
                                      emitter);
        } catch (InstanceNotFoundException|MalformedObjectNameException e) {
            throw new IllegalArgumentException(e);
        }
    }
    public static <T extends PlatformManagedObject>
            T getPlatformMXBean(Class<T> mxbeanInterface) {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null)
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " is not a platform management interface");
        if (!pc.isSingleton())
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " can have zero or more than one instances");
        return pc.getSingletonMXBean(mxbeanInterface);
    }
    public static <T extends PlatformManagedObject> List<T>
            getPlatformMXBeans(Class<T> mxbeanInterface) {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null)
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " is not a platform management interface");
        return Collections.unmodifiableList(pc.getMXBeans(mxbeanInterface));
    }
    public static <T extends PlatformManagedObject>
            T getPlatformMXBean(MBeanServerConnection connection,
                                Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null)
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " is not a platform management interface");
        if (!pc.isSingleton())
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " can have zero or more than one instances");
        return pc.getSingletonMXBean(connection, mxbeanInterface);
    }
    public static <T extends PlatformManagedObject>
            List<T> getPlatformMXBeans(MBeanServerConnection connection,
                                       Class<T> mxbeanInterface)
        throws java.io.IOException
    {
        PlatformComponent pc = PlatformComponent.getPlatformComponent(mxbeanInterface);
        if (pc == null) {
            throw new IllegalArgumentException(mxbeanInterface.getName() +
                " is not a platform management interface");
        }
        return Collections.unmodifiableList(pc.getMXBeans(connection, mxbeanInterface));
    }
    public static Set<Class<? extends PlatformManagedObject>>
           getPlatformManagementInterfaces()
    {
        Set<Class<? extends PlatformManagedObject>> result =
            new TreeSet<>();
        for (PlatformComponent component: PlatformComponent.values()) {
            result.add(component.getMXBeanInterface());
        }
        return Collections.unmodifiableSet(result);
    }
    private static final String NOTIF_EMITTER =
        "javax.management.NotificationEmitter";
    private static void addMXBean(final MBeanServer mbs, final PlatformManagedObject pmo) {
        final DynamicMBean dmbean;
        if (pmo instanceof DynamicMBean) {
            dmbean = DynamicMBean.class.cast(pmo);
        } else if (pmo instanceof NotificationEmitter) {
            dmbean = new StandardEmitterMBean(pmo, null, true, (NotificationEmitter) pmo);
        } else {
            dmbean = new StandardMBean(pmo, null, true);
        }
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                public Void run() throws InstanceAlreadyExistsException,
                                         MBeanRegistrationException,
                                         NotCompliantMBeanException {
                    mbs.registerMBean(dmbean, pmo.getObjectName());
                    return null;
                }
            });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getException());
        }
    }
}

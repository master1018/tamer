public class OldMBeanServerTest {
    private static MBeanServerConnection mbsc;
    private static String failure;
    public static void main(String[] args) throws Exception {
        if (!OldMBeanServerTest.class.desiredAssertionStatus())
            throw new Exception("Test must be run with -ea");
        System.setProperty("javax.management.builder.initial",
                OldMBeanServerBuilder.class.getName());
        assert MBeanServerFactory.newMBeanServer() instanceof OldMBeanServer;
        System.out.println("=== RUNNING TESTS WITH LOCAL MBEANSERVER ===");
        runTests(new Callable<MBeanServerConnection>() {
            public MBeanServerConnection call() {
                return MBeanServerFactory.newMBeanServer();
            }
        }, null);
        System.out.println("=== RUNNING TESTS THROUGH CONNECTOR ===");
        ConnectionBuilder builder = new ConnectionBuilder();
        runTests(builder, builder);
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static class ConnectionBuilder
            implements Callable<MBeanServerConnection>, Runnable {
        private JMXConnector connector;
        public MBeanServerConnection call() {
            MBeanServer mbs = MBeanServerFactory.newMBeanServer();
            try {
                JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:
                JMXConnectorServer cs =
                    JMXConnectorServerFactory.newJMXConnectorServer(
                        url, null, mbs);
                cs.start();
                JMXServiceURL addr = cs.getAddress();
                connector = JMXConnectorFactory.connect(addr);
                return connector.getMBeanServerConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void run() {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    private static void runTests(
            Callable<MBeanServerConnection> maker, Runnable breaker)
    throws Exception {
        for (Method m : OldMBeanServerTest.class.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) &&
                    m.getName().startsWith("test") &&
                    m.getParameterTypes().length == 0) {
                ExpectException expexc = m.getAnnotation(ExpectException.class);
                mbsc = maker.call();
                try {
                    m.invoke(null);
                    if (expexc != null) {
                        failure =
                                m.getName() + " did not got expected exception " +
                                expexc.value().getName();
                        System.out.println(failure);
                    } else
                        System.out.println(m.getName() + " OK");
                } catch (InvocationTargetException ite) {
                    Throwable t = ite.getCause();
                    String prob = null;
                    if (expexc != null) {
                        if (expexc.value().isInstance(t)) {
                            System.out.println(m.getName() + " OK (got expected " +
                                    expexc.value().getName() + ")");
                        } else
                            prob = "got wrong exception";
                    } else
                        prob = "got exception";
                    if (prob != null) {
                        failure = m.getName() + ": " + prob + " " +
                                t.getClass().getName();
                        System.out.println(failure);
                        t.printStackTrace(System.out);
                    }
                } finally {
                    if (breaker != null)
                        breaker.run();
                }
            }
        }
    }
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface ExpectException {
        Class<? extends Exception> value();
    }
    public static interface BoringMBean {
        public String getName();
        public int add(int x, int y);
    }
    public static class Boring implements BoringMBean, Serializable {
        public String getName() {
            return "Jessica";
        }
        public int add(int x, int y) {
            return x + y;
        }
    }
    public static interface BoringNotifierMBean extends BoringMBean {
        public void send();
    }
    public static class BoringNotifier
            extends Boring implements BoringNotifierMBean, NotificationBroadcaster {
        private final NotificationBroadcasterSupport nbs =
                new NotificationBroadcasterSupport();
        public void addNotificationListener(
                NotificationListener listener, NotificationFilter filter, Object handback)
        throws IllegalArgumentException {
            nbs.addNotificationListener(listener, filter, handback);
        }
        public void removeNotificationListener(NotificationListener listener)
        throws ListenerNotFoundException {
            nbs.removeNotificationListener(listener);
        }
        public MBeanNotificationInfo[] getNotificationInfo() {
            return null;
        }
        public void send() {
            Notification n = new Notification("type.type", this, 0L);
            nbs.sendNotification(n);
        }
    }
    private static class CountListener implements NotificationListener {
        volatile int count;
        public void handleNotification(Notification n, Object h) {
            if (h == null)
                h = 1;
            count += (Integer) h;
        }
        void waitForCount(int expect) throws InterruptedException {
            long deadline = System.currentTimeMillis() + 2000L;
            while (count < expect && System.currentTimeMillis() < deadline)
                Thread.sleep(1);
            assert count == expect;
        }
    }
    private static void testBasic() throws Exception {
        CountListener countListener = new CountListener();
        mbsc.addNotificationListener(
                MBeanServerDelegate.DELEGATE_NAME, countListener, null, null);
        assert countListener.count == 0;
        ObjectName name = new ObjectName("a:b=c");
        if (mbsc instanceof MBeanServer)
            ((MBeanServer) mbsc).registerMBean(new Boring(), name);
        else
            mbsc.createMBean(Boring.class.getName(), name);
        countListener.waitForCount(1);
        assert mbsc.isRegistered(name);
        assert mbsc.queryNames(null, null).contains(name);
        assert mbsc.getAttribute(name, "Name").equals("Jessica");
        assert mbsc.invoke(
                name, "add", new Object[] {2, 3}, new String[] {"int", "int"})
                .equals(5);
        mbsc.unregisterMBean(name);
        countListener.waitForCount(2);
        assert !mbsc.isRegistered(name);
        assert !mbsc.queryNames(null, null).contains(name);
        mbsc.createMBean(BoringNotifier.class.getName(), name);
        countListener.waitForCount(3);
        CountListener boringListener = new CountListener();
        class AlwaysNotificationFilter implements NotificationFilter {
            public boolean isNotificationEnabled(Notification notification) {
                return true;
            }
        }
        mbsc.addNotificationListener(
                name, boringListener, new AlwaysNotificationFilter(), 5);
        mbsc.invoke(name, "send", null, null);
        boringListener.waitForCount(5);
    }
    private static void testPrintAttrs() throws Exception {
        printAttrs(mbsc, null);
    }
    private static void testPlatformMBeanServer() throws Exception {
        MBeanServer pmbs = ManagementFactory.getPlatformMBeanServer();
        assert pmbs instanceof OldMBeanServer;
        printAttrs(pmbs, UnsupportedOperationException.class);
        ObjectName memoryMXBeanName =
                new ObjectName(ManagementFactory.MEMORY_MXBEAN_NAME);
        pmbs.invoke(memoryMXBeanName, "gc", null, null);
    }
    private static void printAttrs(
            MBeanServerConnection mbsc1, Class<? extends Exception> expectX)
    throws Exception {
        Set<ObjectName> names = mbsc1.queryNames(null, null);
        for (ObjectName name : names) {
            System.out.println(name + ":");
            MBeanInfo mbi = mbsc1.getMBeanInfo(name);
            MBeanAttributeInfo[] mbais = mbi.getAttributes();
            for (MBeanAttributeInfo mbai : mbais) {
                String attr = mbai.getName();
                Object value;
                try {
                    value = mbsc1.getAttribute(name, attr);
                } catch (Exception e) {
                    if (expectX != null && expectX.isInstance(e))
                        value = "<" + e + ">";
                    else
                        throw e;
                }
                String s = "  " + attr + " = " + value;
                if (s.length() > 80)
                    s = s.substring(0, 77) + "...";
                System.out.println(s);
            }
        }
    }
    private static void testJavaxManagementStandardMBean() throws Exception {
        ObjectName name = new ObjectName("a:b=c");
        Object mbean = new StandardMBean(new Boring(), BoringMBean.class);
        mbsc.createMBean(
                StandardMBean.class.getName(), name,
                new Object[] {new Boring(), BoringMBean.class},
                new String[] {Object.class.getName(), Class.class.getName()});
        assert mbsc.getAttribute(name, "Name").equals("Jessica");
        assert mbsc.invoke(
                name, "add", new Object[] {2, 3}, new String[] {"int", "int"})
                .equals(5);
        mbsc.unregisterMBean(name);
    }
    private static void testConnector() throws Exception {
    }
    public static class OldMBeanServerBuilder extends MBeanServerBuilder {
        public MBeanServer newMBeanServer(
                String defaultDomain, MBeanServer outer, MBeanServerDelegate delegate) {
            return new OldMBeanServer(defaultDomain, delegate);
        }
    }
    public static class OldMBeanServer implements MBeanServer {
        private static final ObjectName clrName;
        static {
            try {
                clrName =
                        new ObjectName("JMImplementation:type=ClassLoaderRepository");
            } catch (MalformedObjectNameException e) {
                throw new RuntimeException(e);
            }
        }
        private final ConcurrentMap<ObjectName, DynamicMBean> mbeans =
                new ConcurrentHashMap<ObjectName, DynamicMBean>();
        private final ConcurrentMap<ObjectName, ListenerTable> listenerMap =
                new ConcurrentHashMap<ObjectName, ListenerTable>();
        private final String defaultDomain;
        private final MBeanServerDelegate delegate;
        private final ClassLoaderRepositoryImpl clr =
                new ClassLoaderRepositoryImpl();
        OldMBeanServer(String defaultDomain, MBeanServerDelegate delegate) {
            this.defaultDomain = defaultDomain;
            this.delegate = delegate;
            try {
                registerMBean(delegate, MBeanServerDelegate.DELEGATE_NAME);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public ObjectInstance createMBean(String className, ObjectName name)
        throws ReflectionException, InstanceAlreadyExistsException,
                MBeanRegistrationException, MBeanException,
                NotCompliantMBeanException {
            return createMBean(className, name, null, null);
        }
        public ObjectInstance createMBean(
                String className, ObjectName name, ObjectName loaderName)
        throws ReflectionException, InstanceAlreadyExistsException,
                MBeanRegistrationException, MBeanException,
                NotCompliantMBeanException, InstanceNotFoundException {
            return createMBean(className, name, loaderName, null, null);
        }
        public ObjectInstance createMBean(
                String className, ObjectName name, Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException,
                MBeanRegistrationException, MBeanException,
                NotCompliantMBeanException {
            try {
                return createMBean(className, name, clrName, params, signature);
            } catch (InstanceNotFoundException ex) {
                throw new RuntimeException(ex);  
            }
        }
        public ObjectInstance createMBean(
                String className, ObjectName name, ObjectName loaderName,
                Object[] params, String[] signature)
        throws ReflectionException, InstanceAlreadyExistsException,
                MBeanRegistrationException, MBeanException,
                NotCompliantMBeanException, InstanceNotFoundException {
            Object mbean = instantiate(className, loaderName, params, signature);
            return registerMBean(mbean, name);
        }
        private void forbidJMImpl(ObjectName name) {
            if (name.getDomain().equals("JMImplementation") &&
                    mbeans.containsKey(MBeanServerDelegate.DELEGATE_NAME))
                throw new IllegalArgumentException("JMImplementation reserved");
        }
        public ObjectInstance registerMBean(Object object, ObjectName name)
        throws InstanceAlreadyExistsException, MBeanRegistrationException,
                NotCompliantMBeanException {
            forbidJMImpl(name);
            if (name.isPattern())
                throw new IllegalArgumentException(name.toString());
            DynamicMBean mbean;
            if (object instanceof DynamicMBean)
                mbean = (DynamicMBean) object;
            else
                mbean = standardToDynamic(object);
            MBeanRegistration reg = mbeanRegistration(object);
            try {
                name = reg.preRegister(this, name);
            } catch (Exception e) {
                throw new MBeanRegistrationException(e);
            }
            DynamicMBean put = mbeans.putIfAbsent(name, mbean);
            if (put != null) {
                reg.postRegister(false);
                throw new InstanceAlreadyExistsException(name.toString());
            }
            reg.postRegister(true);
            if (object instanceof ClassLoader)
                clr.addLoader((ClassLoader) object);
            Notification n = new MBeanServerNotification(
                    MBeanServerNotification.REGISTRATION_NOTIFICATION,
                    MBeanServerDelegate.DELEGATE_NAME,
                    0,
                    name);
            delegate.sendNotification(n);
            String className = mbean.getMBeanInfo().getClassName();
            return new ObjectInstance(name, className);
        }
        public void unregisterMBean(ObjectName name)
        throws InstanceNotFoundException, MBeanRegistrationException {
            forbidJMImpl(name);
            DynamicMBean mbean = getMBean(name);
            if (mbean == null)
                throw new InstanceNotFoundException(name.toString());
            MBeanRegistration reg = mbeanRegistration(mbean);
            try {
                reg.preDeregister();
            } catch (Exception e) {
                throw new MBeanRegistrationException(e);
            }
            if (!mbeans.remove(name, mbean))
                throw new InstanceNotFoundException(name.toString());
            Object userMBean = getUserMBean(mbean);
            if (userMBean instanceof ClassLoader)
                clr.removeLoader((ClassLoader) userMBean);
            Notification n = new MBeanServerNotification(
                    MBeanServerNotification.REGISTRATION_NOTIFICATION,
                    MBeanServerDelegate.DELEGATE_NAME,
                    0,
                    name);
            delegate.sendNotification(n);
            reg.postDeregister();
        }
        public ObjectInstance getObjectInstance(ObjectName name)
        throws InstanceNotFoundException {
            DynamicMBean mbean = getMBean(name);
            return new ObjectInstance(name, mbean.getMBeanInfo().getClassName());
        }
        private static class TrueQueryExp implements QueryExp {
            public boolean apply(ObjectName name) {
                return true;
            }
            public void setMBeanServer(MBeanServer s) {}
        }
        private static final QueryExp trueQuery = new TrueQueryExp();
        public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
            Set<ObjectInstance> instances = newSet();
            if (name == null)
                name = ObjectName.WILDCARD;
            if (query == null)
                query = trueQuery;
            MBeanServer oldMBS = QueryEval.getMBeanServer();
            try {
                query.setMBeanServer(this);
                for (ObjectName n : mbeans.keySet()) {
                    if (name.apply(n)) {
                        try {
                            if (query.apply(n))
                                instances.add(getObjectInstance(n));
                        } catch (Exception e) {
                        }
                    }
                }
            } finally {
                query.setMBeanServer(oldMBS);
            }
            return instances;
        }
        public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
            Set<ObjectInstance> instances = queryMBeans(name, query);
            Set<ObjectName> names = newSet();
            for (ObjectInstance instance : instances)
                names.add(instance.getObjectName());
            return names;
        }
        public boolean isRegistered(ObjectName name) {
            return mbeans.containsKey(name);
        }
        public Integer getMBeanCount() {
            return mbeans.size();
        }
        public Object getAttribute(ObjectName name, String attribute)
        throws MBeanException, AttributeNotFoundException,
                InstanceNotFoundException, ReflectionException {
            return getMBean(name).getAttribute(attribute);
        }
        public AttributeList getAttributes(ObjectName name, String[] attributes)
        throws InstanceNotFoundException, ReflectionException {
            return getMBean(name).getAttributes(attributes);
        }
        public void setAttribute(ObjectName name, Attribute attribute)
        throws InstanceNotFoundException, AttributeNotFoundException,
                InvalidAttributeValueException, MBeanException,
                ReflectionException {
            getMBean(name).setAttribute(attribute);
        }
        public AttributeList setAttributes(
                ObjectName name, AttributeList attributes)
        throws InstanceNotFoundException, ReflectionException {
            return getMBean(name).setAttributes(attributes);
        }
        public Object invoke(
                ObjectName name, String operationName, Object[] params,
                String[] signature)
        throws InstanceNotFoundException, MBeanException, ReflectionException {
            return getMBean(name).invoke(operationName, params, signature);
        }
        public String getDefaultDomain() {
            return defaultDomain;
        }
        public String[] getDomains() {
            Set<String> domains = newSet();
            for (ObjectName name : mbeans.keySet())
                domains.add(name.getDomain());
            return domains.toArray(new String[0]);
        }
        public void addNotificationListener(
                ObjectName name, NotificationListener listener,
                NotificationFilter filter, Object handback)
                throws InstanceNotFoundException {
            NotificationBroadcaster userMBean =
                    (NotificationBroadcaster) getUserMBean(name);
            NotificationListener wrappedListener =
                  wrappedListener(name, userMBean, listener);
            userMBean.addNotificationListener(wrappedListener, filter, handback);
        }
        public void addNotificationListener(
                ObjectName name, ObjectName listener,
                NotificationFilter filter, Object handback)
                throws InstanceNotFoundException {
            NotificationListener nl =
                    (NotificationListener) getUserMBean(listener);
            addNotificationListener(name, nl, filter, handback);
        }
        public void removeNotificationListener(
                ObjectName name, ObjectName listener)
                throws InstanceNotFoundException, ListenerNotFoundException {
            NotificationListener nl =
                    (NotificationListener) getUserMBean(listener);
            removeNotificationListener(name, nl);
        }
        public void removeNotificationListener(
                ObjectName name, ObjectName listener,
                NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, ListenerNotFoundException {
            NotificationListener nl =
                    (NotificationListener) getUserMBean(listener);
            removeNotificationListener(name, nl, filter, handback);
        }
        public void removeNotificationListener(
                ObjectName name, NotificationListener listener)
                throws InstanceNotFoundException, ListenerNotFoundException {
            NotificationBroadcaster userMBean =
                    (NotificationBroadcaster) getUserMBean(name);
            NotificationListener wrappedListener =
                  wrappedListener(name, userMBean, listener);
            userMBean.removeNotificationListener(wrappedListener);
        }
        public void removeNotificationListener(
                ObjectName name, NotificationListener listener,
                NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, ListenerNotFoundException {
            NotificationEmitter userMBean =
                    (NotificationEmitter) getMBean(name);
            NotificationListener wrappedListener =
                  wrappedListener(name, userMBean, listener);
            userMBean.removeNotificationListener(wrappedListener, filter, handback);
        }
        public MBeanInfo getMBeanInfo(ObjectName name)
        throws InstanceNotFoundException, IntrospectionException,
                ReflectionException {
            return getMBean(name).getMBeanInfo();
        }
        public boolean isInstanceOf(ObjectName name, String className)
        throws InstanceNotFoundException {
            DynamicMBean mbean = getMBean(name);
            String mbeanClassName = mbean.getMBeanInfo().getClassName();
            if (className.equals(mbeanClassName))
                return true;
            ClassLoader loader = getUserMBean(mbean).getClass().getClassLoader();
            try {
                Class<?> mbeanClass = Class.forName(mbeanClassName, false, loader);
                Class<?> isInstClass = Class.forName(className, false, loader);
                return isInstClass.isAssignableFrom(mbeanClass);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        public Object instantiate(String className)
        throws ReflectionException, MBeanException {
            return instantiate(className, null, null);
        }
        public Object instantiate(String className, ObjectName loaderName)
        throws ReflectionException, MBeanException, InstanceNotFoundException {
            return instantiate(className, loaderName, null, null);
        }
        public Object instantiate(
                String className, Object[] params, String[] signature)
        throws ReflectionException, MBeanException {
            try {
                return instantiate(className, clrName, params, signature);
            } catch (InstanceNotFoundException e) {
                throw new RuntimeException(e);  
            }
        }
        public Object instantiate(
                String className, ObjectName loaderName,
                Object[] params, String[] signature)
        throws ReflectionException, MBeanException, InstanceNotFoundException {
            if (params == null)
                params = new Object[0];
            if (signature == null)
                signature = new String[0];
            ClassLoader loader;
            if (loaderName == null)
                loader = this.getClass().getClassLoader();
            else if (loaderName.equals(clrName))
                loader = clr;
            else
                loader = (ClassLoader) getMBean(loaderName);
            Class<?> c;
            try {
                c = Class.forName(className, false, loader);
            } catch (ClassNotFoundException e) {
                throw new ReflectionException(e);
            }
            Constructor[] constrs = c.getConstructors();
            Constructor found = null;
            findconstr:
            for (Constructor constr : constrs) {
                Class<?>[] cTypes = constr.getParameterTypes();
                if (cTypes.length == signature.length) {
                    for (int i = 0; i < cTypes.length; i++) {
                        if (!cTypes[i].getName().equals(signature[i]))
                            continue findconstr;
                    }
                    found = constr;
                    break findconstr;
                }
            }
            if (found == null) {
                Exception x = new NoSuchMethodException(
                        className + Arrays.toString(signature));
                throw new ReflectionException(x);
            }
            return invokeSomething(found, null, params);
        }
        @Deprecated
        public ObjectInputStream deserialize(ObjectName name, byte[] data)
        throws InstanceNotFoundException, OperationsException {
            throw new UnsupportedOperationException();
        }
        @Deprecated
        public ObjectInputStream deserialize(String className, byte[] data)
        throws OperationsException, ReflectionException {
            throw new UnsupportedOperationException();
        }
        @Deprecated
        public ObjectInputStream deserialize(
                String className, ObjectName loaderName, byte[] data)
        throws InstanceNotFoundException, OperationsException, ReflectionException {
            throw new UnsupportedOperationException();
        }
        public ClassLoader getClassLoaderFor(ObjectName mbeanName)
        throws InstanceNotFoundException {
            DynamicMBean mbean = getMBean(mbeanName);
            Object userMBean = getUserMBean(mbean);
            return userMBean.getClass().getClassLoader();
        }
        public ClassLoader getClassLoader(ObjectName loaderName)
        throws InstanceNotFoundException {
            return (ClassLoader) getMBean(loaderName);
        }
        public ClassLoaderRepository getClassLoaderRepository() {
            return new ClassLoaderRepository() {
                public Class<?> loadClass(String className)
                throws ClassNotFoundException {
                    return clr.loadClass(className);
                }
                public Class<?> loadClassWithout(
                        ClassLoader exclude, String className)
                throws ClassNotFoundException {
                    return clr.loadClassWithout(exclude, className);
                }
                public Class<?> loadClassBefore(
                        ClassLoader stop, String className)
                throws ClassNotFoundException {
                    return clr.loadClassBefore(stop, className);
                }
            };
        }
        private static class ClassLoaderRepositoryImpl
                extends ClassLoader implements ClassLoaderRepository {
            private List<ClassLoader> loaders = newList();
            {
                loaders.add(this.getClass().getClassLoader());
            }
            void addLoader(ClassLoader loader) {
                loaders.add(loader);
            }
            void removeLoader(ClassLoader loader) {
                if (!loaders.remove(loader))
                    throw new RuntimeException("Loader was not in CLR!");
            }
            public Class<?> loadClassWithout(
                    ClassLoader exclude, String className)
                    throws ClassNotFoundException {
                return loadClassWithoutBefore(exclude, null, className);
            }
            public Class<?> loadClassBefore(ClassLoader stop, String className)
            throws ClassNotFoundException {
                return loadClassWithoutBefore(null, stop, className);
            }
            private Class<?> loadClassWithoutBefore(
                    ClassLoader exclude, ClassLoader stop, String className)
                    throws ClassNotFoundException {
                for (ClassLoader loader : loaders) {
                    if (loader == exclude)
                        continue;
                    if (loader == stop)
                        break;
                    try {
                        return Class.forName(className, false, loader);
                    } catch (ClassNotFoundException e) {
                    }
                }
                throw new ClassNotFoundException(className);
            }
            @Override
            protected Class<?> findClass(String className)
            throws ClassNotFoundException {
                return loadClassWithout(null, className);
            }
        }
        private static class ListenerTable
                extends WeakHashMap<NotificationListener,
                                    WeakReference<NotificationListener>> {
        }
        private static class IdentityListener implements NotificationListener {
            private final NotificationListener userListener;
            IdentityListener(NotificationListener userListener) {
                this.userListener = userListener;
            }
            public void handleNotification(
                    Notification notification, Object handback) {
                userListener.handleNotification(notification, handback);
            }
            @Override
            public boolean equals(Object o) {
                return (this == o);
            }
            @Override
            public int hashCode() {
                return System.identityHashCode(this);
            }
        }
        private static class RewriteListener implements NotificationListener {
            private final ObjectName name;
            private final Object userMBean;
            private final NotificationListener userListener;
            RewriteListener(
                    ObjectName name, Object userMBean,
                    NotificationListener userListener) {
                this.name = name;
                this.userMBean = userMBean;
                this.userListener = userListener;
            }
            public void handleNotification(
                    Notification notification, Object handback) {
                if (notification.getSource() == userMBean)
                    notification.setSource(name);
                userListener.handleNotification(notification, handback);
            }
        }
        private NotificationListener wrappedListener(
                ObjectName name, Object userMBean, NotificationListener userListener)
        throws InstanceNotFoundException {
            ListenerTable table = new ListenerTable();
            ListenerTable oldTable = listenerMap.putIfAbsent(name, table);
            if (oldTable != null)
                table = oldTable;
            NotificationListener identityListener =
                    new IdentityListener(userListener);
            synchronized (table) {
                NotificationListener rewriteListener = null;
                WeakReference<NotificationListener> wr =
                        table.get(identityListener);
                if (wr != null)
                    rewriteListener = wr.get();
                if (rewriteListener == null) {
                    rewriteListener = new RewriteListener(
                            name, userMBean, identityListener);
                    wr = new WeakReference<NotificationListener>(rewriteListener);
                    table.put(identityListener, wr);
                }
                return rewriteListener;
            }
        }
        private DynamicMBean getMBean(ObjectName name)
        throws InstanceNotFoundException {
            DynamicMBean mbean = mbeans.get(name);
            if (mbean == null)
                throw new InstanceNotFoundException(name.toString());
            return mbean;
        }
        private static interface WrapDynamicMBean extends DynamicMBean {
            public Object getWrappedMBean();
        }
        private static class StandardWrapper
                implements WrapDynamicMBean, MBeanRegistration {
            private final Map<String, AttrMethods> attrMap = newMap();
            private final Map<String, List<Method>> opMap = newMap();
            private static class AttrMethods {
                Method getter, setter;
            }
            private final Object std;
            StandardWrapper(Object std) throws NotCompliantMBeanException {
                this.std = std;
                Class<?> intf = mbeanInterface(std.getClass());
                try {
                    initMaps(intf);
                } catch (NotCompliantMBeanException e) {
                    throw e;
                } catch (Exception e) {
                    NotCompliantMBeanException x =
                            new NotCompliantMBeanException(e.getMessage());
                    x.initCause(e);
                    throw x;
                }
            }
            private static Class<?> mbeanInterface(Class<?> c)
            throws NotCompliantMBeanException {
                do {
                    Class<?>[] intfs = c.getInterfaces();
                    String intfName = c.getName() + "MBean";
                    for (Class<?> intf : intfs) {
                        if (intf.getName().equals(intfName))
                            return intf;
                    }
                    c = c.getSuperclass();
                } while (c != null);
                throw new NotCompliantMBeanException(
                        "Does not match Standard or Dynamic MBean patterns: " +
                        c.getName());
            }
            private void initMaps(Class<?> intf) throws NotCompliantMBeanException {
                Method[] methods = intf.getMethods();
                for (Method m : methods) {
                    final String name = m.getName();
                    final int nParams = m.getParameterTypes().length;
                    String attrName = "";
                    if (name.startsWith("get"))
                        attrName = name.substring(3);
                    else if (name.startsWith("is")
                    && m.getReturnType() == boolean.class)
                        attrName = name.substring(2);
                    if (attrName.length() != 0 && m.getParameterTypes().length == 0
                            && m.getReturnType() != void.class) {
                        AttrMethods am = attrMap.get(attrName);
                        if (am == null)
                            am = new AttrMethods();
                        else {
                            if (am.getter != null) {
                                final String msg = "Attribute " + attrName +
                                        " has more than one getter";
                                throw new NotCompliantMBeanException(msg);
                            }
                        }
                        am.getter = m;
                        attrMap.put(attrName, am);
                    } else if (name.startsWith("set") && name.length() > 3
                            && m.getParameterTypes().length == 1 &&
                            m.getReturnType() == void.class) {
                        attrName = name.substring(3);
                        AttrMethods am = attrMap.get(attrName);
                        if (am == null)
                            am = new AttrMethods();
                        else if (am.setter != null) {
                            final String msg = "Attribute " + attrName +
                                    " has more than one setter";
                            throw new NotCompliantMBeanException(msg);
                        }
                        am.setter = m;
                        attrMap.put(attrName, am);
                    } else {
                        List<Method> ops = opMap.get(name);
                        if (ops == null)
                            ops = newList();
                        ops.add(m);
                        opMap.put(name, ops);
                    }
                }
                for (Map.Entry<String, AttrMethods> entry : attrMap.entrySet()) {
                    AttrMethods am = entry.getValue();
                    if (am.getter != null && am.setter != null &&
                            am.getter.getReturnType() != am.setter.getParameterTypes()[0]) {
                        final String msg = "Getter and setter for " + entry.getKey() +
                                " have inconsistent types";
                        throw new NotCompliantMBeanException(msg);
                    }
                }
            }
            public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
                AttrMethods am = attrMap.get(attribute);
                if (am == null || am.getter == null)
                    throw new AttributeNotFoundException(attribute);
                return invokeMethod(am.getter);
            }
            public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
                    MBeanException, ReflectionException {
                String name = attribute.getName();
                AttrMethods am = attrMap.get(name);
                if (am == null || am.setter == null)
                    throw new AttributeNotFoundException(name);
                invokeMethod(am.setter, attribute.getValue());
            }
            public AttributeList getAttributes(String[] attributes) {
                AttributeList list = new AttributeList();
                for (String attr : attributes) {
                    try {
                        list.add(new Attribute(attr, getAttribute(attr)));
                    } catch (Exception e) {
                    }
                }
                return list;
            }
            public AttributeList setAttributes(AttributeList attributes) {
                AttributeList list = new AttributeList();
                for (Iterator<?> it = attributes.iterator(); it.hasNext(); ) {
                    Attribute attr = (Attribute) it.next();
                    try {
                        setAttribute(attr);
                        list.add(attr);
                    } catch (Exception e) {
                    }
                }
                return list;
            }
            public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
                if (params == null)
                    params = new Object[0];
                if (signature == null)
                    signature = new String[0];
                List<Method> methods = opMap.get(actionName);
                if (methods == null) {
                    Exception x = new NoSuchMethodException(actionName);
                    throw new MBeanException(x);
                }
                Method found = null;
                methodloop:
                for (Method m : methods) {
                    Class<?>[] msig = m.getParameterTypes();
                    if (msig.length != signature.length)
                        continue methodloop;
                    for (int i = 0; i < msig.length; i++) {
                        if (!msig[i].getName().equals(signature[i]))
                            continue methodloop;
                    }
                    found = m;
                    break methodloop;
                }
                if (found == null) {
                    Exception x = new NoSuchMethodException(
                            actionName + Arrays.toString(signature));
                    throw new MBeanException(x);
                }
                return invokeMethod(found, params);
            }
            public MBeanInfo getMBeanInfo() {
                List<MBeanAttributeInfo> attrs = newList();
                for (Map.Entry<String, AttrMethods> attr : attrMap.entrySet()) {
                    String name = attr.getKey();
                    AttrMethods am = attr.getValue();
                    try {
                        attrs.add(new MBeanAttributeInfo(
                                name, name, am.getter, am.setter));
                    } catch (IntrospectionException e) { 
                        throw new RuntimeException(e);
                    }
                }
                List<MBeanOperationInfo> ops = newList();
                for (Map.Entry<String, List<Method>> op : opMap.entrySet()) {
                    String name = op.getKey();
                    List<Method> methods = op.getValue();
                    for (Method m : methods)
                        ops.add(new MBeanOperationInfo(name, m));
                }
                List<MBeanConstructorInfo> constrs = newList();
                for (Constructor constr : std.getClass().getConstructors())
                    constrs.add(new MBeanConstructorInfo("Constructor", constr));
                MBeanNotificationInfo[] notifs;
                if (std instanceof NotificationBroadcaster)
                    notifs = ((NotificationBroadcaster) std).getNotificationInfo();
                else
                    notifs = null;
                String className = std.getClass().getName();
                return new MBeanInfo(
                        className, className,
                        attrs.toArray(new MBeanAttributeInfo[0]),
                        constrs.toArray(new MBeanConstructorInfo[0]),
                        ops.toArray(new MBeanOperationInfo[0]),
                        notifs);
            }
            private Object invokeMethod(Method m, Object... args)
            throws MBeanException, ReflectionException {
                return invokeSomething(m, std,args);
            }
            public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
                return mbeanRegistration(std).preRegister(server, name);
            }
            public void postRegister(Boolean registrationDone) {
                mbeanRegistration(std).postRegister(registrationDone);
            }
            public void preDeregister() throws Exception {
                mbeanRegistration(std).preDeregister();
            }
            public void postDeregister() {
                mbeanRegistration(std).postDeregister();
            }
            public Object getWrappedMBean() {
                return std;
            }
        }
        private DynamicMBean standardToDynamic(Object std)
        throws NotCompliantMBeanException {
            return new StandardWrapper(std);
        }
        private static Object invokeSomething(
                AccessibleObject ao, Object target, Object[] args)
        throws MBeanException, ReflectionException {
            try {
                if (ao instanceof Method)
                    return ((Method) ao).invoke(target, args);
                else
                    return ((Constructor) ao).newInstance(args);
            } catch (InvocationTargetException e) {
                try {
                    throw e.getCause();
                } catch (RuntimeException x) {
                    throw new RuntimeMBeanException(x);
                } catch (Error x) {
                    throw new RuntimeErrorException(x);
                } catch (Exception x) {
                    throw new MBeanException(x);
                } catch (Throwable x) {
                    throw new RuntimeException(x); 
                }
            } catch (Exception e) {
                throw new ReflectionException(e);
            }
        }
        private static Object getUserMBean(DynamicMBean mbean) {
            if (mbean instanceof WrapDynamicMBean)
                return ((WrapDynamicMBean) mbean).getWrappedMBean();
            return mbean;
        }
        private Object getUserMBean(ObjectName name)
        throws InstanceNotFoundException {
            return getUserMBean(getMBean(name));
        }
        private static final MBeanRegistration noRegistration =
                new MBeanRegistration() {
            public ObjectName preRegister(MBeanServer server, ObjectName name) {
                return name;
            }
            public void postRegister(Boolean registrationDone) {
            }
            public void preDeregister() throws Exception {
            }
            public void postDeregister() {
            }
        };
        private static MBeanRegistration mbeanRegistration(Object object) {
            if (object instanceof MBeanRegistration)
                return (MBeanRegistration) object;
            else
                return noRegistration;
        }
        private static <E> List<E> newList() {
            return new ArrayList<E>();
        }
        private static <K, V> Map<K, V> newMap() {
            return new HashMap<K, V>();
        }
        private static <E> Set<E> newSet() {
            return new HashSet<E>();
        }
    }
}

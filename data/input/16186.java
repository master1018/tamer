public class ToolProvider {
    private static final String propertyName = "sun.tools.ToolProvider";
    private static final String loggerName   = "javax.tools";
    static <T> T trace(Level level, Object reason) {
        try {
            if (System.getProperty(propertyName) != null) {
                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                String method = "???";
                String cls = ToolProvider.class.getName();
                if (st.length > 2) {
                    StackTraceElement frame = st[2];
                    method = String.format((Locale)null, "%s(%s:%s)",
                                           frame.getMethodName(),
                                           frame.getFileName(),
                                           frame.getLineNumber());
                    cls = frame.getClassName();
                }
                Logger logger = Logger.getLogger(loggerName);
                if (reason instanceof Throwable) {
                    logger.logp(level, cls, method,
                                reason.getClass().getName(), (Throwable)reason);
                } else {
                    logger.logp(level, cls, method, String.valueOf(reason));
                }
            }
        } catch (SecurityException ex) {
            System.err.format((Locale)null, "%s: %s; %s%n",
                              ToolProvider.class.getName(),
                              reason,
                              ex.getLocalizedMessage());
        }
        return null;
    }
    private static final String defaultJavaCompilerName
        = "com.sun.tools.javac.api.JavacTool";
    public static JavaCompiler getSystemJavaCompiler() {
        return instance().getSystemTool(JavaCompiler.class, defaultJavaCompilerName);
    }
    public static ClassLoader getSystemToolClassLoader() {
        try {
            Class<? extends JavaCompiler> c =
                    instance().getSystemToolClass(JavaCompiler.class, defaultJavaCompilerName);
            return c.getClassLoader();
        } catch (Throwable e) {
            return trace(WARNING, e);
        }
    }
    private static ToolProvider instance;
    private static synchronized ToolProvider instance() {
        if (instance == null)
            instance = new ToolProvider();
        return instance;
    }
    private Map<String, Reference<Class<?>>> toolClasses = new HashMap<String, Reference<Class<?>>>();
    private Reference<ClassLoader> refToolClassLoader = null;
    private ToolProvider() { }
    private <T> T getSystemTool(Class<T> clazz, String name) {
        Class<? extends T> c = getSystemToolClass(clazz, name);
        try {
            return c.asSubclass(clazz).newInstance();
        } catch (Throwable e) {
            trace(WARNING, e);
            return null;
        }
    }
    private <T> Class<? extends T> getSystemToolClass(Class<T> clazz, String name) {
        Reference<Class<?>> refClass = toolClasses.get(name);
        Class<?> c = (refClass == null ? null : refClass.get());
        if (c == null) {
            try {
                c = findSystemToolClass(name);
            } catch (Throwable e) {
                return trace(WARNING, e);
            }
            toolClasses.put(name, new WeakReference<Class<?>>(c));
        }
        return c.asSubclass(clazz);
    }
    private static final String[] defaultToolsLocation = { "lib", "tools.jar" };
    private Class<?> findSystemToolClass(String toolClassName)
        throws MalformedURLException, ClassNotFoundException
    {
        try {
            return Class.forName(toolClassName, false, null);
        } catch (ClassNotFoundException e) {
            trace(FINE, e);
            ClassLoader cl = (refToolClassLoader == null ? null : refToolClassLoader.get());
            if (cl == null) {
                File file = new File(System.getProperty("java.home"));
                if (file.getName().equalsIgnoreCase("jre"))
                    file = file.getParentFile();
                for (String name : defaultToolsLocation)
                    file = new File(file, name);
                if (!file.exists())
                    throw e;
                URL[] urls = { file.toURI().toURL() };
                trace(FINE, urls[0].toString());
                cl = URLClassLoader.newInstance(urls);
                refToolClassLoader = new WeakReference<ClassLoader>(cl);
            }
            return Class.forName(toolClassName, false, cl);
        }
    }
}

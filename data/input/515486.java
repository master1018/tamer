public final class System {
    public static final InputStream in;
    public static final PrintStream out;
    public static final PrintStream err;
    private static Properties systemProperties;
    private static SecurityManager securityManager;
    static {
        err = new PrintStream(new FileOutputStream(FileDescriptor.err));
        out = new PrintStream(new FileOutputStream(FileDescriptor.out));
        in = new FileInputStream(FileDescriptor.in);
    }
    public static void setIn(InputStream newIn) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("in", "Ljava/io/InputStream;", newIn);
    }
    public static void setOut(java.io.PrintStream newOut) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("out", "Ljava/io/PrintStream;", newOut);
    }
    public static void setErr(java.io.PrintStream newErr) {
        SecurityManager secMgr = System.getSecurityManager();
        if(secMgr != null) {
            secMgr.checkPermission(RuntimePermission.permissionToSetIO);
        }
        setFieldImpl("err", "Ljava/io/PrintStream;", newErr);
    }
    private System() {
    }
    public static native void arraycopy(Object src, int srcPos, Object dest,
            int destPos, int length);
    public static native long currentTimeMillis();
    public static native long nanoTime();
    public static void exit(int code) {
        Runtime.getRuntime().exit(code);
    }
    public static void gc() {
        Runtime.getRuntime().gc();
    }
    public static String getenv(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv." + name));
        }
        return getEnvByName(name);
    }
    private static native String getEnvByName(String name);
    public static Map<String, String> getenv() {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new RuntimePermission("getenv.*"));
        }
        Map<String, String> map = new HashMap<String, String>();
        int index = 0;
        String entry = getEnvByIndex(index++);
        while (entry != null) {
            int pos = entry.indexOf('=');
            if (pos != -1) {
                map.put(entry.substring(0, pos), entry.substring(pos + 1));
            }
            entry = getEnvByIndex(index++);
        }
        return new SystemEnvironment(map);
    }
    private static native String getEnvByIndex(int index);
    public static Channel inheritedChannel() throws IOException {
        return SelectorProvider.provider().inheritedChannel();
    }
    public static Properties getProperties() {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertiesAccess();
        }
        return internalGetProperties();
    }
    static Properties internalGetProperties() {
        if (System.systemProperties == null) {
            SystemProperties props = new SystemProperties();
            props.preInit();
            props.postInit();
            System.systemProperties = props;
        }
        return systemProperties;
    }
    public static String getProperty(String prop) {
        return getProperty(prop, null);
    }
    public static String getProperty(String prop, String defaultValue) {
        if (prop.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertyAccess(prop);
        }
        return internalGetProperties().getProperty(prop, defaultValue);
    }
    public static String setProperty(String prop, String value) {
        if (prop.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new PropertyPermission(prop, "write"));
        }
        return (String)internalGetProperties().setProperty(prop, value);
    }
    public static String clearProperty(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException();
        }
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPermission(new PropertyPermission(key, "write"));
        }
        return (String)internalGetProperties().remove(key);
    }
    public static SecurityManager getSecurityManager() {
        return securityManager;
    }
    public static native int identityHashCode(Object anObject);
    public static void load(String pathName) {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkLink(pathName);
        }
        Runtime.getRuntime().load(pathName, VMStack.getCallingClassLoader());
    }
    public static void loadLibrary(String libName) {
        SecurityManager smngr = System.getSecurityManager();
        if (smngr != null) {
            smngr.checkLink(libName);
        }
        Runtime.getRuntime().loadLibrary(libName, VMStack.getCallingClassLoader());
    }
    public static void runFinalization() {
        Runtime.getRuntime().runFinalization();
    }
    @SuppressWarnings("deprecation")
    @Deprecated
    public static void runFinalizersOnExit(boolean flag) {
        Runtime.runFinalizersOnExit(flag);
    }
    public static void setProperties(Properties p) {
        SecurityManager secMgr = System.getSecurityManager();
        if (secMgr != null) {
            secMgr.checkPropertiesAccess();
        }
        systemProperties = p;
    }
    public static void setSecurityManager(final SecurityManager sm) {
        if (securityManager != null) {
            securityManager.checkPermission(new java.lang.RuntimePermission("setSecurityManager"));
        }
        if (sm != null) {
            try {
                sm.checkPermission(new SecurityPermission("getProperty.package.access"));
            } catch (Exception ignore) {
            }
            try {
                sm.checkPackageAccess("java.lang");
            } catch (Exception ignore) {
            }
        }
        securityManager = sm;
    }
    public static native String mapLibraryName(String userLibName);
    private static native void setFieldImpl(String fieldName, String signature, Object stream);
}
class SystemProperties extends Properties {
    native void preInit();
    native void postInit();
}
class SystemEnvironment implements Map {
    private Map<String, String> map;
    public SystemEnvironment(Map<String, String> map) {
        this.map = map;
    }
    public void clear() {
        throw new UnsupportedOperationException("Can't modify environment");
    }
    @SuppressWarnings("cast")
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return map.containsKey((String)key);
    }
    @SuppressWarnings("cast")
    public boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return map.containsValue((String)value);
    }
    public Set entrySet() {
        return map.entrySet();
    }
    @SuppressWarnings("cast")
    public String get(Object key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return map.get((String)key);
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }
    public Set<String> keySet() {
        return map.keySet();
    }
    public String put(Object key, Object value) {
        throw new UnsupportedOperationException("Can't modify environment");
    }
    public void putAll(Map map) {
        throw new UnsupportedOperationException("Can't modify environment");
    }
    public String remove(Object key) {
        throw new UnsupportedOperationException("Can't modify environment");
    }
    public int size() {
        return map.size();
    }
    public Collection values() {
        return map.values();
    }
}

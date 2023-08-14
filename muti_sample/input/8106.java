public class JDKBridge {
    public static String getLocalCodebase () {
        return localCodebase;
    }
    public static boolean useCodebaseOnly () {
        return useCodebaseOnly;
    }
    public static Class loadClass (String className,
                                   String remoteCodebase,
                                   ClassLoader loader)
        throws ClassNotFoundException {
        if (loader == null) {
            return loadClassM(className,remoteCodebase,useCodebaseOnly);
        } else {
            try {
                return loadClassM(className,remoteCodebase,useCodebaseOnly);
            } catch (ClassNotFoundException e) {
                return loader.loadClass(className);
            }
        }
    }
    public static Class loadClass (String className,
                                   String remoteCodebase)
        throws ClassNotFoundException {
        return loadClass(className,remoteCodebase,null);
    }
    public static Class loadClass (String className)
        throws ClassNotFoundException {
        return loadClass(className,null,null);
    }
    private static final String LOCAL_CODEBASE_KEY = "java.rmi.server.codebase";
    private static final String USE_CODEBASE_ONLY_KEY = "java.rmi.server.useCodebaseOnly";
    private static String localCodebase = null;
    private static boolean useCodebaseOnly;
    static {
        setCodebaseProperties();
    }
    public static final void main (String[] args) {
        System.out.println("1.2 VM");
    }
    public static synchronized void setCodebaseProperties () {
        String prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(LOCAL_CODEBASE_KEY)
        );
        if (prop != null && prop.trim().length() > 0) {
            localCodebase = prop;
        }
        prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(USE_CODEBASE_ONLY_KEY)
        );
        if (prop != null && prop.trim().length() > 0) {
            useCodebaseOnly = Boolean.valueOf(prop).booleanValue();
        }
    }
    public static synchronized void setLocalCodebase(String codebase) {
        localCodebase = codebase;
    }
    private static Class loadClassM (String className,
                            String remoteCodebase,
                            boolean useCodebaseOnly)
        throws ClassNotFoundException {
        try {
            return JDKClassLoader.loadClass(null,className);
        } catch (ClassNotFoundException e) {}
        try {
            if (!useCodebaseOnly && remoteCodebase != null) {
                return RMIClassLoader.loadClass(remoteCodebase,
                                                className);
            } else {
                return RMIClassLoader.loadClass(className);
            }
        } catch (MalformedURLException e) {
            className = className + ": " + e.toString();
        }
        throw new ClassNotFoundException(className);
    }
}

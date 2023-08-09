final class ProviderConfig {
    private final static sun.security.util.Debug debug =
        sun.security.util.Debug.getInstance("jca", "ProviderConfig");
    private static final String P11_SOL_NAME =
        "sun.security.pkcs11.SunPKCS11";
    private static final String P11_SOL_ARG  =
        "${java.home}/lib/security/sunpkcs11-solaris.cfg";
    private final static int MAX_LOAD_TRIES = 30;
    private final static Class[] CL_STRING = { String.class };
    private final String className;
    private final String argument;
    private int tries;
    private volatile Provider provider;
    private boolean isLoading;
    ProviderConfig(String className, String argument) {
        if (className.equals(P11_SOL_NAME) && argument.equals(P11_SOL_ARG)) {
            checkSunPKCS11Solaris();
        }
        this.className = className;
        this.argument = expand(argument);
    }
    ProviderConfig(String className) {
        this(className, "");
    }
    ProviderConfig(Provider provider) {
        this.className = provider.getClass().getName();
        this.argument = "";
        this.provider = provider;
    }
    private void checkSunPKCS11Solaris() {
        Boolean o = AccessController.doPrivileged(
                                new PrivilegedAction<Boolean>() {
            public Boolean run() {
                File file = new File("/usr/lib/libpkcs11.so");
                if (file.exists() == false) {
                    return Boolean.FALSE;
                }
                if ("false".equalsIgnoreCase(System.getProperty
                        ("sun.security.pkcs11.enable-solaris"))) {
                    return Boolean.FALSE;
                }
                return Boolean.TRUE;
            }
        });
        if (o == Boolean.FALSE) {
            tries = MAX_LOAD_TRIES;
        }
    }
    private boolean hasArgument() {
        return argument.length() != 0;
    }
    private boolean shouldLoad() {
        return (tries < MAX_LOAD_TRIES);
    }
    private void disableLoad() {
        tries = MAX_LOAD_TRIES;
    }
    boolean isLoaded() {
        return (provider != null);
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ProviderConfig == false) {
            return false;
        }
        ProviderConfig other = (ProviderConfig)obj;
        return this.className.equals(other.className)
            && this.argument.equals(other.argument);
    }
    public int hashCode() {
        return className.hashCode() + argument.hashCode();
    }
    public String toString() {
        if (hasArgument()) {
            return className + "('" + argument + "')";
        } else {
            return className;
        }
    }
    synchronized Provider getProvider() {
        Provider p = provider;
        if (p != null) {
            return p;
        }
        if (shouldLoad() == false) {
            return null;
        }
        if (isLoading) {
            if (debug != null) {
                debug.println("Recursion loading provider: " + this);
                new Exception("Call trace").printStackTrace();
            }
            return null;
        }
        try {
            isLoading = true;
            tries++;
            p = doLoadProvider();
        } finally {
            isLoading = false;
        }
        provider = p;
        return p;
    }
    private Provider doLoadProvider() {
        return AccessController.doPrivileged(new PrivilegedAction<Provider>() {
            public Provider run() {
                if (debug != null) {
                    debug.println("Loading provider: " + ProviderConfig.this);
                }
                try {
                    ClassLoader cl = ClassLoader.getSystemClassLoader();
                    Class<?> provClass;
                    if (cl != null) {
                        provClass = cl.loadClass(className);
                    } else {
                        provClass = Class.forName(className);
                    }
                    Object obj;
                    if (hasArgument() == false) {
                        obj = provClass.newInstance();
                    } else {
                        Constructor<?> cons = provClass.getConstructor(CL_STRING);
                        obj = cons.newInstance(argument);
                    }
                    if (obj instanceof Provider) {
                        if (debug != null) {
                            debug.println("Loaded provider " + obj);
                        }
                        return (Provider)obj;
                    } else {
                        if (debug != null) {
                            debug.println(className + " is not a provider");
                        }
                        disableLoad();
                        return null;
                    }
                } catch (Exception e) {
                    Throwable t;
                    if (e instanceof InvocationTargetException) {
                        t = ((InvocationTargetException)e).getCause();
                    } else {
                        t = e;
                    }
                    if (debug != null) {
                        debug.println("Error loading provider " + ProviderConfig.this);
                        t.printStackTrace();
                    }
                    if (t instanceof ProviderException) {
                        throw (ProviderException)t;
                    }
                    if (t instanceof UnsupportedOperationException) {
                        disableLoad();
                    }
                    return null;
                }
            }
        });
    }
    private static String expand(final String value) {
        if (value.contains("${") == false) {
            return value;
        }
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                try {
                    return PropertyExpander.expand(value);
                } catch (GeneralSecurityException e) {
                    throw new ProviderException(e);
                }
            }
        });
    }
}

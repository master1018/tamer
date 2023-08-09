class JSSecurityManager {
    private JSSecurityManager() {
    }
    private static boolean hasSecurityManager() {
        return (System.getSecurityManager() != null);
    }
    static void checkRecordPermission() throws SecurityException {
        if(Printer.trace) Printer.trace("JSSecurityManager.checkRecordPermission()");
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new AudioPermission("record"));
        }
    }
    static void loadLibrary(final String libName) {
        try {
            if (hasSecurityManager()) {
                if(Printer.debug) Printer.debug("using security manager to load library");
                PrivilegedAction action = new PrivilegedAction() {
                        public Object run() {
                            System.loadLibrary(libName);
                            return null;
                        }
                    };
                AccessController.doPrivileged(action);
            } else {
                if(Printer.debug) Printer.debug("not using security manager to load library");
                System.loadLibrary(libName);
            }
            if (Printer.debug) Printer.debug("loaded library " + libName);
        } catch (UnsatisfiedLinkError e2) {
            if (Printer.err)Printer.err("UnsatisfiedLinkError loading native library " + libName);
            throw(e2);
        }
    }
    static String getProperty(final String propertyName) {
        String propertyValue;
        if (hasSecurityManager()) {
            if(Printer.debug) Printer.debug("using JDK 1.2 security to get property");
            try{
                PrivilegedAction action = new PrivilegedAction() {
                        public Object run() {
                            try {
                                return System.getProperty(propertyName);
                            } catch (Throwable t) {
                                return null;
                            }
                        }
                    };
                propertyValue = (String) AccessController.doPrivileged(action);
            } catch( Exception e ) {
                if(Printer.debug) Printer.debug("not using JDK 1.2 security to get properties");
                propertyValue = System.getProperty(propertyName);
            }
        } else {
            if(Printer.debug) Printer.debug("not using JDK 1.2 security to get properties");
            propertyValue = System.getProperty(propertyName);
        }
        return propertyValue;
    }
    static void loadProperties(final Properties properties,
                               final String filename) {
        if(hasSecurityManager()) {
            try {
                PrivilegedAction action = new PrivilegedAction() {
                        public Object run() {
                            loadPropertiesImpl(properties, filename);
                            return null;
                        }
                    };
                AccessController.doPrivileged(action);
                if(Printer.debug)Printer.debug("Loaded properties with JDK 1.2 security");
            } catch (Exception e) {
                if(Printer.debug)Printer.debug("Exception loading properties with JDK 1.2 security");
                loadPropertiesImpl(properties, filename);
            }
        } else {
            loadPropertiesImpl(properties, filename);
        }
    }
    private static void loadPropertiesImpl(Properties properties,
                                           String filename) {
        if(Printer.trace)Printer.trace(">> JSSecurityManager: loadPropertiesImpl()");
        String fname = System.getProperty("java.home");
        try {
            if (fname == null) {
                throw new Error("Can't find java.home ??");
            }
            File f = new File(fname, "lib");
            f = new File(f, filename);
            fname = f.getCanonicalPath();
            InputStream in = new FileInputStream(fname);
            BufferedInputStream bin = new BufferedInputStream(in);
            try {
                properties.load(bin);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        } catch (Throwable t) {
            if (Printer.trace) {
                System.err.println("Could not load properties file \"" + fname + "\"");
                t.printStackTrace();
            }
        }
        if(Printer.trace)Printer.trace("<< JSSecurityManager: loadPropertiesImpl() completed");
    }
    private static ThreadGroup getTopmostThreadGroup() {
        ThreadGroup topmostThreadGroup;
        if(hasSecurityManager()) {
            try {
                PrivilegedAction action = new PrivilegedAction() {
                        public Object run() {
                            try {
                                return getTopmostThreadGroupImpl();
                            } catch (Throwable t) {
                                return null;
                            }
                        }
                    };
                topmostThreadGroup = (ThreadGroup) AccessController.doPrivileged(action);
                if(Printer.debug)Printer.debug("Got topmost thread group with JDK 1.2 security");
            } catch (Exception e) {
                if(Printer.debug)Printer.debug("Exception getting topmost thread group with JDK 1.2 security");
                topmostThreadGroup = getTopmostThreadGroupImpl();
            }
        } else {
            topmostThreadGroup = getTopmostThreadGroupImpl();
        }
        return topmostThreadGroup;
    }
    private static ThreadGroup getTopmostThreadGroupImpl() {
        if(Printer.trace)Printer.trace(">> JSSecurityManager: getTopmostThreadGroupImpl()");
        ThreadGroup g = Thread.currentThread().getThreadGroup();
        while ((g.getParent() != null) && (g.getParent().getParent() != null)) {
            g = g.getParent();
        }
        if(Printer.trace)Printer.trace("<< JSSecurityManager: getTopmostThreadGroupImpl() completed");
        return g;
    }
    static Thread createThread(final Runnable runnable,
                               final String threadName,
                               final boolean isDaemon, final int priority,
                               final boolean doStart) {
        Thread thread = null;
        if(hasSecurityManager()) {
            PrivilegedAction action = new PrivilegedAction() {
                    public Object run() {
                        try {
                            return createThreadImpl(runnable, threadName,
                                                    isDaemon, priority,
                                                    doStart);
                        } catch (Throwable t) {
                            return null;
                        }
                    }
                };
            thread = (Thread) AccessController.doPrivileged(action);
            if(Printer.debug) Printer.debug("created thread with JDK 1.2 security");
        } else {
            if(Printer.debug)Printer.debug("not using JDK 1.2 security");
            thread = createThreadImpl(runnable, threadName, isDaemon, priority,
                                      doStart);
        }
        return thread;
    }
    private static Thread createThreadImpl(Runnable runnable,
                                           String threadName,
                                           boolean isDaemon, int priority,
                                           boolean doStart) {
        ThreadGroup threadGroup = getTopmostThreadGroupImpl();
        Thread thread = new Thread(threadGroup, runnable);
        if (threadName != null) {
            thread.setName(threadName);
        }
        thread.setDaemon(isDaemon);
        if (priority >= 0) {
            thread.setPriority(priority);
        }
        if (doStart) {
            thread.start();
        }
        return thread;
    }
    static List getProviders(final Class providerClass) {
        List p = new ArrayList();
        final Iterator ps = Service.providers(providerClass);
        PrivilegedAction<Boolean> hasNextAction = new PrivilegedAction<Boolean>() {
            public Boolean run() {
                return ps.hasNext();
            }
        };
        while (AccessController.doPrivileged(hasNextAction)) {
            try {
                Object provider = ps.next();
                if (providerClass.isInstance(provider)) {
                    p.add(0, provider);
                }
            } catch (Throwable t) {
                if (Printer.err) t.printStackTrace();
            }
        }
        return p;
    }
}

class AppletSecurity extends AWTSecurityManager {
    private AppContext mainAppContext;
    private static Field facc = null;
    private static Field fcontext = null;
    static {
        try {
            facc = URLClassLoader.class.getDeclaredField("acc");
            facc.setAccessible(true);
            fcontext = AccessControlContext.class.getDeclaredField("context");
            fcontext.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new UnsupportedOperationException(e);
        }
    }
    public AppletSecurity() {
        reset();
        mainAppContext = AppContext.getAppContext();
    }
    private HashSet restrictedPackages = new HashSet();
    public void reset()
    {
        restrictedPackages.clear();
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run()
            {
                Enumeration e = System.getProperties().propertyNames();
                while (e.hasMoreElements())
                {
                    String name = (String) e.nextElement();
                    if (name != null && name.startsWith("package.restrict.access."))
                    {
                        String value = System.getProperty(name);
                        if (value != null && value.equalsIgnoreCase("true"))
                        {
                            String pkg = name.substring(24);
                            restrictedPackages.add(pkg);
                        }
                    }
                }
                return null;
            }
        });
    }
    private AppletClassLoader currentAppletClassLoader()
    {
        ClassLoader loader = currentClassLoader();
        if ((loader == null) || (loader instanceof AppletClassLoader))
            return (AppletClassLoader)loader;
        Class[] context = getClassContext();
        for (int i = 0; i < context.length; i++) {
            loader = context[i].getClassLoader();
            if (loader instanceof AppletClassLoader)
                return (AppletClassLoader)loader;
        }
        for (int i = 0; i < context.length; i++) {
            final ClassLoader currentLoader = context[i].getClassLoader();
            if (currentLoader instanceof URLClassLoader) {
                loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        AccessControlContext acc = null;
                        ProtectionDomain[] pds = null;
                        try {
                            acc = (AccessControlContext) facc.get(currentLoader);
                            if (acc == null) {
                                return null;
                            }
                            pds = (ProtectionDomain[]) fcontext.get(acc);
                            if (pds == null) {
                                return null;
                            }
                        } catch (Exception e) {
                            throw new UnsupportedOperationException(e);
                        }
                        for (int i=0; i<pds.length; i++) {
                            ClassLoader cl = pds[i].getClassLoader();
                            if (cl instanceof AppletClassLoader) {
                                    return cl;
                            }
                        }
                        return null;
                    }
                });
                if (loader != null) {
                    return (AppletClassLoader) loader;
                }
            }
        }
        loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof AppletClassLoader)
            return (AppletClassLoader)loader;
        return (AppletClassLoader)null;
    }
    protected boolean inThreadGroup(ThreadGroup g) {
        if (currentAppletClassLoader() == null)
            return false;
        else
            return getThreadGroup().parentOf(g);
    }
    protected boolean inThreadGroup(Thread thread) {
        return inThreadGroup(thread.getThreadGroup());
    }
    public void checkAccess(Thread t) {
        if ((t.getState() != Thread.State.TERMINATED) && !inThreadGroup(t)) {
            checkPermission(SecurityConstants.MODIFY_THREAD_PERMISSION);
        }
    }
    private boolean inThreadGroupCheck = false;
    public synchronized void checkAccess(ThreadGroup g) {
        if (inThreadGroupCheck) {
            checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        } else {
            try {
                inThreadGroupCheck = true;
                if (!inThreadGroup(g)) {
                    checkPermission(SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
                }
            } finally {
                inThreadGroupCheck = false;
            }
        }
    }
    public void checkPackageAccess(final String pkgname) {
        super.checkPackageAccess(pkgname);
        for (Iterator iter = restrictedPackages.iterator(); iter.hasNext();)
        {
            String pkg = (String) iter.next();
            if (pkgname.equals(pkg) || pkgname.startsWith(pkg + "."))
            {
                checkPermission(new java.lang.RuntimePermission
                            ("accessClassInPackage." + pkgname));
            }
        }
    }
    public void checkAwtEventQueueAccess() {
        AppContext appContext = AppContext.getAppContext();
        AppletClassLoader appletClassLoader = currentAppletClassLoader();
        if ((appContext == mainAppContext) && (appletClassLoader != null)) {
            super.checkAwtEventQueueAccess();
        }
    } 
    public ThreadGroup getThreadGroup() {
        AppletClassLoader appletLoader = currentAppletClassLoader();
        ThreadGroup loaderGroup = (appletLoader == null) ? null
                                          : appletLoader.getThreadGroup();
        if (loaderGroup != null) {
            return loaderGroup;
        } else {
            return super.getThreadGroup();
        }
    } 
    public AppContext getAppContext() {
        AppletClassLoader appletLoader = currentAppletClassLoader();
        if (appletLoader == null) {
            return null;
        } else {
            AppContext context =  appletLoader.getAppContext();
            if (context == null) {
                throw new SecurityException("Applet classloader has invalid AppContext");
            }
            return context;
        }
    }
} 

public class SecurityManager {
    private static final PropertyPermission READ_WRITE_ALL_PROPERTIES_PERMISSION = new PropertyPermission(
            "*", "read,write"); 
    private static final String PKG_ACC_KEY = "package.access"; 
    private static final String PKG_DEF_KEY = "package.definition"; 
    @Deprecated
    protected boolean inCheck;
    public SecurityManager() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security
                    .checkPermission(RuntimePermission.permissionToCreateSecurityManager);
        }
        Class<?> type = Security.class; 
        if (type == null) {
            throw new AssertionError();
        }
    }
    public void checkAccept(String host, int port) {
        if (host == null) {
            throw new NullPointerException();
        }
        checkPermission(new SocketPermission(host + ':' + port, "accept")); 
    }
    public void checkAccess(Thread thread) {
        ThreadGroup group = thread.getThreadGroup();
        if ((group != null) && (group.parent == null)) {
            checkPermission(RuntimePermission.permissionToModifyThread);
        }
    }
    public void checkAccess(ThreadGroup group) {
        if (group == null) {
            throw new NullPointerException();
        }
        if (group.parent == null) {
            checkPermission(RuntimePermission.permissionToModifyThreadGroup);
        }
    }
    public void checkConnect(String host, int port) {
        if (host == null) {
            throw new NullPointerException();
        }
        if (port > 0) {
            checkPermission(new SocketPermission(host + ':' + port, "connect")); 
        } else {
            checkPermission(new SocketPermission(host, "resolve")); 
        }
    }
    public void checkConnect(String host, int port, Object context) {
        if (host == null) {
            throw new NullPointerException();
        }
        if (port > 0) {
            checkPermission(new SocketPermission(host + ':' + port, "connect"), 
                    context);
        } else {
            checkPermission(new SocketPermission(host, "resolve"), context); 
        }
    }
    public void checkCreateClassLoader() {
        checkPermission(RuntimePermission.permissionToCreateClassLoader);
    }
    public void checkDelete(String file) {
        checkPermission(new FilePermission(file, "delete")); 
    }
    public void checkExec(String cmd) {
        checkPermission(new FilePermission(new File(cmd).isAbsolute() ? cmd
                : "<<ALL FILES>>", "execute")); 
    }
    public void checkExit(int status) {
        checkPermission(RuntimePermission.permissionToExitVM);
    }
    public void checkLink(String libName) {
        if (libName == null) {
            throw new NullPointerException();
        }
        checkPermission(new RuntimePermission("loadLibrary." + libName)); 
    }
    public void checkListen(int port) {
        if (port == 0) {
            checkPermission(new SocketPermission("localhost:1024-", "listen")); 
        } else {
            checkPermission(new SocketPermission("localhost:" + port, "listen")); 
        }
    }
    public void checkMemberAccess(Class<?> cls, int type) {
        if (cls == null) {
            throw new NullPointerException();
        }
        if (type == Member.PUBLIC) {
            return;
        }
        if (ClassLoader.getStackClassLoader(3) == cls.getClassLoaderImpl()) {
            return;
        }
        checkPermission(new RuntimePermission("accessDeclaredMembers")); 
    }
    public void checkMulticast(InetAddress maddr) {
        checkPermission(new SocketPermission(maddr.getHostAddress(),
                "accept,connect")); 
    }
    @Deprecated
    public void checkMulticast(InetAddress maddr, byte ttl) {
        checkPermission(new SocketPermission(maddr.getHostAddress(),
                "accept,connect")); 
    }
    public void checkPackageAccess(String packageName) {
        if (packageName == null) {
            throw new NullPointerException();
        }
        if (checkPackageProperty(PKG_ACC_KEY, packageName)) {
            checkPermission(new RuntimePermission("accessClassInPackage." 
                    + packageName));
        }
    }
    public void checkPackageDefinition(String packageName) {
        if (packageName == null) {
            throw new NullPointerException();
        }
        if (checkPackageProperty(PKG_DEF_KEY, packageName)) {
            checkPermission(new RuntimePermission("defineClassInPackage." 
                    + packageName));
        }
    }
    private static boolean checkPackageProperty(final String property,
            final String pkg) {
        String list = AccessController.doPrivileged(PriviAction
                .getSecurityProperty(property));
        if (list != null) {
            int plen = pkg.length();
            String[] tokens = list.split(", *"); 
            for (String token : tokens) {
                int tlen = token.length();
                if (plen > tlen
                        && pkg.startsWith(token)
                        && (token.charAt(tlen - 1) == '.' || pkg.charAt(tlen) == '.')) {
                    return true;
                } else if (plen == tlen && token.startsWith(pkg)) {
                    return true;
                } else if (plen + 1 == tlen && token.startsWith(pkg)
                        && token.charAt(tlen - 1) == '.') {
                    return true;
                }
            }
        }
        return false;
    }
    public void checkPropertiesAccess() {
        checkPermission(READ_WRITE_ALL_PROPERTIES_PERMISSION);
    }
    public void checkPropertyAccess(String key) {
        checkPermission(new PropertyPermission(key, "read")); 
    }
    public void checkRead(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException();
        }
        checkPermission(RuntimePermission.permissionToReadFileDescriptor);
    }
    public void checkRead(String file) {
        checkPermission(new FilePermission(file, "read")); 
    }
    public void checkRead(String file, Object context) {
        checkPermission(new FilePermission(file, "read"), context); 
    }
    public void checkSecurityAccess(String target) {
        checkPermission(new SecurityPermission(target));
    }
    public void checkSetFactory() {
        checkPermission(RuntimePermission.permissionToSetFactory);
    }
    public boolean checkTopLevelWindow(Object window) {
        if (window == null) {
            throw new NullPointerException();
        }
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); 
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor
                    .newInstance("showWindowWithoutWarningBanner"); 
            checkPermission((Permission) perm);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }
    public void checkSystemClipboardAccess() {
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); 
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor.newInstance("accessClipboard"); 
            checkPermission((Permission) perm);
            return;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        throw new SecurityException();
    }
    public void checkAwtEventQueueAccess() {
        try {
            Class<?> awtPermission = Class.forName("java.awt.AWTPermission"); 
            Constructor<?> constructor = awtPermission
                    .getConstructor(String.class);
            Object perm = constructor.newInstance("accessEventQueue"); 
            checkPermission((Permission) perm);
            return;
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        throw new SecurityException();
    }
    public void checkPrintJobAccess() {
        checkPermission(RuntimePermission.permissionToQueuePrintJob);
    }
    public void checkWrite(FileDescriptor fd) {
        if (fd == null) {
            throw new NullPointerException();
        }
        checkPermission(RuntimePermission.permissionToWriteFileDescriptor);
    }
    public void checkWrite(String file) {
        checkPermission(new FilePermission(file, "write")); 
    }
    @Deprecated
    public boolean getInCheck() {
        return inCheck;
    }
    @SuppressWarnings("unchecked")
    protected Class[] getClassContext() {
        return VMStack.getClasses(-1, false);
    }
    @Deprecated
    protected ClassLoader currentClassLoader() {
        try {
            checkPermission(new AllPermission());
            return null;
        } catch (SecurityException ex) {
        }
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return cl;
            }
        }
        return null;
    }
    @Deprecated
    protected int classLoaderDepth() {
        try {
            checkPermission(new AllPermission());
            return -1;
        } catch (SecurityException ex) {
        }
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return i;
            }
        }
        return -1;
    }
    @Deprecated
    protected Class<?> currentLoadedClass() {
        try {
            checkPermission(new AllPermission());
            return null;
        } catch (SecurityException ex) {
        }
        Class<?>[] classes = Class.getStackClasses(-1, true);
        for (int i = 0; i < classes.length; i++) {
            ClassLoader cl = classes[i].getClassLoaderImpl();
            if (!cl.isSystemClassLoader()) {
                return classes[i];
            }
        }
        return null;
    }
    @Deprecated
    protected int classDepth(String name) {
        Class<?>[] classes = Class.getStackClasses(-1, false);
        for (int i = 0; i < classes.length; i++) {
            if (classes[i].getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }
    @Deprecated
    protected boolean inClass(String name) {
        return classDepth(name) != -1;
    }
    @Deprecated
    protected boolean inClassLoader() {
        return currentClassLoader() != null;
    }
    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }
    public Object getSecurityContext() {
        return AccessController.getContext();
    }
    public void checkPermission(Permission permission) {
        try {
            inCheck = true;
            AccessController.checkPermission(permission);
        } finally {
            inCheck = false;
        }
    }
    public void checkPermission(Permission permission, Object context) {
        try {
            inCheck = true;
            if (context instanceof AccessControlContext) {
                ((AccessControlContext) context).checkPermission(permission);
            } else {
                throw new SecurityException();
            }
        } finally {
            inCheck = false;
        }
    }
}

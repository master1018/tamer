class Trampoline {
    private static Object invoke(Method m, Object obj, Object[] params)
        throws InvocationTargetException, IllegalAccessException {
        return m.invoke(obj, params);
    }
}
public final class MethodUtil extends SecureClassLoader {
    private static String MISC_PKG = "sun.reflect.misc.";
    private static String TRAMPOLINE = MISC_PKG + "Trampoline";
    private static Method bounce = getTrampoline();
    private MethodUtil() {
        super();
    }
    public static Method getMethod(Class<?> cls, String name, Class[] args)
        throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(cls);
        return cls.getMethod(name, args);
    }
    public static Method[] getMethods(Class cls) {
        ReflectUtil.checkPackageAccess(cls);
        return cls.getMethods();
    }
     public static Method[] getPublicMethods(Class cls) {
        if (System.getSecurityManager() == null) {
            return cls.getMethods();
        }
        Map<Signature, Method> sigs = new HashMap<Signature, Method>();
        while (cls != null) {
            boolean done = getInternalPublicMethods(cls, sigs);
            if (done) {
                break;
            }
            getInterfaceMethods(cls, sigs);
            cls = cls.getSuperclass();
        }
        return sigs.values().toArray(new Method[sigs.size()]);
    }
    private static void getInterfaceMethods(Class cls,
                                            Map<Signature, Method> sigs) {
        Class[] intfs = cls.getInterfaces();
        for (int i=0; i < intfs.length; i++) {
            Class intf = intfs[i];
            boolean done = getInternalPublicMethods(intf, sigs);
            if (!done) {
                getInterfaceMethods(intf, sigs);
            }
        }
    }
    private static boolean getInternalPublicMethods(Class cls,
                                                    Map<Signature, Method> sigs) {
        Method[] methods = null;
        try {
            if (!Modifier.isPublic(cls.getModifiers())) {
                return false;
            }
            if (!ReflectUtil.isPackageAccessible(cls)) {
                return false;
            }
            methods = cls.getMethods();
        } catch (SecurityException se) {
            return false;
        }
        boolean done = true;
        for (int i=0; i < methods.length; i++) {
            Class dc = methods[i].getDeclaringClass();
            if (!Modifier.isPublic(dc.getModifiers())) {
                done = false;
                break;
            }
        }
        if (done) {
            for (int i=0; i < methods.length; i++) {
                addMethod(sigs, methods[i]);
            }
        } else {
            for (int i=0; i < methods.length; i++) {
                Class dc = methods[i].getDeclaringClass();
                if (cls.equals(dc)) {
                    addMethod(sigs, methods[i]);
                }
            }
        }
        return done;
    }
    private static void addMethod(Map<Signature, Method> sigs, Method method) {
        Signature signature = new Signature(method);
        if (!sigs.containsKey(signature)) {
            sigs.put(signature, method);
        } else if (!method.getDeclaringClass().isInterface()){
            Method old = sigs.get(signature);
            if (old.getDeclaringClass().isInterface()) {
                sigs.put(signature, method);
            }
        }
    }
    private static class Signature {
        private String methodName;
        private Class[] argClasses;
        private volatile int hashCode = 0;
        Signature(Method m) {
            this.methodName = m.getName();
            this.argClasses = m.getParameterTypes();
        }
        public boolean equals(Object o2) {
            if (this == o2) {
                return true;
            }
            Signature that = (Signature)o2;
            if (!(methodName.equals(that.methodName))) {
                return false;
            }
            if (argClasses.length != that.argClasses.length) {
                return false;
            }
            for (int i = 0; i < argClasses.length; i++) {
                if (!(argClasses[i] == that.argClasses[i])) {
                  return false;
                }
            }
            return true;
        }
        public int hashCode() {
            if (hashCode == 0) {
                int result = 17;
                result = 37 * result + methodName.hashCode();
                if (argClasses != null) {
                    for (int i = 0; i < argClasses.length; i++) {
                        result = 37 * result + ((argClasses[i] == null) ? 0 :
                            argClasses[i].hashCode());
                    }
                }
                hashCode = result;
            }
            return hashCode;
        }
    }
    public static Object invoke(Method m, Object obj, Object[] params)
        throws InvocationTargetException, IllegalAccessException {
        if (m.getDeclaringClass().equals(AccessController.class) ||
            m.getDeclaringClass().equals(Method.class))
            throw new InvocationTargetException(
                new UnsupportedOperationException("invocation not supported"));
        try {
            return bounce.invoke(null, new Object[] {m, obj, params});
        } catch (InvocationTargetException ie) {
            Throwable t = ie.getCause();
            if (t instanceof InvocationTargetException) {
                throw (InvocationTargetException)t;
            } else if (t instanceof IllegalAccessException) {
                throw (IllegalAccessException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else if (t instanceof Error) {
                throw (Error)t;
            } else {
                throw new Error("Unexpected invocation error", t);
            }
        } catch (IllegalAccessException iae) {
            throw new Error("Unexpected invocation error", iae);
        }
    }
    private static Method getTrampoline() {
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<Method>() {
                    public Method run() throws Exception {
                        Class<?> t = getTrampolineClass();
                        Class[] types = {
                            Method.class, Object.class, Object[].class
                        };
                        Method b = t.getDeclaredMethod("invoke", types);
                    ((AccessibleObject)b).setAccessible(true);
                    return b;
                }
            });
        } catch (Exception e) {
            throw new InternalError("bouncer cannot be found");
        }
    }
    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        ReflectUtil.checkPackageAccess(name);
        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException e) {
            }
            if (c == null) {
                c = getParent().loadClass(name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    protected Class findClass(final String name)
        throws ClassNotFoundException
    {
        if (!name.startsWith(MISC_PKG)) {
            throw new ClassNotFoundException(name);
        }
        String path = name.replace('.', '/').concat(".class");
        URL res = getResource(path);
        if (res != null) {
            try {
                return defineClass(name, res);
            } catch (IOException e) {
                throw new ClassNotFoundException(name, e);
            }
        } else {
            throw new ClassNotFoundException(name);
        }
    }
    private Class defineClass(String name, URL url) throws IOException {
        byte[] b = getBytes(url);
        CodeSource cs = new CodeSource(null, (java.security.cert.Certificate[])null);
        if (!name.equals(TRAMPOLINE)) {
            throw new IOException("MethodUtil: bad name " + name);
        }
        return defineClass(name, b, 0, b.length, cs);
    }
    private static byte[] getBytes(URL url) throws IOException {
        URLConnection uc = url.openConnection();
        if (uc instanceof java.net.HttpURLConnection) {
            java.net.HttpURLConnection huc = (java.net.HttpURLConnection) uc;
            int code = huc.getResponseCode();
            if (code >= java.net.HttpURLConnection.HTTP_BAD_REQUEST) {
                throw new IOException("open HTTP connection failed.");
            }
        }
        int len = uc.getContentLength();
        InputStream in = new BufferedInputStream(uc.getInputStream());
        byte[] b;
        try {
            b = IOUtils.readFully(in, len, true);
        } finally {
            in.close();
        }
        return b;
    }
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        PermissionCollection perms = super.getPermissions(codesource);
        perms.add(new AllPermission());
        return perms;
    }
    private static Class getTrampolineClass() {
        try {
            return Class.forName(TRAMPOLINE, true, new MethodUtil());
        } catch (ClassNotFoundException e) {
        }
        return null;
    }
}

public class AnonymousClassLoader {
    final Class<?> hostClass;
    private static int CHC_CALLERS = 3;
    public AnonymousClassLoader() {
        this.hostClass = checkHostClass(null);
    }
    public AnonymousClassLoader(Class<?> hostClass) {
        this.hostClass = checkHostClass(hostClass);
    }
    private static Class<?> getTopLevelClass(Class<?> clazz) {
      for(Class<?> outer = clazz.getDeclaringClass(); outer != null;
          outer = outer.getDeclaringClass()) {
        clazz = outer;
      }
      return clazz;
    }
    private static Class<?> checkHostClass(Class<?> hostClass) {
        Class<?> caller = sun.reflect.Reflection.getCallerClass(CHC_CALLERS);
        if (caller == null) {
            if (hostClass == null)
                return AnonymousClassLoader.class; 
            return hostClass;
        }
        if (hostClass == null)
            hostClass = caller; 
        Class<?> callee = hostClass;
        if (caller == callee)
            return hostClass;
        caller = getTopLevelClass(caller);
        callee = getTopLevelClass(callee);
        if (caller == callee)
            return caller;
        ClassLoader callerCL = caller.getClassLoader();
        if (callerCL == null) {
            return hostClass;
        }
        final int ACC_PRIVATE = 2;
        try {
            sun.reflect.Reflection.ensureMemberAccess(caller, callee, null, ACC_PRIVATE);
        } catch (IllegalAccessException ee) {
            throw new IllegalArgumentException(ee);
        }
        return hostClass;
    }
    public Class<?> loadClass(byte[] classFile) {
        if (defineAnonymousClass == null) {
            try {
                return fakeLoadClass(new ConstantPoolParser(classFile).createPatch());
            } catch (InvalidConstantPoolFormatException ee) {
                throw new IllegalArgumentException(ee);
            }
        }
        return loadClass(classFile, null);
    }
    public Class<?> loadClass(ConstantPoolPatch classPatch) {
        if (defineAnonymousClass == null) {
            return fakeLoadClass(classPatch);
        }
        Object[] patches = classPatch.patchArray;
        for (int i = 0; i < patches.length; i++) {
            Object value = patches[i];
            if (value != null) {
                byte tag = classPatch.getTag(i);
                switch (tag) {
                case ConstantPoolVisitor.CONSTANT_Class:
                    if (value instanceof String) {
                        if (patches == classPatch.patchArray)
                            patches = patches.clone();
                        patches[i] = ((String)value).replace('.', '/');
                    }
                    break;
                case ConstantPoolVisitor.CONSTANT_Fieldref:
                case ConstantPoolVisitor.CONSTANT_Methodref:
                case ConstantPoolVisitor.CONSTANT_InterfaceMethodref:
                case ConstantPoolVisitor.CONSTANT_NameAndType:
                    break;
                }
            }
        }
        return loadClass(classPatch.outer.classFile, classPatch.patchArray);
    }
    private Class<?> loadClass(byte[] classFile, Object[] patchArray) {
        try {
            return (Class<?>)
                defineAnonymousClass.invoke(unsafe,
                                            hostClass, classFile, patchArray);
        } catch (Exception ex) {
            throwReflectedException(ex);
            throw new RuntimeException("error loading into "+hostClass, ex);
        }
    }
    private static void throwReflectedException(Exception ex) {
        if (ex instanceof InvocationTargetException) {
            Throwable tex = ((InvocationTargetException)ex).getTargetException();
            if (tex instanceof Error)
                throw (Error) tex;
            ex = (Exception) tex;
        }
        if (ex instanceof RuntimeException) {
            throw (RuntimeException) ex;
        }
    }
    private Class<?> fakeLoadClass(ConstantPoolPatch classPatch) {
        if (true)  throw new UnsupportedOperationException("NYI");
        Object[] cpArray;
        try {
            cpArray = classPatch.getOriginalCP();
        } catch (InvalidConstantPoolFormatException ex) {
            throw new RuntimeException(ex);
        }
        int thisClassIndex = classPatch.getParser().getThisClassIndex();
        String thisClassName = (String) cpArray[thisClassIndex];
        synchronized (AnonymousClassLoader.class) {
            thisClassName = thisClassName+"\\|"+(++fakeNameCounter);
        }
        classPatch.putUTF8(thisClassIndex, thisClassName);
        byte[] classFile = null;
        return unsafe.defineClass(null, classFile, 0, classFile.length,
                                  hostClass.getClassLoader(),
                                  hostClass.getProtectionDomain());
    }
    private static int fakeNameCounter = 99999;
    static sun.misc.Unsafe unsafe = sun.misc.Unsafe.getUnsafe();
    static private final Method defineAnonymousClass;
    static {
        Method dac = null;
        Class<? extends sun.misc.Unsafe> unsafeClass = unsafe.getClass();
        try {
            dac = unsafeClass.getMethod("defineAnonymousClass",
                                        Class.class,
                                        byte[].class,
                                        Object[].class);
        } catch (Exception ee) {
            dac = null;
        }
        defineAnonymousClass = dac;
    }
    private static void noJVMSupport() {
        throw new UnsupportedOperationException("no JVM support for anonymous classes");
    }
    private static native Class<?> loadClassInternal(Class<?> hostClass,
                                                     byte[] classFile,
                                                     Object[] patchArray);
    public static byte[] readClassFile(Class<?> templateClass) throws IOException {
        String templateName = templateClass.getName();
        int lastDot = templateName.lastIndexOf('.');
        java.net.URL url = templateClass.getResource(templateName.substring(lastDot+1)+".class");
        java.net.URLConnection connection = url.openConnection();
        int contentLength = connection.getContentLength();
        if (contentLength < 0)
            throw new IOException("invalid content length "+contentLength);
        return IOUtils.readFully(connection.getInputStream(), contentLength, true);
    }
}

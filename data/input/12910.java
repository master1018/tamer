public class ReflectionFactory {
    private static boolean initted = false;
    private static Permission reflectionFactoryAccessPerm
        = new RuntimePermission("reflectionFactoryAccess");
    private static ReflectionFactory soleInstance = new ReflectionFactory();
    private static volatile LangReflectAccess langReflectAccess;
    private static boolean noInflation        = false;
    private static int     inflationThreshold = 15;
    private ReflectionFactory() {
    }
    public static final class GetReflectionFactoryAction
        implements PrivilegedAction<ReflectionFactory> {
        public ReflectionFactory run() {
            return getReflectionFactory();
        }
    }
    public static ReflectionFactory getReflectionFactory() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(reflectionFactoryAccessPerm);
        }
        return soleInstance;
    }
    public void setLangReflectAccess(LangReflectAccess access) {
        langReflectAccess = access;
    }
    public FieldAccessor newFieldAccessor(Field field, boolean override) {
        checkInitted();
        return UnsafeFieldAccessorFactory.newFieldAccessor(field, override);
    }
    public MethodAccessor newMethodAccessor(Method method) {
        checkInitted();
        if (noInflation) {
            return new MethodAccessorGenerator().
                generateMethod(method.getDeclaringClass(),
                               method.getName(),
                               method.getParameterTypes(),
                               method.getReturnType(),
                               method.getExceptionTypes(),
                               method.getModifiers());
        } else {
            NativeMethodAccessorImpl acc =
                new NativeMethodAccessorImpl(method);
            DelegatingMethodAccessorImpl res =
                new DelegatingMethodAccessorImpl(acc);
            acc.setParent(res);
            return res;
        }
    }
    public ConstructorAccessor newConstructorAccessor(Constructor c) {
        checkInitted();
        Class<?> declaringClass = c.getDeclaringClass();
        if (Modifier.isAbstract(declaringClass.getModifiers())) {
            return new InstantiationExceptionConstructorAccessorImpl(null);
        }
        if (declaringClass == Class.class) {
            return new InstantiationExceptionConstructorAccessorImpl
                ("Can not instantiate java.lang.Class");
        }
        if (Reflection.isSubclassOf(declaringClass,
                                    ConstructorAccessorImpl.class)) {
            return new BootstrapConstructorAccessorImpl(c);
        }
        if (noInflation) {
            return new MethodAccessorGenerator().
                generateConstructor(c.getDeclaringClass(),
                                    c.getParameterTypes(),
                                    c.getExceptionTypes(),
                                    c.getModifiers());
        } else {
            NativeConstructorAccessorImpl acc =
                new NativeConstructorAccessorImpl(c);
            DelegatingConstructorAccessorImpl res =
                new DelegatingConstructorAccessorImpl(acc);
            acc.setParent(res);
            return res;
        }
    }
    public Field newField(Class<?> declaringClass,
                          String name,
                          Class<?> type,
                          int modifiers,
                          int slot,
                          String signature,
                          byte[] annotations)
    {
        return langReflectAccess().newField(declaringClass,
                                            name,
                                            type,
                                            modifiers,
                                            slot,
                                            signature,
                                            annotations);
    }
    public Method newMethod(Class<?> declaringClass,
                            String name,
                            Class<?>[] parameterTypes,
                            Class<?> returnType,
                            Class<?>[] checkedExceptions,
                            int modifiers,
                            int slot,
                            String signature,
                            byte[] annotations,
                            byte[] parameterAnnotations,
                            byte[] annotationDefault)
    {
        return langReflectAccess().newMethod(declaringClass,
                                             name,
                                             parameterTypes,
                                             returnType,
                                             checkedExceptions,
                                             modifiers,
                                             slot,
                                             signature,
                                             annotations,
                                             parameterAnnotations,
                                             annotationDefault);
    }
    public Constructor newConstructor(Class<?> declaringClass,
                                      Class<?>[] parameterTypes,
                                      Class<?>[] checkedExceptions,
                                      int modifiers,
                                      int slot,
                                      String signature,
                                      byte[] annotations,
                                      byte[] parameterAnnotations)
    {
        return langReflectAccess().newConstructor(declaringClass,
                                                  parameterTypes,
                                                  checkedExceptions,
                                                  modifiers,
                                                  slot,
                                                  signature,
                                                  annotations,
                                                  parameterAnnotations);
    }
    public MethodAccessor getMethodAccessor(Method m) {
        return langReflectAccess().getMethodAccessor(m);
    }
    public void setMethodAccessor(Method m, MethodAccessor accessor) {
        langReflectAccess().setMethodAccessor(m, accessor);
    }
    public ConstructorAccessor getConstructorAccessor(Constructor c) {
        return langReflectAccess().getConstructorAccessor(c);
    }
    public void setConstructorAccessor(Constructor c,
                                       ConstructorAccessor accessor)
    {
        langReflectAccess().setConstructorAccessor(c, accessor);
    }
    public Method copyMethod(Method arg) {
        return langReflectAccess().copyMethod(arg);
    }
    public Field copyField(Field arg) {
        return langReflectAccess().copyField(arg);
    }
    public <T> Constructor<T> copyConstructor(Constructor<T> arg) {
        return langReflectAccess().copyConstructor(arg);
    }
    public Constructor newConstructorForSerialization
        (Class<?> classToInstantiate, Constructor constructorToCall)
    {
        if (constructorToCall.getDeclaringClass() == classToInstantiate) {
            return constructorToCall;
        }
        ConstructorAccessor acc = new MethodAccessorGenerator().
            generateSerializationConstructor(classToInstantiate,
                                             constructorToCall.getParameterTypes(),
                                             constructorToCall.getExceptionTypes(),
                                             constructorToCall.getModifiers(),
                                             constructorToCall.getDeclaringClass());
        Constructor c = newConstructor(constructorToCall.getDeclaringClass(),
                                       constructorToCall.getParameterTypes(),
                                       constructorToCall.getExceptionTypes(),
                                       constructorToCall.getModifiers(),
                                       langReflectAccess().
                                       getConstructorSlot(constructorToCall),
                                       langReflectAccess().
                                       getConstructorSignature(constructorToCall),
                                       langReflectAccess().
                                       getConstructorAnnotations(constructorToCall),
                                       langReflectAccess().
                                       getConstructorParameterAnnotations(constructorToCall));
        setConstructorAccessor(c, acc);
        return c;
    }
    static int inflationThreshold() {
        return inflationThreshold;
    }
    private static void checkInitted() {
        if (initted) return;
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                public Void run() {
                    if (System.out == null) {
                        return null;
                    }
                    String val = System.getProperty("sun.reflect.noInflation");
                    if (val != null && val.equals("true")) {
                        noInflation = true;
                    }
                    val = System.getProperty("sun.reflect.inflationThreshold");
                    if (val != null) {
                        try {
                            inflationThreshold = Integer.parseInt(val);
                        } catch (NumberFormatException e) {
                            throw (RuntimeException)
                                new RuntimeException("Unable to parse property sun.reflect.inflationThreshold").
                                    initCause(e);
                        }
                    }
                    initted = true;
                    return null;
                }
            });
    }
    private static LangReflectAccess langReflectAccess() {
        if (langReflectAccess == null) {
            Modifier.isPublic(Modifier.PUBLIC);
        }
        return langReflectAccess;
    }
}

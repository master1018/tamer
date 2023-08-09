public final class Class<T> implements Serializable, AnnotatedElement, GenericDeclaration, Type {
    private static final long serialVersionUID = 3206093459760846163L;
    private ProtectionDomain pd;
    private volatile SoftReference<ClassCache<T>> cacheRef;
    private Class() {
    }
    private String getSignatureAttribute() {
        Object[] annotation = getSignatureAnnotation();
        if (annotation == null) {
            return null;
        }
        return StringUtils.combineStrings(annotation);
    }
    native private Object[] getSignatureAnnotation();
    public static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true, VMStack.getCallingClassLoader());
    }
    public static Class<?> forName(String className, boolean initializeBoolean,
            ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader == null) {
            SecurityManager smgr = System.getSecurityManager();
            if (smgr != null) {
                ClassLoader calling = VMStack.getCallingClassLoader();
                if (calling != null) {
                    smgr.checkPermission(new RuntimePermission("getClassLoader"));
                }
            }
            classLoader = ClassLoader.getSystemClassLoader();
        }
        Class<?> result;
        try {
            result = classForName(className, initializeBoolean,
                    classLoader);
        } catch (ClassNotFoundException e) {
            Throwable cause = e.getCause(); 
            if (cause instanceof ExceptionInInitializerError) {
                throw (ExceptionInInitializerError) cause;
            }
            throw e;
        }
        return result;
    }
    static native Class<?> classForName(String className, boolean initializeBoolean,
            ClassLoader classLoader) throws ClassNotFoundException;
    public Class[] getClasses() {
        checkPublicMemberAccess();
        return getFullListOfClasses(true);
    }
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        Annotation[] list = getAnnotations();
        for (int i = 0; i < list.length; i++) {
            if (annotationClass.isInstance(list[i])) {
                return (A)list[i];
            }
        }
        return null;
    }
    public Annotation[] getAnnotations() {
        HashMap<Class, Annotation> map = new HashMap<Class, Annotation>();
        Annotation[] annos = getDeclaredAnnotations();
        for (int i = annos.length-1; i >= 0; --i)
            map.put(annos[i].annotationType(), annos[i]);
        for (Class sup = getSuperclass(); sup != null;
                sup = sup.getSuperclass()) {
            annos = sup.getDeclaredAnnotations();
            for (int i = annos.length-1; i >= 0; --i) {
                Class clazz = annos[i].annotationType();
                if (!map.containsKey(clazz) &&
                        clazz.isAnnotationPresent(Inherited.class)) {
                    map.put(clazz, annos[i]);
                }
            }
        }
        Collection<Annotation> coll = map.values();
        return coll.toArray(new Annotation[coll.size()]);
    }
    public String getCanonicalName() {
        if (isLocalClass() || isAnonymousClass())
            return null;
        if (isArray()) {
            String name = getComponentType().getCanonicalName();
            if (name != null) {
                return name + "[]";
            } 
        } else if (isMemberClass()) {
            String name = getDeclaringClass().getCanonicalName();
            if (name != null) {
                return name + "." + getSimpleName();
            }
        } else {
            return getName();
        }
        return null;
    }
    public ClassLoader getClassLoader() {
        SecurityManager smgr = System.getSecurityManager();
        ClassLoader loader = getClassLoaderImpl();
        if (smgr != null && loader != null) {
            ClassLoader calling = VMStack.getCallingClassLoader();
            if (calling != null && !calling.isAncestorOf(loader)) {
                smgr.checkPermission(new RuntimePermission("getClassLoader"));
            }
        }
        if (this.isPrimitive()) {
            return null;
        }
        if (loader == null) {
            loader = BootClassLoader.getInstance();
        }
        return loader;
    }
    ClassLoader getClassLoaderImpl() {
        ClassLoader loader = getClassLoader(this);
        return loader == null ? BootClassLoader.getInstance() : loader;
    }
    private static native ClassLoader getClassLoader(Class<?> clazz);
    public native Class<?> getComponentType();
    @SuppressWarnings("unchecked")
    public Constructor<T> getConstructor(Class... parameterTypes) throws NoSuchMethodException,
            SecurityException {
        checkPublicMemberAccess();
        return getMatchingConstructor(getDeclaredConstructors(this, true), parameterTypes);
    }
    public Constructor[] getConstructors() throws SecurityException {
        checkPublicMemberAccess();
        return getDeclaredConstructors(this, true);
    }
    native public Annotation[] getDeclaredAnnotations();
    public Class[] getDeclaredClasses() throws SecurityException {
        checkDeclaredMemberAccess();
        return getDeclaredClasses(this, false);
    }
    private Class<?>[] getFullListOfClasses(boolean publicOnly) {
        Class<?>[] result = getDeclaredClasses(this, publicOnly);
        Class<?> clazz = this.getSuperclass();
        while (clazz != null) {
            Class<?>[] temp = getDeclaredClasses(clazz, publicOnly);
            if (temp.length != 0) {
                result = arraycopy(new Class[result.length + temp.length], result, temp);
            }
            clazz = clazz.getSuperclass();
        }
        return result;
    }
    native private static Class<?>[] getDeclaredClasses(Class<?> clazz,
        boolean publicOnly);
    @SuppressWarnings("unchecked")
    public Constructor<T> getDeclaredConstructor(Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkDeclaredMemberAccess();
        return getMatchingConstructor(getDeclaredConstructors(this, false), parameterTypes);
    }
    public Constructor[] getDeclaredConstructors() throws SecurityException {
        checkDeclaredMemberAccess();
        return getDeclaredConstructors(this, false);
    }
    private static native <T> Constructor<T>[] getDeclaredConstructors(Class<T> clazz, boolean publicOnly);
    private Constructor<T> getMatchingConstructor(
            Constructor<T>[] list, Class<?>[] parameterTypes)
            throws NoSuchMethodException {
        for (int i = 0; i < list.length; i++) {
            if (compareClassLists(list[i].getParameterTypes(), parameterTypes)) {
                return list[i];
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSimpleName());
        sb.append('(');
        boolean first = true;
        if (parameterTypes != null) {
            for (Class<?> p : parameterTypes) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(p.getSimpleName());
            }
        }
        sb.append(')');
        throw new NoSuchMethodException(sb.toString());
    }
    public Field getDeclaredField(String name)
            throws NoSuchFieldException, SecurityException {
        checkDeclaredMemberAccess();
        Field[] fields = getClassCache().getDeclaredFields();
        Field field = findFieldByName(fields, name);
        return REFLECT.clone(field);
    }
    public Field[] getDeclaredFields() throws SecurityException {
        checkDeclaredMemberAccess();
        Field[] fields = getClassCache().getDeclaredFields();
        return ClassCache.deepCopy(fields);
    }
    static native Field[] getDeclaredFields(Class<?> clazz, boolean publicOnly);
    public Method getDeclaredMethod(String name, Class... parameterTypes)
            throws NoSuchMethodException, SecurityException {
        checkDeclaredMemberAccess();
        Method[] methods = getClassCache().getDeclaredMethods();
        Method method = findMethodByName(methods, name, parameterTypes);
        return REFLECT.clone(method);
    }
    public Method[] getDeclaredMethods() throws SecurityException {
        checkDeclaredMemberAccess();
        Method[] methods = getClassCache().getDeclaredMethods();
        return ClassCache.deepCopy(methods);
    }
    static native Method[] getDeclaredMethods(Class<?> clazz, boolean publicOnly);
     ClassCache<T> getClassCache() {
        ClassCache<T> cache = null;
        if (cacheRef != null) {
            cache = cacheRef.get();
        }
        if (cache == null) {
            cache = new ClassCache<T>(this);
            cacheRef = new SoftReference<ClassCache<T>>(cache);
        }
        return cache;
    }
    native public Class<?> getDeclaringClass();
    native public Class<?> getEnclosingClass();
    native public Constructor<?> getEnclosingConstructor();
    native public Method getEnclosingMethod();
    @SuppressWarnings("unchecked")
    public T[] getEnumConstants() {
        if (isEnum()) {
            checkPublicMemberAccess();
            T[] values = getClassCache().getEnumValuesInOrder();
            return (T[]) values.clone();
        }
        return null;
    }
    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        checkPublicMemberAccess();
        Field[] fields = getClassCache().getAllPublicFields();
        Field field = findFieldByName(fields, name);
        return REFLECT.clone(field);
    }
    public Field[] getFields() throws SecurityException {
        checkPublicMemberAccess();
        Field[] fields = getClassCache().getAllPublicFields();
        return ClassCache.deepCopy(fields);
    }
    public Type[] getGenericInterfaces() {
        GenericSignatureParser parser = new GenericSignatureParser(
                VMStack.getCallingClassLoader2());
        parser.parseForClass(this, getSignatureAttribute());
        return Types.getClonedTypeArray(parser.interfaceTypes);
    }
    public Type getGenericSuperclass() {
        GenericSignatureParser parser = new GenericSignatureParser(
                VMStack.getCallingClassLoader2());
        parser.parseForClass(this, getSignatureAttribute());
        return Types.getType(parser.superclassType);
    }
    public native Class[] getInterfaces();
    public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException, 
            SecurityException {
        checkPublicMemberAccess();
        Method[] methods = getClassCache().getAllPublicMethods();
        Method method = findMethodByName(methods, name, parameterTypes);
        return REFLECT.clone(method);
    }
    public Method[] getMethods() throws SecurityException {
        checkPublicMemberAccess();
        Method[] methods = getClassCache().getAllPublicMethods();
        return ClassCache.deepCopy(methods);
    }
     void checkPublicMemberAccess() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkMemberAccess(this, Member.PUBLIC);
            ClassLoader calling = VMStack.getCallingClassLoader2();
            ClassLoader current = getClassLoader();
            if (calling != null && !calling.isAncestorOf(current)) {
                smgr.checkPackageAccess(this.getPackage().getName());
            }
        }
    }
    private void checkDeclaredMemberAccess() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkMemberAccess(this, Member.DECLARED);
            ClassLoader calling = VMStack.getCallingClassLoader2();
            ClassLoader current = getClassLoader();
            if (calling != null && !calling.isAncestorOf(current)) {
                smgr.checkPackageAccess(this.getPackage().getName());
            }
        }
    }
    public int getModifiers() {
        return getModifiers(this, false);
    }
    private static native int getModifiers(Class<?> clazz, boolean ignoreInnerClassesAttrib);
    public native String getName();
    public String getSimpleName() {
        if (isArray()) {
            return getComponentType().getSimpleName() + "[]";
        }
        String name = getName();
        if (isAnonymousClass()) {
            return "";
        }
        if (isMemberClass() || isLocalClass()) {
            return getInnerClassName();
        }
        int dot = name.lastIndexOf('.');
        if (dot != -1) {
            return name.substring(dot + 1);
        }
        return name;
    }
    private native String getInnerClassName();
    public ProtectionDomain getProtectionDomain() {
        SecurityManager smgr = System.getSecurityManager();
        if (smgr != null) {
            smgr.checkPermission(new RuntimePermission("getProtectionDomain"));
        }
        return pd;
    }
    public URL getResource(String resName) {
        if (resName.startsWith("/")) {
            resName = resName.substring(1);
        } else {
            String pkg = getName();
            int dot = pkg.lastIndexOf('.');
            if (dot != -1) {
                pkg = pkg.substring(0, dot).replace('.', '/');
            } else {
                pkg = "";
            }
            resName = pkg + "/" + resName;
        }
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            return loader.getResource(resName);
        } else {
            return ClassLoader.getSystemResource(resName);
        }
    }
    public InputStream getResourceAsStream(String resName) {
        if (resName.startsWith("/")) {
            resName = resName.substring(1);
        } else {
            String pkg = getName();
            int dot = pkg.lastIndexOf('.');
            if (dot != -1) {
                pkg = pkg.substring(0, dot).replace('.', '/');
            } else {
                pkg = "";
            }
            resName = pkg + "/" + resName;
        }
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            return loader.getResourceAsStream(resName);
        } else {
            return ClassLoader.getSystemResourceAsStream(resName);
        }
    }
    public Object[] getSigners() {
        return null;
    }
    public native Class<? super T> getSuperclass();
    @SuppressWarnings("unchecked")
    public synchronized TypeVariable<Class<T>>[] getTypeParameters() {
        GenericSignatureParser parser = new GenericSignatureParser(
                VMStack.getCallingClassLoader2());
        parser.parseForClass(this, getSignatureAttribute());
        return parser.formalTypeParameters.clone();
    }
    public boolean isAnnotation() {
        final int ACC_ANNOTATION = 0x2000;  
        int mod = getModifiers(this, true);
        return (mod & ACC_ANNOTATION) != 0;
    }
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }
    native public boolean isAnonymousClass();
    public boolean isArray() {
        return getComponentType() != null;
    }
    public native boolean isAssignableFrom(Class<?> cls);
    public boolean isEnum() {
        return ((getModifiers() & 0x4000) != 0) && (getSuperclass() == Enum.class);
    }
    public native boolean isInstance(Object object);
    public native boolean isInterface();
    public boolean isLocalClass() {
        boolean enclosed = (getEnclosingMethod() != null ||
                         getEnclosingConstructor() != null);
        return enclosed && !isAnonymousClass();
    }
    public boolean isMemberClass() {
        return getDeclaringClass() != null;
    }
    public native boolean isPrimitive();
    public boolean isSynthetic() {
        final int ACC_SYNTHETIC = 0x1000;   
        int mod = getModifiers(this, true);
        return (mod & ACC_SYNTHETIC) != 0;
    }
    public T newInstance() throws IllegalAccessException,
            InstantiationException {
        checkPublicMemberAccess();        
        return newInstanceImpl();
    }
    private native T newInstanceImpl() throws IllegalAccessException,
            InstantiationException;
    @Override
    public String toString() {
        if (isPrimitive()) {
            return getSimpleName().toLowerCase();
        } else {
            return (isInterface() ? "interface " : "class ") + getName();
        }
    }
    public Package getPackage() {
        ClassLoader loader = getClassLoader();
        if (loader != null) {
            String name = getName();
            int dot = name.lastIndexOf('.');
            return (dot != -1 ? ClassLoader.getPackage(loader, name.substring(0, dot)) : null);
        }
        return null;
    }
    public native boolean desiredAssertionStatus();
    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this)) {
            return (Class<? extends U>)this;
        }
        throw new ClassCastException();
    }
    @SuppressWarnings("unchecked")
    public T cast(Object obj) {
        if (obj == null) {
            return null;
        } else if (this.isInstance(obj)) {
            return (T)obj;            
        }
        throw new ClassCastException();
    }
     static native void setAccessibleNoCheck(AccessibleObject ao,
            boolean flag);
    private static <T extends Object> T[] arraycopy(T[] result, T[] head, T[] tail) {
        System.arraycopy(head, 0, result, 0, head.length);
        System.arraycopy(tail, 0, result, head.length, tail.length);
        return result;
    }
    static final Class<?>[] getStackClasses(int maxDepth, boolean stopAtPrivileged) {
        return VMStack.getClasses(maxDepth, stopAtPrivileged);
    }
}

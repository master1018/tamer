class StandardMBeanIntrospector extends MBeanIntrospector<Method> {
    private static final StandardMBeanIntrospector instance =
        new StandardMBeanIntrospector();
    static StandardMBeanIntrospector getInstance() {
        return instance;
    }
    @Override
    PerInterfaceMap<Method> getPerInterfaceMap() {
        return perInterfaceMap;
    }
    @Override
    MBeanInfoMap getMBeanInfoMap() {
        return mbeanInfoMap;
    }
    @Override
    MBeanAnalyzer<Method> getAnalyzer(Class<?> mbeanInterface)
            throws NotCompliantMBeanException {
        return MBeanAnalyzer.analyzer(mbeanInterface, this);
    }
    @Override
    boolean isMXBean() {
        return false;
    }
    @Override
    Method mFrom(Method m) {
        return m;
    }
    @Override
    String getName(Method m) {
        return m.getName();
    }
    @Override
    Type getGenericReturnType(Method m) {
        return m.getGenericReturnType();
    }
    @Override
    Type[] getGenericParameterTypes(Method m) {
        return m.getGenericParameterTypes();
    }
    @Override
    String[] getSignature(Method m) {
        Class<?>[] params = m.getParameterTypes();
        String[] sig = new String[params.length];
        for (int i = 0; i < params.length; i++)
            sig[i] = params[i].getName();
        return sig;
    }
    @Override
    void checkMethod(Method m) {
    }
    @Override
    Object invokeM2(Method m, Object target, Object[] args, Object cookie)
            throws InvocationTargetException, IllegalAccessException,
                   MBeanException {
        return m.invoke(target, args);
    }
    @Override
    boolean validParameter(Method m, Object value, int paramNo, Object cookie) {
        return isValidParameter(m, value, paramNo);
    }
    @Override
    MBeanAttributeInfo getMBeanAttributeInfo(String attributeName,
            Method getter, Method setter) {
        final String description = "Attribute exposed for management";
        try {
            return new MBeanAttributeInfo(attributeName, description,
                                          getter, setter);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e); 
        }
    }
    @Override
    MBeanOperationInfo getMBeanOperationInfo(String operationName,
            Method operation) {
        final String description = "Operation exposed for management";
        return new MBeanOperationInfo(description, operation);
    }
    @Override
    Descriptor getBasicMBeanDescriptor() {
        return ImmutableDescriptor.EMPTY_DESCRIPTOR;
    }
    @Override
    Descriptor getMBeanDescriptor(Class<?> resourceClass) {
        boolean immutable = isDefinitelyImmutableInfo(resourceClass);
        return new ImmutableDescriptor("mxbean=false",
                                       "immutableInfo=" + immutable);
    }
    static boolean isDefinitelyImmutableInfo(Class<?> implClass) {
        if (!NotificationBroadcaster.class.isAssignableFrom(implClass))
            return true;
        synchronized (definitelyImmutable) {
            Boolean immutable = definitelyImmutable.get(implClass);
            if (immutable == null) {
                final Class<NotificationBroadcasterSupport> nbs =
                        NotificationBroadcasterSupport.class;
                if (nbs.isAssignableFrom(implClass)) {
                    try {
                        Method m = implClass.getMethod("getNotificationInfo");
                        immutable = (m.getDeclaringClass() == nbs);
                    } catch (Exception e) {
                        return false;
                    }
                } else
                    immutable = false;
                definitelyImmutable.put(implClass, immutable);
            }
            return immutable;
        }
    }
    private static final WeakHashMap<Class<?>, Boolean> definitelyImmutable =
            new WeakHashMap<Class<?>, Boolean>();
    private static final PerInterfaceMap<Method>
        perInterfaceMap = new PerInterfaceMap<Method>();
    private static final MBeanInfoMap mbeanInfoMap = new MBeanInfoMap();
}

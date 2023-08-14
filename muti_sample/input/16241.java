class MXBeanIntrospector extends MBeanIntrospector<ConvertingMethod> {
    private static final MXBeanIntrospector instance = new MXBeanIntrospector();
    static MXBeanIntrospector getInstance() {
        return instance;
    }
    @Override
    PerInterfaceMap<ConvertingMethod> getPerInterfaceMap() {
        return perInterfaceMap;
    }
    @Override
    MBeanInfoMap getMBeanInfoMap() {
        return mbeanInfoMap;
    }
    @Override
    MBeanAnalyzer<ConvertingMethod> getAnalyzer(Class<?> mbeanInterface)
            throws NotCompliantMBeanException {
        return MBeanAnalyzer.analyzer(mbeanInterface, this);
    }
    @Override
    boolean isMXBean() {
        return true;
    }
    @Override
    ConvertingMethod mFrom(Method m) {
        return ConvertingMethod.from(m);
    }
    @Override
    String getName(ConvertingMethod m) {
        return m.getName();
    }
    @Override
    Type getGenericReturnType(ConvertingMethod m) {
        return m.getGenericReturnType();
    }
    @Override
    Type[] getGenericParameterTypes(ConvertingMethod m) {
        return m.getGenericParameterTypes();
    }
    @Override
    String[] getSignature(ConvertingMethod m) {
        return m.getOpenSignature();
    }
    @Override
    void checkMethod(ConvertingMethod m) {
        m.checkCallFromOpen();
    }
    @Override
    Object invokeM2(ConvertingMethod m, Object target, Object[] args,
                    Object cookie)
            throws InvocationTargetException, IllegalAccessException,
                   MBeanException {
        return m.invokeWithOpenReturn((MXBeanLookup) cookie, target, args);
    }
    @Override
    boolean validParameter(ConvertingMethod m, Object value, int paramNo,
                           Object cookie) {
        if (value == null) {
            Type t = m.getGenericParameterTypes()[paramNo];
            return (!(t instanceof Class<?>) || !((Class<?>) t).isPrimitive());
        } else {
            Object v;
            try {
                v = m.fromOpenParameter((MXBeanLookup) cookie, value, paramNo);
            } catch (Exception e) {
                return true;
            }
            return isValidParameter(m.getMethod(), v, paramNo);
        }
    }
    @Override
    MBeanAttributeInfo getMBeanAttributeInfo(String attributeName,
            ConvertingMethod getter, ConvertingMethod setter) {
        final boolean isReadable = (getter != null);
        final boolean isWritable = (setter != null);
        final boolean isIs = isReadable && getName(getter).startsWith("is");
        final String description = attributeName;
        final OpenType<?> openType;
        final Type originalType;
        if (isReadable) {
            openType = getter.getOpenReturnType();
            originalType = getter.getGenericReturnType();
        } else {
            openType = setter.getOpenParameterTypes()[0];
            originalType = setter.getGenericParameterTypes()[0];
        }
        Descriptor descriptor = typeDescriptor(openType, originalType);
        if (isReadable) {
            descriptor = ImmutableDescriptor.union(descriptor,
                    getter.getDescriptor());
        }
        if (isWritable) {
            descriptor = ImmutableDescriptor.union(descriptor,
                    setter.getDescriptor());
        }
        final MBeanAttributeInfo ai;
        if (canUseOpenInfo(originalType)) {
            ai = new OpenMBeanAttributeInfoSupport(attributeName,
                                                   description,
                                                   openType,
                                                   isReadable,
                                                   isWritable,
                                                   isIs,
                                                   descriptor);
        } else {
            ai = new MBeanAttributeInfo(attributeName,
                                        originalTypeString(originalType),
                                        description,
                                        isReadable,
                                        isWritable,
                                        isIs,
                                        descriptor);
        }
        return ai;
    }
    @Override
    MBeanOperationInfo getMBeanOperationInfo(String operationName,
            ConvertingMethod operation) {
        final Method method = operation.getMethod();
        final String description = operationName;
        final int impact = MBeanOperationInfo.UNKNOWN;
        final OpenType<?> returnType = operation.getOpenReturnType();
        final Type originalReturnType = operation.getGenericReturnType();
        final OpenType<?>[] paramTypes = operation.getOpenParameterTypes();
        final Type[] originalParamTypes = operation.getGenericParameterTypes();
        final MBeanParameterInfo[] params =
            new MBeanParameterInfo[paramTypes.length];
        boolean openReturnType = canUseOpenInfo(originalReturnType);
        boolean openParameterTypes = true;
        Annotation[][] annots = method.getParameterAnnotations();
        for (int i = 0; i < paramTypes.length; i++) {
            final String paramName = "p" + i;
            final String paramDescription = paramName;
            final OpenType<?> openType = paramTypes[i];
            final Type originalType = originalParamTypes[i];
            Descriptor descriptor =
                typeDescriptor(openType, originalType);
            descriptor = ImmutableDescriptor.union(descriptor,
                    Introspector.descriptorForAnnotations(annots[i]));
            final MBeanParameterInfo pi;
            if (canUseOpenInfo(originalType)) {
                pi = new OpenMBeanParameterInfoSupport(paramName,
                                                       paramDescription,
                                                       openType,
                                                       descriptor);
            } else {
                openParameterTypes = false;
                pi = new MBeanParameterInfo(
                    paramName,
                    originalTypeString(originalType),
                    paramDescription,
                    descriptor);
            }
            params[i] = pi;
        }
        Descriptor descriptor =
            typeDescriptor(returnType, originalReturnType);
        descriptor = ImmutableDescriptor.union(descriptor,
                Introspector.descriptorForElement(method));
        final MBeanOperationInfo oi;
        if (openReturnType && openParameterTypes) {
            final OpenMBeanParameterInfo[] oparams =
                new OpenMBeanParameterInfo[params.length];
            System.arraycopy(params, 0, oparams, 0, params.length);
            oi = new OpenMBeanOperationInfoSupport(operationName,
                                                   description,
                                                   oparams,
                                                   returnType,
                                                   impact,
                                                   descriptor);
        } else {
            oi = new MBeanOperationInfo(operationName,
                                        description,
                                        params,
                                        openReturnType ?
                                        returnType.getClassName() :
                                        originalTypeString(originalReturnType),
                                        impact,
                                        descriptor);
        }
        return oi;
    }
    @Override
    Descriptor getBasicMBeanDescriptor() {
        return new ImmutableDescriptor("mxbean=true",
                                       "immutableInfo=true");
    }
    @Override
    Descriptor getMBeanDescriptor(Class<?> resourceClass) {
        return ImmutableDescriptor.EMPTY_DESCRIPTOR;
    }
    private static Descriptor typeDescriptor(OpenType<?> openType,
                                             Type originalType) {
        return new ImmutableDescriptor(
            new String[] {"openType",
                          "originalType"},
            new Object[] {openType,
                          originalTypeString(originalType)});
    }
    private static boolean canUseOpenInfo(Type type) {
        if (type instanceof GenericArrayType) {
            return canUseOpenInfo(
                ((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
            return canUseOpenInfo(
                ((Class<?>) type).getComponentType());
        }
        return (!(type instanceof Class<?> && ((Class<?>) type).isPrimitive()));
    }
    private static String originalTypeString(Type type) {
        if (type instanceof Class<?>)
            return ((Class<?>) type).getName();
        else
            return typeName(type);
    }
    static String typeName(Type type) {
        if (type instanceof Class<?>) {
            Class<?> c = (Class<?>) type;
            if (c.isArray())
                return typeName(c.getComponentType()) + "[]";
            else
                return c.getName();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            return typeName(gat.getGenericComponentType()) + "[]";
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            StringBuilder sb = new StringBuilder();
            sb.append(typeName(pt.getRawType())).append("<");
            String sep = "";
            for (Type t : pt.getActualTypeArguments()) {
                sb.append(sep).append(typeName(t));
                sep = ", ";
            }
            return sb.append(">").toString();
        } else
            return "???";
    }
    private final PerInterfaceMap<ConvertingMethod>
        perInterfaceMap = new PerInterfaceMap<ConvertingMethod>();
    private static final MBeanInfoMap mbeanInfoMap = new MBeanInfoMap();
}

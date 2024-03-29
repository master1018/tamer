final class ConvertingMethod {
    static ConvertingMethod from(Method m) {
        try {
            return new ConvertingMethod(m);
        } catch (OpenDataException ode) {
            final String msg = "Method " + m.getDeclaringClass().getName() +
                "." + m.getName() + " has parameter or return type that " +
                "cannot be translated into an open type";
            throw new IllegalArgumentException(msg, ode);
        }
    }
    Method getMethod() {
        return method;
    }
    Descriptor getDescriptor() {
        return Introspector.descriptorForElement(method);
    }
    Type getGenericReturnType() {
        return method.getGenericReturnType();
    }
    Type[] getGenericParameterTypes() {
        return method.getGenericParameterTypes();
    }
    String getName() {
        return method.getName();
    }
    OpenType<?> getOpenReturnType() {
        return returnMapping.getOpenType();
    }
    OpenType<?>[] getOpenParameterTypes() {
        final OpenType<?>[] types = new OpenType<?>[paramMappings.length];
        for (int i = 0; i < paramMappings.length; i++)
            types[i] = paramMappings[i].getOpenType();
        return types;
    }
    void checkCallFromOpen() {
        try {
            for (MXBeanMapping paramConverter : paramMappings)
                paramConverter.checkReconstructible();
        } catch (InvalidObjectException e) {
            throw new IllegalArgumentException(e);
        }
    }
    void checkCallToOpen() {
        try {
            returnMapping.checkReconstructible();
        } catch (InvalidObjectException e) {
            throw new IllegalArgumentException(e);
        }
    }
    String[] getOpenSignature() {
        if (paramMappings.length == 0)
            return noStrings;
        String[] sig = new String[paramMappings.length];
        for (int i = 0; i < paramMappings.length; i++)
            sig[i] = paramMappings[i].getOpenClass().getName();
        return sig;
    }
    final Object toOpenReturnValue(MXBeanLookup lookup, Object ret)
            throws OpenDataException {
        return returnMapping.toOpenValue(ret);
    }
    final Object fromOpenReturnValue(MXBeanLookup lookup, Object ret)
            throws InvalidObjectException {
        return returnMapping.fromOpenValue(ret);
    }
    final Object[] toOpenParameters(MXBeanLookup lookup, Object[] params)
            throws OpenDataException {
        if (paramConversionIsIdentity || params == null)
            return params;
        final Object[] oparams = new Object[params.length];
        for (int i = 0; i < params.length; i++)
            oparams[i] = paramMappings[i].toOpenValue(params[i]);
        return oparams;
    }
    final Object[] fromOpenParameters(Object[] params)
            throws InvalidObjectException {
        if (paramConversionIsIdentity || params == null)
            return params;
        final Object[] jparams = new Object[params.length];
        for (int i = 0; i < params.length; i++)
            jparams[i] = paramMappings[i].fromOpenValue(params[i]);
        return jparams;
    }
    final Object toOpenParameter(MXBeanLookup lookup,
                                 Object param,
                                 int paramNo)
        throws OpenDataException {
        return paramMappings[paramNo].toOpenValue(param);
    }
    final Object fromOpenParameter(MXBeanLookup lookup,
                                   Object param,
                                   int paramNo)
        throws InvalidObjectException {
        return paramMappings[paramNo].fromOpenValue(param);
    }
    Object invokeWithOpenReturn(MXBeanLookup lookup,
                                Object obj, Object[] params)
            throws MBeanException, IllegalAccessException,
                   InvocationTargetException {
        MXBeanLookup old = MXBeanLookup.getLookup();
        try {
            MXBeanLookup.setLookup(lookup);
            return invokeWithOpenReturn(obj, params);
        } finally {
            MXBeanLookup.setLookup(old);
        }
    }
    private Object invokeWithOpenReturn(Object obj, Object[] params)
            throws MBeanException, IllegalAccessException,
                   InvocationTargetException {
        final Object[] javaParams;
        try {
            javaParams = fromOpenParameters(params);
        } catch (InvalidObjectException e) {
            final String msg = methodName() + ": cannot convert parameters " +
                "from open values: " + e;
            throw new MBeanException(e, msg);
        }
        final Object javaReturn = method.invoke(obj, javaParams);
        try {
            return returnMapping.toOpenValue(javaReturn);
        } catch (OpenDataException e) {
            final String msg = methodName() + ": cannot convert return " +
                "value to open value: " + e;
            throw new MBeanException(e, msg);
        }
    }
    private String methodName() {
        return method.getDeclaringClass() + "." + method.getName();
    }
    private ConvertingMethod(Method m) throws OpenDataException {
        this.method = m;
        MXBeanMappingFactory mappingFactory = MXBeanMappingFactory.DEFAULT;
        returnMapping =
                mappingFactory.mappingForType(m.getGenericReturnType(), mappingFactory);
        Type[] params = m.getGenericParameterTypes();
        paramMappings = new MXBeanMapping[params.length];
        boolean identity = true;
        for (int i = 0; i < params.length; i++) {
            paramMappings[i] = mappingFactory.mappingForType(params[i], mappingFactory);
            identity &= DefaultMXBeanMappingFactory.isIdentity(paramMappings[i]);
        }
        paramConversionIsIdentity = identity;
    }
    private static final String[] noStrings = new String[0];
    private final Method method;
    private final MXBeanMapping returnMapping;
    private final MXBeanMapping[] paramMappings;
    private final boolean paramConversionIsIdentity;
}

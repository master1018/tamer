public class CompositeDataInvocationHandler implements InvocationHandler {
    public CompositeDataInvocationHandler(CompositeData compositeData) {
        this(compositeData, null);
    }
    CompositeDataInvocationHandler(CompositeData compositeData,
                                   MXBeanLookup lookup) {
        if (compositeData == null)
            throw new IllegalArgumentException("compositeData");
        this.compositeData = compositeData;
        this.lookup = lookup;
    }
    public CompositeData getCompositeData() {
        assert compositeData != null;
        return compositeData;
    }
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        final String methodName = method.getName();
        if (method.getDeclaringClass() == Object.class) {
            if (methodName.equals("toString") && args == null)
                return "Proxy[" + compositeData + "]";
            else if (methodName.equals("hashCode") && args == null)
                return compositeData.hashCode() + 0x43444948;
            else if (methodName.equals("equals") && args.length == 1
                && method.getParameterTypes()[0] == Object.class)
                return equals(proxy, args[0]);
            else {
                return method.invoke(this, args);
            }
        }
        String propertyName = DefaultMXBeanMappingFactory.propertyName(method);
        if (propertyName == null) {
            throw new IllegalArgumentException("Method is not getter: " +
                                               method.getName());
        }
        Object openValue;
        if (compositeData.containsKey(propertyName))
            openValue = compositeData.get(propertyName);
        else {
            String decap = DefaultMXBeanMappingFactory.decapitalize(propertyName);
            if (compositeData.containsKey(decap))
                openValue = compositeData.get(decap);
            else {
                final String msg =
                    "No CompositeData item " + propertyName +
                    (decap.equals(propertyName) ? "" : " or " + decap) +
                    " to match " + methodName;
                throw new IllegalArgumentException(msg);
            }
        }
        MXBeanMapping mapping =
            MXBeanMappingFactory.DEFAULT.mappingForType(method.getGenericReturnType(),
                                   MXBeanMappingFactory.DEFAULT);
        return mapping.fromOpenValue(openValue);
    }
    private boolean equals(Object proxy, Object other) {
        if (other == null)
            return false;
        final Class<?> proxyClass = proxy.getClass();
        final Class<?> otherClass = other.getClass();
        if (proxyClass != otherClass)
            return false;
        InvocationHandler otherih = Proxy.getInvocationHandler(other);
        if (!(otherih instanceof CompositeDataInvocationHandler))
            return false;
        CompositeDataInvocationHandler othercdih =
            (CompositeDataInvocationHandler) otherih;
        return compositeData.equals(othercdih.compositeData);
    }
    private final CompositeData compositeData;
    private final MXBeanLookup lookup;
}

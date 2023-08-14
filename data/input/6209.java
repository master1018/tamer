public abstract class MBeanSupport<M>
        implements DynamicMBean2, MBeanRegistration {
    <T> MBeanSupport(T resource, Class<T> mbeanInterfaceType)
            throws NotCompliantMBeanException {
        if (mbeanInterfaceType == null)
            throw new NotCompliantMBeanException("Null MBean interface");
        if (!mbeanInterfaceType.isInstance(resource)) {
            final String msg =
                "Resource class " + resource.getClass().getName() +
                " is not an instance of " + mbeanInterfaceType.getName();
            throw new NotCompliantMBeanException(msg);
        }
        this.resource = resource;
        MBeanIntrospector<M> introspector = getMBeanIntrospector();
        this.perInterface = introspector.getPerInterface(mbeanInterfaceType);
        this.mbeanInfo = introspector.getMBeanInfo(resource, perInterface);
    }
    abstract MBeanIntrospector<M> getMBeanIntrospector();
    abstract Object getCookie();
    public final boolean isMXBean() {
        return perInterface.isMXBean();
    }
    public abstract void register(MBeanServer mbs, ObjectName name)
            throws Exception;
    public abstract void unregister();
    public final ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        if (resource instanceof MBeanRegistration)
            name = ((MBeanRegistration) resource).preRegister(server, name);
        return name;
    }
    public final void preRegister2(MBeanServer server, ObjectName name)
            throws Exception {
        register(server, name);
    }
    public final void registerFailed() {
        unregister();
    }
    public final void postRegister(Boolean registrationDone) {
        if (resource instanceof MBeanRegistration)
            ((MBeanRegistration) resource).postRegister(registrationDone);
    }
    public final void preDeregister() throws Exception {
        if (resource instanceof MBeanRegistration)
            ((MBeanRegistration) resource).preDeregister();
    }
    public final void postDeregister() {
        try {
            unregister();
        } finally {
            if (resource instanceof MBeanRegistration)
                ((MBeanRegistration) resource).postDeregister();
        }
    }
    public final Object getAttribute(String attribute)
            throws AttributeNotFoundException,
                   MBeanException,
                   ReflectionException {
        return perInterface.getAttribute(resource, attribute, getCookie());
    }
    public final AttributeList getAttributes(String[] attributes) {
        final AttributeList result = new AttributeList(attributes.length);
        for (String attrName : attributes) {
            try {
                final Object attrValue = getAttribute(attrName);
                result.add(new Attribute(attrName, attrValue));
            } catch (Exception e) {
            }
        }
        return result;
    }
    public final void setAttribute(Attribute attribute)
            throws AttributeNotFoundException,
                   InvalidAttributeValueException,
                   MBeanException,
                   ReflectionException {
        final String name = attribute.getName();
        final Object value = attribute.getValue();
        perInterface.setAttribute(resource, name, value, getCookie());
    }
    public final AttributeList setAttributes(AttributeList attributes) {
        final AttributeList result = new AttributeList(attributes.size());
        for (Object attrObj : attributes) {
            Attribute attr = (Attribute) attrObj;
            try {
                setAttribute(attr);
                result.add(new Attribute(attr.getName(), attr.getValue()));
            } catch (Exception e) {
            }
        }
        return result;
    }
    public final Object invoke(String operation, Object[] params,
                         String[] signature)
            throws MBeanException, ReflectionException {
        return perInterface.invoke(resource, operation, params, signature,
                                   getCookie());
    }
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }
    public final String getClassName() {
        return resource.getClass().getName();
    }
    public final Object getResource() {
        return resource;
    }
    public final Class<?> getMBeanInterface() {
        return perInterface.getMBeanInterface();
    }
    private final MBeanInfo mbeanInfo;
    private final Object resource;
    private final PerInterface<M> perInterface;
}

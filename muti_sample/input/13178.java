public final class BeanUtils {
    private BeanUtils() {
    }
    public static BeanDescriptor getBeanDescriptor(Class type) {
        try {
            return Introspector.getBeanInfo(type).getBeanDescriptor();
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    public static PropertyDescriptor[] getPropertyDescriptors(Class type) {
        try {
            return Introspector.getBeanInfo(type).getPropertyDescriptors();
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    public static EventSetDescriptor[] getEventSetDescriptors(Class type) {
        try {
            return Introspector.getBeanInfo(type).getEventSetDescriptors();
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    public static EventSetDescriptor findEventSetDescriptor(Class type, String name) {
        EventSetDescriptor[] esds = getEventSetDescriptors(type);
        for (EventSetDescriptor esd : esds) {
            if (esd.getName().equals(name)) {
                return esd;
            }
        }
        return null;
    }
    public static PropertyDescriptor findPropertyDescriptor(Class type, String name) {
        PropertyDescriptor[] pds = getPropertyDescriptors(type);
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals(name)) {
                return pd;
            }
        }
        return null;
    }
    public static EventSetDescriptor getEventSetDescriptor(Class type, String name) {
        EventSetDescriptor esd = findEventSetDescriptor(type, name);
        if (esd != null) {
            return esd;
        }
        throw new Error("could not find event set '" + name + "' in " + type);
    }
    public static PropertyDescriptor getPropertyDescriptor(Class type, String name) {
        PropertyDescriptor pd = findPropertyDescriptor(type, name);
        if (pd != null) {
            return pd;
        }
        throw new Error("could not find property '" + name + "' in " + type);
    }
    public static IndexedPropertyDescriptor getIndexedPropertyDescriptor(Class type, String name) {
        PropertyDescriptor pd = findPropertyDescriptor(type, name);
        if (pd instanceof IndexedPropertyDescriptor) {
            return (IndexedPropertyDescriptor) pd;
        }
        reportPropertyDescriptor(pd);
        throw new Error("could not find indexed property '" + name + "' in " + type);
    }
    public static void reportPropertyDescriptor(PropertyDescriptor pd) {
        System.out.println("property name:  " + pd.getName());
        System.out.println("         type:  " + pd.getPropertyType());
        System.out.println("         read:  " + pd.getReadMethod());
        System.out.println("         write: " + pd.getWriteMethod());
        if (pd instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
            System.out.println(" indexed type: " + ipd.getIndexedPropertyType());
            System.out.println(" indexed read: " + ipd.getIndexedReadMethod());
            System.out.println(" indexed write: " + ipd.getIndexedWriteMethod());
        }
    }
    public static void reportEventSetDescriptor(EventSetDescriptor esd) {
        System.out.println("event set name:   " + esd.getName());
        System.out.println(" listener type:   " + esd.getListenerType());
        System.out.println("   method get:    " + esd.getGetListenerMethod());
        System.out.println("   method add:    " + esd.getAddListenerMethod());
        System.out.println("   method remove: " + esd.getRemoveListenerMethod());
    }
    public static void reportMethodDescriptor(MethodDescriptor md) {
        System.out.println("method name: " + md.getName());
        System.out.println("     method: " + md.getMethod());
    }
}

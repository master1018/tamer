final class PropertyElementHandler extends AccessorElementHandler {
    static final String GETTER = "get"; 
    static final String SETTER = "set"; 
    private Integer index;
    @Override
    public void addAttribute(String name, String value) {
        if (name.equals("index")) { 
            this.index = Integer.valueOf(value);
        } else {
            super.addAttribute(name, value);
        }
    }
    @Override
    protected boolean isArgument() {
        return false; 
    }
    @Override
    protected Object getValue(String name) {
        try {
            return getPropertyValue(getContextBean(), name, this.index);
        }
        catch (Exception exception) {
            getOwner().handleException(exception);
        }
        return null;
    }
    @Override
    protected void setValue(String name, Object value) {
        try {
            setPropertyValue(getContextBean(), name, this.index, value);
        }
        catch (Exception exception) {
            getOwner().handleException(exception);
        }
    }
    private static Object getPropertyValue(Object bean, String name, Integer index) throws IllegalAccessException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
        Class<?> type = bean.getClass();
        if (index == null) {
            return findGetter(type, name).invoke(bean);
        } else if (type.isArray() && (name == null)) {
            return Array.get(bean, index);
        } else {
            return findGetter(type, name, int.class).invoke(bean, index);
        }
    }
    private static void setPropertyValue(Object bean, String name, Integer index, Object value) throws IllegalAccessException, IntrospectionException, InvocationTargetException, NoSuchMethodException {
        Class<?> type = bean.getClass();
        Class<?> param = (value != null)
                ? value.getClass()
                : null;
        if (index == null) {
            findSetter(type, name, param).invoke(bean, value);
        } else if (type.isArray() && (name == null)) {
            Array.set(bean, index, value);
        } else {
            findSetter(type, name, int.class, param).invoke(bean, index, value);
        }
    }
    private static Method findGetter(Class<?> type, String name, Class<?>...args) throws IntrospectionException, NoSuchMethodException {
        if (name == null) {
            return MethodFinder.findInstanceMethod(type, GETTER, args);
        }
        PropertyDescriptor pd = getProperty(type, name);
        if (args.length == 0) {
            Method method = pd.getReadMethod();
            if (method != null) {
                return method;
            }
        } else if (pd instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
            Method method = ipd.getIndexedReadMethod();
            if (method != null) {
                return method;
            }
        }
        throw new IntrospectionException("Could not find getter for the " + name + " property");
    }
    private static Method findSetter(Class<?> type, String name, Class<?>...args) throws IntrospectionException, NoSuchMethodException {
        if (name == null) {
            return MethodFinder.findInstanceMethod(type, SETTER, args);
        }
        PropertyDescriptor pd = getProperty(type, name);
        if (args.length == 1) {
            Method method = pd.getWriteMethod();
            if (method != null) {
                return method;
            }
        } else if (pd instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
            Method method = ipd.getIndexedWriteMethod();
            if (method != null) {
                return method;
            }
        }
        throw new IntrospectionException("Could not find setter for the " + name + " property");
    }
    private static PropertyDescriptor getProperty(Class<?> type, String name) throws IntrospectionException {
        for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
            if (name.equals(pd.getName())) {
                return pd;
            }
        }
        throw new IntrospectionException("Could not find the " + name + " property descriptor");
    }
}

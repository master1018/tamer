public class IndexedPropertyDescriptor extends PropertyDescriptor {
    private Method indexedGetter;
    private Method indexedSetter;
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass,
            String getterName, String setterName, String indexedGetterName,
            String indexedSetterName) throws IntrospectionException {
        super(propertyName, beanClass, getterName, setterName);
        if (indexedGetterName == null && indexedSetterName == null &&
                (getterName != null || setterName != null)) {
            throw new IntrospectionException(Messages.getString("beans.50"));
        }
        setIndexedReadMethod(beanClass, indexedGetterName);
        setIndexedWriteMethod(beanClass, indexedSetterName);
    }
    public IndexedPropertyDescriptor(String propertyName, Method getter, Method setter,
            Method indexedGetter, Method indexedSetter) throws IntrospectionException {
        super(propertyName, getter, setter);
        if (indexedGetter == null && indexedSetter == null &&
                (getter != null || setter != null)) {
            throw new IntrospectionException(Messages.getString("beans.50"));
        }
        setIndexedReadMethod(indexedGetter);
        setIndexedWriteMethod(indexedSetter);
    }
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass)
            throws IntrospectionException {
        super(propertyName, beanClass, null, null);
        String getterName;
        String setterName;
        String indexedGetterName;
        String indexedSetterName;
        getterName = createDefaultMethodName(propertyName, "get"); 
        if (hasMethod(beanClass, getterName)) {
            setReadMethod(beanClass, getterName);
        }
        setterName = createDefaultMethodName(propertyName, "set"); 
        if (hasMethod(beanClass, setterName)) {
            setWriteMethod(beanClass, setterName);
        }
        indexedGetterName = createDefaultMethodName(propertyName, "get"); 
        if (hasMethod(beanClass, indexedGetterName)) {
            setIndexedReadMethod(beanClass, indexedGetterName);
        }
        indexedSetterName = createDefaultMethodName(propertyName, "set"); 
        if (hasMethod(beanClass, indexedSetterName)) {
            setIndexedWriteMethod(beanClass, indexedSetterName);
        }
        if (indexedGetter == null && indexedSetter == null &&
                getReadMethod() == null && getWriteMethod() == null) {
            throw new IntrospectionException(
                    Messages.getString("beans.01", propertyName)); 
        }
        if (indexedGetter == null && indexedSetter == null) {
            throw new IntrospectionException(Messages.getString("beans.50"));
        }
    }
    public void setIndexedReadMethod(Method indexedGetter) throws IntrospectionException {
        if (indexedGetter != null) {
            int modifiers = indexedGetter.getModifiers();
            Class<?>[] parameterTypes;
            Class<?> returnType;
            Class<?> indexedPropertyType;
            if (!Modifier.isPublic(modifiers)) {
                throw new IntrospectionException(Messages.getString("beans.21")); 
            }
            parameterTypes = indexedGetter.getParameterTypes();
            if (parameterTypes.length != 1) {
                throw new IntrospectionException(Messages.getString("beans.22")); 
            }
            if (!parameterTypes[0].equals(int.class)) {
                throw new IntrospectionException(Messages.getString("beans.23")); 
            }
            returnType = indexedGetter.getReturnType();
            indexedPropertyType = getIndexedPropertyType();
            if ((indexedPropertyType != null) && !returnType.equals(indexedPropertyType)) {
                throw new IntrospectionException(Messages.getString("beans.24")); 
            }
        }
        this.indexedGetter = indexedGetter;
    }
    public void setIndexedWriteMethod(Method indexedSetter) throws IntrospectionException {
        if (indexedSetter != null) {
            int modifiers = indexedSetter.getModifiers();
            Class<?>[] parameterTypes;
            Class<?> firstParameterType;
            Class<?> secondParameterType;
            Class<?> propType;
            if (!Modifier.isPublic(modifiers)) {
                throw new IntrospectionException(Messages.getString("beans.25")); 
            }
            parameterTypes = indexedSetter.getParameterTypes();
            if (parameterTypes.length != 2) {
                throw new IntrospectionException(Messages.getString("beans.26")); 
            }
            firstParameterType = parameterTypes[0];
            if (!firstParameterType.equals(int.class)) {
                throw new IntrospectionException(Messages.getString("beans.27")); 
            }
            secondParameterType = parameterTypes[1];
            propType = getIndexedPropertyType();
            if (propType != null && !secondParameterType.equals(propType)) {
                throw new IntrospectionException(Messages.getString("beans.28")); 
            }
        }
        this.indexedSetter = indexedSetter;
    }
    public Method getIndexedWriteMethod() {
        return indexedSetter;
    }
    public Method getIndexedReadMethod() {
        return indexedGetter;
    }
    @Override
    public boolean equals(Object obj) {
        boolean result = super.equals(obj);
        if (result) {
            IndexedPropertyDescriptor pd = (IndexedPropertyDescriptor) obj;
            if (indexedGetter != null) {
                result = indexedGetter.equals(pd.getIndexedReadMethod());
            } else if (result && indexedGetter == null) {
                result = pd.getIndexedReadMethod() == null;
            }
            if (result) {
                if (indexedSetter != null) {
                    result = indexedSetter.equals(pd.getIndexedWriteMethod());
                } else if (indexedSetter == null) {
                    result = pd.getIndexedWriteMethod() == null;
                }
            }
        }
        return result;
    }
    public Class<?> getIndexedPropertyType() {
        Class<?> result = null;
        if (indexedGetter != null) {
            result = indexedGetter.getReturnType();
        } else if (indexedSetter != null) {
            Class<?>[] parameterTypes = indexedSetter.getParameterTypes();
            result = parameterTypes[1];
        }
        return result;
    }
    private void setIndexedReadMethod(Class<?> beanClass, String indexedGetterName) {
        Method[] getters = findMethods(beanClass, indexedGetterName);
        boolean result = false;
        for (Method element : getters) {
            try {
                setIndexedReadMethod(element);
                result = true;
            } catch (IntrospectionException ie) {}
            if (result) {
                break;
            }
        }
    }
    private void setIndexedWriteMethod(Class<?> beanClass, String indexedSetterName) {
        Method[] setters = findMethods(beanClass, indexedSetterName);
        boolean result = false;
        for (Method element : setters) {
            try {
                setIndexedWriteMethod(element);
                result = true;
            } catch (IntrospectionException ie) {}
            if (result) {
                break;
            }
        }
    }
}

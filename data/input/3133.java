public class IndexedPropertyDescriptor extends PropertyDescriptor {
    private Reference<Class> indexedPropertyTypeRef;
    private Reference<Method> indexedReadMethodRef;
    private Reference<Method> indexedWriteMethodRef;
    private String indexedReadMethodName;
    private String indexedWriteMethodName;
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass)
                throws IntrospectionException {
        this(propertyName, beanClass,
             Introspector.GET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.GET_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName));
    }
    public IndexedPropertyDescriptor(String propertyName, Class<?> beanClass,
                String readMethodName, String writeMethodName,
                String indexedReadMethodName, String indexedWriteMethodName)
                throws IntrospectionException {
        super(propertyName, beanClass, readMethodName, writeMethodName);
        this.indexedReadMethodName = indexedReadMethodName;
        if (indexedReadMethodName != null && getIndexedReadMethod() == null) {
            throw new IntrospectionException("Method not found: " + indexedReadMethodName);
        }
        this.indexedWriteMethodName = indexedWriteMethodName;
        if (indexedWriteMethodName != null && getIndexedWriteMethod() == null) {
            throw new IntrospectionException("Method not found: " + indexedWriteMethodName);
        }
        findIndexedPropertyType(getIndexedReadMethod(), getIndexedWriteMethod());
    }
    public IndexedPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod,
                                            Method indexedReadMethod, Method indexedWriteMethod)
                throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);
        setIndexedReadMethod0(indexedReadMethod);
        setIndexedWriteMethod0(indexedWriteMethod);
        setIndexedPropertyType(findIndexedPropertyType(indexedReadMethod, indexedWriteMethod));
    }
    IndexedPropertyDescriptor(Class<?> bean, String base, Method read, Method write, Method readIndexed, Method writeIndexed) throws IntrospectionException {
        super(bean, base, read, write);
        setIndexedReadMethod0(readIndexed);
        setIndexedWriteMethod0(writeIndexed);
        setIndexedPropertyType(findIndexedPropertyType(readIndexed, writeIndexed));
    }
    public synchronized Method getIndexedReadMethod() {
        Method indexedReadMethod = getIndexedReadMethod0();
        if (indexedReadMethod == null) {
            Class cls = getClass0();
            if (cls == null ||
                (indexedReadMethodName == null && indexedReadMethodRef == null)) {
                return null;
            }
            if (indexedReadMethodName == null) {
                Class type = getIndexedPropertyType0();
                if (type == boolean.class || type == null) {
                    indexedReadMethodName = Introspector.IS_PREFIX + getBaseName();
                } else {
                    indexedReadMethodName = Introspector.GET_PREFIX + getBaseName();
                }
            }
            Class[] args = { int.class };
            indexedReadMethod = Introspector.findMethod(cls, indexedReadMethodName, 1, args);
            if (indexedReadMethod == null) {
                indexedReadMethodName = Introspector.GET_PREFIX + getBaseName();
                indexedReadMethod = Introspector.findMethod(cls, indexedReadMethodName, 1, args);
            }
            setIndexedReadMethod0(indexedReadMethod);
        }
        return indexedReadMethod;
    }
    public synchronized void setIndexedReadMethod(Method readMethod)
        throws IntrospectionException {
        setIndexedPropertyType(findIndexedPropertyType(readMethod,
                                                       getIndexedWriteMethod0()));
        setIndexedReadMethod0(readMethod);
    }
    private void setIndexedReadMethod0(Method readMethod) {
        if (readMethod == null) {
            indexedReadMethodName = null;
            indexedReadMethodRef = null;
            return;
        }
        setClass0(readMethod.getDeclaringClass());
        indexedReadMethodName = readMethod.getName();
        this.indexedReadMethodRef = getSoftReference(readMethod);
        setTransient(readMethod.getAnnotation(Transient.class));
    }
    public synchronized Method getIndexedWriteMethod() {
        Method indexedWriteMethod = getIndexedWriteMethod0();
        if (indexedWriteMethod == null) {
            Class cls = getClass0();
            if (cls == null ||
                (indexedWriteMethodName == null && indexedWriteMethodRef == null)) {
                return null;
            }
            Class type = getIndexedPropertyType0();
            if (type == null) {
                try {
                    type = findIndexedPropertyType(getIndexedReadMethod(), null);
                    setIndexedPropertyType(type);
                } catch (IntrospectionException ex) {
                    Class propType = getPropertyType();
                    if (propType.isArray()) {
                        type = propType.getComponentType();
                    }
                }
            }
            if (indexedWriteMethodName == null) {
                indexedWriteMethodName = Introspector.SET_PREFIX + getBaseName();
            }
            Class[] args = (type == null) ? null : new Class[] { int.class, type };
            indexedWriteMethod = Introspector.findMethod(cls, indexedWriteMethodName, 2, args);
            if (indexedWriteMethod != null) {
                if (!indexedWriteMethod.getReturnType().equals(void.class)) {
                    indexedWriteMethod = null;
                }
            }
            setIndexedWriteMethod0(indexedWriteMethod);
        }
        return indexedWriteMethod;
    }
    public synchronized void setIndexedWriteMethod(Method writeMethod)
        throws IntrospectionException {
        Class type = findIndexedPropertyType(getIndexedReadMethod(),
                                             writeMethod);
        setIndexedPropertyType(type);
        setIndexedWriteMethod0(writeMethod);
    }
    private void setIndexedWriteMethod0(Method writeMethod) {
        if (writeMethod == null) {
            indexedWriteMethodName = null;
            indexedWriteMethodRef = null;
            return;
        }
        setClass0(writeMethod.getDeclaringClass());
        indexedWriteMethodName = writeMethod.getName();
        this.indexedWriteMethodRef = getSoftReference(writeMethod);
        setTransient(writeMethod.getAnnotation(Transient.class));
    }
    public synchronized Class<?> getIndexedPropertyType() {
        Class type = getIndexedPropertyType0();
        if (type == null) {
            try {
                type = findIndexedPropertyType(getIndexedReadMethod(),
                                               getIndexedWriteMethod());
                setIndexedPropertyType(type);
            } catch (IntrospectionException ex) {
            }
        }
        return type;
    }
    private void setIndexedPropertyType(Class type) {
        this.indexedPropertyTypeRef = getWeakReference(type);
    }
    private Class getIndexedPropertyType0() {
        return (this.indexedPropertyTypeRef != null)
                ? this.indexedPropertyTypeRef.get()
                : null;
    }
    private Method getIndexedReadMethod0() {
        return (this.indexedReadMethodRef != null)
                ? this.indexedReadMethodRef.get()
                : null;
    }
    private Method getIndexedWriteMethod0() {
        return (this.indexedWriteMethodRef != null)
                ? this.indexedWriteMethodRef.get()
                : null;
    }
    private Class findIndexedPropertyType(Method indexedReadMethod,
                                          Method indexedWriteMethod)
        throws IntrospectionException {
        Class indexedPropertyType = null;
        if (indexedReadMethod != null) {
            Class params[] = getParameterTypes(getClass0(), indexedReadMethod);
            if (params.length != 1) {
                throw new IntrospectionException("bad indexed read method arg count");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("non int index to indexed read method");
            }
            indexedPropertyType = getReturnType(getClass0(), indexedReadMethod);
            if (indexedPropertyType == Void.TYPE) {
                throw new IntrospectionException("indexed read method returns void");
            }
        }
        if (indexedWriteMethod != null) {
            Class params[] = getParameterTypes(getClass0(), indexedWriteMethod);
            if (params.length != 2) {
                throw new IntrospectionException("bad indexed write method arg count");
            }
            if (params[0] != Integer.TYPE) {
                throw new IntrospectionException("non int index to indexed write method");
            }
            if (indexedPropertyType != null && indexedPropertyType != params[1]) {
                throw new IntrospectionException(
                                                 "type mismatch between indexed read and indexed write methods: "
                                                 + getName());
            }
            indexedPropertyType = params[1];
        }
        Class propertyType = getPropertyType();
        if (propertyType != null && (!propertyType.isArray() ||
                                     propertyType.getComponentType() != indexedPropertyType)) {
            throw new IntrospectionException("type mismatch between indexed and non-indexed methods: "
                                             + getName());
        }
        return indexedPropertyType;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor other = (IndexedPropertyDescriptor)obj;
            Method otherIndexedReadMethod = other.getIndexedReadMethod();
            Method otherIndexedWriteMethod = other.getIndexedWriteMethod();
            if (!compareMethods(getIndexedReadMethod(), otherIndexedReadMethod)) {
                return false;
            }
            if (!compareMethods(getIndexedWriteMethod(), otherIndexedWriteMethod)) {
                return false;
            }
            if (getIndexedPropertyType() != other.getIndexedPropertyType()) {
                return false;
            }
            return super.equals(obj);
        }
        return false;
    }
    IndexedPropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x,y);
        if (x instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor ix = (IndexedPropertyDescriptor)x;
            try {
                Method xr = ix.getIndexedReadMethod();
                if (xr != null) {
                    setIndexedReadMethod(xr);
                }
                Method xw = ix.getIndexedWriteMethod();
                if (xw != null) {
                    setIndexedWriteMethod(xw);
                }
            } catch (IntrospectionException ex) {
                throw new AssertionError(ex);
            }
        }
        if (y instanceof IndexedPropertyDescriptor) {
            IndexedPropertyDescriptor iy = (IndexedPropertyDescriptor)y;
            try {
                Method yr = iy.getIndexedReadMethod();
                if (yr != null && yr.getDeclaringClass() == getClass0()) {
                    setIndexedReadMethod(yr);
                }
                Method yw = iy.getIndexedWriteMethod();
                if (yw != null && yw.getDeclaringClass() == getClass0()) {
                    setIndexedWriteMethod(yw);
                }
            } catch (IntrospectionException ex) {
                throw new AssertionError(ex);
            }
        }
    }
    IndexedPropertyDescriptor(IndexedPropertyDescriptor old) {
        super(old);
        indexedReadMethodRef = old.indexedReadMethodRef;
        indexedWriteMethodRef = old.indexedWriteMethodRef;
        indexedPropertyTypeRef = old.indexedPropertyTypeRef;
        indexedWriteMethodName = old.indexedWriteMethodName;
        indexedReadMethodName = old.indexedReadMethodName;
    }
    public int hashCode() {
        int result = super.hashCode();
        result = 37 * result + ((indexedWriteMethodName == null) ? 0 :
                                indexedWriteMethodName.hashCode());
        result = 37 * result + ((indexedReadMethodName == null) ? 0 :
                                indexedReadMethodName.hashCode());
        result = 37 * result + ((getIndexedPropertyType() == null) ? 0 :
                                getIndexedPropertyType().hashCode());
        return result;
    }
    void appendTo(StringBuilder sb) {
        super.appendTo(sb);
        appendTo(sb, "indexedPropertyType", this.indexedPropertyTypeRef);
        appendTo(sb, "indexedReadMethod", this.indexedReadMethodRef);
        appendTo(sb, "indexedWriteMethod", this.indexedWriteMethodRef);
    }
}

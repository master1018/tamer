public class PropertyDescriptor extends FeatureDescriptor {
    private Reference<Class> propertyTypeRef;
    private Reference<Method> readMethodRef;
    private Reference<Method> writeMethodRef;
    private Reference<Class> propertyEditorClassRef;
    private boolean bound;
    private boolean constrained;
    private String baseName;
    private String writeMethodName;
    private String readMethodName;
    public PropertyDescriptor(String propertyName, Class<?> beanClass)
                throws IntrospectionException {
        this(propertyName, beanClass,
             Introspector.IS_PREFIX + NameGenerator.capitalize(propertyName),
             Introspector.SET_PREFIX + NameGenerator.capitalize(propertyName));
    }
    public PropertyDescriptor(String propertyName, Class<?> beanClass,
                String readMethodName, String writeMethodName)
                throws IntrospectionException {
        if (beanClass == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        if ("".equals(readMethodName) || "".equals(writeMethodName)) {
            throw new IntrospectionException("read or write method name should not be the empty string");
        }
        setName(propertyName);
        setClass0(beanClass);
        this.readMethodName = readMethodName;
        if (readMethodName != null && getReadMethod() == null) {
            throw new IntrospectionException("Method not found: " + readMethodName);
        }
        this.writeMethodName = writeMethodName;
        if (writeMethodName != null && getWriteMethod() == null) {
            throw new IntrospectionException("Method not found: " + writeMethodName);
        }
        Class[] args = { PropertyChangeListener.class };
        this.bound = null != Introspector.findMethod(beanClass, "addPropertyChangeListener", args.length, args);
    }
    public PropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
                throws IntrospectionException {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IntrospectionException("bad property name");
        }
        setName(propertyName);
        setReadMethod(readMethod);
        setWriteMethod(writeMethod);
    }
    PropertyDescriptor(Class<?> bean, String base, Method read, Method write) throws IntrospectionException {
        if (bean == null) {
            throw new IntrospectionException("Target Bean class is null");
        }
        setClass0(bean);
        setName(Introspector.decapitalize(base));
        setReadMethod(read);
        setWriteMethod(write);
        this.baseName = base;
    }
    public synchronized Class<?> getPropertyType() {
        Class type = getPropertyType0();
        if (type  == null) {
            try {
                type = findPropertyType(getReadMethod(), getWriteMethod());
                setPropertyType(type);
            } catch (IntrospectionException ex) {
            }
        }
        return type;
    }
    private void setPropertyType(Class type) {
        this.propertyTypeRef = getWeakReference(type);
    }
    private Class getPropertyType0() {
        return (this.propertyTypeRef != null)
                ? this.propertyTypeRef.get()
                : null;
    }
    public synchronized Method getReadMethod() {
        Method readMethod = getReadMethod0();
        if (readMethod == null) {
            Class cls = getClass0();
            if (cls == null || (readMethodName == null && readMethodRef == null)) {
                return null;
            }
            if (readMethodName == null) {
                Class type = getPropertyType0();
                if (type == boolean.class || type == null) {
                    readMethodName = Introspector.IS_PREFIX + getBaseName();
                } else {
                    readMethodName = Introspector.GET_PREFIX + getBaseName();
                }
            }
            readMethod = Introspector.findMethod(cls, readMethodName, 0);
            if (readMethod == null) {
                readMethodName = Introspector.GET_PREFIX + getBaseName();
                readMethod = Introspector.findMethod(cls, readMethodName, 0);
            }
            try {
                setReadMethod(readMethod);
            } catch (IntrospectionException ex) {
            }
        }
        return readMethod;
    }
    public synchronized void setReadMethod(Method readMethod)
                                throws IntrospectionException {
        if (readMethod == null) {
            readMethodName = null;
            readMethodRef = null;
            return;
        }
        setPropertyType(findPropertyType(readMethod, getWriteMethod0()));
        setClass0(readMethod.getDeclaringClass());
        readMethodName = readMethod.getName();
        this.readMethodRef = getSoftReference(readMethod);
        setTransient(readMethod.getAnnotation(Transient.class));
    }
    public synchronized Method getWriteMethod() {
        Method writeMethod = getWriteMethod0();
        if (writeMethod == null) {
            Class cls = getClass0();
            if (cls == null || (writeMethodName == null && writeMethodRef == null)) {
                return null;
            }
            Class type = getPropertyType0();
            if (type == null) {
                try {
                    type = findPropertyType(getReadMethod(), null);
                    setPropertyType(type);
                } catch (IntrospectionException ex) {
                    return null;
                }
            }
            if (writeMethodName == null) {
                writeMethodName = Introspector.SET_PREFIX + getBaseName();
            }
            Class[] args = (type == null) ? null : new Class[] { type };
            writeMethod = Introspector.findMethod(cls, writeMethodName, 1, args);
            if (writeMethod != null) {
                if (!writeMethod.getReturnType().equals(void.class)) {
                    writeMethod = null;
                }
            }
            try {
                setWriteMethod(writeMethod);
            } catch (IntrospectionException ex) {
            }
        }
        return writeMethod;
    }
    public synchronized void setWriteMethod(Method writeMethod)
                                throws IntrospectionException {
        if (writeMethod == null) {
            writeMethodName = null;
            writeMethodRef = null;
            return;
        }
        setPropertyType(findPropertyType(getReadMethod(), writeMethod));
        setClass0(writeMethod.getDeclaringClass());
        writeMethodName = writeMethod.getName();
        this.writeMethodRef = getSoftReference(writeMethod);
        setTransient(writeMethod.getAnnotation(Transient.class));
    }
    private Method getReadMethod0() {
        return (this.readMethodRef != null)
                ? this.readMethodRef.get()
                : null;
    }
    private Method getWriteMethod0() {
        return (this.writeMethodRef != null)
                ? this.writeMethodRef.get()
                : null;
    }
    void setClass0(Class clz) {
        if (getClass0() != null && clz.isAssignableFrom(getClass0())) {
            return;
        }
        super.setClass0(clz);
    }
    public boolean isBound() {
        return bound;
    }
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    public boolean isConstrained() {
        return constrained;
    }
    public void setConstrained(boolean constrained) {
        this.constrained = constrained;
    }
    public void setPropertyEditorClass(Class<?> propertyEditorClass) {
        this.propertyEditorClassRef = getWeakReference((Class)propertyEditorClass);
    }
    public Class<?> getPropertyEditorClass() {
        return (this.propertyEditorClassRef != null)
                ? this.propertyEditorClassRef.get()
                : null;
    }
    public PropertyEditor createPropertyEditor(Object bean) {
        Object editor = null;
        Class cls = getPropertyEditorClass();
        if (cls != null) {
            Constructor ctor = null;
            if (bean != null) {
                try {
                    ctor = cls.getConstructor(new Class[] { Object.class });
                } catch (Exception ex) {
                }
            }
            try {
                if (ctor == null) {
                    editor = cls.newInstance();
                } else {
                    editor = ctor.newInstance(new Object[] { bean });
                }
            } catch (Exception ex) {
                throw new RuntimeException("PropertyEditor not instantiated",
                                           ex);
            }
        }
        return (PropertyEditor)editor;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj instanceof PropertyDescriptor) {
            PropertyDescriptor other = (PropertyDescriptor)obj;
            Method otherReadMethod = other.getReadMethod();
            Method otherWriteMethod = other.getWriteMethod();
            if (!compareMethods(getReadMethod(), otherReadMethod)) {
                return false;
            }
            if (!compareMethods(getWriteMethod(), otherWriteMethod)) {
                return false;
            }
            if (getPropertyType() == other.getPropertyType() &&
                getPropertyEditorClass() == other.getPropertyEditorClass() &&
                bound == other.isBound() && constrained == other.isConstrained() &&
                writeMethodName == other.writeMethodName &&
                readMethodName == other.readMethodName) {
                return true;
            }
        }
        return false;
    }
    boolean compareMethods(Method a, Method b) {
        if ((a == null) != (b == null)) {
            return false;
        }
        if (a != null && b != null) {
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
    PropertyDescriptor(PropertyDescriptor x, PropertyDescriptor y) {
        super(x,y);
        if (y.baseName != null) {
            baseName = y.baseName;
        } else {
            baseName = x.baseName;
        }
        if (y.readMethodName != null) {
            readMethodName = y.readMethodName;
        } else {
            readMethodName = x.readMethodName;
        }
        if (y.writeMethodName != null) {
            writeMethodName = y.writeMethodName;
        } else {
            writeMethodName = x.writeMethodName;
        }
        if (y.propertyTypeRef != null) {
            propertyTypeRef = y.propertyTypeRef;
        } else {
            propertyTypeRef = x.propertyTypeRef;
        }
        Method xr = x.getReadMethod();
        Method yr = y.getReadMethod();
        try {
            if (yr != null && yr.getDeclaringClass() == getClass0()) {
                setReadMethod(yr);
            } else {
                setReadMethod(xr);
            }
        } catch (IntrospectionException ex) {
        }
        if (xr != null && yr != null &&
                   xr.getDeclaringClass() == yr.getDeclaringClass() &&
                   getReturnType(getClass0(), xr) == boolean.class &&
                   getReturnType(getClass0(), yr) == boolean.class &&
                   xr.getName().indexOf(Introspector.IS_PREFIX) == 0 &&
                   yr.getName().indexOf(Introspector.GET_PREFIX) == 0) {
            try {
                setReadMethod(xr);
            } catch (IntrospectionException ex) {
            }
        }
        Method xw = x.getWriteMethod();
        Method yw = y.getWriteMethod();
        try {
            if (yw != null && yw.getDeclaringClass() == getClass0()) {
                setWriteMethod(yw);
            } else {
                setWriteMethod(xw);
            }
        } catch (IntrospectionException ex) {
        }
        if (y.getPropertyEditorClass() != null) {
            setPropertyEditorClass(y.getPropertyEditorClass());
        } else {
            setPropertyEditorClass(x.getPropertyEditorClass());
        }
        bound = x.bound | y.bound;
        constrained = x.constrained | y.constrained;
    }
    PropertyDescriptor(PropertyDescriptor old) {
        super(old);
        propertyTypeRef = old.propertyTypeRef;
        readMethodRef = old.readMethodRef;
        writeMethodRef = old.writeMethodRef;
        propertyEditorClassRef = old.propertyEditorClassRef;
        writeMethodName = old.writeMethodName;
        readMethodName = old.readMethodName;
        baseName = old.baseName;
        bound = old.bound;
        constrained = old.constrained;
    }
    private Class findPropertyType(Method readMethod, Method writeMethod)
        throws IntrospectionException {
        Class propertyType = null;
        try {
            if (readMethod != null) {
                Class[] params = getParameterTypes(getClass0(), readMethod);
                if (params.length != 0) {
                    throw new IntrospectionException("bad read method arg count: "
                                                     + readMethod);
                }
                propertyType = getReturnType(getClass0(), readMethod);
                if (propertyType == Void.TYPE) {
                    throw new IntrospectionException("read method " +
                                        readMethod.getName() + " returns void");
                }
            }
            if (writeMethod != null) {
                Class params[] = getParameterTypes(getClass0(), writeMethod);
                if (params.length != 1) {
                    throw new IntrospectionException("bad write method arg count: "
                                                     + writeMethod);
                }
                if (propertyType != null && propertyType != params[0]) {
                    throw new IntrospectionException("type mismatch between read and write methods");
                }
                propertyType = params[0];
            }
        } catch (IntrospectionException ex) {
            throw ex;
        }
        return propertyType;
    }
    public int hashCode() {
        int result = 7;
        result = 37 * result + ((getPropertyType() == null) ? 0 :
                                getPropertyType().hashCode());
        result = 37 * result + ((getReadMethod() == null) ? 0 :
                                getReadMethod().hashCode());
        result = 37 * result + ((getWriteMethod() == null) ? 0 :
                                getWriteMethod().hashCode());
        result = 37 * result + ((getPropertyEditorClass() == null) ? 0 :
                                getPropertyEditorClass().hashCode());
        result = 37 * result + ((writeMethodName == null) ? 0 :
                                writeMethodName.hashCode());
        result = 37 * result + ((readMethodName == null) ? 0 :
                                readMethodName.hashCode());
        result = 37 * result + getName().hashCode();
        result = 37 * result + ((bound == false) ? 0 : 1);
        result = 37 * result + ((constrained == false) ? 0 : 1);
        return result;
    }
    String getBaseName() {
        if (baseName == null) {
            baseName = NameGenerator.capitalize(getName());
        }
        return baseName;
    }
    void appendTo(StringBuilder sb) {
        appendTo(sb, "bound", this.bound);
        appendTo(sb, "constrained", this.constrained);
        appendTo(sb, "propertyEditorClass", this.propertyEditorClassRef);
        appendTo(sb, "propertyType", this.propertyTypeRef);
        appendTo(sb, "readMethod", this.readMethodRef);
        appendTo(sb, "writeMethod", this.writeMethodRef);
    }
}

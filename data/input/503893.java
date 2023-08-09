public class ObjectStreamClass implements Serializable {
    private static final long serialVersionUID = -6120832682080437368L;
    private static final String UID_FIELD_NAME = "serialVersionUID"; 
    static final long CONSTRUCTOR_IS_NOT_RESOLVED = -1;
    private static final int CLASS_MODIFIERS_MASK;
    private static final int FIELD_MODIFIERS_MASK;
    private static final int METHOD_MODIFIERS_MASK;
    private static final Class<?>[] READ_PARAM_TYPES;
    private static final Class<?>[] WRITE_PARAM_TYPES;
    static final Class<?>[] EMPTY_CONSTRUCTOR_PARAM_TYPES;
    private static final Class<Void> VOID_CLASS;
    static final Class<?>[] UNSHARED_PARAM_TYPES;
    private static native void oneTimeInitialization();
    static {
        oneTimeInitialization();
        CLASS_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.FINAL
                | Modifier.INTERFACE | Modifier.ABSTRACT;
        FIELD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.VOLATILE | Modifier.TRANSIENT;
        METHOD_MODIFIERS_MASK = Modifier.PUBLIC | Modifier.PRIVATE
                | Modifier.PROTECTED | Modifier.STATIC | Modifier.FINAL
                | Modifier.SYNCHRONIZED | Modifier.NATIVE | Modifier.ABSTRACT
                | Modifier.STRICT;
        READ_PARAM_TYPES = new Class[1];
        WRITE_PARAM_TYPES = new Class[1];
        READ_PARAM_TYPES[0] = ObjectInputStream.class;
        WRITE_PARAM_TYPES[0] = ObjectOutputStream.class;
        EMPTY_CONSTRUCTOR_PARAM_TYPES = new Class[0];
        VOID_CLASS = Void.TYPE;
        UNSHARED_PARAM_TYPES = new Class[1];
        UNSHARED_PARAM_TYPES[0] = Object.class;
    }
    public static final ObjectStreamField[] NO_FIELDS = new ObjectStreamField[0];
    static final Class<?> ARRAY_OF_FIELDS;
    static {
        try {
            ARRAY_OF_FIELDS = Class.forName("[Ljava.io.ObjectStreamField;"); 
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    private static final String CLINIT_NAME = "<clinit>"; 
    private static final int CLINIT_MODIFIERS = Modifier.STATIC;
    private static final String CLINIT_SIGNATURE = "()V"; 
    private static final Class<Serializable> SERIALIZABLE = Serializable.class;
    private static final Class<Externalizable> EXTERNALIZABLE = Externalizable.class;
    static final Class<String> STRINGCLASS = String.class;
    static final Class<?> CLASSCLASS = Class.class;
    static final Class<ObjectStreamClass> OBJECTSTREAMCLASSCLASS = ObjectStreamClass.class;
    private transient Method methodWriteReplace;
    private transient Method methodReadResolve;
    private transient Method methodWriteObject;
    private transient Method methodReadObject;
    private transient Method methodReadObjectNoData;
    private transient boolean arePropertiesResolved;
    private transient boolean isSerializable;
    private transient boolean isExternalizable;
    private transient boolean isProxy;
    private transient boolean isEnum;
    private transient String className;
    private transient WeakReference<Class<?>> resolvedClass;
    private transient long svUID;
    private transient byte flags;
    private transient ObjectStreamClass superclass;
    private transient ObjectStreamField[] fields;
    private transient ObjectStreamField[] loadFields;
    private transient long constructor = CONSTRUCTOR_IS_NOT_RESOLVED;
    void setConstructor(long newConstructor) {
        constructor = newConstructor;
    }
    long getConstructor() {
        return constructor;
    }
    ObjectStreamClass() {
        super();
    }
    private static ObjectStreamClass createClassDesc(Class<?> cl) {
        ObjectStreamClass result = new ObjectStreamClass();
        boolean isArray = cl.isArray();
        boolean serializable = isSerializable(cl);
        boolean externalizable = isExternalizable(cl);
        result.isSerializable = serializable;
        result.isExternalizable = externalizable;
        result.setName(cl.getName());
        result.setClass(cl);
        Class<?> superclass = cl.getSuperclass();
        if (superclass != null) {
            result.setSuperclass(lookup(superclass));
        }
        Field[] declaredFields = null;
        if(serializable || externalizable) {
            if (result.isEnum() || result.isProxy()) {
                result.setSerialVersionUID(0L);
            } else {
                declaredFields = cl.getDeclaredFields();
                result.setSerialVersionUID(computeSerialVersionUID(cl,
                        declaredFields));
            }
        }
        if (serializable && !isArray) {
            if (declaredFields == null) {
                declaredFields = cl.getDeclaredFields();
            }
            result.buildFieldDescriptors(declaredFields);
        } else {
            result.setFields(NO_FIELDS);
        }
        ObjectStreamField[] fields = result.getFields();
        if (fields != null) {
            ObjectStreamField[] loadFields = new ObjectStreamField[fields.length];
            for (int i = 0; i < fields.length; ++i) {
                loadFields[i] = new ObjectStreamField(fields[i].getName(),
                        fields[i].getType(), fields[i].isUnshared());
                loadFields[i].getTypeString();
            }
            result.setLoadFields(loadFields);
        }
        byte flags = 0;
        if (externalizable) {
            flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
            flags |= ObjectStreamConstants.SC_BLOCK_DATA; 
        } else if (serializable) {
            flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        }
        result.methodWriteReplace = findMethod(cl, "writeReplace"); 
        result.methodReadResolve = findMethod(cl, "readResolve"); 
        result.methodWriteObject = findPrivateMethod(cl, "writeObject", 
                WRITE_PARAM_TYPES);
        result.methodReadObject = findPrivateMethod(cl, "readObject", 
                READ_PARAM_TYPES);
        result.methodReadObjectNoData = findPrivateMethod(cl,
                "readObjectNoData", EMPTY_CONSTRUCTOR_PARAM_TYPES); 
        if (result.hasMethodWriteObject()) {
            flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        }
        result.setFlags(flags);
        return result;
    }
    void buildFieldDescriptors(Field[] declaredFields) {
        final Field f = ObjectStreamClass.fieldSerialPersistentFields(this
                .forClass());
        boolean useReflectFields = f == null; 
        ObjectStreamField[] _fields = null;
        if (!useReflectFields) {
            AccessController.doPrivileged(new PriviAction<Object>(f));
            try {
                _fields = (ObjectStreamField[]) f.get(null);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            List<ObjectStreamField> serializableFields = new ArrayList<ObjectStreamField>(
                    declaredFields.length);
            for (int i = 0; i < declaredFields.length; i++) {
                Field declaredField = declaredFields[i];
                int modifiers = declaredField.getModifiers();
                boolean shouldBeSerialized = !(Modifier.isStatic(modifiers) || Modifier
                        .isTransient(modifiers));
                if (shouldBeSerialized) {
                    ObjectStreamField field = new ObjectStreamField(
                            declaredField.getName(), declaredField.getType());
                    serializableFields.add(field);
                }
            }
            if (serializableFields.size() == 0) {
                _fields = NO_FIELDS; 
            } else {
                _fields = new ObjectStreamField[serializableFields.size()];
                _fields = serializableFields.toArray(_fields);
            }
        }
        ObjectStreamField.sortFields(_fields);
        int primOffset = 0, objectOffset = 0;
        for (int i = 0; i < _fields.length; i++) {
            Class<?> type = _fields[i].getType();
            if (type.isPrimitive()) {
                _fields[i].offset = primOffset;
                primOffset += primitiveSize(type);
            } else {
                _fields[i].offset = objectOffset++;
            }
        }
        fields = _fields;
    }
    private static long computeSerialVersionUID(Class<?> cl, Field[] fields) {
        for (int i = 0; i < fields.length; i++) {
            final Field field = fields[i];
            if (Long.TYPE == field.getType()) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    if (UID_FIELD_NAME.equals(field.getName())) {
                        AccessController.doPrivileged(new PriviAction<Object>(
                                field));
                        try {
                            return field.getLong(null);
                        } catch (IllegalAccessException iae) {
                            throw new RuntimeException(Msg.getString(
                                    "K0071", iae)); 
                        }
                    }
                }
            }
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA"); 
        } catch (NoSuchAlgorithmException e) {
            throw new Error(e);
        }
        ByteArrayOutputStream sha = new ByteArrayOutputStream();
        try {
            DataOutputStream output = new DataOutputStream(sha);
            output.writeUTF(cl.getName());
            int classModifiers = CLASS_MODIFIERS_MASK & cl.getModifiers();
            boolean isArray = cl.isArray();
            if (isArray) {
                classModifiers |= Modifier.ABSTRACT;
            }
            if (cl.isInterface() && !Modifier.isPublic(classModifiers)) {
                classModifiers &= ~Modifier.ABSTRACT;
            }
            output.writeInt(classModifiers);
            if (!isArray) {
                Class<?>[] interfaces = cl.getInterfaces();
                if (interfaces.length > 1) {
                    Comparator<Class<?>> interfaceComparator = new Comparator<Class<?>>() {
                        public int compare(Class<?> itf1, Class<?> itf2) {
                            return itf1.getName().compareTo(itf2.getName());
                        }
                    };
                    Arrays.sort(interfaces, interfaceComparator);
                }
                for (int i = 0; i < interfaces.length; i++) {
                    output.writeUTF(interfaces[i].getName());
                }
            }
            if (fields.length > 1) {
                Comparator<Field> fieldComparator = new Comparator<Field>() {
                    public int compare(Field field1, Field field2) {
                        return field1.getName().compareTo(field2.getName());
                    }
                };
                Arrays.sort(fields, fieldComparator);
            }
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                int modifiers = field.getModifiers() & FIELD_MODIFIERS_MASK;
                boolean skip = Modifier.isPrivate(modifiers)
                        && (Modifier.isTransient(modifiers) || Modifier
                                .isStatic(modifiers));
                if (!skip) {
                    output.writeUTF(field.getName());
                    output.writeInt(modifiers);
                    output
                            .writeUTF(descriptorForFieldSignature(getFieldSignature(field)));
                }
            }
            if (hasClinit(cl)) {
                output.writeUTF(CLINIT_NAME);
                output.writeInt(CLINIT_MODIFIERS);
                output.writeUTF(CLINIT_SIGNATURE);
            }
            Constructor<?>[] constructors = cl.getDeclaredConstructors();
            if (constructors.length > 1) {
                Comparator<Constructor<?>> constructorComparator = new Comparator<Constructor<?>>() {
                    public int compare(Constructor<?> ctr1, Constructor<?> ctr2) {
                        return (getConstructorSignature(ctr1)
                                .compareTo(getConstructorSignature(ctr2)));
                    }
                };
                Arrays.sort(constructors, constructorComparator);
            }
            for (int i = 0; i < constructors.length; i++) {
                Constructor<?> constructor = constructors[i];
                int modifiers = constructor.getModifiers()
                        & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    output.writeUTF("<init>"); 
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getConstructorSignature(constructor)).replace('/',
                            '.'));
                }
            }
            Method[] methods = cl.getDeclaredMethods();
            if (methods.length > 1) {
                Comparator<Method> methodComparator = new Comparator<Method>() {
                    public int compare(Method m1, Method m2) {
                        int result = m1.getName().compareTo(m2.getName());
                        if (result == 0) {
                            return getMethodSignature(m1).compareTo(
                                    getMethodSignature(m2));
                        }
                        return result;
                    }
                };
                Arrays.sort(methods, methodComparator);
            }
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                int modifiers = method.getModifiers() & METHOD_MODIFIERS_MASK;
                boolean isPrivate = Modifier.isPrivate(modifiers);
                if (!isPrivate) {
                    output.writeUTF(method.getName());
                    output.writeInt(modifiers);
                    output.writeUTF(descriptorForSignature(
                            getMethodSignature(method)).replace('/', '.'));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(Msg.getString("K0072", e));
        }
        byte[] hash = digest.digest(sha.toByteArray());
        return littleEndianLongAt(hash, 0);
    }
    private static String descriptorForFieldSignature(String signature) {
        return signature.replace('.', '/');
    }
    private static String descriptorForSignature(String signature) {
        return signature.substring(signature.indexOf("(")); 
    }
    static Field fieldSerialPersistentFields(Class<?> cl) {
        try {
            Field f = cl.getDeclaredField("serialPersistentFields"); 
            int modifiers = f.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPrivate(modifiers)
                    && Modifier.isFinal(modifiers)) {
                if (f.getType() == ARRAY_OF_FIELDS) {
                    return f;
                }
            }
        } catch (NoSuchFieldException nsm) {
        }
        return null;
    }
    public Class<?> forClass() {
        if (resolvedClass != null) {
            return resolvedClass.get();
        }
        return null;
    }
    static native String getConstructorSignature(Constructor<?> c);
    public ObjectStreamField getField(String name) {
        ObjectStreamField[] allFields = getFields();
        for (int i = 0; i < allFields.length; i++) {
            ObjectStreamField f = allFields[i];
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
    ObjectStreamField[] fields() {
        if (fields == null) {
            Class<?> forCl = forClass();
            if (forCl != null && isSerializable() && !forCl.isArray()) {
                buildFieldDescriptors(forCl.getDeclaredFields());
            } else {
                setFields(NO_FIELDS);
            }
        }
        return fields;
    }
    public ObjectStreamField[] getFields() {
        copyFieldAttributes();
        return loadFields == null ? fields().clone() : loadFields.clone();
    }
    private void copyFieldAttributes() {
        if ((loadFields == null) || fields == null) {
            return;
        }
        for (int i = 0; i < loadFields.length; i++) {
            ObjectStreamField loadField = loadFields[i];
            String name = loadField.getName();
            for (int j = 0; j < fields.length; j++) {
                ObjectStreamField field = fields[j];
                if (name.equals(field.getName())) {
                    loadField.setUnshared(field.isUnshared());
                    loadField.setOffset(field.getOffset());
                    break;
                }
            }
        }
    }
    ObjectStreamField[] getLoadFields() {
        return loadFields;
    }
    private static native String getFieldSignature(Field f);
    byte getFlags() {
        return flags;
    }
    static native String getMethodSignature(Method m);
    public String getName() {
        return className;
    }
    public long getSerialVersionUID() {
        return svUID;
    }
    ObjectStreamClass getSuperclass() {
        return superclass;
    }
    private static native boolean hasClinit(Class<?> cl);
    static boolean isExternalizable(Class<?> cl) {
        return EXTERNALIZABLE.isAssignableFrom(cl);
    }
    static boolean isPrimitiveType(char typecode) {
        return !(typecode == '[' || typecode == 'L');
    }
    static boolean isSerializable(Class<?> cl) {
        return SERIALIZABLE.isAssignableFrom(cl);
    }
    private void resolveProperties() {
        if (arePropertiesResolved) {
            return;
        }
        Class<?> cl = forClass();
        isProxy = Proxy.isProxyClass(cl);
        isEnum = Enum.class.isAssignableFrom(cl);
        isSerializable = isSerializable(cl);
        isExternalizable = isExternalizable(cl);
        arePropertiesResolved = true;
    }
    boolean isSerializable() {
        resolveProperties();
        return isSerializable;
    }
    boolean isExternalizable() {
        resolveProperties();
        return isExternalizable;
    }
    boolean isProxy() {
        resolveProperties();
        return isProxy;
    }
    boolean isEnum() {
        resolveProperties();
        return isEnum;
    }
    private static long littleEndianLongAt(byte[] buffer, int position) {
        long result = 0;
        for (int i = position + 7; i >= position; i--) {
            result = (result << 8) + (buffer[i] & 0xff);
        }
        return result;
    }
    public static ObjectStreamClass lookup(Class<?> cl) {
        ObjectStreamClass osc = lookupStreamClass(cl);
        if (osc.isSerializable() || osc.isExternalizable()) {
            return osc;
        }
        return null;
    }
    static ObjectStreamClass lookupStreamClass(Class<?> cl) {
        WeakHashMap<Class<?>,ObjectStreamClass> tlc = OSCThreadLocalCache.oscWeakHashMap.get();
        ObjectStreamClass cachedValue = tlc.get(cl);
        if (cachedValue == null) {
            cachedValue = createClassDesc(cl);
            tlc.put(cl, cachedValue);
        }
        return cachedValue;
    }
    static Method findMethod(Class<?> cl, String methodName) {
        Class<?> search = cl;
        Method method = null;
        while (search != null) {
            try {
                method = search.getDeclaredMethod(methodName, (Class[]) null);
                if (search == cl
                        || (method.getModifiers() & Modifier.PRIVATE) == 0) {
                    method.setAccessible(true);
                    return method;
                }
            } catch (NoSuchMethodException nsm) {
            }
            search = search.getSuperclass();
        }
        return null;
    }
    static Method findPrivateMethod(Class<?> cl, String methodName,
            Class<?>[] param) {
        try {
            Method method = cl.getDeclaredMethod(methodName, param);
            if (Modifier.isPrivate(method.getModifiers())
                    && method.getReturnType() == VOID_CLASS) {
                method.setAccessible(true);
                return method;
            }
        } catch (NoSuchMethodException nsm) {
        }
        return null;
    }
    boolean hasMethodWriteReplace() {
        return (methodWriteReplace != null);
    }
    Method getMethodWriteReplace() {
        return methodWriteReplace;
    }
    boolean hasMethodReadResolve() {
        return (methodReadResolve != null);
    }
    Method getMethodReadResolve() {
        return methodReadResolve;
    }
    boolean hasMethodWriteObject() {
        return (methodWriteObject != null);
    }
    Method getMethodWriteObject() {
        return methodWriteObject;
    }
    boolean hasMethodReadObject() {
        return (methodReadObject != null);
    }
    Method getMethodReadObject() {
        return methodReadObject;
    }
    boolean hasMethodReadObjectNoData() {
        return (methodReadObjectNoData != null);
    }
    Method getMethodReadObjectNoData() {
        return methodReadObjectNoData;
    }
    void initPrivateFields(ObjectStreamClass desc) {
        methodWriteReplace = desc.methodWriteReplace;
        methodReadResolve = desc.methodReadResolve;
        methodWriteObject = desc.methodWriteObject;
        methodReadObject = desc.methodReadObject;
        methodReadObjectNoData = desc.methodReadObjectNoData;
    }
    void setClass(Class<?> c) {
        resolvedClass = new WeakReference<Class<?>>(c);
    }
    void setFields(ObjectStreamField[] f) {
        fields = f;
    }
    void setLoadFields(ObjectStreamField[] f) {
        loadFields = f;
    }
    void setFlags(byte b) {
        flags = b;
    }
    void setName(String newName) {
        className = newName;
    }
    void setSerialVersionUID(long l) {
        svUID = l;
    }
    void setSuperclass(ObjectStreamClass c) {
        superclass = c;
    }
    private int primitiveSize(Class<?> type) {
        if (type == Byte.TYPE || type == Boolean.TYPE) {
            return 1;
        }
        if (type == Short.TYPE || type == Character.TYPE) {
            return 2;
        }
        if (type == Integer.TYPE || type == Float.TYPE) {
            return 4;
        }
        if (type == Long.TYPE || type == Double.TYPE) {
            return 8;
        }
        return 0;
    }
    @Override
    public String toString() {
        return getName() + ": static final long serialVersionUID =" 
                + getSerialVersionUID() + "L;"; 
    }
    static class OSCThreadLocalCache extends ThreadLocalCache {
        public static ThreadLocalCache<WeakHashMap<Class<?>,ObjectStreamClass>> oscWeakHashMap = new ThreadLocalCache<WeakHashMap<Class<?>,ObjectStreamClass>>() {
            protected WeakHashMap<Class<?>,ObjectStreamClass> initialValue() {
                return new WeakHashMap<Class<?>,ObjectStreamClass>();
            }
        };
    }
}

public class ObjectInputStream extends InputStream implements ObjectInput,
        ObjectStreamConstants {
    private InputStream emptyStream = new ByteArrayInputStream(
            new byte[0]);
    private static final Object UNSHARED_OBJ = new Object(); 
    private boolean hasPushbackTC;
    private byte pushbackTC;
    private int nestedLevels;
    private int currentHandle;
    private DataInputStream input;
    private DataInputStream primitiveTypes;
    private InputStream primitiveData = emptyStream;
    private boolean enableResolve;
    private HashMap<Integer, Object> objectsRead;
    private Object currentObject;
    private ObjectStreamClass currentClass;
    private InputValidationDesc[] validations;
    private boolean subclassOverridingImplementation;
    private ClassLoader callerClassLoader;
    private boolean mustResolve = true;
    private Integer descriptorHandle;
    private static final HashMap<String, Class<?>> PRIMITIVE_CLASSES =
        new HashMap<String, Class<?>>();
    static {
        PRIMITIVE_CLASSES.put("byte", byte.class); 
        PRIMITIVE_CLASSES.put("short", short.class); 
        PRIMITIVE_CLASSES.put("int", int.class); 
        PRIMITIVE_CLASSES.put("long", long.class); 
        PRIMITIVE_CLASSES.put("boolean", boolean.class); 
        PRIMITIVE_CLASSES.put("char", char.class); 
        PRIMITIVE_CLASSES.put("float", float.class); 
        PRIMITIVE_CLASSES.put("double", double.class); 
    }
    static class InputValidationDesc {
        ObjectInputValidation validator;
        int priority;
    }
    public abstract static class GetField {
        public abstract ObjectStreamClass getObjectStreamClass();
        public abstract boolean defaulted(String name) throws IOException,
                IllegalArgumentException;
        public abstract boolean get(String name, boolean defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract char get(String name, char defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract byte get(String name, byte defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract short get(String name, short defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract int get(String name, int defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract long get(String name, long defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract float get(String name, float defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract double get(String name, double defaultValue)
                throws IOException, IllegalArgumentException;
        public abstract Object get(String name, Object defaultValue)
                throws IOException, IllegalArgumentException;
    }
    protected ObjectInputStream() throws IOException, SecurityException {
        super();
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.subclassOverridingImplementation = true;
    }
    public ObjectInputStream(InputStream input)
            throws StreamCorruptedException, IOException {
        final Class<?> implementationClass = getClass();
        final Class<?> thisClass = ObjectInputStream.class;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null && implementationClass != thisClass) {
            boolean mustCheck = (AccessController
                    .doPrivileged(new PrivilegedAction<Boolean>() {
                        public Boolean run() {
                            try {
                                Method method = implementationClass
                                        .getMethod(
                                                "readFields", 
                                                ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                                if (method.getDeclaringClass() != thisClass) {
                                    return Boolean.TRUE;
                                }
                            } catch (NoSuchMethodException e) {
                            }
                            try {
                                Method method = implementationClass
                                        .getMethod(
                                                "readUnshared", 
                                                ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                                if (method.getDeclaringClass() != thisClass) {
                                    return Boolean.TRUE;
                                }
                            } catch (NoSuchMethodException e) {
                            }
                            return Boolean.FALSE;
                        }
                    })).booleanValue();
            if (mustCheck) {
                sm
                        .checkPermission(ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION);
            }
        }
        this.input = (input instanceof DataInputStream) ? (DataInputStream) input
                : new DataInputStream(input);
        primitiveTypes = new DataInputStream(this);
        enableResolve = false;
        this.subclassOverridingImplementation = false;
        resetState();
        nestedLevels = 0;
        primitiveData = this.input;
        readStreamHeader();
        primitiveData = emptyStream;
    }
    @Override
    public int available() throws IOException {
        checkReadPrimitiveTypes();
        return primitiveData.available();
    }
    private void checkReadPrimitiveTypes() throws IOException {
        if (primitiveData == input || primitiveData.available() > 0) {
            return;
        }
        do {
            int next = 0;
            if (hasPushbackTC) {
                hasPushbackTC = false;
            } else {
                next = input.read();
                pushbackTC = (byte) next;
            }
            switch (pushbackTC) {
                case TC_BLOCKDATA:
                    primitiveData = new ByteArrayInputStream(readBlockData());
                    return;
                case TC_BLOCKDATALONG:
                    primitiveData = new ByteArrayInputStream(
                            readBlockDataLong());
                    return;
                case TC_RESET:
                    resetState();
                    break;
                default:
                    if (next != -1) {
                        pushbackTC();
                    }
                    return;
            }
        } while (true);
    }
    @Override
    public void close() throws IOException {
        input.close();
    }
    public void defaultReadObject() throws IOException, ClassNotFoundException,
            NotActiveException {
        if (currentObject != null || !mustResolve) {
            readFieldValues(currentObject, currentClass);
        } else {
            throw new NotActiveException();
        }
    }
    protected boolean enableResolveObject(boolean enable)
            throws SecurityException {
        if (enable) {
            SecurityManager currentManager = System.getSecurityManager();
            if (currentManager != null) {
                currentManager.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        boolean originalValue = enableResolve;
        enableResolve = enable;
        return originalValue;
    }
    private boolean inSamePackage(Class<?> c1, Class<?> c2) {
        String nameC1 = c1.getName();
        String nameC2 = c2.getName();
        int indexDotC1 = nameC1.lastIndexOf('.');
        int indexDotC2 = nameC2.lastIndexOf('.');
        if (indexDotC1 != indexDotC2) {
            return false; 
        }
        if (indexDotC1 < 0) {
            return true; 
        }
        return nameC1.substring(0, indexDotC1).equals(
                nameC2.substring(0, indexDotC2));
    }
    private static native Object newInstance(Class<?> instantiationClass,
            Class<?> constructorClass);
    private Integer nextHandle() {
        return Integer.valueOf(this.currentHandle++);
    }
    private byte nextTC() throws IOException {
        if (hasPushbackTC) {
            hasPushbackTC = false; 
        } else {
            pushbackTC = input.readByte();
        }
        return pushbackTC;
    }
    private void pushbackTC() {
        hasPushbackTC = true;
    }
    @Override
    public int read() throws IOException {
        checkReadPrimitiveTypes();
        return primitiveData.read();
    }
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        }
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        if (length == 0) {
            return 0;
        }
        checkReadPrimitiveTypes();
        return primitiveData.read(buffer, offset, length);
    }
    private byte[] readBlockData() throws IOException {
        byte[] result = new byte[input.readByte() & 0xff];
        input.readFully(result);
        return result;
    }
    private byte[] readBlockDataLong() throws IOException {
        byte[] result = new byte[input.readInt()];
        input.readFully(result);
        return result;
    }
    public boolean readBoolean() throws IOException {
        return primitiveTypes.readBoolean();
    }
    public byte readByte() throws IOException {
        return primitiveTypes.readByte();
    }
    public char readChar() throws IOException {
        return primitiveTypes.readChar();
    }
    private void discardData() throws ClassNotFoundException, IOException {
        primitiveData = emptyStream;
        boolean resolve = mustResolve;
        mustResolve = false;
        do {
            byte tc = nextTC();
            if (tc == TC_ENDBLOCKDATA) {
                mustResolve = resolve;
                return; 
            }
            readContent(tc);
        } while (true);
    }
    private ObjectStreamClass readClassDesc() throws ClassNotFoundException,
            IOException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_PROXYCLASSDESC:
                Class<?> proxyClass = readNewProxyClassDesc();
                ObjectStreamClass streamClass = ObjectStreamClass
                        .lookup(proxyClass);
                streamClass.setLoadFields(new ObjectStreamField[0]);
                registerObjectRead(streamClass, nextHandle(), false);
                checkedSetSuperClassDesc(streamClass, readClassDesc());
                return streamClass;
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); 
        }
    }
    private Object readContent(byte tc) throws ClassNotFoundException,
            IOException {
        switch (tc) {
            case TC_BLOCKDATA:
                return readBlockData();
            case TC_BLOCKDATALONG:
                return readBlockDataLong();
            case TC_CLASS:
                return readNewClass(false);
            case TC_CLASSDESC:
                return readNewClassDesc(false);
            case TC_ARRAY:
                return readNewArray(false);
            case TC_OBJECT:
                return readNewObject(false);
            case TC_STRING:
                return readNewString(false);
            case TC_LONGSTRING:
                return readNewLongString(false);
            case TC_REFERENCE:
                return readCyclicReference();
            case TC_NULL:
                return null;
            case TC_EXCEPTION:
                Exception exc = readException();
                throw new WriteAbortedException(Msg.getString("K00d3"), exc); 
            case TC_RESET:
                resetState();
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); 
        }
    }
    private Object readNonPrimitiveContent(boolean unshared)
            throws ClassNotFoundException, IOException {
        checkReadPrimitiveTypes();
        if (primitiveData.available() > 0) {
            OptionalDataException e = new OptionalDataException();
            e.length = primitiveData.available();
            throw e;
        }
        do {
            byte tc = nextTC();
            switch (tc) {
                case TC_CLASS:
                    return readNewClass(unshared);
                case TC_CLASSDESC:
                    return readNewClassDesc(unshared);
                case TC_ARRAY:
                    return readNewArray(unshared);
                case TC_OBJECT:
                    return readNewObject(unshared);
                case TC_STRING:
                    return readNewString(unshared);
                case TC_LONGSTRING:
                    return readNewLongString(unshared);
                case TC_ENUM:
                    return readEnum(unshared);
                case TC_REFERENCE:
                    if (unshared) {
                        readNewHandle();
                        throw new InvalidObjectException(Msg.getString("KA002")); 
                    }
                    return readCyclicReference();
                case TC_NULL:
                    return null;
                case TC_EXCEPTION:
                    Exception exc = readException();
                    throw new WriteAbortedException(Msg.getString("K00d3"), exc); 
                case TC_RESET:
                    resetState();
                    break;
                case TC_ENDBLOCKDATA: 
                    pushbackTC();
                    OptionalDataException e = new OptionalDataException();
                    e.eof = true;
                    throw e;
                default:
                    throw new StreamCorruptedException(Msg.getString(
                            "K00d2", Integer.toHexString(tc & 0xff))); 
            }
        } while (true);
    }
    private Object readCyclicReference() throws InvalidObjectException,
            IOException {
        return registeredObjectRead(readNewHandle());
    }
    public double readDouble() throws IOException {
        return primitiveTypes.readDouble();
    }
    private Exception readException() throws WriteAbortedException,
            OptionalDataException, ClassNotFoundException, IOException {
        resetSeenObjects();
        Exception exc = (Exception) readObject();
        resetSeenObjects();
        return exc;
    }
    private void readFieldDescriptors(ObjectStreamClass cDesc)
            throws ClassNotFoundException, IOException {
        short numFields = input.readShort();
        ObjectStreamField[] fields = new ObjectStreamField[numFields];
        cDesc.setLoadFields(fields);
        for (short i = 0; i < numFields; i++) {
            char typecode = (char) input.readByte();
            String fieldName = input.readUTF();
            boolean isPrimType = ObjectStreamClass.isPrimitiveType(typecode);
            String classSig;
            if (isPrimType) {
                classSig = String.valueOf(typecode);
            } else {
                boolean old = enableResolve;
                try {
                    enableResolve = false;
                    classSig = (String) readObject();
                } finally {
                    enableResolve = old;
                }
            }
            classSig = formatClassSig(classSig);
            ObjectStreamField f = new ObjectStreamField(classSig, fieldName);
            fields[i] = f;
        }
    }
    private static String formatClassSig(String classSig) {
        int start = 0;
        int end = classSig.length();
        if (end <= 0) {
            return classSig;
        }
        while (classSig.startsWith("[L", start) 
                && classSig.charAt(end - 1) == ';') {
            start += 2;
            end--;
        }
        if (start > 0) {
            start -= 2;
            end++;
            return classSig.substring(start, end);
        }
        return classSig;
    }
    public GetField readFields() throws IOException, ClassNotFoundException,
            NotActiveException {
        if (currentObject == null) {
            throw new NotActiveException();
        }
        EmulatedFieldsForLoading result = new EmulatedFieldsForLoading(
                currentClass);
        readFieldValues(result);
        return result;
    }
    private void readFieldValues(EmulatedFieldsForLoading emulatedFields)
            throws OptionalDataException, InvalidClassException, IOException {
        EmulatedFields.ObjectSlot[] slots = emulatedFields.emulatedFields()
                .slots();
        for (ObjectSlot element : slots) {
            element.defaulted = false;
            Class<?> type = element.field.getType();
            if (type == Integer.TYPE) {
                element.fieldValue = Integer.valueOf(input.readInt());
            } else if (type == Byte.TYPE) {
                element.fieldValue = Byte.valueOf(input.readByte());
            } else if (type == Character.TYPE) {
                element.fieldValue = Character.valueOf(input.readChar());
            } else if (type == Short.TYPE) {
                element.fieldValue = Short.valueOf(input.readShort());
            } else if (type == Boolean.TYPE) {
                element.fieldValue = Boolean.valueOf(input.readBoolean());
            } else if (type == Long.TYPE) {
                element.fieldValue = Long.valueOf(input.readLong());
            } else if (type == Float.TYPE) {
                element.fieldValue = Float.valueOf(input.readFloat());
            } else if (type == Double.TYPE) {
                element.fieldValue = Double.valueOf(input.readDouble());
            } else {
                try {
                    element.fieldValue = readObject();
                } catch (ClassNotFoundException cnf) {
                    throw new InvalidClassException(cnf.toString());
                }
            }
        }
    }
    private void readFieldValues(Object obj, ObjectStreamClass classDesc)
            throws OptionalDataException, ClassNotFoundException, IOException {
        ObjectStreamField[] fields = classDesc.getLoadFields();
        fields = (null == fields ? new ObjectStreamField[] {} : fields);
        Class<?> declaringClass = classDesc.forClass();
        if (declaringClass == null && mustResolve) {
            throw new ClassNotFoundException(classDesc.getName());
        }
        for (ObjectStreamField fieldDesc : fields) {
            if (fieldDesc.isPrimitive()) {
                try {
                    switch (fieldDesc.getTypeCode()) {
                        case 'B':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readByte());
                            break;
                        case 'C':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readChar());
                            break;
                        case 'D':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readDouble());
                            break;
                        case 'F':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readFloat());
                            break;
                        case 'I':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readInt());
                            break;
                        case 'J':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readLong());
                            break;
                        case 'S':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readShort());
                            break;
                        case 'Z':
                            setField(obj, declaringClass, fieldDesc.getName(),
                                    input.readBoolean());
                            break;
                        default:
                            throw new StreamCorruptedException(Msg.getString(
                                    "K00d5", fieldDesc.getTypeCode())); 
                    }
                } catch (NoSuchFieldError err) {
                }
            } else {
                String fieldName = fieldDesc.getName();
                boolean setBack = false;
                ObjectStreamField field = classDesc.getField(fieldName);
                if (mustResolve && fieldDesc == null) {
                    setBack = true;
                    mustResolve = false;
                }
                Object toSet;
                if (fieldDesc != null && fieldDesc.isUnshared()) {
                    toSet = readUnshared();
                } else {
                    toSet = readObject();
                }
                if (setBack) {
                    mustResolve = true;
                }
                if (fieldDesc != null) {
                    if (toSet != null) {
                        Class<?> fieldType = field.getTypeInternal();
                        Class<?> valueType = toSet.getClass();
                        if (!fieldType.isAssignableFrom(valueType)) {
                            throw new ClassCastException(Msg.getString(
                                    "K00d4", new String[] { 
                                    fieldType.toString(), valueType.toString(),
                                            classDesc.getName() + "." 
                                                    + fieldName }));
                        }
                        try {
                            objSetField(obj, declaringClass, fieldName, field
                                    .getTypeString(), toSet);
                        } catch (NoSuchFieldError e) {
                        }
                    }
                }
            }
        }
    }
    public float readFloat() throws IOException {
        return primitiveTypes.readFloat();
    }
    public void readFully(byte[] buffer) throws IOException {
        primitiveTypes.readFully(buffer);
    }
    public void readFully(byte[] buffer, int offset, int length)
            throws IOException {
        primitiveTypes.readFully(buffer, offset, length);
    }
    private void readHierarchy(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        if (object == null && mustResolve) {
            throw new NotActiveException();
        }
        ArrayList<ObjectStreamClass> streamClassList = new ArrayList<ObjectStreamClass>(
                32);
        ObjectStreamClass nextStreamClass = classDesc;
        while (nextStreamClass != null) {
            streamClassList.add(0, nextStreamClass);
            nextStreamClass = nextStreamClass.getSuperclass();
        }
        if (object == null) {
            Iterator<ObjectStreamClass> streamIt = streamClassList.iterator();
            while (streamIt.hasNext()) {
                ObjectStreamClass streamClass = streamIt.next();
                readObjectForClass(null, streamClass);
            }
        } else {
            ArrayList<Class<?>> classList = new ArrayList<Class<?>>(32);
            Class<?> nextClass = object.getClass();
            while (nextClass != null) {
                Class<?> testClass = nextClass.getSuperclass();
                if (testClass != null) {
                    classList.add(0, nextClass);
                }
                nextClass = testClass;
            }
            int lastIndex = 0;
            for (int i = 0; i < classList.size(); i++) {
                Class<?> superclass = classList.get(i);
                int index = findStreamSuperclass(superclass, streamClassList,
                        lastIndex);
                if (index == -1) {
                    readObjectNoData(object, superclass, ObjectStreamClass.lookupStreamClass(superclass));
                } else {
                    for (int j = lastIndex; j <= index; j++) {
                        readObjectForClass(object, streamClassList.get(j));
                    }
                    lastIndex = index + 1;
                }
            }
        }
    }
    private int findStreamSuperclass(Class<?> cl,
            ArrayList<ObjectStreamClass> classList, int lastIndex) {
        ObjectStreamClass objCl;
        String forName;
        for (int i = lastIndex; i < classList.size(); i++) {
            objCl = classList.get(i);
            forName = objCl.forClass().getName();
            if (objCl.getName().equals(forName)) {
                if (cl.getName().equals(objCl.getName())) {
                    return i;
                }
            } else {
                if (cl.getName().equals(forName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    private void readObjectNoData(Object object, Class<?> cl, ObjectStreamClass classDesc)
            throws ObjectStreamException {
        if (!classDesc.isSerializable()) {
            return;
        }
        if (classDesc.hasMethodReadObjectNoData()){
            final Method readMethod = classDesc.getMethodReadObjectNoData();
            try {
                readMethod.invoke(object, new Object[0]);
            } catch (InvocationTargetException e) {
                Throwable ex = e.getTargetException();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else if (ex instanceof Error) {
                    throw (Error) ex;
                }
                throw (ObjectStreamException) ex;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.toString());
            }
        }
    }
    private void readObjectForClass(Object object, ObjectStreamClass classDesc)
            throws IOException, ClassNotFoundException, NotActiveException {
        currentObject = object;
        currentClass = classDesc;
        boolean hadWriteMethod = (classDesc.getFlags() & SC_WRITE_METHOD) > 0;
        Class<?> targetClass = classDesc.forClass();
        final Method readMethod;
        if (targetClass == null || !mustResolve) {
            readMethod = null;
        } else {
            readMethod = classDesc.getMethodReadObject();
        }
        try {
            if (readMethod != null) {
                AccessController.doPrivileged(new PriviAction<Object>(
                        readMethod));
                try {
                    readMethod.invoke(object, new Object[] { this });
                } catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof ClassNotFoundException) {
                        throw (ClassNotFoundException) ex;
                    } else if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    }
                    throw (IOException) ex;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.toString());
                }
            } else {
                defaultReadObject();
            }
            if (hadWriteMethod) {
                discardData();
            }
        } finally {
            currentObject = null; 
            currentClass = null;
        }
    }
    public int readInt() throws IOException {
        return primitiveTypes.readInt();
    }
    @Deprecated
    public String readLine() throws IOException {
        return primitiveTypes.readLine();
    }
    public long readLong() throws IOException {
        return primitiveTypes.readLong();
    }
    private Object readNewArray(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();
        if (classDesc == null) {
            throw new InvalidClassException(Msg.getString("K00d1")); 
        }
        Integer newHandle = nextHandle();
        int size = input.readInt();
        Class<?> arrayClass = classDesc.forClass();
        Class<?> componentType = arrayClass.getComponentType();
        Object result = Array.newInstance(componentType, size);
        registerObjectRead(result, newHandle, unshared);
        if (componentType.isPrimitive()) {
            if (componentType == Integer.TYPE) {
                int[] intArray = (int[]) result;
                for (int i = 0; i < size; i++) {
                    intArray[i] = input.readInt();
                }
            } else if (componentType == Byte.TYPE) {
                byte[] byteArray = (byte[]) result;
                input.readFully(byteArray, 0, size);
            } else if (componentType == Character.TYPE) {
                char[] charArray = (char[]) result;
                for (int i = 0; i < size; i++) {
                    charArray[i] = input.readChar();
                }
            } else if (componentType == Short.TYPE) {
                short[] shortArray = (short[]) result;
                for (int i = 0; i < size; i++) {
                    shortArray[i] = input.readShort();
                }
            } else if (componentType == Boolean.TYPE) {
                boolean[] booleanArray = (boolean[]) result;
                for (int i = 0; i < size; i++) {
                    booleanArray[i] = input.readBoolean();
                }
            } else if (componentType == Long.TYPE) {
                long[] longArray = (long[]) result;
                for (int i = 0; i < size; i++) {
                    longArray[i] = input.readLong();
                }
            } else if (componentType == Float.TYPE) {
                float[] floatArray = (float[]) result;
                for (int i = 0; i < size; i++) {
                    floatArray[i] = input.readFloat();
                }
            } else if (componentType == Double.TYPE) {
                double[] doubleArray = (double[]) result;
                for (int i = 0; i < size; i++) {
                    doubleArray[i] = input.readDouble();
                }
            } else {
                throw new ClassNotFoundException(Msg.getString(
                        "K00d7", classDesc.getName())); 
            }
        } else {
            Object[] objectArray = (Object[]) result;
            for (int i = 0; i < size; i++) {
                objectArray[i] = readObject();
            }
        }
        if (enableResolve) {
            result = resolveObject(result);
            registerObjectRead(result, newHandle, false);
        }
        return result;
    }
    private Class<?> readNewClass(boolean unshared)
            throws ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();
        if (classDesc != null) {
            Class<?> localClass = classDesc.forClass();
            if (localClass != null) {
                registerObjectRead(localClass, nextHandle(), unshared);
            }
            return localClass;
        }
        throw new InvalidClassException(Msg.getString("K00d1")); 
    }
    private ObjectStreamClass readEnumDesc() throws IOException,
            ClassNotFoundException {
        byte tc = nextTC();
        switch (tc) {
            case TC_CLASSDESC:
                return readEnumDescInternal();
            case TC_REFERENCE:
                return (ObjectStreamClass) readCyclicReference();
            case TC_NULL:
                return null;
            default:
                throw new StreamCorruptedException(Msg.getString(
                        "K00d2", Integer.toHexString(tc & 0xff))); 
        }
    }
    private ObjectStreamClass readEnumDescInternal() throws IOException,
            ClassNotFoundException {
        ObjectStreamClass classDesc;
        primitiveData = input;
        Integer oldHandle = descriptorHandle;
        descriptorHandle = nextHandle();
        classDesc = readClassDescriptor();
        registerObjectRead(classDesc, descriptorHandle, false);
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;
        classDesc.setClass(resolveClass(classDesc));
        discardData();
        ObjectStreamClass superClass = readClassDesc();
        checkedSetSuperClassDesc(classDesc, superClass);
        if (0L != classDesc.getSerialVersionUID()
                || 0L != superClass.getSerialVersionUID()) {
            throw new InvalidClassException(superClass.getName(), Msg
                    .getString("K00da", superClass, 
                            superClass));
        }
        byte tc = nextTC();
        if (tc == TC_ENDBLOCKDATA) {
            superClass.setSuperclass(readClassDesc());
        } else {
            pushbackTC();
        }
        return classDesc;
    }
    @SuppressWarnings("unchecked")
    private Object readEnum(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readEnumDesc();
        Integer newHandle = nextHandle();
        String name;
        byte tc = nextTC();
        switch (tc) {
            case TC_REFERENCE:
                if (unshared) {
                    readNewHandle();
                    throw new InvalidObjectException(Msg.getString("KA002")); 
                }
                name = (String) readCyclicReference();
                break;
            case TC_STRING:
                name = (String) readNewString(unshared);
                break;
            default:
                throw new StreamCorruptedException(Msg.getString("K00d2"));
        }
        Enum<?> result = Enum.valueOf((Class) classDesc.forClass(), name);
        registerObjectRead(result, newHandle, unshared);
        return result;
    }
    private ObjectStreamClass readNewClassDesc(boolean unshared)
            throws ClassNotFoundException, IOException {
        primitiveData = input;
        Integer oldHandle = descriptorHandle;
        descriptorHandle = nextHandle();
        ObjectStreamClass newClassDesc = readClassDescriptor();
        registerObjectRead(newClassDesc, descriptorHandle, unshared);
        descriptorHandle = oldHandle;
        primitiveData = emptyStream;
        try {
            newClassDesc.setClass(resolveClass(newClassDesc));
            verifyAndInit(newClassDesc);
        } catch (ClassNotFoundException e) {
            if (mustResolve) {
                throw e;
            }
        }
        ObjectStreamField[] fields = newClassDesc.getLoadFields();
        fields = (null == fields ? new ObjectStreamField[] {} : fields);
        ClassLoader loader = newClassDesc.forClass() == null ? callerClassLoader
                : newClassDesc.forClass().getClassLoader();
        for (ObjectStreamField element : fields) {
            element.resolve(loader);
        }
        discardData();
        checkedSetSuperClassDesc(newClassDesc, readClassDesc());
        return newClassDesc;
    }
    private Class<?> readNewProxyClassDesc() throws ClassNotFoundException,
            IOException {
        int count = input.readInt();
        String[] interfaceNames = new String[count];
        for (int i = 0; i < count; i++) {
            interfaceNames[i] = input.readUTF();
        }
        Class<?> proxy = resolveProxyClass(interfaceNames);
        discardData();
        return proxy;
    }
    protected ObjectStreamClass readClassDescriptor() throws IOException,
            ClassNotFoundException {
        ObjectStreamClass newClassDesc = new ObjectStreamClass();
        String name = input.readUTF();
        if (name.length() == 0) {
            throw new IOException(Messages.getString("luni.07")); 
        }
        newClassDesc.setName(name);
        newClassDesc.setSerialVersionUID(input.readLong());
        newClassDesc.setFlags(input.readByte());
        descriptorHandle = (null == descriptorHandle ? nextHandle() : descriptorHandle);
        registerObjectRead(newClassDesc, descriptorHandle, false);
        readFieldDescriptors(newClassDesc);
        return newClassDesc;
    }
    protected Class<?> resolveProxyClass(String[] interfaceNames)
            throws IOException, ClassNotFoundException {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        Class<?>[] interfaces = new Class<?>[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++) {
            interfaces[i] = Class.forName(interfaceNames[i], false, loader);
        }
        try {
            return Proxy.getProxyClass(loader, interfaces);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(e.toString(), e);
        }
    }
    private int readNewHandle() throws IOException {
        return input.readInt();
    }
    private Class<?> resolveConstructorClass(Class<?> objectClass, boolean wasSerializable, boolean wasExternalizable)
        throws OptionalDataException, ClassNotFoundException, IOException {
            Class<?> constructorClass = objectClass;
            if (wasSerializable) {
                while (constructorClass != null
                        && ObjectStreamClass.isSerializable(constructorClass)) {
                    constructorClass = constructorClass.getSuperclass();
                }
            }
            Constructor<?> constructor = null;
            if (constructorClass != null) {
                try {
                    constructor = constructorClass
                            .getDeclaredConstructor(ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                } catch (NoSuchMethodException nsmEx) {
                }
            }
            if (constructor == null) {
                throw new InvalidClassException(constructorClass.getName(), Msg
                        .getString("K00dc")); 
            }
            int constructorModifiers = constructor.getModifiers();
            if (Modifier.isPrivate(constructorModifiers)
                    || (wasExternalizable && !Modifier
                            .isPublic(constructorModifiers))) {
                throw new InvalidClassException(constructorClass.getName(), Msg
                        .getString("K00dc")); 
            }
            if (!Modifier.isPublic(constructorModifiers)
                    && !Modifier.isProtected(constructorModifiers)) {
                if (!inSamePackage(constructorClass, objectClass)) {
                    throw new InvalidClassException(constructorClass.getName(),
                            Msg.getString("K00dc")); 
                }
            }
            return constructorClass;
    }
    private Object readNewObject(boolean unshared)
            throws OptionalDataException, ClassNotFoundException, IOException {
        ObjectStreamClass classDesc = readClassDesc();
        if (classDesc == null) {
            throw new InvalidClassException(Msg.getString("K00d1")); 
        }
        Integer newHandle = nextHandle();
        boolean wasExternalizable = (classDesc.getFlags() & SC_EXTERNALIZABLE) > 0;
        boolean wasSerializable = (classDesc.getFlags() & SC_SERIALIZABLE) > 0;
        Class<?> objectClass = classDesc.forClass();
        Object result, registeredResult = null;
        if (objectClass != null) {
            Class constructorClass = resolveConstructorClass(objectClass, wasSerializable, wasExternalizable);
            result = newInstance(objectClass, constructorClass);
            registerObjectRead(result, newHandle, unshared);
            registeredResult = result;
        } else {
            result = null;
        }
        try {
            currentObject = result;
            currentClass = classDesc;
            if (wasExternalizable) {
                boolean blockData = (classDesc.getFlags() & SC_BLOCK_DATA) > 0;
                if (!blockData) {
                    primitiveData = input;
                }
                if (mustResolve) {
                    Externalizable extern = (Externalizable) result;
                    extern.readExternal(this);
                }
                if (blockData) {
                    discardData();
                } else {
                    primitiveData = emptyStream;
                }
            } else {
                readHierarchy(result, classDesc);
            }
        } finally {
            currentObject = null;
            currentClass = null;
        }
        if (objectClass != null) {
            if (classDesc.hasMethodReadResolve()){
                Method methodReadResolve = classDesc.getMethodReadResolve();
                try {
                    result = methodReadResolve.invoke(result, (Object[]) null);
                } catch (IllegalAccessException iae) {
                } catch (InvocationTargetException ite) {
                    Throwable target = ite.getTargetException();
                    if (target instanceof ObjectStreamException) {
                        throw (ObjectStreamException) target;
                    } else if (target instanceof Error) {
                        throw (Error) target;
                    } else {
                        throw (RuntimeException) target;
                    }
                }
            }
        }
        if (result != null && enableResolve) {
            result = resolveObject(result);
        }
        if (registeredResult != result) {
            registerObjectRead(result, newHandle, unshared);
        }
        return result;
    }
    private Object readNewString(boolean unshared) throws IOException {
        Object result = input.readUTF();
        if (enableResolve) {
            result = resolveObject(result);
        }
		registerObjectRead(result, nextHandle(), unshared);
        return result;
    }
    private Object readNewLongString(boolean unshared) throws IOException {
        long length = input.readLong();
        Object result = input.decodeUTF((int) length);
        if (enableResolve) {
            result = resolveObject(result);
        }
        registerObjectRead(result, nextHandle(), unshared);
        return result;
    }
    public final Object readObject() throws OptionalDataException,
            ClassNotFoundException, IOException {
        return readObject(false);
    }
    public Object readUnshared() throws IOException, ClassNotFoundException {
        return readObject(true);
    }
    private Object readObject(boolean unshared) throws OptionalDataException,
            ClassNotFoundException, IOException {
        boolean restoreInput = (primitiveData == input);
        if (restoreInput) {
            primitiveData = emptyStream;
        }
        if (subclassOverridingImplementation && !unshared) {
            return readObjectOverride();
        }
        Object result;
        try {
            if (++nestedLevels == 1) {
                callerClassLoader = getClosestUserClassLoader();
            }
            result = readNonPrimitiveContent(unshared);
            if (restoreInput) {
                primitiveData = input;
            }
        } finally {
            if (--nestedLevels == 0) {
                callerClassLoader = null;
            }
        }
        if (nestedLevels == 0 && validations != null) {
            try {
                for (InputValidationDesc element : validations) {
                    element.validator.validateObject();
                }
            } finally {
                validations = null;
            }
        }
        return result;
    }
    private static final ClassLoader bootstrapLoader
            = Object.class.getClassLoader();
    private static final ClassLoader systemLoader
            = ClassLoader.getSystemClassLoader();
    private static ClassLoader getClosestUserClassLoader() {
        Class<?>[] stackClasses = VMStack.getClasses(-1, false);
        for (Class<?> stackClass : stackClasses) {
            ClassLoader loader = stackClass.getClassLoader();
            if (loader != null && loader != bootstrapLoader
                    && loader != systemLoader) {
                return loader;
            }
        }
        return null;
    }
    protected Object readObjectOverride() throws OptionalDataException,
            ClassNotFoundException, IOException {
        if (input == null) {
            return null;
        }
        throw new IOException();
    }
    public short readShort() throws IOException {
        return primitiveTypes.readShort();
    }
    protected void readStreamHeader() throws IOException,
            StreamCorruptedException {
        if (input.readShort() == STREAM_MAGIC
                && input.readShort() == STREAM_VERSION) {
            return;
        }
        throw new StreamCorruptedException();
    }
    public int readUnsignedByte() throws IOException {
        return primitiveTypes.readUnsignedByte();
    }
    public int readUnsignedShort() throws IOException {
        return primitiveTypes.readUnsignedShort();
    }
    public String readUTF() throws IOException {
        return primitiveTypes.readUTF();
    }
    private Object registeredObjectRead(Integer handle)
            throws InvalidObjectException {
        Object res = objectsRead.get(handle);
        if (res == UNSHARED_OBJ) {
            throw new InvalidObjectException(Msg.getString("KA010")); 
        }
        return res;
    }
    private void registerObjectRead(Object obj, Integer handle, boolean unshared) {
        objectsRead.put(handle, unshared ? UNSHARED_OBJ : obj);
    }
    public synchronized void registerValidation(ObjectInputValidation object,
            int priority) throws NotActiveException, InvalidObjectException {
        Object instanceBeingRead = this.currentObject;
        if (instanceBeingRead == null && nestedLevels == 0) {
            throw new NotActiveException();
        }
        if (object == null) {
            throw new InvalidObjectException(Msg.getString("K00d9")); 
        }
        InputValidationDesc desc = new InputValidationDesc();
        desc.validator = object;
        desc.priority = priority;
        if (validations == null) {
            validations = new InputValidationDesc[1];
            validations[0] = desc;
        } else {
            int i = 0;
            for (; i < validations.length; i++) {
                InputValidationDesc validation = validations[i];
                if (priority >= validation.priority) {
                    break; 
                }
            }
            InputValidationDesc[] oldValidations = validations;
            int currentSize = oldValidations.length;
            validations = new InputValidationDesc[currentSize + 1];
            System.arraycopy(oldValidations, 0, validations, 0, i);
            System.arraycopy(oldValidations, i, validations, i + 1, currentSize
                    - i);
            validations[i] = desc;
        }
    }
    private void resetSeenObjects() {
        objectsRead = new HashMap<Integer, Object>();
        currentHandle = baseWireHandle;
        primitiveData = emptyStream;
    }
    private void resetState() {
        resetSeenObjects();
        hasPushbackTC = false;
        pushbackTC = 0;
    }
    protected Class<?> resolveClass(ObjectStreamClass osClass)
            throws IOException, ClassNotFoundException {
        Class<?> cls = osClass.forClass();
        if (null == cls) {
            String className = osClass.getName();
            cls = PRIMITIVE_CLASSES.get(className);
            if (null == cls) {
                cls = Class.forName(className, true, callerClassLoader);
            }
        }
        return cls;
    }
    protected Object resolveObject(Object object) throws IOException {
        return object;
    }
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, byte value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, char value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, double value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, float value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, int value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, long value)
            throws NoSuchFieldError;
    private static native void objSetField(Object instance,
            Class<?> declaringClass, String fieldName, String fieldTypeName,
            Object value) throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, short value)
            throws NoSuchFieldError;
    private static native void setField(Object instance,
            Class<?> declaringClass, String fieldName, boolean value)
            throws NoSuchFieldError;
    public int skipBytes(int length) throws IOException {
        if (input == null) {
            throw new NullPointerException();
        }
        int offset = 0;
        while (offset < length) {
            checkReadPrimitiveTypes();
            long skipped = primitiveData.skip(length - offset);
            if (skipped == 0) {
                return offset;
            }
            offset += (int) skipped;
        }
        return length;
    }
    private void verifyAndInit(ObjectStreamClass loadedStreamClass)
            throws InvalidClassException {
        Class<?> localClass = loadedStreamClass.forClass();
        ObjectStreamClass localStreamClass = ObjectStreamClass
                .lookupStreamClass(localClass);
        if (loadedStreamClass.getSerialVersionUID() != localStreamClass
                .getSerialVersionUID()) {
            throw new InvalidClassException(loadedStreamClass.getName(), Msg
                    .getString("K00da", loadedStreamClass, 
                            localStreamClass));
        }
        String loadedClassBaseName = getBaseName(loadedStreamClass.getName());
        String localClassBaseName = getBaseName(localStreamClass.getName());
        if (!loadedClassBaseName.equals(localClassBaseName)) {
            throw new InvalidClassException(loadedStreamClass.getName(), Msg
                    .getString("KA015", loadedClassBaseName, 
                            localClassBaseName));
        }
        loadedStreamClass.initPrivateFields(localStreamClass);
    }
    private static String getBaseName(String fullName) {
        int k = fullName.lastIndexOf('.');
        if (k == -1 || k == (fullName.length() - 1)) {
            return fullName;
        }
        return fullName.substring(k + 1);
    }
    private static void checkedSetSuperClassDesc(ObjectStreamClass desc,
            ObjectStreamClass superDesc) throws StreamCorruptedException {
        if (desc.equals(superDesc)) {
            throw new StreamCorruptedException();
        }
        desc.setSuperclass(superDesc);
    }
}

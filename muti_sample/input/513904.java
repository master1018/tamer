public class ObjectOutputStream extends OutputStream implements ObjectOutput,
        ObjectStreamConstants {
    private static final byte NOT_SC_BLOCK_DATA = (byte) (SC_BLOCK_DATA ^ 0xFF);
    private int nestedLevels;
    private DataOutputStream output;
    private boolean enableReplace;
    private DataOutputStream primitiveTypes;
    private ByteArrayOutputStream primitiveTypesBuffer;
    private IdentityHashMap<Object, Integer> objectsWritten;
    private int currentHandle;
    private Object currentObject;
    private ObjectStreamClass currentClass;
    private int protocolVersion;
    private StreamCorruptedException nestedException;
    private EmulatedFieldsForDumping currentPutField;
    private boolean subclassOverridingImplementation;
    private final ObjectStreamClass proxyClassDesc = ObjectStreamClass.lookup(Proxy.class);
    public static abstract class PutField {
        public abstract void put(String name, boolean value);
        public abstract void put(String name, char value);
        public abstract void put(String name, byte value);
        public abstract void put(String name, short value);
        public abstract void put(String name, int value);
        public abstract void put(String name, long value);
        public abstract void put(String name, float value);
        public abstract void put(String name, double value);
        public abstract void put(String name, Object value);
        @Deprecated
        public abstract void write(ObjectOutput out) throws IOException;
    }
    protected ObjectOutputStream() throws IOException, SecurityException {
        super();
        SecurityManager currentManager = System.getSecurityManager();
        if (currentManager != null) {
            currentManager.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        this.subclassOverridingImplementation = true;
    }
    public ObjectOutputStream(OutputStream output) throws IOException {
        Class<?> implementationClass = getClass();
        Class<?> thisClass = ObjectOutputStream.class;
        if (implementationClass != thisClass) {
            boolean mustCheck = false;
            try {
                Method method = implementationClass.getMethod("putFields", 
                        ObjectStreamClass.EMPTY_CONSTRUCTOR_PARAM_TYPES);
                mustCheck = method.getDeclaringClass() != thisClass;
            } catch (NoSuchMethodException e) {
            }
            if (!mustCheck) {
                try {
                    Method method = implementationClass.getMethod(
                            "writeUnshared", 
                            ObjectStreamClass.UNSHARED_PARAM_TYPES);
                    mustCheck = method.getDeclaringClass() != thisClass;
                } catch (NoSuchMethodException e) {
                }
            }
            if (mustCheck) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm
                            .checkPermission(ObjectStreamConstants.SUBCLASS_IMPLEMENTATION_PERMISSION);
                }
            }
        }
        this.output = (output instanceof DataOutputStream) ? (DataOutputStream) output
                : new DataOutputStream(output);
        this.enableReplace = false;
        this.protocolVersion = PROTOCOL_VERSION_2;
        this.subclassOverridingImplementation = false;
        resetState();
        this.nestedException = new StreamCorruptedException();
        primitiveTypes = this.output;
        writeStreamHeader();
        primitiveTypes = null;
    }
    protected void annotateClass(Class<?> aClass) throws IOException {
    }
    protected void annotateProxyClass(Class<?> aClass) throws IOException {
    }
    private void checkWritePrimitiveTypes() {
        if (primitiveTypes == null) {
            primitiveTypesBuffer = new ByteArrayOutputStream(128);
            primitiveTypes = new DataOutputStream(primitiveTypesBuffer);
        }
    }
    @Override
    public void close() throws IOException {
        flush();
        output.close();
    }
    private void computePutField() {
        currentPutField = new EmulatedFieldsForDumping(currentClass);
    }
    public void defaultWriteObject() throws IOException {
        if (currentObject == null) {
            throw new NotActiveException();
        }
        writeFieldValues(currentObject, currentClass);
    }
    protected void drain() throws IOException {
        if (primitiveTypes == null || primitiveTypesBuffer == null) {
            return;
        }
        int offset = 0;
        byte[] written = primitiveTypesBuffer.toByteArray();
        while (offset < written.length) {
            int toWrite = written.length - offset > 1024 ? 1024
                    : written.length - offset;
            if (toWrite < 256) {
                output.writeByte(TC_BLOCKDATA);
                output.writeByte((byte) toWrite);
            } else {
                output.writeByte(TC_BLOCKDATALONG);
                output.writeInt(toWrite);
            }
            output.write(written, offset, toWrite);
            offset += toWrite;
        }
        primitiveTypes = null;
        primitiveTypesBuffer = null;
    }
    private Integer dumpCycle(Object obj) throws IOException {
        Integer handle = objectsWritten.get(obj);
        if (handle != null) {
            writeCyclicReference(handle);
            return handle;
        }
        return null;
    }
    protected boolean enableReplaceObject(boolean enable)
            throws SecurityException {
        if (enable) {
            SecurityManager currentManager = System.getSecurityManager();
            if (currentManager != null) {
                currentManager.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        boolean originalValue = enableReplace;
        enableReplace = enable;
        return originalValue;
    }
    @Override
    public void flush() throws IOException {
        drain();
        output.flush();
    }
    private static native boolean getFieldBool(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native byte getFieldByte(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native char getFieldChar(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native double getFieldDouble(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native float getFieldFloat(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native int getFieldInt(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native long getFieldLong(Object instance,
            Class<?> declaringClass, String fieldName);
    private static native Object getFieldObj(Object instance,
            Class<?> declaringClass, String fieldName, String fieldTypeName);
    private static native short getFieldShort(Object instance,
            Class<?> declaringClass, String fieldName);
    private Integer nextHandle() {
        return Integer.valueOf(this.currentHandle++);
    }
    public PutField putFields() throws IOException {
        if (currentObject == null) {
            throw new NotActiveException();
        }
        if (currentPutField == null) {
            computePutField();
        }
        return currentPutField;
    }
    private Integer registerObjectWritten(Object obj) {
        Integer handle = nextHandle();
        objectsWritten.put(obj, handle);
        return handle;
    }
    private void removeUnsharedReference(Object obj, Integer previousHandle) {
        if (previousHandle != null) {
            objectsWritten.put(obj, previousHandle);
        } else {
            objectsWritten.remove(obj);
        }
    }
    protected Object replaceObject(Object object) throws IOException {
        return object;
    }
    public void reset() throws IOException {
        drain();
        output.writeByte(TC_RESET);
        resetState();
    }
    private void resetSeenObjects() {
        objectsWritten = new IdentityHashMap<Object, Integer>();
        currentHandle = baseWireHandle;
    }
    private void resetState() {
        resetSeenObjects();
        nestedLevels = 0;
    }
    public void useProtocolVersion(int version) throws IOException {
        if (!objectsWritten.isEmpty()) {
            throw new IllegalStateException(Msg.getString("KA028")); 
        }
        if (version != ObjectStreamConstants.PROTOCOL_VERSION_1
                && version != ObjectStreamConstants.PROTOCOL_VERSION_2) {
            throw new IllegalArgumentException(Msg.getString("K00b3", version)); 
        }
        protocolVersion = version;
    }
    @Override
    public void write(byte[] buffer) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(buffer);
    }
    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(buffer, offset, length);
    }
    @Override
    public void write(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.write(value);
    }
    public void writeBoolean(boolean value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeBoolean(value);
    }
    public void writeByte(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeByte(value);
    }
    public void writeBytes(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeBytes(value);
    }
    public void writeChar(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeChar(value);
    }
    public void writeChars(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeChars(value);
    }
    private Integer writeClassDesc(ObjectStreamClass classDesc, boolean unshared)
            throws IOException {
        if (classDesc == null) {
            writeNull();
            return null;
        }
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(classDesc);
        }
        if (handle == null) {
            Class<?> classToWrite = classDesc.forClass();
            Integer previousHandle = null;
            if (unshared) {
                previousHandle = objectsWritten.get(classDesc);
            }
            handle = nextHandle();
            objectsWritten.put(classDesc, handle);
            if (classDesc.isProxy()) {
                output.writeByte(TC_PROXYCLASSDESC);
                Class<?>[] interfaces = classToWrite.getInterfaces();
                output.writeInt(interfaces.length);
                for (int i = 0; i < interfaces.length; i++) {
                    output.writeUTF(interfaces[i].getName());
                }
                annotateProxyClass(classToWrite);
                output.writeByte(TC_ENDBLOCKDATA);
                writeClassDesc(proxyClassDesc, false);
                if (unshared) {
                    removeUnsharedReference(classDesc, previousHandle);
                }
                return handle;
            }
            output.writeByte(TC_CLASSDESC);
            if (protocolVersion == PROTOCOL_VERSION_1) {
                writeNewClassDesc(classDesc);
            } else {
                primitiveTypes = output;
                writeClassDescriptor(classDesc);
                primitiveTypes = null;
            }
            annotateClass(classToWrite);
            drain(); 
            output.writeByte(TC_ENDBLOCKDATA);
            writeClassDesc(classDesc.getSuperclass(), unshared);
            if (unshared) {
                removeUnsharedReference(classDesc, previousHandle);
            }
        }
        return handle;
    }
    private void writeCyclicReference(Integer handle) throws IOException {
        output.writeByte(TC_REFERENCE);
        output.writeInt(handle.intValue());
    }
    public void writeDouble(double value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeDouble(value);
    }
    private void writeFieldDescriptors(ObjectStreamClass classDesc,
            boolean externalizable) throws IOException {
        Class<?> loadedClass = classDesc.forClass();
        ObjectStreamField[] fields = null;
        int fieldCount = 0;
        if (!externalizable && loadedClass != ObjectStreamClass.STRINGCLASS) {
            fields = classDesc.fields();
            fieldCount = fields.length;
        }
        output.writeShort(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            ObjectStreamField f = fields[i];
            output.writeByte(f.getTypeCode());
            output.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                writeObject(f.getTypeString());
            }
        }
    }
    public void writeFields() throws IOException {
        if (currentPutField == null) {
            throw new NotActiveException();
        }
        writeFieldValues(currentPutField);
    }
    private void writeFieldValues(EmulatedFieldsForDumping emulatedFields)
            throws IOException {
        EmulatedFields accessibleSimulatedFields = emulatedFields
                .emulatedFields(); 
        EmulatedFields.ObjectSlot[] slots = accessibleSimulatedFields.slots();
        for (int i = 0; i < slots.length; i++) {
            EmulatedFields.ObjectSlot slot = slots[i];
            Object fieldValue = slot.getFieldValue();
            Class<?> type = slot.getField().getType();
            if (type == Integer.TYPE) {
                output.writeInt(fieldValue != null ? ((Integer) fieldValue)
                        .intValue() : 0);
            } else if (type == Byte.TYPE) {
                output.writeByte(fieldValue != null ? ((Byte) fieldValue)
                        .byteValue() : (byte) 0);
            } else if (type == Character.TYPE) {
                output.writeChar(fieldValue != null ? ((Character) fieldValue)
                        .charValue() : (char) 0);
            } else if (type == Short.TYPE) {
                output.writeShort(fieldValue != null ? ((Short) fieldValue)
                        .shortValue() : (short) 0);
            } else if (type == Boolean.TYPE) {
                output.writeBoolean(fieldValue != null ? ((Boolean) fieldValue)
                        .booleanValue() : false);
            } else if (type == Long.TYPE) {
                output.writeLong(fieldValue != null ? ((Long) fieldValue)
                        .longValue() : (long) 0);
            } else if (type == Float.TYPE) {
                output.writeFloat(fieldValue != null ? ((Float) fieldValue)
                        .floatValue() : (float) 0);
            } else if (type == Double.TYPE) {
                output.writeDouble(fieldValue != null ? ((Double) fieldValue)
                        .doubleValue() : (double) 0);
            } else {
                writeObject(fieldValue);
            }
        }
    }
    private void writeFieldValues(Object obj, ObjectStreamClass classDesc)
            throws IOException {
        ObjectStreamField[] fields = classDesc.fields();
        Class<?> declaringClass = classDesc.forClass();
        for(ObjectStreamField fieldDesc : fields) {
            try {
                if (fieldDesc.isPrimitive()) {
                    switch (fieldDesc.getTypeCode()) {
                        case 'B':
                            output.writeByte(getFieldByte(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'C':
                            output.writeChar(getFieldChar(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'D':
                            output.writeDouble(getFieldDouble(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'F':
                            output.writeFloat(getFieldFloat(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'I':
                            output.writeInt(getFieldInt(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'J':
                            output.writeLong(getFieldLong(obj, declaringClass,
                                    fieldDesc.getName()));
                            break;
                        case 'S':
                            output.writeShort(getFieldShort(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        case 'Z':
                            output.writeBoolean(getFieldBool(obj,
                                    declaringClass, fieldDesc.getName()));
                            break;
                        default:
                            throw new IOException(
                                    org.apache.harmony.luni.util.Msg.getString(
                                            "K00d5", fieldDesc.getTypeCode())); 
                    }
                } else {
                    Object objField = getFieldObj(obj, declaringClass, fieldDesc
                            .getName(), fieldDesc.getTypeString());
                    if (fieldDesc.isUnshared()) {
                        writeUnshared(objField);
                    } else {
                        writeObject(objField);
                    }
                }
            } catch (NoSuchFieldError nsf) {
                throw new InvalidClassException(classDesc.getName());
            }
        }
    }
    public void writeFloat(float value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeFloat(value);
    }
    private void writeHierarchy(Object object, ObjectStreamClass classDesc)
            throws IOException, NotActiveException {
        if (object == null) {
            throw new NotActiveException();
        }
        if (classDesc.getSuperclass() != null) {
            writeHierarchy(object, classDesc.getSuperclass());
        }
        currentObject = object;
        currentClass = classDesc;
        boolean executed = false;
        try {
            if (classDesc.hasMethodWriteObject()){
                final Method method = classDesc.getMethodWriteObject();
                try {
                    method.invoke(object, new Object[] { this });
                    executed = true;
                } catch (InvocationTargetException e) {
                    Throwable ex = e.getTargetException();
                    if (ex instanceof RuntimeException) {
                        throw (RuntimeException) ex;
                    } else if (ex instanceof Error) {
                        throw (Error) ex;
                    }
                    throw (IOException) ex;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e.toString());
                }
            }
            if (executed) {
                drain();
                output.writeByte(TC_ENDBLOCKDATA);
            } else {
                defaultWriteObject();
            }
        } finally {
            currentObject = null;
            currentClass = null;
            currentPutField = null;
        }
    }
    public void writeInt(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeInt(value);
    }
    public void writeLong(long value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeLong(value);
    }
    private Integer writeNewArray(Object array, Class<?> arrayClass, ObjectStreamClass arrayClDesc,
            Class<?> componentType, boolean unshared) throws IOException {
        output.writeByte(TC_ARRAY);
        writeClassDesc(arrayClDesc, false);
        Integer handle = nextHandle();
        if (!unshared) {
            objectsWritten.put(array, handle);
        }
        if (componentType.isPrimitive()) {
            if (componentType == Integer.TYPE) {
                int[] intArray = (int[]) array;
                output.writeInt(intArray.length);
                for (int i = 0; i < intArray.length; i++) {
                    output.writeInt(intArray[i]);
                }
            } else if (componentType == Byte.TYPE) {
                byte[] byteArray = (byte[]) array;
                output.writeInt(byteArray.length);
                output.write(byteArray, 0, byteArray.length);
            } else if (componentType == Character.TYPE) {
                char[] charArray = (char[]) array;
                output.writeInt(charArray.length);
                for (int i = 0; i < charArray.length; i++) {
                    output.writeChar(charArray[i]);
                }
            } else if (componentType == Short.TYPE) {
                short[] shortArray = (short[]) array;
                output.writeInt(shortArray.length);
                for (int i = 0; i < shortArray.length; i++) {
                    output.writeShort(shortArray[i]);
                }
            } else if (componentType == Boolean.TYPE) {
                boolean[] booleanArray = (boolean[]) array;
                output.writeInt(booleanArray.length);
                for (int i = 0; i < booleanArray.length; i++) {
                    output.writeBoolean(booleanArray[i]);
                }
            } else if (componentType == Long.TYPE) {
                long[] longArray = (long[]) array;
                output.writeInt(longArray.length);
                for (int i = 0; i < longArray.length; i++) {
                    output.writeLong(longArray[i]);
                }
            } else if (componentType == Float.TYPE) {
                float[] floatArray = (float[]) array;
                output.writeInt(floatArray.length);
                for (int i = 0; i < floatArray.length; i++) {
                    output.writeFloat(floatArray[i]);
                }
            } else if (componentType == Double.TYPE) {
                double[] doubleArray = (double[]) array;
                output.writeInt(doubleArray.length);
                for (int i = 0; i < doubleArray.length; i++) {
                    output.writeDouble(doubleArray[i]);
                }
            } else {
                throw new InvalidClassException(
                        org.apache.harmony.luni.util.Msg.getString(
                                "K00d7", arrayClass.getName())); 
            }
        } else {
            Object[] objectArray = (Object[]) array;
            output.writeInt(objectArray.length);
            for (int i = 0; i < objectArray.length; i++) {
                writeObject(objectArray[i]);
            }
        }
        return handle;
    }
    private Integer writeNewClass(Class<?> object, boolean unshared)
            throws IOException {
        output.writeByte(TC_CLASS);
        ObjectStreamClass clDesc = ObjectStreamClass.lookupStreamClass(object);
        if (clDesc.isEnum()) {
            writeEnumDesc(object, clDesc, unshared);
        } else {
            writeClassDesc(clDesc, unshared);
        }
        Integer handle = nextHandle();
        if (!unshared) {
            objectsWritten.put(object, handle);
        }
        return handle;
    }
    private void writeNewClassDesc(ObjectStreamClass classDesc)
            throws IOException {
        output.writeUTF(classDesc.getName());
        output.writeLong(classDesc.getSerialVersionUID());
        byte flags = classDesc.getFlags();
        boolean externalizable = classDesc.isExternalizable();
        if (externalizable) {
            if (protocolVersion == PROTOCOL_VERSION_1) {
                flags &= NOT_SC_BLOCK_DATA;
            } else {
                flags |= SC_BLOCK_DATA;
            }
        }
        output.writeByte(flags);
        if ((SC_ENUM | SC_SERIALIZABLE) != classDesc.getFlags()) {
            writeFieldDescriptors(classDesc, externalizable);
        } else {
            output.writeShort(0);
        }
    }
    protected void writeClassDescriptor(ObjectStreamClass classDesc)
            throws IOException {
        writeNewClassDesc(classDesc);
    }
    private void writeNewException(Exception ex) throws IOException {
        output.writeByte(TC_EXCEPTION);
        resetSeenObjects();
        writeObjectInternal(ex, false, false, false); 
        resetSeenObjects();
    }
    private Integer writeNewObject(Object object, Class<?> theClass, ObjectStreamClass clDesc,
            boolean unshared) throws IOException {
        EmulatedFieldsForDumping originalCurrentPutField = currentPutField; 
        currentPutField = null; 
        boolean externalizable = clDesc.isExternalizable();
        boolean serializable = clDesc.isSerializable();
        if (!externalizable && !serializable) {
            throw new NotSerializableException(theClass.getName());
        }
        output.writeByte(TC_OBJECT);
        writeClassDesc(clDesc, false);
        Integer previousHandle = null;
        if (unshared) {
            previousHandle = objectsWritten.get(object);
        }
        Integer handle = nextHandle();
        objectsWritten.put(object, handle);
        currentObject = object;
        currentClass = clDesc;
        try {
            if (externalizable) {
                boolean noBlockData = protocolVersion == PROTOCOL_VERSION_1;
                if (noBlockData) {
                    primitiveTypes = output;
                }
                ((Externalizable) object).writeExternal(this);
                if (noBlockData) {
                    primitiveTypes = null;
                } else {
                    drain();
                    output.writeByte(TC_ENDBLOCKDATA);
                }
            } else { 
                writeHierarchy(object, currentClass);
            }
        } finally {
            if (unshared) {
                removeUnsharedReference(object, previousHandle);
            }
            currentObject = null;
            currentClass = null;
            currentPutField = originalCurrentPutField;
        }
        return handle;
    }
    private Integer writeNewString(String object, boolean unshared)
            throws IOException {
        long count = output.countUTFBytes(object);
        byte[] buffer;
        int offset = 0;
        if (count <= 0xffff) {
            buffer = new byte[(int)count+3];
            buffer[offset++] = TC_STRING;
            offset = output.writeShortToBuffer((short) count, buffer, offset);
        } else {
            buffer = new byte[(int)count+9];
            buffer[offset++] = TC_LONGSTRING;
            offset = output.writeLongToBuffer(count, buffer, offset);
        }
        offset = output.writeUTFBytesToBuffer(object, buffer, offset);
        output.write(buffer, 0, offset);
        Integer handle = nextHandle();
        if (!unshared) {
            objectsWritten.put(object, handle);
        }
        return handle;
    }
    private void writeNull() throws IOException {
        output.writeByte(TC_NULL);
    }
    public final void writeObject(Object object) throws IOException {
        writeObject(object, false);
    }
    public void writeUnshared(Object object) throws IOException {
        writeObject(object, true);
    }
    private void writeObject(Object object, boolean unshared)
            throws IOException {
        boolean setOutput = (primitiveTypes == output);
        if (setOutput) {
            primitiveTypes = null;
        }
        if (subclassOverridingImplementation && !unshared) {
            writeObjectOverride(object);
        } else {
            try {
                drain();
                writeObjectInternal(object, unshared, true, true);
                if (setOutput) {
                    primitiveTypes = output;
                }
            } catch (IOException ioEx1) {
                if (nestedLevels == 0 && ioEx1 != nestedException) {
                    try {
                        writeNewException(ioEx1);
                    } catch (IOException ioEx2) {
                        nestedException.fillInStackTrace();
                        throw nestedException;
                    }
                }
                throw ioEx1; 
            }
        }
    }
    private Integer writeObjectInternal(Object object, boolean unshared,
            boolean computeClassBasedReplacement,
            boolean computeStreamReplacement) throws IOException {
        if (object == null) {
            writeNull();
            return null;
        }
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(object);
            if (handle != null) {
                return handle; 
            }
        }
        Class<?> objClass = object.getClass();
        ObjectStreamClass clDesc = ObjectStreamClass.lookupStreamClass(objClass);
        nestedLevels++;
        try {
            if (!(enableReplace && computeStreamReplacement)) {
                if (objClass == ObjectStreamClass.CLASSCLASS) {
                    return writeNewClass((Class<?>) object, unshared);
                }
                if (objClass == ObjectStreamClass.OBJECTSTREAMCLASSCLASS) {
                    return writeClassDesc((ObjectStreamClass) object, unshared);
                }
            }
            if (clDesc.isSerializable()
                    && computeClassBasedReplacement) {
                if(clDesc.hasMethodWriteReplace()){
                    Method methodWriteReplace = clDesc.getMethodWriteReplace();
                    Object replObj = null;
                    try {
                        replObj = methodWriteReplace.invoke(object, (Object[]) null);
                    } catch (IllegalAccessException iae) {
                        replObj = object;
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
                    if (replObj != object) {
                        Integer replacementHandle = writeObjectInternal(
                                replObj, false, false,
                                computeStreamReplacement);
                        if (replacementHandle != null) {
                            objectsWritten.put(object, replacementHandle);
                        }
                        return replacementHandle;
                    }
                }
            }
            if (enableReplace && computeStreamReplacement) {
                Object streamReplacement = replaceObject(object);
                if (streamReplacement != object) {
                    Integer replacementHandle = writeObjectInternal(
                            streamReplacement, false,
                            computeClassBasedReplacement, false);
                    if (replacementHandle != null) {
                        objectsWritten.put(object, replacementHandle);
                    }
                    return replacementHandle;
                }
            }
            if (objClass == ObjectStreamClass.CLASSCLASS) {
                return writeNewClass((Class<?>) object, unshared);
            }
            if (objClass == ObjectStreamClass.OBJECTSTREAMCLASSCLASS) {
                return writeClassDesc((ObjectStreamClass) object, unshared);
            }
            if (objClass == ObjectStreamClass.STRINGCLASS) {
                return writeNewString((String) object, unshared);
            }
            if (objClass.isArray()) {
                return writeNewArray(object, objClass, clDesc, objClass
                        .getComponentType(), unshared);
            }
            if (object instanceof Enum) {
                return writeNewEnum(object, objClass, unshared);
            }
            return writeNewObject(object, objClass, clDesc, unshared);
        } finally {
            nestedLevels--;
        }
    }
    private ObjectStreamClass writeEnumDesc(Class<?> theClass, ObjectStreamClass classDesc, boolean unshared)
            throws IOException {
        classDesc.setFlags((byte) (SC_SERIALIZABLE | SC_ENUM));
        Integer previousHandle = null;
        if (unshared) {
            previousHandle = objectsWritten.get(classDesc);
        }
        Integer handle = null;
        if (!unshared) {
            handle = dumpCycle(classDesc);
        }
        if (handle == null) {
            Class<?> classToWrite = classDesc.forClass();
            objectsWritten.put(classDesc, nextHandle());
            output.writeByte(TC_CLASSDESC);
            if (protocolVersion == PROTOCOL_VERSION_1) {
                writeNewClassDesc(classDesc);
            } else {
                primitiveTypes = output;
                writeClassDescriptor(classDesc);
                primitiveTypes = null;
            }
            annotateClass(classToWrite);
            drain(); 
            output.writeByte(TC_ENDBLOCKDATA);
            ObjectStreamClass superClassDesc = classDesc.getSuperclass();
            if (null != superClassDesc) {
                superClassDesc.setFlags((byte) (SC_SERIALIZABLE | SC_ENUM));
                writeEnumDesc(superClassDesc.forClass(), superClassDesc, unshared);
            } else {
                output.writeByte(TC_NULL);
            }
            if (unshared) {
                removeUnsharedReference(classDesc, previousHandle);
            }
        }
        return classDesc;
    }
    private Integer writeNewEnum(Object object, Class<?> theClass,
            boolean unshared) throws IOException {
        EmulatedFieldsForDumping originalCurrentPutField = currentPutField; 
        currentPutField = null;
        output.writeByte(TC_ENUM);
        while (theClass != null && !theClass.isEnum()) {
            theClass = theClass.getSuperclass();
        }
        ObjectStreamClass classDesc = ObjectStreamClass.lookup(theClass);
        writeEnumDesc(theClass, classDesc, unshared);
        Integer previousHandle = null;
        if (unshared) {
            previousHandle = objectsWritten.get(object);
        }
        Integer handle = nextHandle();
        objectsWritten.put(object, handle);
        ObjectStreamField[] fields = classDesc.getSuperclass().fields();
        Class<?> declaringClass = classDesc.getSuperclass().forClass();
        if (null != fields && fields.length > 1) {
            String str = (String) getFieldObj(object, declaringClass, fields[1]
                    .getName(), fields[1].getTypeString());
            Integer strhandle = null;
            if (!unshared) {
                strhandle = dumpCycle(str);
            }
            if (null == strhandle) {
                writeNewString(str, unshared);
            }
        }
        if (unshared) {
            removeUnsharedReference(object, previousHandle);
        }
        currentPutField = originalCurrentPutField;
        return handle;
    }
    protected void writeObjectOverride(Object object) throws IOException {
        if (!subclassOverridingImplementation) {
            throw new IOException();
        }
    }
    public void writeShort(int value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeShort(value);
    }
    protected void writeStreamHeader() throws IOException {
        output.writeShort(STREAM_MAGIC);
        output.writeShort(STREAM_VERSION);
    }
    public void writeUTF(String value) throws IOException {
        checkWritePrimitiveTypes();
        primitiveTypes.writeUTF(value);
    }
}

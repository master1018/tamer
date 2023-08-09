public class HeapHprofBinWriter extends AbstractHeapGraphWriter {
    private static final String HPROF_HEADER = "JAVA PROFILE 1.0.1";
    private static final int HPROF_UTF8             = 0x01;
    private static final int HPROF_LOAD_CLASS       = 0x02;
    private static final int HPROF_UNLOAD_CLASS     = 0x03;
    private static final int HPROF_FRAME            = 0x04;
    private static final int HPROF_TRACE            = 0x05;
    private static final int HPROF_ALLOC_SITES      = 0x06;
    private static final int HPROF_HEAP_SUMMARY     = 0x07;
    private static final int HPROF_START_THREAD     = 0x0A;
    private static final int HPROF_END_THREAD       = 0x0B;
    private static final int HPROF_HEAP_DUMP        = 0x0C;
    private static final int HPROF_CPU_SAMPLES      = 0x0D;
    private static final int HPROF_CONTROL_SETTINGS = 0x0E;
    private static final int HPROF_GC_ROOT_UNKNOWN       = 0xFF;
    private static final int HPROF_GC_ROOT_JNI_GLOBAL    = 0x01;
    private static final int HPROF_GC_ROOT_JNI_LOCAL     = 0x02;
    private static final int HPROF_GC_ROOT_JAVA_FRAME    = 0x03;
    private static final int HPROF_GC_ROOT_NATIVE_STACK  = 0x04;
    private static final int HPROF_GC_ROOT_STICKY_CLASS  = 0x05;
    private static final int HPROF_GC_ROOT_THREAD_BLOCK  = 0x06;
    private static final int HPROF_GC_ROOT_MONITOR_USED  = 0x07;
    private static final int HPROF_GC_ROOT_THREAD_OBJ    = 0x08;
    private static final int HPROF_GC_CLASS_DUMP         = 0x20;
    private static final int HPROF_GC_INSTANCE_DUMP      = 0x21;
    private static final int HPROF_GC_OBJ_ARRAY_DUMP     = 0x22;
    private static final int HPROF_GC_PRIM_ARRAY_DUMP    = 0x23;
    private static final int HPROF_ARRAY_OBJECT  = 1;
    private static final int HPROF_NORMAL_OBJECT = 2;
    private static final int HPROF_BOOLEAN       = 4;
    private static final int HPROF_CHAR          = 5;
    private static final int HPROF_FLOAT         = 6;
    private static final int HPROF_DOUBLE        = 7;
    private static final int HPROF_BYTE          = 8;
    private static final int HPROF_SHORT         = 9;
    private static final int HPROF_INT           = 10;
    private static final int HPROF_LONG          = 11;
    private static final int JVM_SIGNATURE_BOOLEAN = 'Z';
    private static final int JVM_SIGNATURE_CHAR    = 'C';
    private static final int JVM_SIGNATURE_BYTE    = 'B';
    private static final int JVM_SIGNATURE_SHORT   = 'S';
    private static final int JVM_SIGNATURE_INT     = 'I';
    private static final int JVM_SIGNATURE_LONG    = 'J';
    private static final int JVM_SIGNATURE_FLOAT   = 'F';
    private static final int JVM_SIGNATURE_DOUBLE  = 'D';
    private static final int JVM_SIGNATURE_ARRAY   = '[';
    private static final int JVM_SIGNATURE_CLASS   = 'L';
    public synchronized void write(String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        FileChannel chn = fos.getChannel();
        out = new DataOutputStream(new BufferedOutputStream(fos));
        VM vm = VM.getVM();
        dbg = vm.getDebugger();
        objectHeap = vm.getObjectHeap();
        symTbl = vm.getSymbolTable();
        OBJ_ID_SIZE = (int) vm.getOopSize();
        BOOLEAN_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_BOOLEAN);
        BYTE_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_BYTE);
        CHAR_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_CHAR);
        SHORT_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_SHORT);
        INT_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_INT);
        LONG_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_LONG);
        FLOAT_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_FLOAT);
        DOUBLE_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_DOUBLE);
        OBJECT_BASE_OFFSET = TypeArray.baseOffsetInBytes(BasicType.T_OBJECT);
        BOOLEAN_SIZE = objectHeap.getBooleanSize();
        BYTE_SIZE = objectHeap.getByteSize();
        CHAR_SIZE = objectHeap.getCharSize();
        SHORT_SIZE = objectHeap.getShortSize();
        INT_SIZE = objectHeap.getIntSize();
        LONG_SIZE = objectHeap.getLongSize();
        FLOAT_SIZE = objectHeap.getFloatSize();
        DOUBLE_SIZE = objectHeap.getDoubleSize();
        writeFileHeader();
        writeDummyTrace();
        writeSymbols();
        writeClasses();
        out.writeByte((byte)HPROF_HEAP_DUMP);
        out.writeInt(0); 
        out.flush();
        long dumpStart = chn.position();
        out.writeInt(0);
        writeClassDumpRecords();
        super.write();
        out.flush();
        out = null;
        long dumpEnd = chn.position();
        int dumpLen = (int) (dumpEnd - dumpStart - 4);
        chn.position(dumpStart);
        fos.write((dumpLen >>> 24) & 0xFF);
        fos.write((dumpLen >>> 16) & 0xFF);
        fos.write((dumpLen >>> 8) & 0xFF);
        fos.write((dumpLen >>> 0) & 0xFF);
        fos.close();
    }
    private void writeClassDumpRecords() throws IOException {
        SystemDictionary sysDict = VM.getVM().getSystemDictionary();
        try {
            sysDict.allClassesDo(new SystemDictionary.ClassVisitor() {
                            public void visit(Klass k) {
                                try {
                                    writeClassDumpRecord(k);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
        } catch (RuntimeException re) {
            handleRuntimeException(re);
        }
    }
    protected void writeClass(Instance instance) throws IOException {
        Klass reflectedKlass = java_lang_Class.asKlass(instance);
        if (reflectedKlass == null) {
            writeInstance(instance);
        }
    }
    private void writeClassDumpRecord(Klass k) throws IOException {
        out.writeByte((byte)HPROF_GC_CLASS_DUMP);
        writeObjectID(k.getJavaMirror());
        out.writeInt(DUMMY_STACK_TRACE_ID);
        Klass superKlass = k.getJavaSuper();
        if (superKlass != null) {
            writeObjectID(superKlass.getJavaMirror());
        } else {
            writeObjectID(null);
        }
        if (k instanceof InstanceKlass) {
            InstanceKlass ik = (InstanceKlass) k;
            writeObjectID(ik.getClassLoader());
            writeObjectID(ik.getSigners());
            writeObjectID(ik.getProtectionDomain());
            writeObjectID(null);
            writeObjectID(null);
            List fields = getInstanceFields(ik);
            int instSize = getSizeForFields(fields);
            classDataCache.put(ik, new ClassData(instSize, fields));
            out.writeInt(instSize);
            out.writeShort((short) 0);
            List declaredFields = ik.getImmediateFields();
            List staticFields = new ArrayList();
            List instanceFields = new ArrayList();
            Iterator itr = null;
            for (itr = declaredFields.iterator(); itr.hasNext();) {
                Field field = (Field) itr.next();
                if (field.isStatic()) {
                    staticFields.add(field);
                } else {
                    instanceFields.add(field);
                }
            }
            writeFieldDescriptors(staticFields, ik);
            writeFieldDescriptors(instanceFields, null);
        } else {
            if (k instanceof ObjArrayKlass) {
                ObjArrayKlass oak = (ObjArrayKlass) k;
                Klass bottomKlass = oak.getBottomKlass();
                if (bottomKlass instanceof InstanceKlass) {
                    InstanceKlass ik = (InstanceKlass) bottomKlass;
                    writeObjectID(ik.getClassLoader());
                    writeObjectID(ik.getSigners());
                    writeObjectID(ik.getProtectionDomain());
                } else {
                    writeObjectID(null);
                    writeObjectID(null);
                    writeObjectID(null);
                }
            } else {
                writeObjectID(null);
                writeObjectID(null);
                writeObjectID(null);
            }
            writeObjectID(null);
            writeObjectID(null);
            out.writeInt(0);
            out.writeShort((short) 0);
            out.writeShort((short) 0);
            out.writeShort((short) 0);
        }
    }
    protected void writeJavaThread(JavaThread jt, int index) throws IOException {
        out.writeByte((byte) HPROF_GC_ROOT_THREAD_OBJ);
        writeObjectID(jt.getThreadObj());
        out.writeInt(index);
        out.writeInt(DUMMY_STACK_TRACE_ID);
        writeLocalJNIHandles(jt, index);
    }
    protected void writeLocalJNIHandles(JavaThread jt, int index) throws IOException {
        final int threadIndex = index;
        JNIHandleBlock blk = jt.activeHandles();
        if (blk != null) {
            try {
                blk.oopsDo(new AddressVisitor() {
                           public void visitAddress(Address handleAddr) {
                               try {
                                   if (handleAddr != null) {
                                       OopHandle oopHandle = handleAddr.getOopHandleAt(0);
                                       Oop oop = objectHeap.newOop(oopHandle);
                                       if (oop != null && isJavaVisible(oop)) {
                                           out.writeByte((byte) HPROF_GC_ROOT_JNI_LOCAL);
                                           writeObjectID(oop);
                                           out.writeInt(threadIndex);
                                           out.writeInt(EMPTY_FRAME_DEPTH);
                                       }
                                   }
                               } catch (IOException exp) {
                                   throw new RuntimeException(exp);
                               }
                           }
                           public void visitCompOopAddress(Address handleAddr) {
                             throw new RuntimeException(
                                   " Should not reach here. JNIHandles are not compressed \n");
                           }
                       });
            } catch (RuntimeException re) {
                handleRuntimeException(re);
            }
        }
    }
    protected void writeGlobalJNIHandle(Address handleAddr) throws IOException {
        OopHandle oopHandle = handleAddr.getOopHandleAt(0);
        Oop oop = objectHeap.newOop(oopHandle);
        if (oop != null && isJavaVisible(oop)) {
            out.writeByte((byte) HPROF_GC_ROOT_JNI_GLOBAL);
            writeObjectID(oop);
            writeObjectID(getAddressValue(handleAddr));
        }
    }
    protected void writeObjectArray(ObjArray array) throws IOException {
        out.writeByte((byte) HPROF_GC_OBJ_ARRAY_DUMP);
        writeObjectID(array);
        out.writeInt(DUMMY_STACK_TRACE_ID);
        out.writeInt((int) array.getLength());
        writeObjectID(array.getKlass().getJavaMirror());
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
            OopHandle handle = array.getOopHandleAt(index);
            writeObjectID(getAddressValue(handle));
        }
    }
    protected void writePrimitiveArray(TypeArray array) throws IOException {
        out.writeByte((byte) HPROF_GC_PRIM_ARRAY_DUMP);
        writeObjectID(array);
        out.writeInt(DUMMY_STACK_TRACE_ID);
        out.writeInt((int) array.getLength());
        TypeArrayKlass tak = (TypeArrayKlass) array.getKlass();
        final int type = (int) tak.getElementType();
        out.writeByte((byte) type);
        switch (type) {
            case TypeArrayKlass.T_BOOLEAN:
                writeBooleanArray(array);
                break;
            case TypeArrayKlass.T_CHAR:
                writeCharArray(array);
                break;
            case TypeArrayKlass.T_FLOAT:
                writeFloatArray(array);
                break;
            case TypeArrayKlass.T_DOUBLE:
                writeDoubleArray(array);
                break;
            case TypeArrayKlass.T_BYTE:
                writeByteArray(array);
                break;
            case TypeArrayKlass.T_SHORT:
                writeShortArray(array);
                break;
            case TypeArrayKlass.T_INT:
                writeIntArray(array);
                break;
            case TypeArrayKlass.T_LONG:
                writeLongArray(array);
                break;
            default:
                throw new RuntimeException("should not reach here");
        }
    }
    private void writeBooleanArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = BOOLEAN_BASE_OFFSET + index * BOOLEAN_SIZE;
             out.writeBoolean(array.getHandle().getJBooleanAt(offset));
        }
    }
    private void writeByteArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = BYTE_BASE_OFFSET + index * BYTE_SIZE;
             out.writeByte(array.getHandle().getJByteAt(offset));
        }
    }
    private void writeShortArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = SHORT_BASE_OFFSET + index * SHORT_SIZE;
             out.writeShort(array.getHandle().getJShortAt(offset));
        }
    }
    private void writeIntArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = INT_BASE_OFFSET + index * INT_SIZE;
             out.writeInt(array.getHandle().getJIntAt(offset));
        }
    }
    private void writeLongArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = LONG_BASE_OFFSET + index * LONG_SIZE;
             out.writeLong(array.getHandle().getJLongAt(offset));
        }
    }
    private void writeCharArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = CHAR_BASE_OFFSET + index * CHAR_SIZE;
             out.writeChar(array.getHandle().getJCharAt(offset));
        }
    }
    private void writeFloatArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = FLOAT_BASE_OFFSET + index * FLOAT_SIZE;
             out.writeFloat(array.getHandle().getJFloatAt(offset));
        }
    }
    private void writeDoubleArray(TypeArray array) throws IOException {
        final int length = (int) array.getLength();
        for (int index = 0; index < length; index++) {
             long offset = DOUBLE_BASE_OFFSET + index * DOUBLE_SIZE;
             out.writeDouble(array.getHandle().getJDoubleAt(offset));
        }
    }
    protected void writeInstance(Instance instance) throws IOException {
        out.writeByte((byte) HPROF_GC_INSTANCE_DUMP);
        writeObjectID(instance);
        out.writeInt(DUMMY_STACK_TRACE_ID);
        Klass klass = instance.getKlass();
        writeObjectID(klass.getJavaMirror());
        ClassData cd = (ClassData) classDataCache.get(klass);
        if (Assert.ASSERTS_ENABLED) {
            Assert.that(cd != null, "can not get class data for " + klass.getName().asString() + klass.getHandle());
        }
        List fields = cd.fields;
        int size = cd.instSize;
        out.writeInt(size);
        for (Iterator itr = fields.iterator(); itr.hasNext();) {
            writeField((Field) itr.next(), instance);
        }
    }
    private void writeFieldDescriptors(List fields, InstanceKlass ik)
        throws IOException {
        out.writeShort((short) fields.size());
        for (Iterator itr = fields.iterator(); itr.hasNext();) {
            Field field = (Field) itr.next();
            Symbol name = symTbl.probe(field.getID().getName());
            writeSymbolID(name);
            char typeCode = (char) field.getSignature().getByteAt(0);
            int kind = signatureToHprofKind(typeCode);
            out.writeByte((byte)kind);
            if (ik != null) {
                writeField(field, ik.getJavaMirror());
            }
        }
    }
    public static int signatureToHprofKind(char ch) {
        switch (ch) {
        case JVM_SIGNATURE_CLASS:
        case JVM_SIGNATURE_ARRAY:
            return HPROF_NORMAL_OBJECT;
        case JVM_SIGNATURE_BOOLEAN:
            return HPROF_BOOLEAN;
        case JVM_SIGNATURE_CHAR:
            return HPROF_CHAR;
        case JVM_SIGNATURE_FLOAT:
            return HPROF_FLOAT;
        case JVM_SIGNATURE_DOUBLE:
            return HPROF_DOUBLE;
        case JVM_SIGNATURE_BYTE:
            return HPROF_BYTE;
        case JVM_SIGNATURE_SHORT:
            return HPROF_SHORT;
        case JVM_SIGNATURE_INT:
            return HPROF_INT;
        case JVM_SIGNATURE_LONG:
            return HPROF_LONG;
        default:
            throw new RuntimeException("should not reach here");
        }
    }
    private void writeField(Field field, Oop oop) throws IOException {
        char typeCode = (char) field.getSignature().getByteAt(0);
        switch (typeCode) {
        case JVM_SIGNATURE_BOOLEAN:
            out.writeBoolean(((BooleanField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_CHAR:
            out.writeChar(((CharField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_BYTE:
            out.writeByte(((ByteField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_SHORT:
            out.writeShort(((ShortField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_INT:
            out.writeInt(((IntField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_LONG:
            out.writeLong(((LongField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_FLOAT:
            out.writeFloat(((FloatField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_DOUBLE:
            out.writeDouble(((DoubleField)field).getValue(oop));
            break;
        case JVM_SIGNATURE_CLASS:
        case JVM_SIGNATURE_ARRAY: {
            if (VM.getVM().isCompressedOopsEnabled()) {
              OopHandle handle = ((NarrowOopField)field).getValueAsOopHandle(oop);
              writeObjectID(getAddressValue(handle));
            } else {
              OopHandle handle = ((OopField)field).getValueAsOopHandle(oop);
              writeObjectID(getAddressValue(handle));
            }
            break;
        }
        default:
            throw new RuntimeException("should not reach here");
        }
    }
    private void writeHeader(int tag, int len) throws IOException {
        out.writeByte((byte)tag);
        out.writeInt(0); 
        out.writeInt(len);
    }
    private void writeDummyTrace() throws IOException {
        writeHeader(HPROF_TRACE, 3 * 4);
        out.writeInt(DUMMY_STACK_TRACE_ID);
        out.writeInt(0);
        out.writeInt(0);
    }
    private void writeSymbols() throws IOException {
        try {
            symTbl.symbolsDo(new SymbolTable.SymbolVisitor() {
                    public void visit(Symbol sym) {
                        try {
                            writeSymbol(sym);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                });
        } catch (RuntimeException re) {
            handleRuntimeException(re);
        }
    }
    private void writeSymbol(Symbol sym) throws IOException {
        byte[] buf = sym.asString().getBytes("UTF-8");
        writeHeader(HPROF_UTF8, buf.length + OBJ_ID_SIZE);
        writeSymbolID(sym);
        out.write(buf);
    }
    private void writeClasses() throws IOException {
        SystemDictionary sysDict = VM.getVM().getSystemDictionary();
        try {
            sysDict.allClassesDo(new SystemDictionary.ClassVisitor() {
                private int serialNum = 1;
                public void visit(Klass k) {
                    try {
                        Instance clazz = k.getJavaMirror();
                        writeHeader(HPROF_LOAD_CLASS, 2 * (OBJ_ID_SIZE + 4));
                        out.writeInt(serialNum);
                        writeObjectID(clazz);
                        out.writeInt(DUMMY_STACK_TRACE_ID);
                        writeSymbolID(k.getName());
                        serialNum++;
                    } catch (IOException exp) {
                        throw new RuntimeException(exp);
                    }
                }
            });
        } catch (RuntimeException re) {
            handleRuntimeException(re);
        }
    }
    private void writeFileHeader() throws IOException {
        out.writeBytes(HPROF_HEADER);
        out.writeByte((byte)'\0');
        out.writeInt(OBJ_ID_SIZE);
        out.writeLong(System.currentTimeMillis());
    }
    private void writeObjectID(Oop oop) throws IOException {
        OopHandle handle = (oop != null)? oop.getHandle() : null;
        long address = getAddressValue(handle);
        writeObjectID(address);
    }
    private void writeSymbolID(Symbol sym) throws IOException {
        writeObjectID(getAddressValue(sym.getAddress()));
    }
    private void writeObjectID(long address) throws IOException {
        if (OBJ_ID_SIZE == 4) {
            out.writeInt((int) address);
        } else {
            out.writeLong(address);
        }
    }
    private long getAddressValue(Address addr) {
        return (addr == null)? 0L : dbg.getAddressValue(addr);
    }
    private static List getInstanceFields(InstanceKlass ik) {
        InstanceKlass klass = ik;
        List res = new ArrayList();
        while (klass != null) {
            List curFields = klass.getImmediateFields();
            for (Iterator itr = curFields.iterator(); itr.hasNext();) {
                Field f = (Field) itr.next();
                if (! f.isStatic()) {
                    res.add(f);
                }
            }
            klass = (InstanceKlass) klass.getSuper();
        }
        return res;
    }
    private int getSizeForFields(List fields) {
        int size = 0;
        for (Iterator itr = fields.iterator(); itr.hasNext();) {
            Field field = (Field) itr.next();
            char typeCode = (char) field.getSignature().getByteAt(0);
            switch (typeCode) {
            case JVM_SIGNATURE_BOOLEAN:
            case JVM_SIGNATURE_BYTE:
                size++;
                break;
            case JVM_SIGNATURE_CHAR:
            case JVM_SIGNATURE_SHORT:
                size += 2;
                break;
            case JVM_SIGNATURE_INT:
            case JVM_SIGNATURE_FLOAT:
                size += 4;
                break;
            case JVM_SIGNATURE_CLASS:
            case JVM_SIGNATURE_ARRAY:
                size += OBJ_ID_SIZE;
                break;
            case JVM_SIGNATURE_LONG:
            case JVM_SIGNATURE_DOUBLE:
                size += 8;
                break;
            default:
                throw new RuntimeException("should not reach here");
            }
        }
        return size;
    }
    private static final int DUMMY_STACK_TRACE_ID = 1;
    private static final int EMPTY_FRAME_DEPTH = -1;
    private DataOutputStream out;
    private Debugger dbg;
    private ObjectHeap objectHeap;
    private SymbolTable symTbl;
    private int OBJ_ID_SIZE;
    private long BOOLEAN_BASE_OFFSET;
    private long BYTE_BASE_OFFSET;
    private long CHAR_BASE_OFFSET;
    private long SHORT_BASE_OFFSET;
    private long INT_BASE_OFFSET;
    private long LONG_BASE_OFFSET;
    private long FLOAT_BASE_OFFSET;
    private long DOUBLE_BASE_OFFSET;
    private long OBJECT_BASE_OFFSET;
    private long BOOLEAN_SIZE;
    private long BYTE_SIZE;
    private long CHAR_SIZE;
    private long SHORT_SIZE;
    private long INT_SIZE;
    private long LONG_SIZE;
    private long FLOAT_SIZE;
    private long DOUBLE_SIZE;
    private static class ClassData {
        int instSize;
        List fields;
        ClassData(int instSize, List fields) {
            this.instSize = instSize;
            this.fields = fields;
        }
    }
    private Map classDataCache = new HashMap(); 
}

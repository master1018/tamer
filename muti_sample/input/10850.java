public abstract class AbstractHeapGraphWriter implements HeapGraphWriter {
    protected void write() throws IOException {
        SymbolTable symTbl = VM.getVM().getSymbolTable();
        javaLangClass = symTbl.probe("java/lang/Class");
        javaLangString = symTbl.probe("java/lang/String");
        javaLangThread = symTbl.probe("java/lang/Thread");
        ObjectHeap heap = VM.getVM().getObjectHeap();
        try {
            heap.iterate(new DefaultHeapVisitor() {
                    public void prologue(long usedSize) {
                        try {
                            writeHeapHeader();
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public boolean doObj(Oop oop) {
                        try {
                            if (oop instanceof TypeArray) {
                                writePrimitiveArray((TypeArray)oop);
                            } else if (oop instanceof ObjArray) {
                                Klass klass = oop.getKlass();
                                ObjArrayKlass oak = (ObjArrayKlass) klass;
                                Klass bottomType = oak.getBottomKlass();
                                if (bottomType instanceof InstanceKlass ||
                                    bottomType instanceof TypeArrayKlass) {
                                    writeObjectArray((ObjArray)oop);
                                } else {
                                    writeInternalObject(oop);
                                }
                            } else if (oop instanceof Instance) {
                                Instance instance = (Instance) oop;
                                Klass klass = instance.getKlass();
                                Symbol name = klass.getName();
                                if (name.equals(javaLangString)) {
                                    writeString(instance);
                                } else if (name.equals(javaLangClass)) {
                                    writeClass(instance);
                                } else if (name.equals(javaLangThread)) {
                                    writeThread(instance);
                                } else {
                                    klass = klass.getSuper();
                                    while (klass != null) {
                                        name = klass.getName();
                                        if (name.equals(javaLangThread)) {
                                            writeThread(instance);
                                            return false;
                                        }
                                        klass = klass.getSuper();
                                    }
                                    writeInstance(instance);
                                }
                            } else {
                                writeInternalObject(oop);
                            }
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                        return false;
                    }
                    public void epilogue() {
                        try {
                            writeHeapFooter();
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                });
                writeJavaThreads();
                writeGlobalJNIHandles();
        } catch (RuntimeException re) {
            handleRuntimeException(re);
        }
    }
    protected void writeJavaThreads() throws IOException {
        Threads threads = VM.getVM().getThreads();
        JavaThread jt = threads.first();
        int index = 1;
        while (jt != null) {
            if (jt.getThreadObj() != null) {
                writeJavaThread(jt, index);
                index++;
            }
            jt = jt.next();
        }
    }
    protected void writeJavaThread(JavaThread jt, int index)
                            throws IOException {
    }
    protected void writeGlobalJNIHandles() throws IOException {
        JNIHandles handles = VM.getVM().getJNIHandles();
        JNIHandleBlock blk = handles.globalHandles();
        if (blk != null) {
            try {
                blk.oopsDo(new AddressVisitor() {
                          public void visitAddress(Address handleAddr) {
                              try {
                                  if (handleAddr != null) {
                                      writeGlobalJNIHandle(handleAddr);
                                  }
                              } catch (IOException exp) {
                                  throw new RuntimeException(exp);
                              }
                          }
                              public void visitCompOopAddress(Address handleAddr) {
                             throw new RuntimeException("Should not reach here. JNIHandles are not compressed");
                          }
                       });
            } catch (RuntimeException re) {
                handleRuntimeException(re);
            }
        }
    }
    protected void writeGlobalJNIHandle(Address handleAddr) throws IOException {
    }
    protected void writeHeapHeader() throws IOException {
    }
    protected void writeInternalObject(Oop oop) throws IOException {
    }
    protected void writePrimitiveArray(TypeArray array) throws IOException {
        writeObject(array);
    }
    protected void writeObjectArray(ObjArray array) throws IOException {
        writeObject(array);
    }
    protected void writeInstance(Instance instance) throws IOException {
        writeObject(instance);
    }
    protected void writeString(Instance instance) throws IOException {
        writeInstance(instance);
    }
    protected void writeClass(Instance instance) throws IOException {
        writeInstance(instance);
    }
    protected void writeThread(Instance instance) throws IOException {
        writeInstance(instance);
    }
    protected void writeObject(Oop oop) throws IOException {
        writeObjectHeader(oop);
        writeObjectFields(oop);
        writeObjectFooter(oop);
    }
    protected void writeObjectHeader(Oop oop) throws IOException {
    }
    protected void writeObjectFields(final Oop oop) throws IOException {
        try {
            oop.iterate(new DefaultOopVisitor() {
                    public void doOop(OopField field, boolean isVMField) {
                        try {
                            Oop ref = field.getValue(oop);
                            if (ref instanceof TypeArray ||
                                ref instanceof ObjArray  ||
                                ref instanceof Instance) {
                                writeReferenceField(oop, field);
                            } else {
                                writeInternalReferenceField(oop, field);
                            }
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doByte(ByteField field, boolean isVMField) {
                        try {
                            writeByteField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doChar(CharField field, boolean isVMField) {
                        try {
                            writeCharField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doBoolean(BooleanField field, boolean vField) {
                        try {
                            writeBooleanField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doShort(ShortField field, boolean isVMField) {
                        try {
                            writeShortField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doInt(IntField field, boolean isVMField) {
                        try {
                            writeIntField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doLong(LongField field, boolean isVMField) {
                        try {
                            writeLongField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doFloat(FloatField field, boolean isVMField) {
                        try {
                            writeFloatField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                    public void doDouble(DoubleField field, boolean vField) {
                        try {
                            writeDoubleField(oop, field);
                        } catch (IOException exp) {
                            throw new RuntimeException(exp);
                        }
                    }
                }, false);
        } catch (RuntimeException re) {
            handleRuntimeException(re);
        }
    }
    protected void writeInternalReferenceField(Oop oop, OopField field)
        throws IOException {
    }
    protected void writeReferenceField(Oop oop, OopField field)
        throws IOException {
    }
    protected void writeByteField(Oop oop, ByteField field)
        throws IOException {
    }
    protected void writeCharField(Oop oop, CharField field)
        throws IOException {
    }
    protected void writeBooleanField(Oop oop, BooleanField field)
        throws IOException {
    }
    protected void writeShortField(Oop oop, ShortField field)
        throws IOException {
    }
    protected void writeIntField(Oop oop, IntField field)
        throws IOException {
    }
    protected void writeLongField(Oop oop, LongField field)
        throws IOException {
    }
    protected void writeFloatField(Oop oop, FloatField field)
        throws IOException {
    }
    protected void writeDoubleField(Oop oop, DoubleField field)
        throws IOException {
    }
    protected void writeObjectFooter(Oop oop) throws IOException {
    }
    protected void writeHeapFooter() throws IOException {
    }
    protected void handleRuntimeException(RuntimeException re)
        throws IOException {
        Throwable cause = re.getCause();
        if (cause != null && cause instanceof IOException) {
            throw (IOException) cause;
        } else {
            throw re;
        }
    }
    protected boolean isJavaVisible(Oop oop) {
        if (oop instanceof Instance || oop instanceof TypeArray) {
            return true;
        } else if (oop instanceof ObjArray) {
            ObjArrayKlass oak = (ObjArrayKlass) oop.getKlass();
            Klass bottomKlass = oak.getBottomKlass();
            return bottomKlass instanceof InstanceKlass ||
                   bottomKlass instanceof TypeArrayKlass;
        } else {
            return false;
        }
    }
    protected Symbol javaLangClass;
    protected Symbol javaLangString;
    protected Symbol javaLangThread;
}

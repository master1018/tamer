public class ClassWriter implements  ClassConstants
{
    public static final boolean DEBUG = false;
    protected void debugMessage(String message) {
        System.out.println(message);
    }
    protected InstanceKlass     klass;
    protected DataOutputStream  dos;
    protected ConstantPool      cpool;
    protected Map               classToIndex = new HashMap();
    protected Map               utf8ToIndex = new HashMap();
    protected short  _sourceFileIndex;
    protected short  _innerClassesIndex;
    protected short  _syntheticIndex;
    protected short  _deprecatedIndex;
    protected short  _constantValueIndex;
    protected short  _codeIndex;
    protected short  _exceptionsIndex;
    protected short  _lineNumberTableIndex;
    protected short  _localVariableTableIndex;
    protected short  _signatureIndex;
    protected static int extractHighShortFromInt(int val) {
        return (val >> 16) & 0xFFFF;
    }
    protected static int extractLowShortFromInt(int val) {
        return val & 0xFFFF;
    }
    public ClassWriter(InstanceKlass kls, OutputStream os) {
        klass = kls;
        dos = new DataOutputStream(os);
        cpool = klass.getConstants();
    }
    public void write() throws IOException {
        if (DEBUG) debugMessage("class name = " + klass.getName().asString());
        dos.writeInt(0xCAFEBABE);
        writeVersion();
        writeConstantPool();
        writeClassAccessFlags();
        writeThisClass();
        writeSuperClass();
        writeInterfaces();
        writeFields();
        writeMethods();
        writeClassAttributes();
        dos.flush();
    }
    protected void writeVersion() throws IOException {
        dos.writeShort((short)klass.minorVersion());
        dos.writeShort((short)klass.majorVersion());
    }
    protected void writeIndex(int index) throws IOException {
        if (index == 0) throw new InternalError();
        dos.writeShort(index);
    }
    protected void writeConstantPool() throws IOException {
        final TypeArray tags = cpool.getTags();
        final long len = tags.getLength();
        dos.writeShort((short) len);
        if (DEBUG) debugMessage("constant pool length = " + len);
        int ci = 0; 
        for (ci = 1; ci < len; ci++) {
            byte cpConstType = tags.getByteAt(ci);
            if(cpConstType == JVM_CONSTANT_Utf8) {
                Symbol sym = cpool.getSymbolAt(ci);
                utf8ToIndex.put(sym.asString(), new Short((short) ci));
            }
            else if(cpConstType == JVM_CONSTANT_Long ||
                      cpConstType == JVM_CONSTANT_Double) {
                ci++;
            }
        }
        Short sourceFileIndex = (Short) utf8ToIndex.get("SourceFile");
        _sourceFileIndex = (sourceFileIndex != null)? sourceFileIndex.shortValue() : 0;
        if (DEBUG) debugMessage("SourceFile index = " + _sourceFileIndex);
        Short innerClassesIndex = (Short) utf8ToIndex.get("InnerClasses");
        _innerClassesIndex = (innerClassesIndex != null)? innerClassesIndex.shortValue() : 0;
        if (DEBUG) debugMessage("InnerClasses index = " + _innerClassesIndex);
        Short constantValueIndex = (Short) utf8ToIndex.get("ConstantValue");
        _constantValueIndex = (constantValueIndex != null)?
                                          constantValueIndex.shortValue() : 0;
        if (DEBUG) debugMessage("ConstantValue index = " + _constantValueIndex);
        Short syntheticIndex = (Short) utf8ToIndex.get("Synthetic");
        _syntheticIndex = (syntheticIndex != null)? syntheticIndex.shortValue() : 0;
        if (DEBUG) debugMessage("Synthetic index = " + _syntheticIndex);
        Short deprecatedIndex = (Short) utf8ToIndex.get("Deprecated");
        _deprecatedIndex = (deprecatedIndex != null)? deprecatedIndex.shortValue() : 0;
        if (DEBUG) debugMessage("Deprecated index = " + _deprecatedIndex);
        Short codeIndex = (Short) utf8ToIndex.get("Code");
        _codeIndex = (codeIndex != null)? codeIndex.shortValue() : 0;
        if (DEBUG) debugMessage("Code index = " + _codeIndex);
        Short exceptionsIndex = (Short) utf8ToIndex.get("Exceptions");
        _exceptionsIndex = (exceptionsIndex != null)? exceptionsIndex.shortValue() : 0;
        if (DEBUG) debugMessage("Exceptions index = " + _exceptionsIndex);
        Short lineNumberTableIndex = (Short) utf8ToIndex.get("LineNumberTable");
        _lineNumberTableIndex = (lineNumberTableIndex != null)?
                                       lineNumberTableIndex.shortValue() : 0;
        if (DEBUG) debugMessage("LineNumberTable index = " + _lineNumberTableIndex);
        Short localVariableTableIndex = (Short) utf8ToIndex.get("LocalVariableTable");
        _localVariableTableIndex = (localVariableTableIndex != null)?
                                       localVariableTableIndex.shortValue() : 0;
        if (DEBUG) debugMessage("LocalVariableTable index = " + _localVariableTableIndex);
        Short signatureIdx = (Short) utf8ToIndex.get("Signature");
        _signatureIndex = (signatureIdx != null)? signatureIdx.shortValue() : 0;
        if (DEBUG) debugMessage("Signature index = " + _signatureIndex);
        for(ci = 1; ci < len; ci++) {
            byte cpConstType = tags.getByteAt(ci);
            switch(cpConstType) {
                case JVM_CONSTANT_Utf8: {
                     dos.writeByte(cpConstType);
                     Symbol sym = cpool.getSymbolAt(ci);
                     dos.writeShort((short)sym.getLength());
                     dos.write(sym.asByteArray());
                     if (DEBUG) debugMessage("CP[" + ci + "] = modified UTF-8 " + sym.asString());
                     break;
                }
                case JVM_CONSTANT_Unicode:
                     throw new IllegalArgumentException("Unicode constant!");
                case JVM_CONSTANT_Integer:
                     dos.writeByte(cpConstType);
                     dos.writeInt(cpool.getIntAt(ci));
                     if (DEBUG) debugMessage("CP[" + ci + "] = int " + cpool.getIntAt(ci));
                     break;
                case JVM_CONSTANT_Float:
                     dos.writeByte(cpConstType);
                     dos.writeFloat(cpool.getFloatAt(ci));
                     if (DEBUG) debugMessage("CP[" + ci + "] = float " + cpool.getFloatAt(ci));
                     break;
                case JVM_CONSTANT_Long: {
                     dos.writeByte(cpConstType);
                     long l = cpool.getLongAt(ci);
                     ci++;
                     dos.writeLong(l);
                     break;
                }
                case JVM_CONSTANT_Double:
                     dos.writeByte(cpConstType);
                     dos.writeDouble(cpool.getDoubleAt(ci));
                     ci++;
                     break;
                case JVM_CONSTANT_Class: {
                     dos.writeByte(cpConstType);
                     Klass refKls = (Klass) cpool.getObjAtRaw(ci);
                     String klassName = refKls.getName().asString();
                     Short s = (Short) utf8ToIndex.get(klassName);
                     classToIndex.put(klassName, new Short((short)ci));
                     dos.writeShort(s.shortValue());
                     if (DEBUG) debugMessage("CP[" + ci  + "] = class " + s);
                     break;
                }
                case JVM_CONSTANT_UnresolvedClassInError:
                case JVM_CONSTANT_UnresolvedClass: {
                     dos.writeByte(JVM_CONSTANT_Class);
                     String klassName = cpool.getSymbolAt(ci).asString();
                     Short s = (Short) utf8ToIndex.get(klassName);
                     classToIndex.put(klassName, new Short((short) ci));
                     dos.writeShort(s.shortValue());
                     if (DEBUG) debugMessage("CP[" + ci + "] = class " + s);
                     break;
                }
                case JVM_CONSTANT_String: {
                     dos.writeByte(cpConstType);
                     String str = OopUtilities.stringOopToString(cpool.getObjAtRaw(ci));
                     Short s = (Short) utf8ToIndex.get(str);
                     dos.writeShort(s.shortValue());
                     if (DEBUG) debugMessage("CP[" + ci + "] = string " + s);
                     break;
                }
                case JVM_CONSTANT_UnresolvedString: {
                     dos.writeByte(JVM_CONSTANT_String);
                     String val = cpool.getSymbolAt(ci).asString();
                     Short s = (Short) utf8ToIndex.get(val);
                     dos.writeShort(s.shortValue());
                     if (DEBUG) debugMessage("CP[" + ci + "] = string " + s);
                     break;
                }
                case JVM_CONSTANT_Fieldref:
                case JVM_CONSTANT_Methodref:
                case JVM_CONSTANT_InterfaceMethodref: {
                     dos.writeByte(cpConstType);
                     int value = cpool.getIntAt(ci);
                     short klassIndex = (short) extractLowShortFromInt(value);
                     short nameAndTypeIndex = (short) extractHighShortFromInt(value);
                     dos.writeShort(klassIndex);
                     dos.writeShort(nameAndTypeIndex);
                     if (DEBUG) debugMessage("CP[" + ci + "] = ref klass = " +
                           klassIndex + ", N&T = " + nameAndTypeIndex);
                     break;
                }
                case JVM_CONSTANT_NameAndType: {
                     dos.writeByte(cpConstType);
                     int value = cpool.getIntAt(ci);
                     short nameIndex = (short) extractLowShortFromInt(value);
                     short signatureIndex = (short) extractHighShortFromInt(value);
                     dos.writeShort(nameIndex);
                     dos.writeShort(signatureIndex);
                     if (DEBUG) debugMessage("CP[" + ci + "] = N&T name = " + nameIndex
                                        + ", type = " + signatureIndex);
                     break;
                }
                case JVM_CONSTANT_MethodHandle: {
                     dos.writeByte(cpConstType);
                     int value = cpool.getIntAt(ci);
                     byte refKind = (byte) extractLowShortFromInt(value);
                     short memberIndex = (short) extractHighShortFromInt(value);
                     dos.writeByte(refKind);
                     dos.writeShort(memberIndex);
                     if (DEBUG) debugMessage("CP[" + ci + "] = MH kind = " +
                           refKind + ", mem = " + memberIndex);
                     break;
                }
                case JVM_CONSTANT_MethodType: {
                     dos.writeByte(cpConstType);
                     int value = cpool.getIntAt(ci);
                     short refIndex = (short) value;
                     dos.writeShort(refIndex);
                     if (DEBUG) debugMessage("CP[" + ci + "] = MT index = " + refIndex);
                     break;
                }
                case JVM_CONSTANT_InvokeDynamic: {
                     dos.writeByte(cpConstType);
                     int value = cpool.getIntAt(ci);
                     short bsmIndex = (short) extractLowShortFromInt(value);
                     short nameAndTypeIndex = (short) extractHighShortFromInt(value);
                     dos.writeShort(bsmIndex);
                     dos.writeShort(nameAndTypeIndex);
                     if (DEBUG) debugMessage("CP[" + ci + "] = INDY bsm = " +
                           bsmIndex + ", N&T = " + nameAndTypeIndex);
                     break;
                }
                default:
                  throw new InternalError("Unknown tag: " + cpConstType);
            } 
        }
    }
    protected void writeClassAccessFlags() throws IOException {
        int flags = (int)(klass.getAccessFlags() & JVM_RECOGNIZED_CLASS_MODIFIERS);
        dos.writeShort((short)flags);
    }
    protected void writeThisClass() throws IOException {
        String klassName = klass.getName().asString();
        Short index = (Short) classToIndex.get(klassName);
        dos.writeShort(index.shortValue());
        if (DEBUG) debugMessage("this class = " + index);
    }
    protected void writeSuperClass() throws IOException {
        Klass superKlass = klass.getSuper();
        if (superKlass != null) { 
            String superName = superKlass.getName().asString();
            Short index = (Short) classToIndex.get(superName);
            if (DEBUG) debugMessage("super class = " + index);
            dos.writeShort(index.shortValue());
        } else {
            dos.writeShort(0); 
        }
    }
    protected void writeInterfaces() throws IOException {
        ObjArray interfaces = klass.getLocalInterfaces();
        final int len = (int) interfaces.getLength();
        if (DEBUG) debugMessage("number of interfaces = " + len);
        dos.writeShort((short) len);
        for (int i = 0; i < len; i++) {
           Klass k = (Klass) interfaces.getObjAt(i);
           Short index = (Short) classToIndex.get(k.getName().asString());
           dos.writeShort(index.shortValue());
           if (DEBUG) debugMessage("\t" + index);
        }
    }
    protected void writeFields() throws IOException {
        TypeArray fields = klass.getFields();
        final int length = (int) fields.getLength();
        dos.writeShort((short) (length / InstanceKlass.NEXT_OFFSET) );
        if (DEBUG) debugMessage("number of fields = "
                                + length/InstanceKlass.NEXT_OFFSET);
        for (int index = 0; index < length; index += InstanceKlass.NEXT_OFFSET) {
            short accessFlags    = fields.getShortAt(index + InstanceKlass.ACCESS_FLAGS_OFFSET);
            dos.writeShort(accessFlags & (short) JVM_RECOGNIZED_FIELD_MODIFIERS);
            short nameIndex    = fields.getShortAt(index + InstanceKlass.NAME_INDEX_OFFSET);
            dos.writeShort(nameIndex);
            short signatureIndex = fields.getShortAt(index + InstanceKlass.SIGNATURE_INDEX_OFFSET);
            dos.writeShort(signatureIndex);
            if (DEBUG) debugMessage("\tfield name = " + nameIndex + ", signature = " + signatureIndex);
            short fieldAttributeCount = 0;
            boolean hasSyn = hasSyntheticAttribute(accessFlags);
            if (hasSyn)
                fieldAttributeCount++;
            short initvalIndex = fields.getShortAt(index + InstanceKlass.INITVAL_INDEX_OFFSET);
            if (initvalIndex != 0)
                fieldAttributeCount++;
            short genSigIndex = fields.getShortAt(index + InstanceKlass.GENERIC_SIGNATURE_INDEX_OFFSET);
            if (genSigIndex != 0)
                fieldAttributeCount++;
            dos.writeShort(fieldAttributeCount);
            if (hasSyn)
                writeSynthetic();
            if (initvalIndex != 0) {
                writeIndex(_constantValueIndex);
                dos.writeInt(2);
                dos.writeShort(initvalIndex);
                if (DEBUG) debugMessage("\tfield init value = " + initvalIndex);
            }
            if (genSigIndex != 0) {
                writeIndex(_signatureIndex);
                dos.writeInt(2);
                dos.writeShort(genSigIndex);
                if (DEBUG) debugMessage("\tfield generic signature index " + genSigIndex);
            }
        }
    }
    protected boolean isSynthetic(short accessFlags) {
        return (accessFlags & (short) JVM_ACC_SYNTHETIC) != 0;
    }
    protected boolean hasSyntheticAttribute(short accessFlags) {
        return isSynthetic(accessFlags) && _syntheticIndex != 0;
    }
    protected void writeSynthetic() throws IOException {
        writeIndex(_syntheticIndex);
        dos.writeInt(0);
    }
    protected void writeMethods() throws IOException {
        ObjArray methods = klass.getMethods();
        final int len = (int) methods.getLength();
        dos.writeShort((short) len);
        if (DEBUG) debugMessage("number of methods = " + len);
        for (int m = 0; m < len; m++) {
            writeMethod((Method) methods.getObjAt(m));
        }
    }
    protected void writeMethod(Method m) throws IOException {
        long accessFlags = m.getAccessFlags();
        dos.writeShort((short) (accessFlags & JVM_RECOGNIZED_METHOD_MODIFIERS));
        dos.writeShort((short) m.getNameIndex());
        dos.writeShort((short) m.getSignatureIndex());
        if (DEBUG) debugMessage("\tmethod name = " + m.getNameIndex() + ", signature = "
                        + m.getSignatureIndex());
        final boolean isNative = ((accessFlags & JVM_ACC_NATIVE) != 0);
        final boolean isAbstract = ((accessFlags & JVM_ACC_ABSTRACT) != 0);
        short methodAttributeCount = 0;
        final boolean hasSyn = hasSyntheticAttribute((short)accessFlags);
        if (hasSyn)
            methodAttributeCount++;
        final boolean hasCheckedExceptions = m.hasCheckedExceptions();
        if (hasCheckedExceptions)
            methodAttributeCount++;
        final boolean isCodeAvailable = (!isNative) && (!isAbstract);
        if (isCodeAvailable)
            methodAttributeCount++;
        final boolean isGeneric = (m.getGenericSignature() != null);
        if (isGeneric)
            methodAttributeCount++;
        dos.writeShort(methodAttributeCount);
        if (DEBUG) debugMessage("\tmethod attribute count = " + methodAttributeCount);
        if (hasSyn) {
            if (DEBUG) debugMessage("\tmethod is synthetic");
            writeSynthetic();
        }
        if (isCodeAvailable) {
            byte[] code = m.getByteCode();
            short codeAttrCount = 0;
            int codeSize  = 2            +
                            2            +
                            4            +
                            code.length  +
                            2            +
                            2           ;
            TypeArray exceptionTable = m.getExceptionTable();
            final int exceptionTableLen = (int) exceptionTable.getLength();
            if (exceptionTableLen != 0) {
                if (DEBUG) debugMessage("\tmethod has exception table");
                codeSize += (exceptionTableLen / 4) 
                                         * (2  +
                                            2  +
                                            2  +
                                            2 );
            }
            boolean hasLineNumberTable = m.hasLineNumberTable();
            LineNumberTableElement[] lineNumberTable = null;
            int lineNumberAttrLen = 0;
            if (hasLineNumberTable) {
                if (DEBUG) debugMessage("\tmethod has line number table");
                lineNumberTable = m.getLineNumberTable();
                if (DEBUG) debugMessage("\t\tline table length = " + lineNumberTable.length);
                lineNumberAttrLen = 2  +
                           lineNumberTable.length * (2  + 2 );
                codeSize += 2  +
                            4  +
                            lineNumberAttrLen;
                if (DEBUG) debugMessage("\t\tline number table attr size = " +
                                              lineNumberAttrLen);
                codeAttrCount++;
            }
            boolean hasLocalVariableTable = m.hasLocalVariableTable();
            LocalVariableTableElement[] localVariableTable = null;
            int localVarAttrLen = 0;
            if (hasLocalVariableTable) {
                if (DEBUG) debugMessage("\tmethod has local variable table");
                localVariableTable = m.getLocalVariableTable();
                if (DEBUG) debugMessage("\t\tlocal variable table length = "
                              + localVariableTable.length);
                localVarAttrLen =
                               2  +
                               localVariableTable.length * ( 2  +
                                                          2  +
                                                          2  +
                                                          2  +
                                                          2  );
                if (DEBUG) debugMessage("\t\tlocal variable attr size = " +
                                              localVarAttrLen);
                codeSize += 2  +
                            4  +
                            localVarAttrLen;
                codeAttrCount++;
            }
            rewriteByteCode(m, code);
            writeIndex(_codeIndex);
            dos.writeInt(codeSize);
            if (DEBUG) debugMessage("\tcode attribute length = " + codeSize);
            dos.writeShort((short) m.getMaxStack());
            if (DEBUG) debugMessage("\tmax stack = " + m.getMaxStack());
            dos.writeShort((short) m.getMaxLocals());
            if (DEBUG) debugMessage("\tmax locals = " + m.getMaxLocals());
            dos.writeInt(code.length);
            if (DEBUG) debugMessage("\tcode size = " + code.length);
            dos.write(code);
            dos.writeShort((short) (exceptionTableLen / 4));
            if (DEBUG) debugMessage("\texception table length = " + (exceptionTableLen / 4));
            if (exceptionTableLen != 0) {
                for (int e = 0; e < exceptionTableLen; e += 4) {
                     dos.writeShort((short) exceptionTable.getIntAt(e));
                     dos.writeShort((short) exceptionTable.getIntAt(e + 1));
                     dos.writeShort((short) exceptionTable.getIntAt(e + 2));
                     dos.writeShort((short) exceptionTable.getIntAt(e + 3));
                }
            }
            dos.writeShort((short)codeAttrCount);
            if (DEBUG) debugMessage("\tcode attribute count = " + codeAttrCount);
            if (hasLineNumberTable) {
                writeIndex(_lineNumberTableIndex);
                dos.writeInt(lineNumberAttrLen);
                dos.writeShort((short) lineNumberTable.length);
                for (int l = 0; l < lineNumberTable.length; l++) {
                     dos.writeShort((short) lineNumberTable[l].getStartBCI());
                     dos.writeShort((short) lineNumberTable[l].getLineNumber());
                }
            }
            if (hasLocalVariableTable) {
                writeIndex((short) _localVariableTableIndex);
                dos.writeInt(localVarAttrLen);
                dos.writeShort((short) localVariableTable.length);
                for (int l = 0; l < localVariableTable.length; l++) {
                     dos.writeShort((short) localVariableTable[l].getStartBCI());
                     dos.writeShort((short) localVariableTable[l].getLength());
                     dos.writeShort((short) localVariableTable[l].getNameCPIndex());
                     dos.writeShort((short) localVariableTable[l].getDescriptorCPIndex());
                     dos.writeShort((short) localVariableTable[l].getSlot());
                }
            }
        }
        if (hasCheckedExceptions) {
            CheckedExceptionElement[] exceptions = m.getCheckedExceptions();
            writeIndex(_exceptionsIndex);
            int attrSize = 2  +
                           exceptions.length * 2 ;
            dos.writeInt(attrSize);
            dos.writeShort(exceptions.length);
            if (DEBUG) debugMessage("\tmethod has " + exceptions.length
                                        +  " checked exception(s)");
            for (int e = 0; e < exceptions.length; e++) {
                 short cpIndex = (short) exceptions[e].getClassCPIndex();
                 dos.writeShort(cpIndex);
            }
        }
        if (isGeneric) {
           writeGenericSignature(m.getGenericSignature().asString());
        }
    }
    protected void rewriteByteCode(Method m, byte[] code) {
        ByteCodeRewriter r = new ByteCodeRewriter(m, cpool, code);
        r.rewrite();
    }
    protected void writeGenericSignature(String signature) throws IOException {
        writeIndex(_signatureIndex);
        if (DEBUG) debugMessage("signature attribute = " + _signatureIndex);
        dos.writeInt(2);
        Short index = (Short) utf8ToIndex.get(signature);
        dos.writeShort(index.shortValue());
        if (DEBUG) debugMessage("generic signature = " + index);
    }
    protected void writeClassAttributes() throws IOException {
        final long flags = klass.getAccessFlags();
        final boolean hasSyn = hasSyntheticAttribute((short) flags);
        short classAttributeCount = 0;
        if (hasSyn)
            classAttributeCount++;
        Symbol sourceFileName = klass.getSourceFileName();
        if (sourceFileName != null)
            classAttributeCount++;
        Symbol genericSignature = klass.getGenericSignature();
        if (genericSignature != null)
            classAttributeCount++;
        TypeArray innerClasses = klass.getInnerClasses();
        final int numInnerClasses = (int) (innerClasses.getLength() / 4);
        if (numInnerClasses != 0)
            classAttributeCount++;
        dos.writeShort(classAttributeCount);
        if (DEBUG) debugMessage("class attribute count = " + classAttributeCount);
        if (hasSyn)
            writeSynthetic();
        if (sourceFileName != null) {
            writeIndex(_sourceFileIndex);
            if (DEBUG) debugMessage("source file attribute = " + _sourceFileIndex);
            dos.writeInt(2);
            Short index = (Short) utf8ToIndex.get(sourceFileName.asString());
            dos.writeShort(index.shortValue());
            if (DEBUG) debugMessage("source file name = " + index);
        }
        if (genericSignature != null) {
            writeGenericSignature(genericSignature.asString());
        }
        if (numInnerClasses != 0) {
            writeIndex(_innerClassesIndex);
            final int innerAttrLen = 2  +
                                     numInnerClasses * (
                                                 2  +
                                                 2  +
                                                 2  +
                                                 2 );
            dos.writeInt(innerAttrLen);
            dos.writeShort(numInnerClasses);
            if (DEBUG) debugMessage("class has " + numInnerClasses + " inner class entries");
            for (int index = 0; index < numInnerClasses * 4; index++) {
                dos.writeShort(innerClasses.getShortAt(index));
            }
        }
    }
}

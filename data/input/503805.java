public class StdAttributeFactory
    extends AttributeFactory {
    public static final StdAttributeFactory THE_ONE =
        new StdAttributeFactory();
    public StdAttributeFactory() {
    }
    @Override
    protected Attribute parse0(DirectClassFile cf, int context, String name,
            int offset, int length, ParseObserver observer) {
        switch (context) {
            case CTX_CLASS: {
                if (name == AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == AttEnclosingMethod.ATTRIBUTE_NAME) {
                    return enclosingMethod(cf, offset, length, observer);
                }
                if (name == AttInnerClasses.ATTRIBUTE_NAME) {
                    return innerClasses(cf, offset, length, observer);
                }
                if (name == AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                if (name == AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == AttSourceFile.ATTRIBUTE_NAME) {
                    return sourceFile(cf, offset, length, observer);
                }
                break;
            }
            case CTX_FIELD: {
                if (name == AttConstantValue.ATTRIBUTE_NAME) {
                    return constantValue(cf, offset, length, observer);
                }
                if (name == AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                break;
            }
            case CTX_METHOD: {
                if (name == AttAnnotationDefault.ATTRIBUTE_NAME) {
                    return annotationDefault(cf, offset, length, observer);
                }
                if (name == AttCode.ATTRIBUTE_NAME) {
                    return code(cf, offset, length, observer);
                }
                if (name == AttDeprecated.ATTRIBUTE_NAME) {
                    return deprecated(cf, offset, length, observer);
                }
                if (name == AttExceptions.ATTRIBUTE_NAME) {
                    return exceptions(cf, offset, length, observer);
                }
                if (name == AttRuntimeInvisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeInvisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttRuntimeVisibleAnnotations.ATTRIBUTE_NAME) {
                    return runtimeVisibleAnnotations(cf, offset, length,
                            observer);
                }
                if (name == AttRuntimeInvisibleParameterAnnotations.
                        ATTRIBUTE_NAME) {
                    return runtimeInvisibleParameterAnnotations(
                            cf, offset, length, observer);
                }
                if (name == AttRuntimeVisibleParameterAnnotations.
                        ATTRIBUTE_NAME) {
                    return runtimeVisibleParameterAnnotations(
                            cf, offset, length, observer);
                }
                if (name == AttSignature.ATTRIBUTE_NAME) {
                    return signature(cf, offset, length, observer);
                }
                if (name == AttSynthetic.ATTRIBUTE_NAME) {
                    return synthetic(cf, offset, length, observer);
                }
                break;
            }
            case CTX_CODE: {
                if (name == AttLineNumberTable.ATTRIBUTE_NAME) {
                    return lineNumberTable(cf, offset, length, observer);
                }
                if (name == AttLocalVariableTable.ATTRIBUTE_NAME) {
                    return localVariableTable(cf, offset, length, observer);
                }
                if (name == AttLocalVariableTypeTable.ATTRIBUTE_NAME) {
                    return localVariableTypeTable(cf, offset, length,
                            observer);
                }
                break;
            }
        }
        return super.parse0(cf, context, name, offset, length, observer);
    }
    private Attribute annotationDefault(DirectClassFile cf,
            int offset, int length, ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }
        AnnotationParser ap =
            new AnnotationParser(cf, offset, length, observer);
        Constant cst = ap.parseValueAttribute();
        return new AttAnnotationDefault(cst, length);
    }
    private Attribute code(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length < 12) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int maxStack = bytes.getUnsignedShort(offset); 
        int maxLocals = bytes.getUnsignedShort(offset + 2); 
        int codeLength = bytes.getInt(offset + 4); 
        int origOffset = offset;
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "max_stack: " + Hex.u2(maxStack));
            observer.parsed(bytes, offset + 2, 2,
                            "max_locals: " + Hex.u2(maxLocals));
            observer.parsed(bytes, offset + 4, 4,
                            "code_length: " + Hex.u4(codeLength));
        }
        offset += 8;
        length -= 8;
        if (length < (codeLength + 4)) {
            return throwTruncated();
        }
        int codeOffset = offset;
        offset += codeLength;
        length -= codeLength;
        BytecodeArray code =
            new BytecodeArray(bytes.slice(codeOffset, codeOffset + codeLength),
                              pool);
        if (observer != null) {
            code.forEach(new CodeObserver(code.getBytes(), observer));
        }
        int exceptionTableLength = bytes.getUnsignedShort(offset);
        ByteCatchList catches = (exceptionTableLength == 0) ?
            ByteCatchList.EMPTY :
            new ByteCatchList(exceptionTableLength);
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "exception_table_length: " +
                            Hex.u2(exceptionTableLength));
        }
        offset += 2;
        length -= 2;
        if (length < (exceptionTableLength * 8 + 2)) {
            return throwTruncated();
        }
        for (int i = 0; i < exceptionTableLength; i++) {
            if (observer != null) {
                observer.changeIndent(1);
            }
            int startPc = bytes.getUnsignedShort(offset);
            int endPc = bytes.getUnsignedShort(offset + 2);
            int handlerPc = bytes.getUnsignedShort(offset + 4);
            int catchTypeIdx = bytes.getUnsignedShort(offset + 6);
            CstType catchType = (CstType) pool.get0Ok(catchTypeIdx);
            catches.set(i, startPc, endPc, handlerPc, catchType);
            if (observer != null) {
                observer.parsed(bytes, offset, 8,
                                Hex.u2(startPc) + ".." + Hex.u2(endPc) +
                                " -> " + Hex.u2(handlerPc) + " " +
                                ((catchType == null) ? "<any>" :
                                 catchType.toHuman()));
            }
            offset += 8;
            length -= 8;
            if (observer != null) {
                observer.changeIndent(-1);
            }
        }
        catches.setImmutable();
        AttributeListParser parser =
            new AttributeListParser(cf, CTX_CODE, offset, this);
        parser.setObserver(observer);
        StdAttributeList attributes = parser.getList();
        attributes.setImmutable();
        int attributeByteCount = parser.getEndOffset() - offset;
        if (attributeByteCount != length) {
            return throwBadLength(attributeByteCount + (offset - origOffset));
        }
        return new AttCode(maxStack, maxLocals, code, catches, attributes);
    }
    private Attribute constantValue(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length != 2) {
            return throwBadLength(2);
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        TypedConstant cst = (TypedConstant) pool.get(idx);
        Attribute result = new AttConstantValue(cst);
        if (observer != null) {
            observer.parsed(bytes, offset, 2, "value: " + cst);
        }
        return result;
    }
    private Attribute deprecated(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length != 0) {
            return throwBadLength(0);
        }
        return new AttDeprecated();
    }
    private Attribute enclosingMethod(DirectClassFile cf, int offset,
            int length, ParseObserver observer) {
        if (length != 4) {
            throwBadLength(4);
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        CstType type = (CstType) pool.get(idx);
        idx = bytes.getUnsignedShort(offset + 2);
        CstNat method = (CstNat) pool.get0Ok(idx);
        Attribute result = new AttEnclosingMethod(type, method);
        if (observer != null) {
            observer.parsed(bytes, offset, 2, "class: " + type);
            observer.parsed(bytes, offset + 2, 2, "method: " + 
                            DirectClassFile.stringOrNone(method));
        }
        return result;
    }
    private Attribute exceptions(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset); 
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "number_of_exceptions: " + Hex.u2(count));
        }
        offset += 2;
        length -= 2;
        if (length != (count * 2)) {
            throwBadLength((count * 2) + 2);
        }
        TypeList list = cf.makeTypeList(offset, count);
        return new AttExceptions(list);
    }
    private Attribute innerClasses(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int count = bytes.getUnsignedShort(offset); 
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "number_of_classes: " + Hex.u2(count));
        }
        offset += 2;
        length -= 2;
        if (length != (count * 8)) {
            throwBadLength((count * 8) + 2);
        }
        InnerClassList list = new InnerClassList(count);
        for (int i = 0; i < count; i++) {
            int innerClassIdx = bytes.getUnsignedShort(offset);
            int outerClassIdx = bytes.getUnsignedShort(offset + 2);
            int nameIdx = bytes.getUnsignedShort(offset + 4);
            int accessFlags = bytes.getUnsignedShort(offset + 6);
            CstType innerClass = (CstType) pool.get(innerClassIdx);
            CstType outerClass = (CstType) pool.get0Ok(outerClassIdx);
            CstUtf8 name = (CstUtf8) pool.get0Ok(nameIdx);
            list.set(i, innerClass, outerClass, name, accessFlags);
            if (observer != null) {
                observer.parsed(bytes, offset, 2,
                                "inner_class: " + 
                                DirectClassFile.stringOrNone(innerClass));
                observer.parsed(bytes, offset + 2, 2,
                                "  outer_class: " + 
                                DirectClassFile.stringOrNone(outerClass));
                observer.parsed(bytes, offset + 4, 2,
                                "  name: " + 
                                DirectClassFile.stringOrNone(name));
                observer.parsed(bytes, offset + 6, 2,
                                "  access_flags: " +
                                AccessFlags.innerClassString(accessFlags));
            }
            offset += 8;
        }
        list.setImmutable();
        return new AttInnerClasses(list);
    }
    private Attribute lineNumberTable(DirectClassFile cf, int offset,
            int length, ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset); 
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                            "line_number_table_length: " + Hex.u2(count));
        }
        offset += 2;
        length -= 2;
        if (length != (count * 4)) {
            throwBadLength((count * 4) + 2);
        }
        LineNumberList list = new LineNumberList(count);
        for (int i = 0; i < count; i++) {
            int startPc = bytes.getUnsignedShort(offset);
            int lineNumber = bytes.getUnsignedShort(offset + 2);
            list.set(i, startPc, lineNumber);
            if (observer != null) {
                observer.parsed(bytes, offset, 4,
                                Hex.u2(startPc) + " " + lineNumber);
            }
            offset += 4;
        }
        list.setImmutable();
        return new AttLineNumberTable(list);
    }
    private Attribute localVariableTable(DirectClassFile cf, int offset,
            int length, ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset);
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "local_variable_table_length: " + Hex.u2(count));
        }
        LocalVariableList list = parseLocalVariables(
                bytes.slice(offset + 2, offset + length), cf.getConstantPool(),
                observer, count, false);
        return new AttLocalVariableTable(list);
    }
    private Attribute localVariableTypeTable(DirectClassFile cf, int offset,
            int length, ParseObserver observer) {
        if (length < 2) {
            return throwSeverelyTruncated();
        }
        ByteArray bytes = cf.getBytes();
        int count = bytes.getUnsignedShort(offset);
        if (observer != null) {
            observer.parsed(bytes, offset, 2,
                    "local_variable_type_table_length: " + Hex.u2(count));
        }
        LocalVariableList list = parseLocalVariables(
                bytes.slice(offset + 2, offset + length), cf.getConstantPool(),
                observer, count, true);
        return new AttLocalVariableTypeTable(list);
    }
    private LocalVariableList parseLocalVariables(ByteArray bytes,
            ConstantPool pool, ParseObserver observer, int count,
            boolean typeTable) {
        if (bytes.size() != (count * 10)) {
            throwBadLength((count * 10) + 2);
        }
        ByteArray.MyDataInputStream in = bytes.makeDataInputStream();
        LocalVariableList list = new LocalVariableList(count);
        try {
            for (int i = 0; i < count; i++) {
                int startPc = in.readUnsignedShort();
                int length = in.readUnsignedShort();
                int nameIdx = in.readUnsignedShort();
                int typeIdx = in.readUnsignedShort();
                int index = in.readUnsignedShort();
                CstUtf8 name = (CstUtf8) pool.get(nameIdx);
                CstUtf8 type = (CstUtf8) pool.get(typeIdx);
                CstUtf8 descriptor = null;
                CstUtf8 signature = null;
                if (typeTable) {
                    signature = type;
                } else {
                    descriptor = type;
                }
                list.set(i, startPc, length, name,
                        descriptor, signature, index);
                if (observer != null) {
                    observer.parsed(bytes, i * 10, 10, Hex.u2(startPc) +
                            ".." + Hex.u2(startPc + length) + " " +
                            Hex.u2(index) + " " + name.toHuman() + " " +
                            type.toHuman());
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("shouldn't happen", ex);
        }
        list.setImmutable();
        return list;
    }
    private Attribute runtimeInvisibleAnnotations(DirectClassFile cf,
            int offset, int length, ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }
        AnnotationParser ap =
            new AnnotationParser(cf, offset, length, observer);
        Annotations annotations = 
            ap.parseAnnotationAttribute(AnnotationVisibility.BUILD);
        return new AttRuntimeInvisibleAnnotations(annotations, length);
    }
    private Attribute runtimeVisibleAnnotations(DirectClassFile cf,
            int offset, int length, ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }
        AnnotationParser ap =
            new AnnotationParser(cf, offset, length, observer);
        Annotations annotations = 
            ap.parseAnnotationAttribute(AnnotationVisibility.RUNTIME);
        return new AttRuntimeVisibleAnnotations(annotations, length);
    }
    private Attribute runtimeInvisibleParameterAnnotations(DirectClassFile cf,
            int offset, int length, ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }
        AnnotationParser ap =
            new AnnotationParser(cf, offset, length, observer);
        AnnotationsList list =
            ap.parseParameterAttribute(AnnotationVisibility.BUILD);
        return new AttRuntimeInvisibleParameterAnnotations(list, length);
    }
    private Attribute runtimeVisibleParameterAnnotations(DirectClassFile cf,
            int offset, int length, ParseObserver observer) {
        if (length < 2) {
            throwSeverelyTruncated();
        }
        AnnotationParser ap =
            new AnnotationParser(cf, offset, length, observer);
        AnnotationsList list =
            ap.parseParameterAttribute(AnnotationVisibility.RUNTIME);
        return new AttRuntimeVisibleParameterAnnotations(list, length);
    }
    private Attribute signature(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length != 2) {
            throwBadLength(2);
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        CstUtf8 cst = (CstUtf8) pool.get(idx);
        Attribute result = new AttSignature(cst);
        if (observer != null) {
            observer.parsed(bytes, offset, 2, "signature: " + cst);
        }
        return result;
    }
    private Attribute sourceFile(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length != 2) {
            throwBadLength(2);
        }
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        int idx = bytes.getUnsignedShort(offset);
        CstUtf8 cst = (CstUtf8) pool.get(idx);
        Attribute result = new AttSourceFile(cst);
        if (observer != null) {
            observer.parsed(bytes, offset, 2, "source: " + cst);
        }
        return result;
    }
    private Attribute synthetic(DirectClassFile cf, int offset, int length,
            ParseObserver observer) {
        if (length != 0) {
            return throwBadLength(0);
        }
        return new AttSynthetic();
    }
    private static Attribute throwSeverelyTruncated() {
        throw new ParseException("severely truncated attribute");
    }
    private static Attribute throwTruncated() {
        throw new ParseException("truncated attribute");
    }
    private static Attribute throwBadLength(int expected) {
        throw new ParseException("bad attribute length; expected length " +
                                 Hex.u4(expected));
    }
}

public final class ValueEncoder {
    private static final int VALUE_BYTE = 0x00;
    private static final int VALUE_SHORT = 0x02;
    private static final int VALUE_CHAR = 0x03;
    private static final int VALUE_INT = 0x04;
    private static final int VALUE_LONG = 0x06;
    private static final int VALUE_FLOAT = 0x10;
    private static final int VALUE_DOUBLE = 0x11;
    private static final int VALUE_STRING = 0x17;
    private static final int VALUE_TYPE = 0x18;
    private static final int VALUE_FIELD = 0x19;
    private static final int VALUE_METHOD = 0x1a;
    private static final int VALUE_ENUM = 0x1b;
    private static final int VALUE_ARRAY = 0x1c;
    private static final int VALUE_ANNOTATION = 0x1d;
    private static final int VALUE_NULL = 0x1e;
    private static final int VALUE_BOOLEAN = 0x1f;
    private final DexFile file;
    private final AnnotatedOutput out;
    public ValueEncoder(DexFile file, AnnotatedOutput out) {
        if (file == null) {
            throw new NullPointerException("file == null");
        }
        if (out == null) {
            throw new NullPointerException("out == null");
        }
        this.file = file;
        this.out = out;
    }
    public void writeConstant(Constant cst) {
        int type = constantToValueType(cst);
        int arg;
        switch (type) {
            case VALUE_BYTE:
            case VALUE_SHORT:
            case VALUE_INT:
            case VALUE_LONG: {
                long value = ((CstLiteralBits) cst).getLongBits();
                writeSignedIntegralValue(type, value);
                break;
            }
            case VALUE_CHAR: {
                long value = ((CstLiteralBits) cst).getLongBits();
                writeUnsignedIntegralValue(type, value);
                break;
            }
            case VALUE_FLOAT: {
                long value = ((CstFloat) cst).getLongBits() << 32;
                writeRightZeroExtendedValue(type, value);
                break;
            }
            case VALUE_DOUBLE: {
                long value = ((CstDouble) cst).getLongBits();
                writeRightZeroExtendedValue(type, value);
                break;
            }
            case VALUE_STRING: {
                int index = file.getStringIds().indexOf((CstString) cst);
                writeUnsignedIntegralValue(type, (long) index);
                break;
            }
            case VALUE_TYPE: {
                int index = file.getTypeIds().indexOf((CstType) cst);
                writeUnsignedIntegralValue(type, (long) index);
                break;
            }
            case VALUE_FIELD: {
                int index = file.getFieldIds().indexOf((CstFieldRef) cst);
                writeUnsignedIntegralValue(type, (long) index);
                break;
            }
            case VALUE_METHOD: {
                int index = file.getMethodIds().indexOf((CstMethodRef) cst);
                writeUnsignedIntegralValue(type, (long) index);
                break;
            }
            case VALUE_ENUM: {
                CstFieldRef fieldRef = ((CstEnumRef) cst).getFieldRef();
                int index = file.getFieldIds().indexOf(fieldRef);
                writeUnsignedIntegralValue(type, (long) index);
                break;
            }
            case VALUE_ARRAY: {
                out.writeByte(type);
                writeArray((CstArray) cst, false);
                break;
            }
            case VALUE_ANNOTATION: {
                out.writeByte(type);
                writeAnnotation(((CstAnnotation) cst).getAnnotation(),
                        false);
                break;
            }
            case VALUE_NULL: {
                out.writeByte(type);
                break;
            }
            case VALUE_BOOLEAN: {
                int value = ((CstBoolean) cst).getIntBits();
                out.writeByte(type | (value << 5));
                break;
            }
            default: {
                throw new RuntimeException("Shouldn't happen");
            }
        }
    }
    private static int constantToValueType(Constant cst) {
        if (cst instanceof CstByte) {
            return VALUE_BYTE;
        } else if (cst instanceof CstShort) {
            return VALUE_SHORT;
        } else if (cst instanceof CstChar) {
            return VALUE_CHAR;
        } else if (cst instanceof CstInteger) {
            return VALUE_INT;
        } else if (cst instanceof CstLong) {
            return VALUE_LONG;
        } else if (cst instanceof CstFloat) {
            return VALUE_FLOAT;
        } else if (cst instanceof CstDouble) {
            return VALUE_DOUBLE;
        } else if (cst instanceof CstString) {
            return VALUE_STRING;
        } else if (cst instanceof CstType) {
            return VALUE_TYPE;
        } else if (cst instanceof CstFieldRef) {
            return VALUE_FIELD;
        } else if (cst instanceof CstMethodRef) {
            return VALUE_METHOD;
        } else if (cst instanceof CstEnumRef) {
            return VALUE_ENUM;
        } else if (cst instanceof CstArray) {
            return VALUE_ARRAY;
        } else if (cst instanceof CstAnnotation) {
            return VALUE_ANNOTATION;
        } else if (cst instanceof CstKnownNull) {
            return VALUE_NULL;
        } else if (cst instanceof CstBoolean) {
            return VALUE_BOOLEAN;
        } else {
            throw new RuntimeException("Shouldn't happen");
        }
    }
    public void writeArray(CstArray array, boolean topLevel) {
        boolean annotates = topLevel && out.annotates();
        CstArray.List list = ((CstArray) array).getList();
        int size = list.size();
        if (annotates) {
            out.annotate("  size: " + Hex.u4(size));
        }
        out.writeUnsignedLeb128(size);
        for (int i = 0; i < size; i++) {
            Constant cst = list.get(i);
            if (annotates) {
                out.annotate("  [" + Integer.toHexString(i) + "] " +
                        constantToHuman(cst));
            }
            writeConstant(cst);
        }
        if (annotates) {
            out.endAnnotation();
        }
    }
    public void writeAnnotation(Annotation annotation, boolean topLevel) {
        boolean annotates = topLevel && out.annotates();
        StringIdsSection stringIds = file.getStringIds();
        TypeIdsSection typeIds = file.getTypeIds();
        CstType type = annotation.getType();
        int typeIdx = typeIds.indexOf(type);
        if (annotates) {
            out.annotate("  type_idx: " + Hex.u4(typeIdx) + " 
                    type.toHuman());
        }
        out.writeUnsignedLeb128(typeIds.indexOf(annotation.getType()));
        Collection<NameValuePair> pairs = annotation.getNameValuePairs();
        int size = pairs.size();
        if (annotates) {
            out.annotate("  size: " + Hex.u4(size));
        }
        out.writeUnsignedLeb128(size);
        int at = 0;
        for (NameValuePair pair : pairs) {
            CstUtf8 name = pair.getName();
            int nameIdx = stringIds.indexOf(name);
            Constant value = pair.getValue();
            if (annotates) {
                out.annotate(0, "  elements[" + at + "]:");
                at++;
                out.annotate("    name_idx: " + Hex.u4(nameIdx) + " 
                        name.toHuman());
            }
            out.writeUnsignedLeb128(nameIdx);
            if (annotates) {
                out.annotate("    value: " + constantToHuman(value));
            }
            writeConstant(value);
        }
        if (annotates) {
            out.endAnnotation();
        }
    }
    public static String constantToHuman(Constant cst) {
        int type = constantToValueType(cst);
        if (type == VALUE_NULL) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(cst.typeName());
        sb.append(' ');
        sb.append(cst.toHuman());
        return sb.toString();
    }
    private void writeSignedIntegralValue(int type, long value) {
        int requiredBits =
            65 - Long.numberOfLeadingZeros(value ^ (value >> 63));
        int requiredBytes = (requiredBits + 0x07) >> 3;
        out.writeByte(type | ((requiredBytes - 1) << 5));
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }
    private void writeUnsignedIntegralValue(int type, long value) {
        int requiredBits = 64 - Long.numberOfLeadingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        int requiredBytes = (requiredBits + 0x07) >> 3;
        out.writeByte(type | ((requiredBytes - 1) << 5));
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }
    private void writeRightZeroExtendedValue(int type, long value) {
        int requiredBits = 64 - Long.numberOfTrailingZeros(value);
        if (requiredBits == 0) {
            requiredBits = 1;
        }
        int requiredBytes = (requiredBits + 0x07) >> 3;
        value >>= 64 - (requiredBytes * 8);
        out.writeByte(type | ((requiredBytes - 1) << 5));
        while (requiredBytes > 0) {
            out.writeByte((byte) value);
            value >>= 8;
            requiredBytes--;
        }
    }
    public static void addContents(DexFile file, Annotation annotation) {
        TypeIdsSection typeIds = file.getTypeIds();
        StringIdsSection stringIds = file.getStringIds();
        typeIds.intern(annotation.getType());
        for (NameValuePair pair : annotation.getNameValuePairs()) {
            stringIds.intern(pair.getName());
            addContents(file, pair.getValue());
        }
    }
    public static void addContents(DexFile file, Constant cst) {
        if (cst instanceof CstAnnotation) {
            addContents(file, ((CstAnnotation) cst).getAnnotation());
        } else if (cst instanceof CstArray) {
            CstArray.List list = ((CstArray) cst).getList();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                addContents(file, list.get(i));
            }
        } else {
            file.internIfAppropriate(cst);
        }
    }
}

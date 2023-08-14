public final class ConstantPoolParser {
    private final ByteArray bytes;
    private final StdConstantPool pool;
    private final int[] offsets;
    private int endOffset;
    private ParseObserver observer;
    public ConstantPoolParser(ByteArray bytes) {
        int size = bytes.getUnsignedShort(8); 
        this.bytes = bytes;
        this.pool = new StdConstantPool(size);
        this.offsets = new int[size];
        this.endOffset = -1;
    }
    public void setObserver(ParseObserver observer) {
        this.observer = observer;
    }
    public int getEndOffset() {
        parseIfNecessary();
        return endOffset;
    }
    public StdConstantPool getPool() {
        parseIfNecessary();
        return pool;
    }
    private void parseIfNecessary() {
        if (endOffset < 0) {
            parse();
        }
    }
    private void parse() {
        determineOffsets();
        if (observer != null) {
            observer.parsed(bytes, 8, 2,
                            "constant_pool_count: " + Hex.u2(offsets.length));
            observer.parsed(bytes, 10, 0, "\nconstant_pool:");
            observer.changeIndent(1);
        }
        for (int i = 1; i < offsets.length; i++) {
            int offset = offsets[i];
            if ((offset != 0) && (pool.getOrNull(i) == null)) {
                parse0(i);
            }
        }
        if (observer != null) {
            for (int i = 1; i < offsets.length; i++) {
                Constant cst = pool.getOrNull(i);
                if (cst == null) {
                    continue;
                }
                int offset = offsets[i];
                int nextOffset = endOffset;
                for (int j = i + 1; j < offsets.length; j++) {
                    int off = offsets[j];
                    if (off != 0) {
                        nextOffset = off;
                        break;
                    }
                }
                observer.parsed(bytes, offset, nextOffset - offset,
                                Hex.u2(i) + ": " + cst.toString());
            }
            observer.changeIndent(-1);
            observer.parsed(bytes, endOffset, 0, "end constant_pool");
        }
    }
    private void determineOffsets() {
        int at = 10; 
        int lastCategory;
        for (int i = 1; i < offsets.length; i += lastCategory) {
            offsets[i] = at;
            int tag = bytes.getUnsignedByte(at);
            switch (tag) {
                case CONSTANT_Integer:
                case CONSTANT_Float:
                case CONSTANT_Fieldref:
                case CONSTANT_Methodref:
                case CONSTANT_InterfaceMethodref:
                case CONSTANT_NameAndType: {
                    lastCategory = 1;
                    at += 5;
                    break;
                }
                case CONSTANT_Long:
                case CONSTANT_Double: {
                    lastCategory = 2;
                    at += 9;
                    break;
                }
                case CONSTANT_Class:
                case CONSTANT_String: {
                    lastCategory = 1;
                    at += 3;
                    break;
                }
                case CONSTANT_Utf8: {
                    lastCategory = 1;
                    at += bytes.getUnsignedShort(at + 1) + 3;
                    break;
                }
                default: {
                    ParseException ex =
                        new ParseException("unknown tag byte: " + Hex.u1(tag));
                    ex.addContext("...while preparsing cst " + Hex.u2(i) +
                                  " at offset " + Hex.u4(at));
                    throw ex;
                }
            }
        }
        endOffset = at;
    }
    private Constant parse0(int idx) {
        Constant cst = pool.getOrNull(idx);
        if (cst != null) {
            return cst;
        }
        int at = offsets[idx];
        try {
            int tag = bytes.getUnsignedByte(at);
            switch (tag) {
                case CONSTANT_Utf8: {
                    cst = parseUtf8(at);
                    break;
                }
                case CONSTANT_Integer: {
                    int value = bytes.getInt(at + 1);
                    cst = CstInteger.make(value);
                    break;
                }
                case CONSTANT_Float: {
                    int bits = bytes.getInt(at + 1);
                    cst = CstFloat.make(bits);
                    break;
                }
                case CONSTANT_Long: {
                    long value = bytes.getLong(at + 1);
                    cst = CstLong.make(value);
                    break;
                }
                case CONSTANT_Double: {
                    long bits = bytes.getLong(at + 1);
                    cst = CstDouble.make(bits);
                    break;
                }
                case CONSTANT_Class: {
                    int nameIndex = bytes.getUnsignedShort(at + 1);
                    CstUtf8 name = (CstUtf8) parse0(nameIndex);
                    cst = new CstType(Type.internClassName(name.getString()));
                    break;
                }
                case CONSTANT_String: {
                    int stringIndex = bytes.getUnsignedShort(at + 1);
                    CstUtf8 string = (CstUtf8) parse0(stringIndex);
                    cst = new CstString(string);
                    break;
                }
                case CONSTANT_Fieldref: {
                    int classIndex = bytes.getUnsignedShort(at + 1);
                    CstType type = (CstType) parse0(classIndex);
                    int natIndex = bytes.getUnsignedShort(at + 3);
                    CstNat nat = (CstNat) parse0(natIndex);
                    cst = new CstFieldRef(type, nat);
                    break;
                }
                case CONSTANT_Methodref: {
                    int classIndex = bytes.getUnsignedShort(at + 1);
                    CstType type = (CstType) parse0(classIndex);
                    int natIndex = bytes.getUnsignedShort(at + 3);
                    CstNat nat = (CstNat) parse0(natIndex);
                    cst = new CstMethodRef(type, nat);
                    break;
                }
                case CONSTANT_InterfaceMethodref: {
                    int classIndex = bytes.getUnsignedShort(at + 1);
                    CstType type = (CstType) parse0(classIndex);
                    int natIndex = bytes.getUnsignedShort(at + 3);
                    CstNat nat = (CstNat) parse0(natIndex);
                    cst = new CstInterfaceMethodRef(type, nat);
                    break;
                }
                case CONSTANT_NameAndType: {
                    int nameIndex = bytes.getUnsignedShort(at + 1);
                    CstUtf8 name = (CstUtf8) parse0(nameIndex);
                    int descriptorIndex = bytes.getUnsignedShort(at + 3);
                    CstUtf8 descriptor = (CstUtf8) parse0(descriptorIndex);
                    cst = new CstNat(name, descriptor);
                    break;
                }
            }
        } catch (ParseException ex) {
            ex.addContext("...while parsing cst " + Hex.u2(idx) +
                          " at offset " + Hex.u4(at));
            throw ex;
        } catch (RuntimeException ex) {
            ParseException pe = new ParseException(ex);
            pe.addContext("...while parsing cst " + Hex.u2(idx) +
                          " at offset " + Hex.u4(at));
            throw pe;
        }
        pool.set(idx, cst);
        return cst;
    }
    private CstUtf8 parseUtf8(int at) {
        int length = bytes.getUnsignedShort(at + 1);
        at += 3; 
        ByteArray ubytes = bytes.slice(at, at + length);
        try {
            return new CstUtf8(ubytes);
        } catch (IllegalArgumentException ex) {
            throw new ParseException(ex);
        }
    }
}

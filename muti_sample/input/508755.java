public final class CstType extends TypedConstant {
    private static final HashMap<Type, CstType> interns =
        new HashMap<Type, CstType>(100);
    public static final CstType OBJECT = intern(Type.OBJECT);
    public static final CstType BOOLEAN = intern(Type.BOOLEAN_CLASS);
    public static final CstType BYTE = intern(Type.BYTE_CLASS);
    public static final CstType CHARACTER = intern(Type.CHARACTER_CLASS);
    public static final CstType DOUBLE = intern(Type.DOUBLE_CLASS);
    public static final CstType FLOAT = intern(Type.FLOAT_CLASS);
    public static final CstType LONG = intern(Type.LONG_CLASS);
    public static final CstType INTEGER = intern(Type.INTEGER_CLASS);
    public static final CstType SHORT = intern(Type.SHORT_CLASS);
    public static final CstType VOID = intern(Type.VOID_CLASS);
    public static final CstType BOOLEAN_ARRAY = intern(Type.BOOLEAN_ARRAY);
    public static final CstType BYTE_ARRAY = intern(Type.BYTE_ARRAY);
    public static final CstType CHAR_ARRAY = intern(Type.CHAR_ARRAY);
    public static final CstType DOUBLE_ARRAY = intern(Type.DOUBLE_ARRAY);
    public static final CstType FLOAT_ARRAY = intern(Type.FLOAT_ARRAY);
    public static final CstType LONG_ARRAY = intern(Type.LONG_ARRAY);
    public static final CstType INT_ARRAY = intern(Type.INT_ARRAY);
    public static final CstType SHORT_ARRAY = intern(Type.SHORT_ARRAY);
    private final Type type;
    private CstUtf8 descriptor;
    public static CstType forBoxedPrimitiveType(Type primitiveType) {
        switch (primitiveType.getBasicType()) {
            case Type.BT_BOOLEAN: return BOOLEAN;
            case Type.BT_BYTE:    return BYTE;
            case Type.BT_CHAR:    return CHARACTER;
            case Type.BT_DOUBLE:  return DOUBLE;
            case Type.BT_FLOAT:   return FLOAT;
            case Type.BT_INT:     return INTEGER;
            case Type.BT_LONG:    return LONG;
            case Type.BT_SHORT:   return SHORT;
            case Type.BT_VOID:    return VOID;
        }
        throw new IllegalArgumentException("not primitive: " + primitiveType);
    }
    public static CstType intern(Type type) {
        CstType cst = interns.get(type);
        if (cst == null) {
            cst = new CstType(type);
            interns.put(type, cst);
        }
        return cst;
    }
    public CstType(Type type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        if (type == type.KNOWN_NULL) {
            throw new UnsupportedOperationException(
                    "KNOWN_NULL is not representable");
        }
        this.type = type;
        this.descriptor = null;
    }
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CstType)) {
            return false;
        }
        return type == ((CstType) other).type;
    }
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    @Override
    protected int compareTo0(Constant other) {
        String thisDescriptor = type.getDescriptor();
        String otherDescriptor = ((CstType) other).type.getDescriptor();
        return thisDescriptor.compareTo(otherDescriptor);
    }
    @Override
    public String toString() {
        return "type{" + toHuman() + '}';
    }
    public Type getType() {
        return Type.CLASS;
    }
    @Override
    public String typeName() {
        return "type";
    }
    @Override
    public boolean isCategory2() {
        return false;
    }
    public String toHuman() {
        return type.toHuman();
    }
    public Type getClassType() {
        return type;
    }
    public CstUtf8 getDescriptor() {
        if (descriptor == null) {
            descriptor = new CstUtf8(type.getDescriptor());
        }
        return descriptor;
    }
}

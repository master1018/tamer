public final class SimpleType<T> extends OpenType<T> {
    static final long serialVersionUID = 2215577471957694503L;
    public static final SimpleType<Void> VOID =
        new SimpleType<Void>(Void.class);
    public static final SimpleType<Boolean> BOOLEAN =
        new SimpleType<Boolean>(Boolean.class);
    public static final SimpleType<Character> CHARACTER =
        new SimpleType<Character>(Character.class);
    public static final SimpleType<Byte> BYTE =
        new SimpleType<Byte>(Byte.class);
    public static final SimpleType<Short> SHORT =
        new SimpleType<Short>(Short.class);
    public static final SimpleType<Integer> INTEGER =
        new SimpleType<Integer>(Integer.class);
    public static final SimpleType<Long> LONG =
        new SimpleType<Long>(Long.class);
    public static final SimpleType<Float> FLOAT =
        new SimpleType<Float>(Float.class);
    public static final SimpleType<Double> DOUBLE =
        new SimpleType<Double>(Double.class);
    public static final SimpleType<String> STRING =
        new SimpleType<String>(String.class);
    public static final SimpleType<BigDecimal> BIGDECIMAL =
        new SimpleType<BigDecimal>(BigDecimal.class);
    public static final SimpleType<BigInteger> BIGINTEGER =
        new SimpleType<BigInteger>(BigInteger.class);
    public static final SimpleType<Date> DATE =
        new SimpleType<Date>(Date.class);
    public static final SimpleType<ObjectName> OBJECTNAME =
        new SimpleType<ObjectName>(ObjectName.class);
    private static final SimpleType<?>[] typeArray = {
        VOID, BOOLEAN, CHARACTER, BYTE, SHORT, INTEGER, LONG, FLOAT,
        DOUBLE, STRING, BIGDECIMAL, BIGINTEGER, DATE, OBJECTNAME,
    };
    private transient Integer myHashCode = null;        
    private transient String  myToString = null;        
    private SimpleType(Class<T> valueClass) {
        super(valueClass.getName(), valueClass.getName(), valueClass.getName(),
              false);
    }
    public boolean isValue(Object obj) {
        if (obj == null) {
            return false;
        }
        return this.getClassName().equals(obj.getClass().getName());
    }
    public boolean equals(Object obj) {
        if (!(obj instanceof SimpleType<?>))
            return false;
        SimpleType<?> other = (SimpleType<?>) obj;
        return this.getClassName().equals(other.getClassName());
    }
    public int hashCode() {
        if (myHashCode == null) {
            myHashCode = Integer.valueOf(this.getClassName().hashCode());
        }
        return myHashCode.intValue();
    }
    public String toString() {
        if (myToString == null) {
            myToString = this.getClass().getName()+ "(name="+ getTypeName() +")";
        }
        return myToString;
    }
    private static final Map<SimpleType<?>,SimpleType<?>> canonicalTypes =
        new HashMap<SimpleType<?>,SimpleType<?>>();
    static {
        for (int i = 0; i < typeArray.length; i++) {
            final SimpleType<?> type = typeArray[i];
            canonicalTypes.put(type, type);
        }
    }
    public Object readResolve() throws ObjectStreamException {
        final SimpleType<?> canonical = canonicalTypes.get(this);
        if (canonical == null) {
            throw new InvalidObjectException("Invalid SimpleType: " + this);
        }
        return canonical;
    }
}

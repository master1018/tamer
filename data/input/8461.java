public abstract class MXBeanMapping {
    private final Type javaType;
    private final OpenType<?> openType;
    private final Class<?> openClass;
    protected MXBeanMapping(Type javaType, OpenType<?> openType) {
        if (javaType == null || openType == null)
            throw new NullPointerException("Null argument");
        this.javaType = javaType;
        this.openType = openType;
        this.openClass = makeOpenClass(javaType, openType);
    }
    public final Type getJavaType() {
        return javaType;
    }
    public final OpenType<?> getOpenType() {
        return openType;
    }
    public final Class<?> getOpenClass() {
        return openClass;
    }
    private static Class<?> makeOpenClass(Type javaType, OpenType<?> openType) {
        if (javaType instanceof Class<?> && ((Class<?>) javaType).isPrimitive())
            return (Class<?>) javaType;
        try {
            String className = openType.getClassName();
            return Class.forName(className, false, null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);  
        }
    }
    public abstract Object fromOpenValue(Object openValue)
    throws InvalidObjectException;
    public abstract Object toOpenValue(Object javaValue)
    throws OpenDataException;
    public void checkReconstructible() throws InvalidObjectException {}
}

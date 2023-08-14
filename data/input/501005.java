public abstract class Enum<E extends Enum<E>> implements Serializable,
        Comparable<E> {
    private static final long serialVersionUID = -4300926546619394005L;
    private final String name;
    private final int ordinal;
    protected Enum(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }
    public final String name() {
        return name;
    }
    public final int ordinal() {
        return ordinal;
    }
    @Override
    public String toString() {
        return name;
    }
    @Override
    public final boolean equals(Object other) {
        return this == other;
    }
    @Override
    public final int hashCode() {
        return ordinal + (name == null ? 0 : name.hashCode());
    }
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(Msg.getString("KA004")); 
    }
    public final int compareTo(E o) {
        return ordinal - o.ordinal;
    }
    @SuppressWarnings("unchecked")
    public final Class<E> getDeclaringClass() {
        Class<?> myClass = getClass();
        Class<?> mySuperClass = myClass.getSuperclass();
        if (Enum.class == mySuperClass) {
            return (Class<E>)myClass;
        }
        return (Class<E>)mySuperClass;
    }
    public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
        if ((enumType == null) || (name == null)) {
            throw new NullPointerException(Msg.getString("KA001")); 
        }
        enumType.checkPublicMemberAccess();
        T result = enumType.getClassCache().getEnumValue(name);
        if (result == null) {
            if (!enumType.isEnum()) {
                throw new IllegalArgumentException(Msg.getString("KA005", enumType)); 
            } else {
                throw new IllegalArgumentException(Msg.getString("KA006", name, 
                                enumType));
            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    static <T extends Enum<T>> T[] getValues(final Class<T> enumType) {
        try {
            Method values = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Method>() {
                        public Method run() throws Exception {
                            Method valsMethod = enumType.getMethod("values", 
                                    (Class[]) null);
                            valsMethod.setAccessible(true);
                            return valsMethod;
                        }
                    });
            return (T[]) values.invoke(enumType, (Object[])null);
        } catch (Exception e) {
            return null;
        }
    }
}

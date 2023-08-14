public class GenericArrayTypeImpl
    implements GenericArrayType {
    private Type genericComponentType;
    private GenericArrayTypeImpl(Type ct) {
        genericComponentType = ct;
    }
    public static GenericArrayTypeImpl make(Type ct) {
        return new GenericArrayTypeImpl(ct);
    }
    public Type getGenericComponentType() {
        return genericComponentType; 
    }
    public String toString() {
        Type componentType = getGenericComponentType();
        StringBuilder sb = new StringBuilder();
        if (componentType instanceof Class)
            sb.append(((Class)componentType).getName() );
        else
            sb.append(componentType.toString());
        sb.append("[]");
        return sb.toString();
    }
    @Override
    public boolean equals(Object o) {
        if (o instanceof GenericArrayType) {
            GenericArrayType that = (GenericArrayType) o;
            Type thatComponentType = that.getGenericComponentType();
            return genericComponentType == null ?
                thatComponentType == null :
                genericComponentType.equals(thatComponentType);
        } else
            return false;
    }
    @Override
    public int hashCode() {
        return (genericComponentType == null) ?
            0:
            genericComponentType.hashCode();
    }
}

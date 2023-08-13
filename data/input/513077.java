public class SigArrayType implements IArrayType, Serializable {
    private ITypeReference componentType;
    public SigArrayType(ITypeReference componentType) {
        this.componentType = componentType;
    }
    public ITypeReference getComponentType() {
        return componentType;
    }
    @Override
    public int hashCode() {
        return SigArrayType.hashCode(this);
    }
    public static int hashCode(IArrayType type) {
        return type.getComponentType().hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        return SigArrayType.equals(this, obj);
    }
    public static boolean equals(IArrayType thiz, Object that) {
        if (!(that instanceof IArrayType)) {
            return false;
        }
        IArrayType other = (IArrayType) that;
        return thiz.getComponentType().equals(other.getComponentType());
    }
    @Override
    public String toString() {
        return SigArrayType.toString(this);
    }
    public static String toString(IArrayType type) {
        StringBuilder builder = new StringBuilder();
        builder.append(type.getComponentType().toString());
        builder.append("[]");
        return builder.toString();
    }
}

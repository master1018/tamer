public final class ImplForArray implements GenericArrayType {
    private final Type componentType;
    public ImplForArray(Type componentType) {
        this.componentType = componentType;
    }
    public Type getGenericComponentType() {
        try { 
            return ((ImplForType)componentType).getResolvedType(); 
        } catch (ClassCastException e) { 
            return componentType; 
        }
    }
    public String toString() {
        return componentType.toString() + "[]";
    }
}

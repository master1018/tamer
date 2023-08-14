public class SigParameterizedType implements IParameterizedType, Serializable {
    private ITypeReference ownerType;
    private IClassReference rawType;
    private List<ITypeReference> typeArguments;
    public SigParameterizedType(ITypeReference ownerType,
            IClassReference rawType, List<ITypeReference> typeArguments) {
        this.ownerType = ownerType;
        this.rawType = rawType;
        this.typeArguments = typeArguments;
    }
    public ITypeReference getOwnerType() {
        ITypeReference returnValue = ownerType;
        if (returnValue == null) {
            if (rawType.getClassDefinition().getDeclaringClass() != null) {
                returnValue = new SigClassReference(rawType
                        .getClassDefinition().getDeclaringClass());
            }
        }
        return returnValue;
    }
    public IClassReference getRawType() {
        return rawType;
    }
    public List<ITypeReference> getTypeArguments() {
        return typeArguments;
    }
    @Override
    public int hashCode() {
        return hashCode(this);
    }
    public static int hashCode(IParameterizedType type) {
        final int prime = 31;
        int result = 1;
        result = prime * type.getRawType().hashCode();
        result = prime * result + type.getTypeArguments().hashCode();
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        return equals(this, obj);
    }
    public static boolean equals(IParameterizedType thiz, Object that) {
        if (!(that instanceof IParameterizedType)) {
            return false;
        }
        IParameterizedType other = (IParameterizedType) that;
        if (thiz.getOwnerType() == null) {
            if (other.getOwnerType() != null) {
                return false;
            }
        } else if (Uninitialized.isInitialized(thiz.getOwnerType())) {
            if (!Uninitialized.isInitialized(other.getOwnerType())) {
                return false;
            }
        } else if (!thiz.getOwnerType().equals(other.getOwnerType())) {
            return false;
        }
        if (!thiz.getRawType().equals(other.getRawType())) {
            return false;
        }
        if (!thiz.getTypeArguments().equals(other.getTypeArguments())) {
            return false;
        }
        return true;
    }
    @Override
    public String toString() {
        return SigParameterizedType.toString(this);
    }
    public static String toString(IParameterizedType type) {
        StringBuilder builder = new StringBuilder();
        if (type.getOwnerType() != null) {
            builder.append(type.getOwnerType().toString());
            builder.append("::");
        }
        builder.append(type.getRawType());
        builder.append("<");
        builder.append(ModelUtil.separate(type.getTypeArguments(), ", "));
        builder.append(">");
        return builder.toString();
    }
}

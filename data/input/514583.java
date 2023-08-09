public final class ImplForType implements ParameterizedType {
    private final ListOfTypes args;
    private final ImplForType ownerType0; 
    private Type ownerTypeRes;
    private Class rawType; 
    private final String rawTypeName;
    private ClassLoader loader;
    public ImplForType(ImplForType ownerType, String rawTypeName, 
            ListOfTypes args, ClassLoader loader) {
        this.ownerType0 = ownerType;
        this.rawTypeName = rawTypeName;
        this.args = args;
        this.loader = loader;
    }
    public Type[] getActualTypeArguments() {
        return args.getResolvedTypes().clone();
    }
    public Type getOwnerType() {
        if (ownerTypeRes == null) {
            if (ownerType0 != null) {
                ownerTypeRes = ownerType0.getResolvedType();
            } else {
                ownerTypeRes = getRawType().getDeclaringClass();
            }
        }
        return ownerTypeRes;
    }
    public Class getRawType() {
        if (rawType == null) {
            try {
                rawType = Class.forName(rawTypeName, false, loader);
            } catch (ClassNotFoundException e) {
                throw new TypeNotPresentException(rawTypeName, e);
            }
        }
        return rawType;
    }
    Type getResolvedType() {
        if (args.getResolvedTypes().length == 0) {
            return getRawType();
        } else {
            return this;
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rawTypeName);
        if (args.length() > 0) {
            sb.append("<").append(args).append(">");
        }
        return sb.toString();
    }
}

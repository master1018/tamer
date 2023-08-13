abstract class AbstractTypeImpl implements com.sun.javadoc.Type {
    protected final DocEnv env;
    protected final Type type;
    protected AbstractTypeImpl(DocEnv env, Type type) {
        this.env = env;
        this.type = type;
    }
    public String typeName() {
        return type.tsym.name.toString();
    }
    public String qualifiedTypeName() {
        return type.tsym.getQualifiedName().toString();
    }
    public String simpleTypeName() {
        return type.tsym.name.toString();
    }
    public String name() {
        return typeName();
    }
    public String qualifiedName() {
        return qualifiedTypeName();
    }
    public String toString() {
        return qualifiedTypeName();
    }
    public String dimension() {
        return "";
    }
    public boolean isPrimitive() {
        return false;
    }
    public ClassDoc asClassDoc() {
        return null;
    }
    public TypeVariable asTypeVariable() {
        return null;
    }
    public WildcardType asWildcardType() {
        return null;
    }
    public ParameterizedType asParameterizedType() {
        return null;
    }
    public AnnotationTypeDoc asAnnotationTypeDoc() {
        return null;
    }
}

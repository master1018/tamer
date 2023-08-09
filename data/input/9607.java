abstract class DeclaredTypeImpl extends TypeMirrorImpl
                                implements DeclaredType {
    protected Type.ClassType type;
    protected DeclaredTypeImpl(AptEnv env, Type.ClassType type) {
        super(env, type);
        this.type = type;
    }
    public String toString() {
        return toString(env, type);
    }
    public TypeDeclaration getDeclaration() {
        return env.declMaker.getTypeDeclaration((ClassSymbol) type.tsym);
    }
    public DeclaredType getContainingType() {
        if (type.getEnclosingType().tag == TypeTags.CLASS) {
            return (DeclaredType) env.typeMaker.getType(type.getEnclosingType());
        }
        ClassSymbol enclosing = type.tsym.owner.enclClass();
        if (enclosing != null) {
            return (DeclaredType) env.typeMaker.getType(
                                        env.jctypes.erasure(enclosing.type));
        }
        return null;
    }
    public Collection<TypeMirror> getActualTypeArguments() {
        return env.typeMaker.getTypes(type.getTypeArguments());
    }
    public Collection<InterfaceType> getSuperinterfaces() {
        return env.typeMaker.getTypes(env.jctypes.interfaces(type),
                                      InterfaceType.class);
    }
    static String toString(AptEnv env, Type.ClassType c) {
        return c.toString();
    }
}

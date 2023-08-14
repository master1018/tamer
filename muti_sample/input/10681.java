class ParameterImpl implements Parameter {
    private final DocEnv env;
    private final VarSymbol sym;
    private final com.sun.javadoc.Type type;
    ParameterImpl(DocEnv env, VarSymbol sym) {
        this.env = env;
        this.sym = sym;
        this.type = TypeMaker.getType(env, sym.type, false);
    }
    public com.sun.javadoc.Type type() {
        return type;
    }
    public String name() {
        return sym.toString();
    }
    public String typeName() {
        return (type instanceof ClassDoc || type instanceof TypeVariable)
                ? type.typeName()       
                : type.toString();
    }
    public String toString() {
        return typeName() + " " + sym;
    }
    public AnnotationDesc[] annotations() {
        AnnotationDesc res[] = new AnnotationDesc[sym.getAnnotationMirrors().length()];
        int i = 0;
        for (Attribute.Compound a : sym.getAnnotationMirrors()) {
            res[i++] = new AnnotationDescImpl(env, a);
        }
        return res;
    }
}

public class AnnotationTypeElementDocImpl
        extends MethodDocImpl implements AnnotationTypeElementDoc {
    AnnotationTypeElementDocImpl(DocEnv env, MethodSymbol sym) {
        super(env, sym);
    }
    AnnotationTypeElementDocImpl(DocEnv env, MethodSymbol sym,
                                 String doc, JCMethodDecl tree, Position.LineMap lineMap) {
        super(env, sym, doc, tree, lineMap);
    }
    public boolean isAnnotationTypeElement() {
        return !isMethod();
    }
    public boolean isMethod() {
        return env.legacyDoclet;
    }
    public boolean isAbstract() {
        return false;
    }
    public AnnotationValue defaultValue() {
        return (sym.defaultValue == null)
               ? null
               : new AnnotationValueImpl(env, sym.defaultValue);
    }
}

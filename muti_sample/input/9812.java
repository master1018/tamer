public class SimpleTypeVisitor6<R, P> extends AbstractTypeVisitor6<R, P> {
    protected final R DEFAULT_VALUE;
    protected SimpleTypeVisitor6(){
        DEFAULT_VALUE = null;
    }
    protected SimpleTypeVisitor6(R defaultValue){
        DEFAULT_VALUE = defaultValue;
    }
    protected R defaultAction(TypeMirror e, P p) {
        return DEFAULT_VALUE;
    }
    public R visitPrimitive(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }
    public R visitNull(NullType t, P p){
        return defaultAction(t, p);
    }
    public R visitArray(ArrayType t, P p){
        return defaultAction(t, p);
    }
    public R visitDeclared(DeclaredType t, P p){
        return defaultAction(t, p);
    }
    public R visitError(ErrorType t, P p){
        return defaultAction(t, p);
    }
    public R visitTypeVariable(TypeVariable t, P p){
        return defaultAction(t, p);
    }
    public R visitWildcard(WildcardType t, P p){
        return defaultAction(t, p);
    }
    public R visitExecutable(ExecutableType t, P p) {
        return defaultAction(t, p);
    }
    public R visitNoType(NoType t, P p){
        return defaultAction(t, p);
    }
}

public class ConstructorScope extends AbstractScope<Constructor> {
    private ConstructorScope(Constructor c){
        super(c);
    }
    private Class<?> getEnclosingClass(){
        return getRecvr().getDeclaringClass();
    }
    protected Scope computeEnclosingScope() {
        return ClassScope.make(getEnclosingClass());
    }
    public static ConstructorScope make(Constructor c) {
        return new ConstructorScope(c);
    }
}

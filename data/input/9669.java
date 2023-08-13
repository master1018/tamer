public class MethodScope extends AbstractScope<Method> {
    private MethodScope(Method m){
        super(m);
    }
    private Class<?> getEnclosingClass(){
        return getRecvr().getDeclaringClass();
    }
    protected Scope computeEnclosingScope() {
        return ClassScope.make(getEnclosingClass());
    }
    public static MethodScope make(Method m) {
        return new MethodScope(m);
    }
}

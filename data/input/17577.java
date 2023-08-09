public class ClassScope extends AbstractScope<Class<?>> implements Scope {
    private ClassScope(Class<?> c){
        super(c);
    }
    protected Scope computeEnclosingScope() {
        Class<?> receiver = getRecvr();
        Method m = receiver.getEnclosingMethod();
        if (m != null)
            return MethodScope.make(m);
        Constructor<?> cnstr = receiver.getEnclosingConstructor();
        if (cnstr != null)
            return ConstructorScope.make(cnstr);
        Class<?> c = receiver.getEnclosingClass();
        if (c != null)
            return ClassScope.make(c);
        return DummyScope.make();
    }
    public static ClassScope make(Class<?> c) { return new ClassScope(c);}
}

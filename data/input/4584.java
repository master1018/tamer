public class TypeVar<T, S extends Number & Runnable> extends Tester {
    public static void main(String[] args) {
        (new TypeVar()).run();
    }
    private T t;
    private S s;
    private TypeVariable tvT;   
    private TypeVariable tvS;   
    protected void init() {
        tvT = (TypeVariable) getField("t").getType();
        tvS = (TypeVariable) getField("s").getType();
    }
    @Test(result="type var")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        tvT.accept(new SimpleTypeVisitor() {
            public void visitTypeMirror(TypeMirror t) {
                res.add("type");
            }
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
            public void visitTypeVariable(TypeVariable t) {
                res.add("type var");
            }
        });
        return res;
    }
    @Test(result="T")
    String toStringTest1() {
        return tvT.toString();
    }
    @Test(result="S")
    String toStringTest2() {
        return tvS.toString();
    }
    @Test(result="S extends java.lang.Number & java.lang.Runnable")
    TypeParameterDeclaration getDeclaration() {
        return tvS.getDeclaration();
    }
}

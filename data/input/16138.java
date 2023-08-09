public class ParameterDecl extends Tester {
    public static void main(String[] args) {
        (new ParameterDecl()).run();
    }
    @interface AT1 {
    }
    @interface AT2 {
        boolean value();
    }
    private void m1(@AT1 @AT2(true) final int p1) {
    }
    private void m2(int p1) {
    }
    private ParameterDeclaration p1 = null;     
    protected void init() {
        p1 = getMethod("m1").getParameters().iterator().next();
    }
    @Test(result="param")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        p1.accept(new SimpleDeclarationVisitor() {
            public void visitFieldDeclaration(FieldDeclaration f) {
                res.add("field");
            }
            public void visitParameterDeclaration(ParameterDeclaration p) {
                res.add("param");
            }
        });
        return res;
    }
    @Test(result={"@ParameterDecl.AT1", "@ParameterDecl.AT2(true)"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return p1.getAnnotationMirrors();
    }
    @Test(result={"final"})
    Collection<Modifier> getModifiers() {
        return p1.getModifiers();
    }
    @Test(result="ParameterDecl.java")
    String getPosition() {
        return p1.getPosition().file().getName();
    }
    @Test(result="p1")
    String getSimpleName() {
        return p1.getSimpleName();
    }
    @Test(result="int")
    TypeMirror getType() {
        return p1.getType();
    }
    @Test(result="int p1")
    String toStringTest() {
        return p1.toString();
    }
    @Test(result="true")
    boolean equalsTest1() {
        ParameterDeclaration p =
            getMethod("m1").getParameters().iterator().next();
        return p1.equals(p);
    }
    @Test(result="false")
    boolean equalsTest2() {
        ParameterDeclaration p2 =
            getMethod("m2").getParameters().iterator().next();
        return p1.equals(p2);
    }
}

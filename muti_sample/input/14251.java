public class FieldDecl extends Tester {
    public static void main(String[] args) {
        (new FieldDecl()).run();
    }
    private FieldDeclaration f1 = null;         
    private FieldDeclaration f2 = null;         
    private FieldDeclaration f3 = null;         
    protected void init() {
        f1 = getField("aField");
        f2 = getField("aStaticField");
        f3 = getField("aConstantField");
    }
    @Test(result="field")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        f1.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitFieldDeclaration(FieldDeclaration f) {
                res.add("field");
            }
            public void visitEnumConstantDeclaration(
                                                EnumConstantDeclaration e) {
                res.add("enum const");
            }
        });
        return res;
    }
    @Test(result={"@FieldDecl.AT1"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return f1.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return f1.getDocComment();
    }
    @Test(result={"public"})
    Collection<Modifier> getModifiers() {
        return f1.getModifiers();
    }
    @Test(result="FieldDecl.java")
    String getPosition() {
        return f1.getPosition().file().getName();
    }
    @Test(result="aField")
    String getSimpleName() {
        return f1.getSimpleName();
    }
    @Test(result="FieldDecl")
    TypeDeclaration getDeclaringType() {
        return f1.getDeclaringType();
    }
    @Test(result="java.util.List<java.lang.String>")
    TypeMirror getType1() {
        return f1.getType();
    }
    @Test(result="int")
    TypeMirror getType2() {
        return f2.getType();
    }
    @Test(result="null")
    Object getConstantValue1() {
        return f1.getConstantValue();
    }
    @Test(result="true")
    Object getConstantValue2() {
        return f3.getConstantValue();
    }
    @Test(result="aField")
    String toStringTest() {
        return f1.toString();
    }
    @AT1
    public List<String> aField = new ArrayList<String>();
    static int aStaticField;
    public static final boolean aConstantField = true;
    @interface AT1 {
    }
}

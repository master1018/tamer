public class ClassDecl extends Tester {
    public static void main(String[] args) {
        (new ClassDecl()).run();
    }
    private ClassDeclaration nested = null;     
    private ClassDeclaration object = null;     
    private ClassDecl() {
    }
    private ClassDecl(int i) {
        this();
    }
    static int i;
    static {
        i = 7;
    }
    private static strictfp class NestedClass<T> implements Serializable {
        void m1() {}
        void m2() {}
        void m2(int i) {}
    }
    protected void init() {
        nested = (ClassDeclaration)
            thisClassDecl.getNestedTypes().iterator().next();
        object = (ClassDeclaration)
            env.getTypeDeclaration("java.lang.Object");
    }
    @Test(result="class")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        thisClassDecl.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitClassDeclaration(ClassDeclaration c) {
                res.add("class");
            }
            public void visitEnumDeclaration(EnumDeclaration e) {
                res.add("enum");
            }
        });
        return res;
    }
    @Test(result={"@AT1", "@AT2"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return thisClassDecl.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return thisClassDecl.getDocComment();
    }
    @Test(result={"public"})
    Collection<Modifier> getModifiers1() {
        return thisClassDecl.getModifiers();
    }
    @Test(result={"private", "static", "strictfp"})
    Collection<Modifier> getModifiers2() {
        return nested.getModifiers();
    }
    @Test(result="ClassDecl.java")
    String getPosition() {
        return thisClassDecl.getPosition().file().getName();
    }
    @Test(result="ClassDecl")
    String getSimpleName1() {
        return thisClassDecl.getSimpleName();
    }
    @Test(result="NestedClass")
    String getSimpleName2() {
        return nested.getSimpleName();
    }
    @Test(result="null")
    TypeDeclaration getDeclaringType1() {
        return thisClassDecl.getDeclaringType();
    }
    @Test(result="ClassDecl")
    TypeDeclaration getDeclaringType2() {
        return nested.getDeclaringType();
    }
    @Test(result={"nested", "object", "i"})
    Collection<FieldDeclaration> getFields() {
        return thisClassDecl.getFields();
    }
    @Test(result={})
    Collection<TypeParameterDeclaration> getFormalTypeParameters1() {
        return thisClassDecl.getFormalTypeParameters();
    }
    @Test(result="T")
    Collection<TypeParameterDeclaration> getFormalTypeParameters2() {
        return nested.getFormalTypeParameters();
    }
    @Test(result="ClassDecl.NestedClass<T>")
    Collection<TypeDeclaration> getNestedTypes() {
        return thisClassDecl.getNestedTypes();
    }
    @Test(result="")
    PackageDeclaration getPackage1() {
        return thisClassDecl.getPackage();
    }
    @Test(result="java.lang")
    PackageDeclaration getPackage2() {
        return object.getPackage();
    }
    @Test(result="ClassDecl")
    String getQualifiedName1() {
        return thisClassDecl.getQualifiedName();
    }
    @Test(result="ClassDecl.NestedClass")
    String getQualifiedName2() {
        return nested.getQualifiedName();
    }
    @Test(result="java.lang.Object")
    String getQualifiedName3() {
        return object.getQualifiedName();
    }
    @Test(result="java.io.Serializable")
    Collection<InterfaceType> getSuperinterfaces() {
        return nested.getSuperinterfaces();
    }
    @Test(result={"ClassDecl()", "ClassDecl(int)"})
    Collection<ConstructorDeclaration> getConstructors1() {
        return thisClassDecl.getConstructors();
    }
    @Test(result={"NestedClass()"})
    Collection<ConstructorDeclaration> getConstructors2() {
        return nested.getConstructors();
    }
    @Test(result={"m1()", "m2()", "m2(int)"})
    Collection<MethodDeclaration> getMethods() {
        return nested.getMethods();
    }
    @Test(result={"Tester"})
    ClassType getSuperclass() {
        return thisClassDecl.getSuperclass();
    }
    @Test(result={"null"})
    ClassType objectHasNoSuperclass() {
        return object.getSuperclass();
    }
}
@interface AT1 {
}
@interface AT2 {
}

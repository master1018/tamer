public class InterfaceDecl extends Tester {
    public static void main(String[] args) {
        (new InterfaceDecl()).run();
    }
    private InterfaceDeclaration iDecl = null;          
    private InterfaceDeclaration nested = null;         
    protected void init() {
        iDecl = (InterfaceDeclaration) env.getTypeDeclaration("I");
        nested = (InterfaceDeclaration)
            iDecl.getNestedTypes().iterator().next();
    }
    @Test(result="interface")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        iDecl.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitClassDeclaration(ClassDeclaration c) {
                res.add("class");
            }
            public void visitInterfaceDeclaration(InterfaceDeclaration e) {
                res.add("interface");
            }
            public void visitAnnotationTypeDeclaration(
                                        AnnotationTypeDeclaration e) {
                res.add("annotation type");
            }
        });
        return res;
    }
    @Test(result="true")
    boolean equals1() {
        return iDecl.equals(iDecl);
    }
    @Test(result="false")
    boolean equals2() {
        return iDecl.equals(nested);
    }
    @Test(result="true")
    boolean equals3() {
        return iDecl.equals(env.getTypeDeclaration("I"));
    }
    @Test(result={"@AT1", "@AT2"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return iDecl.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return iDecl.getDocComment();
    }
    @Test(result={"abstract"})
    Collection<Modifier> getModifiers1() {
        return iDecl.getModifiers();
    }
    @Test(result={"public", "abstract", "static"})
    Collection<Modifier> getModifiers2() {
        return nested.getModifiers();
    }
    @Test(result="InterfaceDecl.java")
    String getPosition() {
        return iDecl.getPosition().file().getName();
    }
    @Test(result="I")
    String getSimpleName1() {
        return iDecl.getSimpleName();
    }
    @Test(result="Nested")
    String getSimpleName2() {
        return nested.getSimpleName();
    }
    @Test(result="null")
    TypeDeclaration getDeclaringType1() {
        return iDecl.getDeclaringType();
    }
    @Test(result="I<T extends java.lang.Number>")
    TypeDeclaration getDeclaringType2() {
        return nested.getDeclaringType();
    }
    @Test(result={"i"})
    Collection<FieldDeclaration> getFields() {
        return iDecl.getFields();
    }
    @Test(result={"T extends java.lang.Number"})
    Collection<TypeParameterDeclaration> getFormalTypeParameters1() {
        return iDecl.getFormalTypeParameters();
    }
    @Test(result={})
    Collection<TypeParameterDeclaration> getFormalTypeParameters2() {
        return nested.getFormalTypeParameters();
    }
    @Test(result={"m()", "toString()"})
    Collection<? extends MethodDeclaration> getMethods() {
        return nested.getMethods();
    }
    @Test(result="I.Nested")
    Collection<TypeDeclaration> getNestedTypes() {
        return iDecl.getNestedTypes();
    }
    @Test(result="")
    PackageDeclaration getPackage1() {
        return iDecl.getPackage();
    }
    @Test(result="java.util")
    PackageDeclaration getPackage2() {
        InterfaceDeclaration set =
            (InterfaceDeclaration) env.getTypeDeclaration("java.util.Set");
        return set.getPackage();
    }
    @Test(result="I")
    String getQualifiedName1() {
        return iDecl.getQualifiedName();
    }
    @Test(result="I.Nested")
    String getQualifiedName2() {
        return nested.getQualifiedName();
    }
    @Test(result="java.util.Set")
    String getQualifiedName3() {
        InterfaceDeclaration set =
            (InterfaceDeclaration) env.getTypeDeclaration("java.util.Set");
        return set.getQualifiedName();
    }
    @Test(result="java.lang.Runnable")
    Collection<InterfaceType> getSuperinterfaces() {
        return iDecl.getSuperinterfaces();
    }
}
@AT1
@AT2
interface I<T extends Number> extends Runnable {
    int i = 6;
    void m1();
    void m2();
    void m2(int j);
    interface Nested {
        void m();
        String toString();
    }
}
@interface AT1 {
}
@interface AT2 {
}

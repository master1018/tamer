public class MethodDecl extends Tester {
    public static void main(String[] args) {
        (new MethodDecl()).run();
    }
    private MethodDeclaration meth1 = null;             
    private MethodDeclaration meth2 = null;             
    protected void init() {
        meth1 = getMethod("m1");
        meth2 = getMethod("m2");
    }
    @Test(result="method")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        meth1.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitExecutableDeclaration(ExecutableDeclaration e) {
                res.add("executable");
            }
            public void visitMethodDeclaration(MethodDeclaration m) {
                res.add("method");
            }
            public void visitAnnotationTypeElementDeclaration(
                                        AnnotationTypeElementDeclaration a) {
                res.add("anno type element");
            }
        });
        return res;
    }
    @Test(result={"@AT1"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return meth1.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return meth1.getDocComment();
    }
    @Test(result={"private", "static", "strictfp"})
    Collection<Modifier> getModifiers() {
        return meth1.getModifiers();
    }
    @Test(result={"public", "abstract"})
    Collection<Modifier> getModifiersInterface() {
        for (TypeDeclaration t : thisClassDecl.getNestedTypes()) {
            for (MethodDeclaration m : t.getMethods()) {
                return m.getModifiers();
            }
        }
        throw new AssertionError();
    }
    @Test(result="MethodDecl.java")
    String getPosition() {
        return meth1.getPosition().file().getName();
    }
    @Test(result="m2")
    String getSimpleName() {
        return meth2.getSimpleName();
    }
    @Test(result="MethodDecl")
    TypeDeclaration getDeclaringType() {
        return meth1.getDeclaringType();
    }
    @Test(result={})
    Collection<TypeParameterDeclaration> getFormalTypeParameters1() {
        return meth1.getFormalTypeParameters();
    }
    @Test(result={"T", "N extends java.lang.Number"},
          ordered=true)
    Collection<TypeParameterDeclaration> getFormalTypeParameters2() {
        return meth2.getFormalTypeParameters();
    }
    @Test(result={})
    Collection<ParameterDeclaration> getParameters1() {
        return meth1.getParameters();
    }
    @Test(result={"N n", "java.lang.String[] ss"},
          ordered=true)
    Collection<ParameterDeclaration> getParameters2() {
        return meth2.getParameters();
    }
    @Test(result="true")
    boolean parameterEquals1() {
        ParameterDeclaration p1 =
            getMethod("m3").getParameters().iterator().next();
        ParameterDeclaration p2 =
            getMethod("m3").getParameters().iterator().next();
        return p1.equals(p2);
    }
    @Test(result="false")
    boolean parameterEquals2() {
        ParameterDeclaration p1 =
            getMethod("m3").getParameters().iterator().next();
        ParameterDeclaration p2 =
            getMethod("m4").getParameters().iterator().next();
        return p1.equals(p2);
    }
    @Test(result="true")
    boolean parameterHashCode() {
        ParameterDeclaration p1 =
            getMethod("m3").getParameters().iterator().next();
        ParameterDeclaration p2 =
            getMethod("m3").getParameters().iterator().next();
        return p1.hashCode() == p2.hashCode();
    }
    @Test(result={"java.lang.Throwable"})
    Collection<ReferenceType> getThrownTypes() {
        return meth2.getThrownTypes();
    }
    @Test(result="false")
    Boolean isVarArgs1() {
        return meth1.isVarArgs();
    }
    @Test(result="true")
    Boolean isVarArgs2() {
        return meth2.isVarArgs();
    }
    @Test(result="void")
    TypeMirror getReturnType1() {
        return meth1.getReturnType();
    }
    @Test(result="N")
    TypeMirror getReturnType2() {
        return meth2.getReturnType();
    }
    @Test(result="<T, N extends java.lang.Number> m2(N, java.lang.String...)")
    @Ignore("This is what it would be nice to see.")
    String toStringTest() {
        return meth2.toString();
    }
    @AT1
    private static strictfp void m1() {
    }
    private <T, N extends Number> N m2(N n, String... ss) throws Throwable {
        return null;
    }
    private void m3(String s) {
    }
    private void m4(String s) {
    }
    interface I {
        void m();
    }
}
@interface AT1 {
}

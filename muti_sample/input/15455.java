public class ConstructorDecl extends Tester {
    @AT1
    public ConstructorDecl() {
    }
    public static void main(String[] args) {
        (new ConstructorDecl()).run();
    }
    private ConstructorDeclaration ctor = null;         
    private ConstructorDeclaration ctorDef = null;      
    private ConstructorDeclaration ctorInner = null;    
    protected void init() {
        ctor = getAConstructor(thisClassDecl);
        ctorDef = getAConstructor((ClassDeclaration)
                                  env.getTypeDeclaration("C1"));
        ctorInner = getAConstructor((ClassDeclaration)
                                    env.getTypeDeclaration("C1.C2"));
    }
    private ConstructorDeclaration getAConstructor(ClassDeclaration c) {
        return c.getConstructors().iterator().next();
    }
    @Test(result="constructor")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        ctor.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitExecutableDeclaration(ExecutableDeclaration e) {
                res.add("executable");
            }
            public void visitConstructorDeclaration(ConstructorDeclaration c) {
                res.add("constructor");
            }
        });
        return res;
    }
    @Test(result={"@AT1"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return ctor.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return ctor.getDocComment();
    }
    @Test(result={"public"})
    Collection<Modifier> getModifiers() {
        return ctor.getModifiers();
    }
    @Test(result="ConstructorDecl.java")
    String getPosition() {
        return ctor.getPosition().file().getName();
    }
    @Test(result="ConstructorDecl.java")
    String getPositionDefault() {
        return ctorDef.getPosition().file().getName();
    }
    @Test(result="ConstructorDecl")
    String getSimpleName() {
        return ctor.getSimpleName();
    }
    @Test(result="C2")
    String getSimpleNameInner() {
        return ctorInner.getSimpleName();
    }
    @Test(result="ConstructorDecl")
    TypeDeclaration getDeclaringType() {
        return ctor.getDeclaringType();
    }
    @Test(result={})
    Collection<TypeParameterDeclaration> getFormalTypeParameters1() {
        return ctor.getFormalTypeParameters();
    }
    @Test(result={"N extends java.lang.Number"})
    Collection<TypeParameterDeclaration> getFormalTypeParameters2() {
        return ctorInner.getFormalTypeParameters();
    }
    @Test(result={})
    Collection<ParameterDeclaration> getParameters1() {
        return ctor.getParameters();
    }
    @Test(result={"N n1", "N n2", "java.lang.String[] ss"},
          ordered=true)
    Collection<ParameterDeclaration> getParameters2() {
        return ctorInner.getParameters();
    }
    @Test(result={"java.lang.Throwable"})
    Collection<ReferenceType> getThrownTypes() {
        return ctorInner.getThrownTypes();
    }
    @Test(result="false")
    Boolean isVarArgs1() {
        return ctor.isVarArgs();
    }
    @Test(result="true")
    Boolean isVarArgs2() {
        return ctorInner.isVarArgs();
    }
    @Test(result="<N extends java.lang.Number> C2(N, N, String...)")
    @Ignore("This is what it would be nice to see.")
    String toStringTest() {
        return ctorInner.toString();
    }
}
class C1 {
    class C2 {
        <N extends Number> C2(N n1, N n2, String... ss) throws Throwable {
        }
    }
}
@interface AT1 {
}

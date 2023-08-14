public class AnnoTypeElemDecl extends Tester {
    public static void main(String[] args) {
        (new AnnoTypeElemDecl()).run();
    }
    private AnnotationTypeElementDeclaration elem1 = null;      
    private AnnotationTypeElementDeclaration elem2 = null;      
    private AnnotationTypeElementDeclaration elem3 = null;      
    protected void init() {
        for (TypeDeclaration at : thisClassDecl.getNestedTypes()) {
            for (MethodDeclaration meth : at.getMethods()) {
                AnnotationTypeElementDeclaration elem =
                    (AnnotationTypeElementDeclaration) meth;
                if (elem.getSimpleName().equals("s")) {
                    elem1 = elem;       
                } else if (elem.getSimpleName().equals("i")) {
                    elem2 = elem;       
                } else {
                    elem3 = elem;       
                }
            }
        }
    }
    @Test(result="anno type element")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        elem1.accept(new SimpleDeclarationVisitor() {
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
    @Test(result={"@AnnoTypeElemDecl.AT2"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return elem1.getAnnotationMirrors();
    }
    @Test(result=" Sed Quis custodiet ipsos custodes?\n")
    String getDocComment() {
        return elem1.getDocComment();
    }
    @Test(result={"public", "abstract"})
    Collection<Modifier> getModifiers() {
        return elem1.getModifiers();
    }
    @Test(result="AnnoTypeElemDecl.java")
    String getPosition() {
        return elem1.getPosition().file().getName();
    }
    @Test(result="s")
    String getSimpleName() {
        return elem1.getSimpleName();
    }
    @Test(result="AnnoTypeElemDecl.AT1")
    TypeDeclaration getDeclaringType() {
        return elem1.getDeclaringType();
    }
    @Test(result={})
    Collection<TypeParameterDeclaration> getFormalTypeParameters() {
        return elem1.getFormalTypeParameters();
    }
    @Test(result={})
    Collection<ParameterDeclaration> getParameters() {
        return elem1.getParameters();
    }
    @Test(result={})
    Collection<ReferenceType> getThrownTypes() {
        return elem1.getThrownTypes();
    }
    @Test(result="false")
    Boolean isVarArgs() {
        return elem1.isVarArgs();
    }
    @Test(result="\"default\"")
    AnnotationValue getDefaultValue1() {
        return elem1.getDefaultValue();
    }
    @Test(result="null")
    AnnotationValue getDefaultValue2() {
        return elem2.getDefaultValue();
    }
    @Test(result="false")
    Boolean getDefaultValue3() {
        return (Boolean) elem3.getDefaultValue().getValue();
    }
    @Test(result="s()")
    String toStringTest() {
        return elem1.toString();
    }
    @interface AT1 {
        @AT2
        String s() default "default";
        int i();
        boolean b() default false;
    }
    @interface AT2 {
    }
}

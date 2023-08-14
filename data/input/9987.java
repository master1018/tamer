public class AnnoTypeDecl extends Tester {
    public static void main(String[] args) {
        (new AnnoTypeDecl()).run();
    }
    private AnnotationTypeDeclaration at;
    protected void init() {
        at = (AnnotationTypeDeclaration) env.getTypeDeclaration("AT");
    }
    @Test(result="annotation type")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        at.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitClassDeclaration(ClassDeclaration c) {
                res.add("class");
            }
            public void visitInterfaceDeclaration(InterfaceDeclaration i) {
                res.add("interface");
            }
            public void visitAnnotationTypeDeclaration(
                                                AnnotationTypeDeclaration a) {
                res.add("annotation type");
            }
        });
        return res;
    }
    @Test(result={"s()"})
    Collection<AnnotationTypeElementDeclaration> getMethods() {
        return at.getMethods();
    }
}
@interface AT {
    String s();
}

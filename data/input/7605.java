public class EnumDecl extends Tester {
    public static void main(String[] args) {
        (new EnumDecl()).run();
    }
    private EnumDeclaration eDecl;
    protected void init() {
        eDecl = (EnumDeclaration) env.getTypeDeclaration("E");
    }
    @Test(result="enum")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        eDecl.accept(new SimpleDeclarationVisitor() {
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
    @Test(result={"E(java.lang.String)"})
    Collection<ConstructorDeclaration> getConstructors() {
        return eDecl.getConstructors();
    }
    @Test(result={"java.lang.String color"})
    Collection<ParameterDeclaration> getConstructorParams() {
        return eDecl.getConstructors().iterator().next().getParameters();
    }
    @Test(result={"values()", "valueOf(java.lang.String)"})
    Collection<MethodDeclaration> getMethods() {
        return eDecl.getMethods();
    }
    @Test(result={"java.lang.String name"})
    Collection<ParameterDeclaration> getMethodParams() {
        for (MethodDeclaration m : eDecl.getMethods()) {
            if (m.getSimpleName().equals("valueOf")) {
                return m.getParameters();
            }
        }
        throw new AssertionError();
    }
    @Test(result={"stop", "slow", "go"})
    Collection<EnumConstantDeclaration> getEnumConstants() {
        return eDecl.getEnumConstants();
    }
}
enum E {
    stop("red"),
    slow("amber"),
    go("green");
    private String color;
    E(String color) {
        this.color = color;
    }
}

public class PackageDecl extends Tester {
    public PackageDecl() {
        super(System.getProperty("test.src", ".") + File.separator +
              "pkg1" + File.separator + "package-info.java");
    }
    public static void main(String[] args) {
        (new PackageDecl()).run();
    }
    private PackageDeclaration pkg1 = null;             
    private PackageDeclaration pkg2 = null;             
    protected void init() {
        pkg1 = env.getPackage("pkg1");
        pkg2 = env.getPackage("pkg1.pkg2");
    }
    @Test(result="package")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        pkg1.accept(new SimpleDeclarationVisitor() {
            public void visitTypeDeclaration(TypeDeclaration t) {
                res.add("type");
            }
            public void visitPackageDeclaration(PackageDeclaration p) {
                res.add("package");
            }
        });
        return res;
    }
    @Test(result={"@pkg1.AnAnnoType"})
    Collection<AnnotationMirror> getAnnotationMirrors() {
        return pkg1.getAnnotationMirrors();
    }
    @Test(result=" Herein lieth the package comment.\n" +
                 " A doc comment it be, and wonderous to behold.\n")
    String getDocCommentFromPackageInfoFile() {
        return pkg1.getDocComment();
    }
    @Test(result="\nHerein lieth the package comment.\n" +
                 "An HTML file it be, and wonderous to behold.\n\n")
    @Ignore("Not yet supported")
    String getDocCommentFromHtmlFile() {
        return pkg2.getDocComment();
    }
    @Test(result={})
    Collection<Modifier> getModifiers() {
        return pkg1.getModifiers();
    }
    @Test(result="null")
    SourcePosition getPosition() {
        return thisClassDecl.getPackage().getPosition();
    }
    @Test(result="package-info.java")
    String getPositionFromPackageInfoFile() {
        return pkg1.getPosition().file().getName();
    }
    @Test(result="pkg1/pkg2/package.html")
    @Ignore("Not yet supported")
    String getPositionFromHtmlFile() {
        return pkg2.getPosition().file().getName()
                                            .replace(File.separatorChar, '/');
    }
    @Test(result="pkg1")
    String getSimpleName1() {
        return pkg1.getSimpleName();
    }
    @Test(result="pkg2")
    String getSimpleName2() {
        return pkg2.getSimpleName();
    }
    @Test(result="pkg1.AnAnnoType")
    Collection<AnnotationTypeDeclaration> getAnnotationTypes() {
        return pkg1.getAnnotationTypes();
    }
    @Test(result={"pkg1.AClass", "pkg1.AnEnum"})
    Collection<ClassDeclaration> getClasses() {
        return pkg1.getClasses();
    }
    @Test(result="pkg1.AnEnum")
    Collection<EnumDeclaration> getEnums() {
        return pkg1.getEnums();
    }
    @Test(result={"pkg1.AnInterface", "pkg1.AnAnnoType"})
    Collection<InterfaceDeclaration> getInterfaces() {
        return pkg1.getInterfaces();
    }
    @Test(result="pkg1")
    String getQualifiedName1() {
        return pkg1.getQualifiedName();
    }
    @Test(result="pkg1.pkg2")
    String getQualifiedName2() {
        return pkg2.getQualifiedName();
    }
}

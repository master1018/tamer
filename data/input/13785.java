public class AnnoTyp extends Tester {
    public static void main(String[] args) {
        (new AnnoTyp()).run();
    }
    @interface AT {
    }
    private AnnotationType at;  
    @AT
    protected void init() {
        at = getAnno("init", "AnnoTyp.AT").getAnnotationType();
    }
    @Test(result="anno type")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        at.accept(new SimpleTypeVisitor() {
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
            public void visitClassType(ClassType t) {
                res.add("class");
            }
            public void visitInterfaceType(InterfaceType t) {
                res.add("interface");
            }
            public void visitAnnotationType(AnnotationType t) {
                res.add("anno type");
            }
        });
        return res;
    }
    @Test(result="AnnoTyp.AT")
    AnnotationTypeDeclaration getDeclaration() {
        return at.getDeclaration();
    }
}

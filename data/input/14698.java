public class EnumTyp extends Tester {
    public static void main(String[] args) {
        (new EnumTyp()).run();
    }
    enum Suit {
        CIVIL,
        CRIMINAL
    }
    private Suit s;
    private EnumType e;         
    protected void init() {
        e = (EnumType) getField("s").getType();
    }
    @Test(result="enum")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        e.accept(new SimpleTypeVisitor() {
            public void visitTypeMirror(TypeMirror t) {
                res.add("type");
            }
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
            public void visitClassType(ClassType t) {
                res.add("class");
            }
            public void visitEnumType(EnumType t) {
                res.add("enum");
            }
            public void visitInterfaceType(InterfaceType t) {
                res.add("interface");
            }
        });
        return res;
    }
    @Test(result="EnumTyp.Suit")
    EnumDeclaration getDeclaration() {
        return e.getDeclaration();
    }
}

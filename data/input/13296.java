public class PrimitiveTyp extends Tester {
    public static void main(String[] args) {
        (new PrimitiveTyp()).run();
    }
    private boolean b;
    private PrimitiveType prim;         
    protected void init() {
        prim = (PrimitiveType) getField("b").getType();
    }
    @Test(result="primitive")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        prim.accept(new SimpleTypeVisitor() {
            public void visitTypeMirror(TypeMirror t) {
                res.add("type");
            }
            public void visitPrimitiveType(PrimitiveType t) {
                res.add("primitive");
            }
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
        });
        return res;
    }
    @Test(result="boolean")
    String toStringTest() {
        return prim.toString();
    }
    @Test(result="BOOLEAN")
    PrimitiveType.Kind getKind() {
        return prim.getKind();
    }
}

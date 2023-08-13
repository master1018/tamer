public class ArrayTyp extends Tester {
    public static void main(String[] args) {
        (new ArrayTyp()).run();
    }
    private boolean[] bs;
    private String[][] bss;
    private ArrayType arr;              
    private ArrayType arrarr;           
    protected void init() {
        arr = (ArrayType) getField("bs").getType();
        arrarr = (ArrayType) getField("bss").getType();
    }
    @Test(result="array")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        arr.accept(new SimpleTypeVisitor() {
            public void visitTypeMirror(TypeMirror t) {
                res.add("type");
            }
            public void visitArrayType(ArrayType t) {
                res.add("array");
            }
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
        });
        return res;
    }
    @Test(result="boolean[]")
    String toStringTest() {
        return arr.toString();
    }
    @Test(result="java.lang.String[][]")
    String toStringTestMulti() {
        return arrarr.toString();
    }
    @Test(result="boolean")
    TypeMirror getComponentType() {
        return (PrimitiveType) arr.getComponentType();
    }
    @Test(result="java.lang.String[]")
    TypeMirror getComponentTypeMulti() {
        return (ArrayType) arrarr.getComponentType();
    }
}

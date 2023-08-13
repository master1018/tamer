public class WildcardTyp extends Tester {
    public static void main(String[] args) {
        (new WildcardTyp()).run();
    }
    interface G<T> {
    }
    interface G1<N extends Number & Runnable> {
    }
    interface G2<T extends G2<T>> {
    }
    private G<?> f0;                    
    private G<? extends Number> f1;     
    private G<? super Number> f2;       
    private G<? extends Object> f3;     
    private G1<?> f4;   
    private G2<?> f5;   
    private static final int NUMTYPES = 6;
    private WildcardType[] t = new WildcardType[NUMTYPES];
    protected void init() {
        for (int i = 0; i < t.length; i++) {
            DeclaredType type = (DeclaredType) getField("f"+i).getType();
            t[i] = (WildcardType)
                type.getActualTypeArguments().iterator().next();
        }
    }
    private WildcardType wildcardFor(String field) {
        DeclaredType d = (DeclaredType) getField(field).getType();
        return (WildcardType) d.getActualTypeArguments().iterator().next();
    }
    @Test(result="wild thing")
    Collection<String> accept() {
        final Collection<String> res = new ArrayList<String>();
        t[0].accept(new SimpleTypeVisitor() {
            public void visitTypeMirror(TypeMirror t) {
                res.add("type");
            }
            public void visitReferenceType(ReferenceType t) {
                res.add("ref type");
            }
            public void visitWildcardType(WildcardType t) {
                res.add("wild thing");
            }
        });
        return res;
    }
    @Test(result={
                "?",
                "? extends java.lang.Number",
                "? super java.lang.Number",
                "? extends java.lang.Object",
                "?",
                "?"
          },
          ordered=true)
    Collection<String> toStringTests() {
        Collection<String> res = new ArrayList<String>();
        for (WildcardType w : t) {
            res.add(w.toString());
        }
        return res;
    }
    @Test(result={
                "null",
                "null",
                "java.lang.Number",
                "null",
                "null",
                "null"
          },
          ordered=true)
    Collection<ReferenceType> getLowerBounds() {
        Collection<ReferenceType> res = new ArrayList<ReferenceType>();
        for (WildcardType w : t) {
            Collection<ReferenceType> bounds = w.getLowerBounds();
            int num = bounds.size();
            if (num > 1) {
                throw new AssertionError("Bounds abound");
            }
            res.add((num > 0) ? bounds.iterator().next() : null);
        }
        return res;
    }
    @Test(result={
                "null",
                "java.lang.Number",
                "null",
                "java.lang.Object",
                "null",
                "null"
          },
          ordered=true)
    Collection<ReferenceType> getUpperBounds() {
        Collection<ReferenceType> res = new ArrayList<ReferenceType>();
        for (WildcardType w : t) {
            Collection<ReferenceType> bounds = w.getUpperBounds();
            int num = bounds.size();
            if (num > 1) {
                throw new AssertionError("Bounds abound");
            }
            res.add((num > 0) ? bounds.iterator().next() : null);
        }
        return res;
    }
}

public class TypeKindVisitor7<R, P> extends TypeKindVisitor6<R, P> {
    protected TypeKindVisitor7() {
        super(null);
    }
    protected TypeKindVisitor7(R defaultValue) {
        super(defaultValue);
    }
    @Override
    public R visitUnion(UnionType t, P p) {
        return defaultAction(t, p);
    }
}

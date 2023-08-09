public class SimpleTypeVisitor7<R, P> extends SimpleTypeVisitor6<R, P> {
    protected SimpleTypeVisitor7(){
        super(null);
    }
    protected SimpleTypeVisitor7(R defaultValue){
        super(defaultValue);
    }
    @Override
    public R visitUnion(UnionType t, P p) {
        return defaultAction(t, p);
    }
}

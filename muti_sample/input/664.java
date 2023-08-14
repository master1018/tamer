public class SimpleElementVisitor7<R, P> extends SimpleElementVisitor6<R, P> {
    protected SimpleElementVisitor7(){
        super(null);
    }
    protected SimpleElementVisitor7(R defaultValue){
        super(defaultValue);
    }
    @Override
    public R visitVariable(VariableElement e, P p) {
        return defaultAction(e, p);
    }
}

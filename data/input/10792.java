public class ElementScanner7<R, P> extends ElementScanner6<R, P> {
    protected ElementScanner7(){
        super(null);
    }
    protected ElementScanner7(R defaultValue){
        super(defaultValue);
    }
    @Override
    public R visitVariable(VariableElement e, P p) {
        return scan(e.getEnclosedElements(), p);
    }
}

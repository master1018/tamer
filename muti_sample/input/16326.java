public class Future implements Closure {
    private boolean evaluated ;
    private Closure closure ;
    private Object value ;
    public Future( Closure value )
    {
        this.evaluated = false ;
        this.closure = (Closure)value ;
        this.value = null ;
    }
    public synchronized Object evaluate()
    {
        if (!evaluated) {
            evaluated = true ;
            value = closure.evaluate() ;
        }
        return value ;
    }
}

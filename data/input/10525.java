public abstract class ClosureFactory {
    private ClosureFactory() {}
    public static Closure makeConstant( Object value )
    {
        return new Constant( value ) ;
    }
    public static Closure makeFuture( Closure value )
    {
        return new Future( value ) ;
    }
}

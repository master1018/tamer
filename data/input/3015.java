public class LocalResolverImpl implements LocalResolver {
    java.util.Map nameToClosure = new java.util.HashMap() ;
    public synchronized org.omg.CORBA.Object resolve( String name )
    {
        Closure cl = (Closure)nameToClosure.get( name ) ;
        if (cl == null)
            return null ;
        return (org.omg.CORBA.Object)(cl.evaluate()) ;
    }
    public synchronized java.util.Set list()
    {
        return nameToClosure.keySet() ;
    }
    public synchronized void register( String name, Closure closure )
    {
        nameToClosure.put( name, closure ) ;
    }
}

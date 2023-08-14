public class ZeroPortPolicy extends LocalObject implements Policy
{
    private static ZeroPortPolicy policy = new ZeroPortPolicy( true ) ;
    private boolean flag = true ;
    private ZeroPortPolicy( boolean type )
    {
        this.flag = type ;
    }
    public String toString()
    {
        return "ZeroPortPolicy[" + flag + "]" ;
    }
    public boolean forceZeroPort()
    {
        return flag ;
    }
    public synchronized static ZeroPortPolicy getPolicy()
    {
        return policy ;
    }
    public int policy_type ()
    {
        return ORBConstants.ZERO_PORT_POLICY ;
    }
    public org.omg.CORBA.Policy copy ()
    {
        return this ;
    }
    public void destroy ()
    {
    }
}

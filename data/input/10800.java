public final class IIOPAddressClosureImpl extends IIOPAddressBase
{
    private Closure host;
    private Closure port;
    public IIOPAddressClosureImpl( Closure host, Closure port )
    {
        this.host = host ;
        this.port = port ;
    }
    public String getHost()
    {
        return (String)(host.evaluate()) ;
    }
    public int getPort()
    {
        Integer value = (Integer)(port.evaluate()) ;
        return value.intValue() ;
    }
}

public class AlternateIIOPAddressComponentImpl extends TaggedComponentBase
    implements AlternateIIOPAddressComponent
{
    private IIOPAddress addr ;
    public boolean equals( Object obj )
    {
        if (!(obj instanceof AlternateIIOPAddressComponentImpl))
            return false ;
        AlternateIIOPAddressComponentImpl other =
            (AlternateIIOPAddressComponentImpl)obj ;
        return addr.equals( other.addr ) ;
    }
    public int hashCode()
    {
        return addr.hashCode() ;
    }
    public String toString()
    {
        return "AlternateIIOPAddressComponentImpl[addr=" + addr + "]" ;
    }
    public AlternateIIOPAddressComponentImpl( IIOPAddress addr )
    {
        this.addr = addr ;
    }
    public IIOPAddress getAddress()
    {
        return addr ;
    }
    public void writeContents(OutputStream os)
    {
        addr.write( os ) ;
    }
    public int getId()
    {
        return TAG_ALTERNATE_IIOP_ADDRESS.value ; 
    }
}

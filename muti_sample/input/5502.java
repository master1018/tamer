abstract class IIOPAddressBase implements IIOPAddress
{
    protected short intToShort( int value )
    {
        if (value > 32767)
            return (short)(value - 65536) ;
        return (short)value ;
    }
    protected int shortToInt( short value )
    {
        if (value < 0)
            return value + 65536 ;
        return value ;
    }
    public void write( OutputStream os )
    {
        os.write_string( getHost() ) ;
        int port = getPort() ;
        os.write_short( intToShort( port ) ) ;
    }
    public boolean equals( Object obj )
    {
        if (!(obj instanceof IIOPAddress))
            return false ;
        IIOPAddress other = (IIOPAddress)obj ;
        return getHost().equals(other.getHost()) &&
            (getPort() == other.getPort()) ;
    }
    public int hashCode()
    {
        return getHost().hashCode() ^ getPort() ;
    }
    public String toString()
    {
        return "IIOPAddress[" + getHost() + "," + getPort() + "]" ;
    }
}

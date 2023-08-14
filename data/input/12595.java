public class IIOPEndpointInfo
{
    private int major, minor;
    private String host;
    private int port;
    IIOPEndpointInfo( ) {
        major = ORBConstants.DEFAULT_INS_GIOP_MAJOR_VERSION;
        minor = ORBConstants.DEFAULT_INS_GIOP_MINOR_VERSION;
        host = ORBConstants.DEFAULT_INS_HOST;
        port = ORBConstants.DEFAULT_INS_PORT;
    }
    public void setHost( String theHost ) {
        host = theHost;
    }
    public String getHost( ) {
        return host;
    }
    public void setPort( int thePort ) {
        port = thePort;
    }
    public int getPort( ) {
        return port;
    }
    public void setVersion( int theMajor, int theMinor ) {
        major = theMajor;
        minor = theMinor;
    }
    public int getMajor( ) {
        return major;
    }
    public int getMinor( ) {
        return minor;
    }
    public void dump( ) {
        System.out.println( " Major -> " + major + " Minor -> " + minor );
        System.out.println( "host -> " + host );
        System.out.println( "port -> " + port );
    }
}

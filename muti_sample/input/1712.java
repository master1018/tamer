public abstract class INSURLBase implements INSURL {
    protected boolean rirFlag = false ;
    protected java.util.ArrayList theEndpointInfo = null ;
    protected String theKeyString = "NameService" ;
    protected String theStringifiedName = null ;
    public boolean getRIRFlag( ) {
        return rirFlag;
    }
    public java.util.List getEndpointInfo( ) {
        return theEndpointInfo;
    }
    public String getKeyString( ) {
        return theKeyString;
    }
    public String getStringifiedName( ) {
        return theStringifiedName;
    }
    public abstract boolean isCorbanameURL( );
    public void dPrint( ) {
        System.out.println( "URL Dump..." );
        System.out.println( "Key String = " + getKeyString( ) );
        System.out.println( "RIR Flag = " + getRIRFlag( ) );
        System.out.println( "isCorbanameURL = " + isCorbanameURL() );
        for( int i = 0; i < theEndpointInfo.size( ); i++ ) {
            ((IIOPEndpointInfo) theEndpointInfo.get( i )).dump( );
        }
        if( isCorbanameURL( ) ) {
            System.out.println( "Stringified Name = " + getStringifiedName() );
        }
    }
}

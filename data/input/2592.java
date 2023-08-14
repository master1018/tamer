public class CorbanameURL extends INSURLBase
{
    private static NamingSystemException wrapper =
        NamingSystemException.get( CORBALogDomains.NAMING ) ;
    public CorbanameURL( String aURL ) {
        String url = aURL;
        try {
            url = Utility.cleanEscapes( url );
        } catch( Exception e ) {
            badAddress( e );
        }
        int delimiterIndex = url.indexOf( '#' );
        String corbalocString = null;
        if( delimiterIndex != -1 ) {
                corbalocString = "corbaloc:" +
                    url.substring( 0, delimiterIndex ) + "/";
        } else {
            corbalocString = "corbaloc:" + url.substring( 0, url.length() );
            if( corbalocString.endsWith( "/" ) != true ) {
                corbalocString = corbalocString + "/";
            }
        }
        try {
            INSURL insURL =
                INSURLHandler.getINSURLHandler().parseURL( corbalocString );
            copyINSURL( insURL );
            if((delimiterIndex > -1) &&
               (delimiterIndex < (aURL.length() - 1)))
            {
                int start = delimiterIndex + 1 ;
                String result = url.substring(start) ;
                theStringifiedName = result ;
            }
        } catch( Exception e ) {
            badAddress( e );
        }
    }
    private void badAddress( java.lang.Throwable e )
        throws org.omg.CORBA.BAD_PARAM
    {
        throw wrapper.insBadAddress( e ) ;
    }
    private void copyINSURL( INSURL url ) {
        rirFlag = url.getRIRFlag( );
        theEndpointInfo = (java.util.ArrayList) url.getEndpointInfo( );
        theKeyString = url.getKeyString( );
        theStringifiedName = url.getStringifiedName( );
    }
    public boolean isCorbanameURL( ) {
        return true;
    }
}

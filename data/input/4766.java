public class INSURLHandler {
    private static INSURLHandler insURLHandler = null;
    private static final int CORBALOC_PREFIX_LENGTH = 9;
    private static final int CORBANAME_PREFIX_LENGTH = 10;
    private INSURLHandler( ) {
    }
    public synchronized static INSURLHandler getINSURLHandler( ) {
        if( insURLHandler == null ) {
            insURLHandler = new INSURLHandler( );
        }
        return insURLHandler;
    }
    public INSURL parseURL( String aUrl ) {
        String url = aUrl;
        if ( url.startsWith( "corbaloc:" ) == true ) {
            return new CorbalocURL( url.substring( CORBALOC_PREFIX_LENGTH ) );
        } else if ( url.startsWith ( "corbaname:" ) == true ) {
            return new CorbanameURL( url.substring( CORBANAME_PREFIX_LENGTH ) );
        }
        return null;
    }
}

public class CorbalocURL extends INSURLBase
{
    static NamingSystemException wrapper = NamingSystemException.get(
        CORBALogDomains.NAMING_READ ) ;
    public CorbalocURL( String aURL ) {
        String url = aURL;
        if( url != null ) {
            try {
                url = Utility.cleanEscapes( url );
            } catch( Exception e ) {
                badAddress( e );
            }
            int endIndex = url.indexOf( '/' );
            if( endIndex == -1 ) {
                endIndex = url.length();
            }
            if( endIndex == 0 )  {
                badAddress( null );
            }
            StringTokenizer endpoints = new StringTokenizer(
                url.substring( 0, endIndex ), "," );
            while( endpoints.hasMoreTokens( ) ) {
                String endpointInfo = endpoints.nextToken();
                IIOPEndpointInfo iiopEndpointInfo = null;
                if( endpointInfo.startsWith( "iiop:" ) ) {
                    iiopEndpointInfo = handleIIOPColon( endpointInfo );
                } else if( endpointInfo.startsWith( "rir:" ) ) {
                    handleRIRColon( endpointInfo );
                    rirFlag = true;
                } else if( endpointInfo.startsWith( ":" ) ) {
                    iiopEndpointInfo = handleColon( endpointInfo );
                } else {
                    badAddress( null );
                }
                if ( rirFlag == false ) {
                    if( theEndpointInfo == null ) {
                        theEndpointInfo = new java.util.ArrayList( );
                    }
                    theEndpointInfo.add( iiopEndpointInfo );
                }
            }
            if( url.length() > (endIndex + 1) ) {
                theKeyString = url.substring( endIndex + 1 );
            }
        }
    }
    private void badAddress( java.lang.Throwable e )
    {
        throw wrapper.insBadAddress( e ) ;
    }
    private IIOPEndpointInfo handleIIOPColon( String iiopInfo )
    {
         iiopInfo = iiopInfo.substring( NamingConstants.IIOP_LENGTH  );
         return handleColon( iiopInfo );
    }
    private IIOPEndpointInfo handleColon( String iiopInfo ) {
         iiopInfo = iiopInfo.substring( 1 );
         String hostandport = iiopInfo;
         StringTokenizer tokenizer = new StringTokenizer( iiopInfo, "@" );
         IIOPEndpointInfo iiopEndpointInfo = new IIOPEndpointInfo( );
         int tokenCount = tokenizer.countTokens( );
         if( ( tokenCount == 0 )
           ||( tokenCount > 2 ))
         {
             badAddress( null );
         }
         if( tokenCount == 2 ) {
            String version     = tokenizer.nextToken( );
            int dot = version.indexOf('.');
            if (dot == -1) {
                badAddress( null );
            }
            try {
                iiopEndpointInfo.setVersion(
                    Integer.parseInt( version.substring( 0, dot )),
                    Integer.parseInt( version.substring(dot+1)) );
                hostandport = tokenizer.nextToken( );
            } catch( Throwable e ) {
                badAddress( e );
            }
         }
         try {
           int squareBracketBeginIndex = hostandport.indexOf ( '[' );
           if( squareBracketBeginIndex != -1 ) {
               String ipv6Port = getIPV6Port( hostandport );
               if( ipv6Port != null ) {
                   iiopEndpointInfo.setPort( Integer.parseInt( ipv6Port ));
               }
               iiopEndpointInfo.setHost( getIPV6Host( hostandport ));
               return iiopEndpointInfo;
           }
           tokenizer = new StringTokenizer( hostandport, ":" );
           if( tokenizer.countTokens( ) == 2 ) {
               iiopEndpointInfo.setHost( tokenizer.nextToken( ) );
               iiopEndpointInfo.setPort( Integer.parseInt(
                   tokenizer.nextToken( )));
           } else {
               if( ( hostandport != null )
                 &&( hostandport.length() != 0 ) )
               {
                   iiopEndpointInfo.setHost( hostandport );
               }
           }
       } catch( Throwable e ) {
           badAddress( e );
       }
       Utility.validateGIOPVersion( iiopEndpointInfo );
       return iiopEndpointInfo;
    }
    private void handleRIRColon( String rirInfo )
    {
        if( rirInfo.length() != NamingConstants.RIRCOLON_LENGTH ) {
            badAddress( null );
        }
    }
     private String getIPV6Port( String endpointInfo )
     {
         int squareBracketEndIndex = endpointInfo.indexOf ( ']' );
         if( (squareBracketEndIndex + 1) != (endpointInfo.length( )) ) {
             if( endpointInfo.charAt( squareBracketEndIndex + 1 ) != ':' ) {
                  throw new RuntimeException(
                      "Host and Port is not separated by ':'" );
             }
             return endpointInfo.substring( squareBracketEndIndex + 2 );
         }
         return null;
     }
     private String getIPV6Host( String endpointInfo ) {
          int squareBracketEndIndex = endpointInfo.indexOf ( ']' );
          String ipv6Host = endpointInfo.substring( 1, squareBracketEndIndex  );
          return ipv6Host;
    }
    public boolean isCorbanameURL( ) {
        return false;
    }
}

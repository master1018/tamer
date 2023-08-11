public class HardCodedPassword_LocalControlFlow_259
{
    public  HardCodedPassword_LocalControlFlow_259()
    {
         byte inputBuffer[] = new byte[ 128 ];
         try
         {
             int byteCount = System.in.read( inputBuffer );
             if( byteCount <= 0 )
             {
               return;
             }
             int i = 1;
             switch ( i )
             {
                 case 1:
                     String s = new String( inputBuffer );
                     s = s.substring( 0, byteCount-2 );		    
                     if( ( s.equals( "admin" ) ) == true )
                     {
                         highlevel_authorized( s );
                     }
                 default:
                     break;
             }
         }
         catch ( IOException e )
         {
             final Logger logger = Logger.getAnonymousLogger();
             String exception = "Exception " + e;
             logger.warning( exception );
         }
     }
     static    int  highlevel_authorized( String parm )
     {
         return 1;
     }
     public static void main( String[] argv )
     {
         new HardCodedPassword_LocalControlFlow_259();
     }
 }

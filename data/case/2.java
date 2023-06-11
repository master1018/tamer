public class main
{
    public int fun1(String inputBuffer, int byteCount)
    {
        String s = new String( inputBuffer );
        s = s.substring( 0, byteCount-2 );		    
        if( ( s.equals( "admin" ) ) == true )
        {
            return 0;
        }
        return 1;
     }
        
     static int  highlevel_authorized( String parm )
     {
        parm = "";
     }
 }
 
public class CheckCaseInsensitiveEncAliases
{
  public static void main(String args[]) throws Exception
  {
    tryToEncode( "ANSI_X3.4-1968" );
    tryToEncode( "iso-ir-6" );
    tryToEncode( "ANSI_X3.4-1986" );
    tryToEncode( "ISO_646.irv:1991" );
    tryToEncode( "ASCII" );
    tryToEncode( "ascii" );
    tryToEncode( "Ascii" );
    tryToEncode( "Ascii7" );
    tryToEncode( "ascii7" );
    tryToEncode( "ISO646-US" );
    tryToEncode( "US-ASCII" );
    tryToEncode( "us-ascii" );
    tryToEncode( "US-Ascii" );
    tryToEncode( "us" );
    tryToEncode( "IBM367" );
    tryToEncode( "cp367" );
    tryToEncode( "csASCII" );
    tryToEncode( "Unicode" );
    tryToEncode( "UNICODE" );
    tryToEncode( "unicode" );
    tryToEncode( "Big5" );
    tryToEncode( "big5" );
    tryToEncode( "bIg5" );
    tryToEncode( "biG5" );
    tryToEncode( "bIG5" );
    tryToEncode( "Cp1252" );
    tryToEncode( "cp1252" );
    tryToEncode( "CP1252" );
    tryToEncode( "pck" );
    tryToEncode( "Pck" );
  }
  public static final String ENCODE_STRING = "Encode me";
  public static void tryToEncode( String encoding) throws Exception
  {
    try
    {
      byte[] bytes = ENCODE_STRING.getBytes( encoding );
      System.out.println( "Encoding \"" + encoding + "\" recognized" );
    }
    catch( UnsupportedEncodingException e )
    {
      throw new Exception("Encoding \"" + encoding + "\" NOT recognized");
    }
  }
}

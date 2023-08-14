public abstract class StringUtil {
    public static String toMixedCase( String str )
    {
        StringBuffer sbuf = new StringBuffer( str.length() ) ;
        boolean uppercaseNext = false ;
        for (int ctr=0; ctr<str.length(); ctr++) {
            char ch = str.charAt( ctr ) ;
            if (ch == '_') {
                uppercaseNext = true ;
            } else if (uppercaseNext) {
                sbuf.append( Character.toUpperCase( ch ) ) ;
                uppercaseNext = false ;
            } else {
                sbuf.append( Character.toLowerCase( ch ) ) ;
            }
        }
        return sbuf.toString() ;
    }
    public static int countArgs( String str )
    {
        int result = 0 ;
        for( int ctr = 0; ctr<str.length(); ctr++ )
            if (str.charAt(ctr) == '{')
                result++ ;
        return result ;
    }
}

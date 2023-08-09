public class NameModifierImpl implements NameModifier {
    private String prefix ;
    private String suffix ;
    public NameModifierImpl( )
    {
        this.prefix = null ;
        this.suffix = null ;
    }
    public NameModifierImpl( String prefix, String suffix )
    {
        this.prefix = prefix ;
        this.suffix = suffix ;
    }
    public NameModifierImpl( String pattern )
    {
        int first = pattern.indexOf( '%' ) ;
        int last  = pattern.lastIndexOf( '%' ) ;
        if (first != last)
            throw new IllegalArgumentException(
                Util.getMessage( "NameModifier.TooManyPercent" ) ) ;
        if (first == -1)
            throw new IllegalArgumentException(
                Util.getMessage( "NameModifier.NoPercent" ) ) ;
        for (int ctr = 0; ctr<pattern.length(); ctr++) {
            char ch = pattern.charAt( ctr ) ;
            if (invalidChar( ch, ctr==0 )) {
                char[] chars = new char[] { ch } ;
                throw new IllegalArgumentException(
                    Util.getMessage( "NameModifier.InvalidChar",
                        new String( chars )) ) ;
            }
        }
        prefix = pattern.substring( 0, first ) ;
        suffix = pattern.substring( first+1 ) ;
    }
    private boolean invalidChar( char ch, boolean isFirst )
    {
        if (('A'<=ch) && (ch<='Z'))
            return false ;
        else if (('a'<=ch) && (ch<='z'))
            return false ;
        else if (('0'<=ch) && (ch<='9'))
            return isFirst ;
        else if (ch=='%')
            return false ;
        else if (ch=='$')
            return false ;
        else if (ch=='_')
            return false ;
        else
            return true ;
    }
    public String makeName( String base )
    {
        StringBuffer sb = new StringBuffer() ;
        if (prefix != null)
            sb.append( prefix ) ;
        sb.append( base ) ;
        if (suffix != null)
            sb.append( suffix ) ;
        return sb.toString() ;
    }
}

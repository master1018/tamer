public class IndentingPrintWriter extends PrintWriter {
    private int level = 0 ;
    private int indentWidth = 4 ;
    private String indentString = "" ;
    public void printMsg( String msg, Object... data )
    {
        StringTokenizer st = new StringTokenizer( msg, "@", true ) ;
        StringBuffer result = new StringBuffer() ;
        String token = null ;
        int pos = 0;
        while (st.hasMoreTokens()) {
            token = st.nextToken() ;
            if (token.equals("@")) {
                if (pos < data.length) {
                    result.append( data[pos] );
                    ++pos;
                } else {
                    throw new Error( "List too short for message" ) ;
                }
            } else {
                result.append( token ) ;
            }
        }
        print( result ) ;
        println() ;
    }
    public IndentingPrintWriter (Writer out) {
        super( out, true ) ;
    }
    public IndentingPrintWriter(Writer out, boolean autoFlush) {
        super( out, autoFlush ) ;
    }
    public IndentingPrintWriter(OutputStream out) {
        super(out, true);
    }
    public IndentingPrintWriter(OutputStream out, boolean autoFlush) {
        super(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
    }
    public void setIndentWidth( int indentWidth )
    {
        this.indentWidth = indentWidth ;
        updateIndentString() ;
    }
    public void indent()
    {
        level++ ;
        updateIndentString() ;
    }
    public void undent()
    {
        if (level > 0) {
            level-- ;
            updateIndentString() ;
        }
    }
    private void updateIndentString()
    {
        int size = level * indentWidth ;
        StringBuffer sbuf = new StringBuffer( size ) ;
        for (int ctr = 0; ctr<size; ctr++ )
            sbuf.append( " " ) ;
        indentString = sbuf.toString() ;
    }
    public void println()
    {
        super.println() ;
        print( indentString ) ;
    }
}

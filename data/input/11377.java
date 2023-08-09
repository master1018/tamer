public class NameBase {
    private String name ;
    private String toStringName ;
    private String getClassName()
    {
        String fqn = this.getClass().getName() ;
        StringTokenizer st = new StringTokenizer( fqn, "." ) ;
        String token = st.nextToken() ;
        while (st.hasMoreTokens())
            token = st.nextToken() ;
        return token ;
    }
    private String getPreferredClassName()
    {
        if (this instanceof Action)
            return "Action" ;
        if (this instanceof State)
            return "State" ;
        if (this instanceof Guard)
            return "Guard" ;
        if (this instanceof Input)
            return "Input" ;
        return getClassName() ;
    }
    public NameBase( String name )
    {
        this.name = name ;
        toStringName = getPreferredClassName() + "[" + name + "]" ;
    }
    public String getName()
    {
        return name ;
    }
    public String toString() {
        return toStringName ;
    }
}

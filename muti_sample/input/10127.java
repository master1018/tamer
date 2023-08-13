public class CodeSetsComponentImpl extends TaggedComponentBase
    implements CodeSetsComponent
{
    CodeSetComponentInfo csci ;
    public boolean equals( Object obj )
    {
        if (!(obj instanceof CodeSetsComponentImpl))
            return false ;
        CodeSetsComponentImpl other = (CodeSetsComponentImpl)obj ;
        return csci.equals( other.csci ) ;
    }
    public int hashCode()
    {
        return csci.hashCode() ;
    }
    public String toString()
    {
        return "CodeSetsComponentImpl[csci=" + csci + "]" ;
    }
    public CodeSetsComponentImpl()
    {
        csci = new CodeSetComponentInfo() ;
    }
    public CodeSetsComponentImpl( InputStream is )
    {
        csci = new CodeSetComponentInfo() ;
        csci.read( (MarshalInputStream)is ) ;
    }
    public CodeSetsComponentImpl(com.sun.corba.se.spi.orb.ORB orb)
    {
        if (orb == null)
            csci = new CodeSetComponentInfo();
        else
            csci = orb.getORBData().getCodeSetComponentInfo();
    }
    public CodeSetComponentInfo getCodeSetComponentInfo()
    {
        return csci ;
    }
    public void writeContents(OutputStream os)
    {
        csci.write( (MarshalOutputStream)os ) ;
    }
    public int getId()
    {
        return TAG_CODE_SETS.value ; 
    }
}

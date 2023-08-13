public class Factories
{
  public GenFactory genFactory ()
  {
    return null;
  } 
  public SymtabFactory symtabFactory ()
  {
    return new DefaultSymtabFactory ();
  } 
  public com.sun.tools.corba.se.idl.constExpr.ExprFactory exprFactory ()
  {
    return new com.sun.tools.corba.se.idl.constExpr.DefaultExprFactory ();
  } 
  public Arguments arguments ()
  {
    return new Arguments ();
  } 
  public String[] languageKeywords ()
  {
    return null;
  } 
} 

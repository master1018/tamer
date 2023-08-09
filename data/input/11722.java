public class PrimitiveEntry extends SymtabEntry
{
  protected PrimitiveEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } 
  protected PrimitiveEntry (String name)
  {
    name (name);
    module ("");
    repositoryID (Util.emptyID);
  } 
  protected PrimitiveEntry (PrimitiveEntry that)
  {
    super (that);
  } 
  public Object clone ()
  {
    return new PrimitiveEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    primitiveGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return primitiveGen;
  } 
  static PrimitiveGen primitiveGen;
} 

public class PragmaEntry extends SymtabEntry
{
  protected PragmaEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } 
  protected PragmaEntry (SymtabEntry that)
  {
    super (that, new IDLID ());
    module (that.name ());
    name ("");
  } 
  protected PragmaEntry (PragmaEntry that)
  {
    super (that);
  } 
  public Object clone ()
  {
    return new PragmaEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    pragmaGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return pragmaGen;
  } 
  public String data ()
  {
    return _data;
  } 
  public void data (String newData)
  {
    _data = newData;
  } 
  static PragmaGen pragmaGen;
  private String _data = null;
} 

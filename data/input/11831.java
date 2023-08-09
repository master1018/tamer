public class StringEntry extends SymtabEntry
{
  protected StringEntry ()
  {
    super ();
    String override = (String)Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (Util.emptyID);
  } 
  protected StringEntry (StringEntry that)
  {
    super (that);
    _maxSize = that._maxSize;
  } 
  protected StringEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    module ("");
    String override = (String)Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (Util.emptyID);
  } 
  public Object clone ()
  {
    return new StringEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    stringGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return stringGen;
  } 
  public void maxSize (Expression expr)
  {
    _maxSize = expr;
  } 
  public Expression maxSize ()
  {
    return _maxSize;
  } 
  static StringGen  stringGen;
  private Expression _maxSize = null;
} 

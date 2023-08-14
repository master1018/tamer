public class IncludeEntry extends SymtabEntry
{
  protected IncludeEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } 
  protected IncludeEntry (SymtabEntry that)
  {
    super (that, new IDLID ());
    module (that.name ());
    name ("");
  } 
  protected IncludeEntry (IncludeEntry that)
  {
    super (that);
  } 
  public Object clone ()
  {
    return new IncludeEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    includeGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return includeGen;
  } 
  public void absFilename (String afn)
  {
    _absFilename = afn;
  }
  public String absFilename ()
  {
    return _absFilename;
  }
  public void addInclude (IncludeEntry entry)
  {
    includeList.addElement (entry);
  } 
  public Vector includes ()
  {
    return includeList;
  } 
  static  IncludeGen includeGen;
  private Vector     includeList = new Vector ();
  private String     _absFilename       = null;
} 

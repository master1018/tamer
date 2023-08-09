public class StructEntry extends SymtabEntry
{
  protected StructEntry ()
  {
    super ();
  } 
  protected StructEntry (StructEntry that)
  {
    super (that);
    if (!name ().equals (""))
    {
      module (module () + name ());
      name ("");
    }
    _members   = (Vector)that._members.clone ();
    _contained = (Vector)that._contained.clone ();
  } 
  protected StructEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new StructEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    structGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return structGen;
  } 
  public void addMember (TypedefEntry member)
  {
    _members.addElement (member);
  } 
  public Vector members ()
  {
    return _members;
  } 
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } 
  public Vector contained ()
  {
    return _contained;
  } 
  private Vector _members   = new Vector ();
  private Vector _contained = new Vector ();
  static StructGen structGen;
} 

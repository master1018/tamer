public class ModuleEntry extends SymtabEntry
{
  protected ModuleEntry ()
  {
    super ();
  }  
  protected ModuleEntry (ModuleEntry that)
  {
    super (that);
    _contained = (Vector)that._contained.clone ();
  } 
  protected ModuleEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new ModuleEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    moduleGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return moduleGen;
  } 
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } 
  public Vector contained ()
  {
    return _contained;
  } 
  private Vector _contained = new Vector ();
  static ModuleGen moduleGen;
} 

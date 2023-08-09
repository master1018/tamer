public class EnumEntry extends SymtabEntry
{
  protected EnumEntry ()
  {
    super ();
  } 
  protected EnumEntry (EnumEntry that)
  {
    super (that);
    _elements = (Vector)that._elements.clone ();
  } 
  protected EnumEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new EnumEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    enumGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return enumGen;
  } 
  public void addElement (String element)
  {
    _elements.addElement (element);
  } 
  public Vector elements ()
  {
    return _elements;
  } 
  static  EnumGen enumGen;
  private Vector  _elements = new Vector ();
} 

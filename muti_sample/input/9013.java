public class AttributeEntry extends MethodEntry
{
  protected AttributeEntry ()
  {
    super ();
  } 
  protected AttributeEntry (AttributeEntry that)
  {
    super (that);
    _readOnly  = that._readOnly;
  } 
  protected AttributeEntry (InterfaceEntry that, IDLID clone)
  {
    super (that, clone);
  } 
  public Object clone ()
  {
    return new AttributeEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    attributeGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return attributeGen;
  } 
  public boolean readOnly ()
  {
    return _readOnly;
  } 
  public void readOnly (boolean readOnly)
  {
    _readOnly = readOnly;
  } 
  static AttributeGen attributeGen;
  public boolean      _readOnly = false;
} 

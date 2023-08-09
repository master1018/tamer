public class ConstEntry extends SymtabEntry
{
  protected ConstEntry ()
  {
    super ();
  } 
  protected ConstEntry (ConstEntry that)
  {
    super (that);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
    _value = that._value;
  } 
  protected ConstEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new ConstEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    constGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return constGen;
  } 
  public Expression value ()
  {
    return _value;
  } 
  public void value (Expression newValue)
  {
    _value = newValue;
  } 
  static ConstGen    constGen;
  private Expression _value = null;
} 

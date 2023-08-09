public class TypedefEntry extends SymtabEntry
{
  protected TypedefEntry ()
  {
    super ();
  } 
  protected TypedefEntry (TypedefEntry that)
  {
    super (that);
    _arrayInfo = (Vector)that._arrayInfo.clone ();
  } 
  protected TypedefEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Vector arrayInfo ()
  {
    return _arrayInfo;
  } 
  public void addArrayInfo (Expression e)
  {
    _arrayInfo.addElement (e);
  } 
  public Object clone ()
  {
    return new TypedefEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    typedefGen.generate (symbolTable, this, stream);
  } 
  public boolean isReferencable()
  {
    return type().isReferencable() ;
  }
  public void isReferencable( boolean value )
  {
  }
  public Generator generator ()
  {
    return typedefGen;
  } 
  private Vector _arrayInfo = new Vector ();
  static  TypedefGen typedefGen;
} 

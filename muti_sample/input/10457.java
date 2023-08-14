public class ParameterEntry extends SymtabEntry
{
  public static final int In    = 0,
                          Inout = 1,
                          Out   = 2;
  protected ParameterEntry ()
  {
    super ();
  } 
  protected ParameterEntry (ParameterEntry that)
  {
    super (that);
    _passType = that._passType;
  } 
  protected ParameterEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new ParameterEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    parameterGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return parameterGen;
  } 
  public void passType (int passType)
  {
    if (passType >= In && passType <= Out)
      _passType = passType;
  } 
  public int passType ()
  {
    return _passType;
  } 
  private int _passType = In;
  static ParameterGen parameterGen;
} 

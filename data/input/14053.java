public class MethodEntry extends SymtabEntry
{
  protected MethodEntry ()
  {
    super ();
  } 
  protected MethodEntry (MethodEntry that)
  {
    super (that);
    _exceptionNames = (Vector)that._exceptionNames.clone ();
    _exceptions     = (Vector)that._exceptions.clone ();
    _contexts       = (Vector)that._contexts.clone ();
    _parameters     = (Vector)that._parameters.clone ();
    _oneway         = that._oneway;
  } 
  protected MethodEntry (InterfaceEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new MethodEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    methodGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return methodGen;
  } 
  public void type (SymtabEntry newType)
  {
    super.type (newType);
    if (newType == null)
      typeName ("void");
  } 
  public void addException (ExceptionEntry exception)
  {
    _exceptions.addElement (exception);
  } 
  public Vector exceptions ()
  {
    return _exceptions;
  } 
  public void addExceptionName (String name)
  {
    _exceptionNames.addElement (name);
  } 
  public Vector exceptionNames ()
  {
    return _exceptionNames;
  } 
  public void addContext (String context)
  {
    _contexts.addElement (context);
  } 
  public Vector contexts ()
  {
    return _contexts;
  } 
  public void addParameter (ParameterEntry parameter)
  {
    _parameters.addElement (parameter);
  } 
  public Vector parameters ()
  {
    return _parameters;
  } 
  public void oneway (boolean yes)
  {
    _oneway = yes;
  } 
  public boolean oneway ()
  {
    return _oneway;
  } 
  public void valueMethod (boolean yes)
  {
    _valueMethod = yes;
  } 
  public boolean valueMethod ()
  {
    return _valueMethod;
  } 
  void exceptionsAddElement (ExceptionEntry e)
  {
    addException (e);
    addExceptionName (e.fullName ());
  } 
  private Vector  _exceptionNames = new Vector ();
  private Vector  _exceptions     = new Vector ();
  private Vector  _contexts       = new Vector ();
  private Vector  _parameters     = new Vector ();
  private boolean _oneway         = false;
  private boolean _valueMethod    = false;
  static MethodGen methodGen;
} 

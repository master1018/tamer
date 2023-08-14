public class SymtabEntry
{
  public SymtabEntry ()
  {
    initDynamicVars ();
  } 
  SymtabEntry (SymtabEntry that, IDLID clone)
  {
    _module     = that._module;
    _name       = that._name;
    _type       = that._type;
    _typeName   = that._typeName;
    _sourceFile = that._sourceFile;
    _info       = that._info;
    _repID      = (RepositoryID)clone.clone ();
    ((IDLID)_repID).appendToName (_name);
    if (that instanceof InterfaceEntry || that instanceof ModuleEntry || that instanceof StructEntry || that instanceof UnionEntry || (that instanceof SequenceEntry && this instanceof SequenceEntry))
      _container = that;
    else
      _container = that._container;
    initDynamicVars ();
        _comment = that._comment;       
  } 
  SymtabEntry (SymtabEntry that)
  {
    _module     = that._module;
    _name       = that._name;
    _type       = that._type;
    _typeName   = that._typeName;
    _sourceFile = that._sourceFile;
    _info       = that._info;
    _repID      = (RepositoryID)that._repID.clone ();
    _container  = that._container;
    if (_type instanceof ForwardEntry)
      ((ForwardEntry)_type).types.addElement (this);
    initDynamicVars ();
        _comment = that._comment;
  } 
  void initDynamicVars ()
  {
    _dynamicVars = new Vector (maxKey + 1);
    for (int i = 0; i <= maxKey; ++i)
      _dynamicVars.addElement (null);
  } 
  public Object clone ()
  {
    return new SymtabEntry (this);
  } 
  public final String fullName ()
  {
    return _module.equals ("") ? _name : _module + '/' + _name;
  } 
  public String module ()
  {
    return _module;
  } 
  public void module (String newName)
  {
    if (newName == null)
      _module = "";
    else
      _module = newName;
  } 
  public String name ()
  {
    return _name;
  } 
  public void name (String newName)
  {
    if (newName == null)
      _name = "";
    else
      _name = newName;
    if (_repID instanceof IDLID)
      ((IDLID)_repID).replaceName (newName);
  } 
  public String typeName ()
  {
    return _typeName;
  } 
  protected void typeName (String typeName)
  {
    _typeName = typeName;
  } 
  public SymtabEntry type ()
  {
    return _type;
  } 
  public void type (SymtabEntry newType)
  {
    if (newType == null)
      typeName ("");
    else
      typeName (newType.fullName ());
    _type = newType;
    if (_type instanceof ForwardEntry)
      ((ForwardEntry)_type).types.addElement (this);
  } 
  public IncludeEntry sourceFile ()
  {
    return _sourceFile;
  } 
  public void sourceFile (IncludeEntry file)
  {
    _sourceFile = file;
  } 
  public SymtabEntry container ()
  {
    return _container;
  } 
  public void container (SymtabEntry newContainer)
  {
    if (newContainer instanceof InterfaceEntry || newContainer instanceof ModuleEntry)
      _container = newContainer;
  } 
  public RepositoryID repositoryID ()
  {
    return _repID;
  } 
  public void repositoryID (RepositoryID id)
  {
    _repID = id;
  } 
  public boolean emit ()
  {
    return _emit && _isReferencable ;
  } 
  public void emit (boolean emit)
  {
    _emit = emit;
  } 
  public Comment comment()
  {
    return _comment;
  }
  public void comment( Comment comment )
  {
    _comment = comment;
  }
  public boolean isReferencable()
  {
    return _isReferencable ;
  }
  public void isReferencable( boolean value )
  {
    _isReferencable = value ;
  }
  static Stack includeStack = new Stack ();
  static void enteringInclude ()
  {
    includeStack.push (new Boolean (setEmit));
    setEmit = false;
  } 
  static void exitingInclude ()
  {
    setEmit = ((Boolean)includeStack.pop ()).booleanValue ();
  } 
  public static int getVariableKey ()
  {
    return ++maxKey;
  } 
  public void dynamicVariable (int key, Object value) throws NoSuchFieldException
  {
    if (key > maxKey)
      throw new NoSuchFieldException (Integer.toString (key));
    else
    {
      if (key >= _dynamicVars.size ())
        growVars ();
      _dynamicVars.setElementAt (value, key);
    }
  } 
  public Object dynamicVariable (int key) throws NoSuchFieldException
  {
    if (key > maxKey)
      throw new NoSuchFieldException (Integer.toString (key));
    else
    {
      if (key >= _dynamicVars.size ())
        growVars ();
      return _dynamicVars.elementAt (key);
    }
  } 
  void growVars ()
  {
    int diff = maxKey - _dynamicVars.size () + 1;
    for (int i = 0; i < diff; ++i)
      _dynamicVars.addElement (null);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
  } 
  public Generator generator ()
  {
    return null;
  } 
          static boolean setEmit   = true;
          static int   maxKey      = -1;
  private SymtabEntry  _container  = null;
  private String       _module     = "";
  private String       _name       = "";
  private String       _typeName   = "";
  private SymtabEntry  _type       = null;
  private IncludeEntry _sourceFile = null;
  private Object       _info       = null;
  private RepositoryID _repID      = new IDLID ("", "", "1.0");
  private boolean      _emit       = setEmit;
  private Comment      _comment    = null;
  private Vector       _dynamicVars;
  private boolean      _isReferencable = true ;
} 

public class ValueEntry extends InterfaceEntry
{
  protected ValueEntry ()
  {
    super ();
  } 
  protected ValueEntry (ValueEntry that)
  {
     super (that);
    _supportsNames = (Vector)that._supportsNames.clone ();
    _supports      = (Vector)that._supports.clone ();
    _initializers  = (Vector)that._initializers.clone ();
    _custom        = that._custom;
    _isSafe        = that._isSafe;
  } 
  protected ValueEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
  } 
  public Object clone ()
  {
    return new ValueEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    valueGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return valueGen;
  } 
  public void addSupport (SymtabEntry supports)
  {
    _supports.addElement (supports);
  } 
  public Vector supports ()
  {
    return _supports;
  } 
  public void addSupportName (String name)
  {
    _supportsNames.addElement (name);
  } 
  public Vector supportsNames ()
  {
    return _supportsNames;
  } 
  void derivedFromAddElement (SymtabEntry e, boolean isSafe, Scanner scanner)
  {
    if (((InterfaceType)e).getInterfaceType() != InterfaceType.ABSTRACT) {
      if (isAbstract ())
        ParseException.nonAbstractParent2 (scanner, fullName (), e.fullName ());
      else if (derivedFrom ().size () > 0)
        ParseException.nonAbstractParent3 (scanner, fullName (), e.fullName ());
    }
    if (derivedFrom ().contains (e))
      ParseException.alreadyDerived (scanner, e.fullName (), fullName ());
    if (isSafe)
      _isSafe = true;
    addDerivedFrom (e);
    addDerivedFromName (e.fullName ());
    addParentType (e, scanner);
  } 
  void derivedFromAddElement (SymtabEntry e, Scanner scanner)
  {
    addSupport (e);
    addSupportName (e.fullName ());
    addParentType (e, scanner);
  } 
  public boolean replaceForwardDecl (ForwardEntry oldEntry, InterfaceEntry newEntry)
  {
    if (super.replaceForwardDecl (oldEntry, newEntry))
      return true;
    int index = _supports.indexOf (oldEntry);
    if ( index >= 0)
      _supports.setElementAt (newEntry, index);
    return (index >= 0);
  }
  void initializersAddElement (MethodEntry method, Scanner scanner)
  {
    Vector params = method.parameters ();
    int    args   = params.size ();
    for (Enumeration e = _initializers.elements (); e.hasMoreElements ();)
    {
      Vector params2 = ( (MethodEntry) e.nextElement ()).parameters ();
      if (args == params2.size ())
      {
        int i = 0;
        for (; i < args; i++)
          if (!((ParameterEntry)params.elementAt (i)).type ().equals (
                ((ParameterEntry)params2.elementAt (i)).type ()))
            break;
        if (i >= args)
          ParseException.duplicateInit (scanner);
      }
    }
    _initializers.addElement (method);
  } 
  public Vector initializers ()
  {
    return _initializers;
  }
  public void tagMethods ()
  {
    for (Enumeration e = methods ().elements (); e.hasMoreElements ();)
      ((MethodEntry)e.nextElement ()).valueMethod (true);
  }
  public boolean isCustom ()
  {
    return _custom;
  }
  public void setCustom (boolean isCustom)
  {
    _custom = isCustom;
  }
  public boolean isSafe ()
  {
    return _isSafe;
  }
  private Vector   _supportsNames = new Vector ();
  private Vector   _supports      = new Vector ();
  private Vector   _initializers  = new Vector ();
  private boolean  _custom        = false;
  private boolean  _isSafe        = false;
  static  ValueGen valueGen;
} 

public class InterfaceEntry extends SymtabEntry implements InterfaceType
{
  protected InterfaceEntry ()
  {
    super ();
  } 
  protected InterfaceEntry (InterfaceEntry that)
  {
    super (that);
    _derivedFromNames = (Vector)that._derivedFromNames.clone ();
    _derivedFrom      = (Vector)that._derivedFrom.clone ();
    _methods          = (Vector)that._methods.clone ();
    _allMethods       = (Vector)that._allMethods.clone ();
    forwardedDerivers = (Vector)that.forwardedDerivers.clone ();
    _contained        = (Vector)that._contained.clone ();
    _interfaceType    = that._interfaceType;
  } 
  protected InterfaceEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public boolean isAbstract()
  {
      return _interfaceType == ABSTRACT ;
  }
  public boolean isLocal()
  {
      return _interfaceType == LOCAL ;
  }
  public boolean isLocalServant()
  {
      return _interfaceType == LOCALSERVANT ;
  }
  public boolean isLocalSignature()
  {
      return _interfaceType == LOCAL_SIGNATURE_ONLY ;
  }
  public Object clone ()
  {
    return new InterfaceEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    interfaceGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return interfaceGen;
  } 
  public void addDerivedFrom (SymtabEntry derivedFrom)
  {
    _derivedFrom.addElement (derivedFrom);
  } 
  public Vector derivedFrom ()
  {
    return _derivedFrom;
  } 
  public void addDerivedFromName (String name)
  {
    _derivedFromNames.addElement (name);
  } 
  public Vector derivedFromNames ()
  {
    return _derivedFromNames;
  } 
  public void addMethod (MethodEntry method)
  {
    _methods.addElement (method);
  } 
  public Vector methods ()
  {
    return _methods;
  } 
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } 
  public Vector contained ()
  {
    return _contained;
  } 
  void methodsAddElement (MethodEntry method, Scanner scanner)
  {
    if (verifyMethod (method, scanner, false))
    {
      addMethod (method);
      _allMethods.addElement (method);
      addToForwardedAllMethods (method, scanner);
    }
  } 
  void addToForwardedAllMethods (MethodEntry method, Scanner scanner)
  {
    Enumeration e = forwardedDerivers.elements ();
    while (e.hasMoreElements ())
    {
      InterfaceEntry derived = (InterfaceEntry)e.nextElement ();
      if (derived.verifyMethod (method, scanner, true))
        derived._allMethods.addElement (method);
    }
  } 
  private boolean verifyMethod (MethodEntry method, Scanner scanner, boolean clash)
  {
    boolean unique = true;
    String  lcName = method.name ().toLowerCase ();
    Enumeration e  = _allMethods.elements ();
    while (e.hasMoreElements ())
    {
      MethodEntry emethod = (MethodEntry)e.nextElement ();
      String lceName = emethod.name ().toLowerCase ();
      if (method != emethod && lcName.equals (lceName))
      {
        if (clash)
          ParseException.methodClash (scanner, fullName (), method.name ());
        else
          ParseException.alreadyDeclared (scanner, method.name ());
        unique = false;
        break;
      }
    }
    return unique;
  } 
  void derivedFromAddElement (SymtabEntry e, Scanner scanner)
  {
    addDerivedFrom (e);
    addDerivedFromName (e.fullName ());
    addParentType( e, scanner );
  } 
  void addParentType (SymtabEntry e, Scanner scanner)
  {
    if (e instanceof ForwardEntry)
      addToDerivers ((ForwardEntry)e);
    else
    { 
      InterfaceEntry derivedFrom = (InterfaceEntry)e;
      for ( Enumeration enumeration = derivedFrom._allMethods.elements ();
            enumeration.hasMoreElements (); )
      {
        MethodEntry method = (MethodEntry)enumeration.nextElement ();
        if ( verifyMethod (method, scanner, true))
          _allMethods.addElement (method);
        addToForwardedAllMethods (method, scanner);
      }
      lookForForwardEntrys (scanner, derivedFrom);
    }
  }  
  private void lookForForwardEntrys (Scanner scanner, InterfaceEntry entry)
  {
    Enumeration parents = entry.derivedFrom ().elements ();
    while (parents.hasMoreElements ())
    {
      SymtabEntry parent = (SymtabEntry)parents.nextElement ();
      if (parent instanceof ForwardEntry)
        addToDerivers ((ForwardEntry)parent);
      else if (parent == entry)
        ParseException.selfInherit (scanner, entry.fullName ());
      else 
        lookForForwardEntrys (scanner, (InterfaceEntry)parent);
    }
  } 
  public boolean replaceForwardDecl (ForwardEntry oldEntry, InterfaceEntry newEntry)
  {
    int index = _derivedFrom.indexOf( oldEntry );
    if ( index >= 0 )
      _derivedFrom.setElementAt( newEntry, index );
    return (index >= 0);
  } 
  private void addToDerivers (ForwardEntry forward)
  {
    forward.derivers.addElement (this);
    Enumeration e = forwardedDerivers.elements ();
    while (e.hasMoreElements ())
      forward.derivers.addElement ((InterfaceEntry)e.nextElement ());
  } 
  public Vector state ()
  {
    return _state;
  } 
  public void initState ()
  {
    _state = new Vector ();
  } 
  public void addStateElement (InterfaceState state, Scanner scanner)
  {
    if (_state == null)
      _state = new Vector ();
    String name = state.entry.name ();
    for (Enumeration e = _state.elements (); e.hasMoreElements ();)
      if (name.equals (((InterfaceState) e.nextElement ()).entry.name ()))
        ParseException.duplicateState (scanner, name);
    _state.addElement (state);
  } 
  public int getInterfaceType ()
  {
    return _interfaceType;
  }
  public void setInterfaceType (int type)
  {
    _interfaceType = type;
  }
  public Vector allMethods ()
  {
    return _allMethods;
  }
  private Vector  _derivedFromNames = new Vector();
  private Vector  _derivedFrom      = new Vector();
  private Vector  _methods          = new Vector();
          Vector  _allMethods       = new Vector();
          Vector  forwardedDerivers = new Vector();
  private Vector  _contained        = new Vector();
  private Vector  _state            = null;
  private int _interfaceType         = NORMAL;
  static  InterfaceGen interfaceGen;
} 

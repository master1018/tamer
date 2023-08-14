public class UnionEntry extends SymtabEntry
{
  protected UnionEntry ()
  {
    super ();
  } 
  protected UnionEntry (UnionEntry that)
  {
    super (that);
    if (!name ().equals (""))
    {
      module (module () + name ());
      name ("");
    }
    _branches      = (Vector)that._branches.clone ();
    _defaultBranch = that._defaultBranch;
    _contained     = that._contained;
  } 
  protected UnionEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new UnionEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    unionGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return unionGen;
  } 
  public void addBranch (UnionBranch branch)
  {
    _branches.addElement (branch);
  } 
  public Vector branches ()
  {
    return _branches;
  } 
  public void defaultBranch (TypedefEntry branch)
  {
    _defaultBranch = branch;
  } 
  public TypedefEntry defaultBranch ()
  {
    return _defaultBranch;
  } 
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } 
  public Vector contained ()
  {
    return _contained;
  } 
  boolean has (Expression label)
  {
    Enumeration eBranches = _branches.elements ();
    while (eBranches.hasMoreElements ())
    {
      Enumeration eLabels = ((UnionBranch)eBranches.nextElement ()).labels.elements ();
      while (eLabels.hasMoreElements ())
      {
        Expression exp = (Expression)eLabels.nextElement ();
        if (exp.equals (label) || exp.value ().equals (label.value ()))
          return true;
      }
    }
    return false;
  } 
  boolean has (TypedefEntry typedef)
  {
    Enumeration e = _branches.elements ();
    while (e.hasMoreElements ())
    {
      UnionBranch branch = (UnionBranch)e.nextElement ();
      if (!branch.typedef.equals (typedef) && branch.typedef.name ().equals (typedef.name ()))
        return true;
    }
    return false;
  } 
  private Vector       _branches      = new Vector ();
  private TypedefEntry _defaultBranch = null;
  private Vector       _contained     = new Vector ();
  static UnionGen unionGen;
} 

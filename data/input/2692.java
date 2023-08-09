public class SequenceEntry extends SymtabEntry
{
  protected SequenceEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } 
  protected SequenceEntry (SequenceEntry that)
  {
    super (that);
    _maxSize = that._maxSize;
  } 
  protected SequenceEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (!(that instanceof SequenceEntry))
      if (module ().equals (""))
        module (name ());
      else if (!name ().equals (""))
        module (module () + "/" + name ());
    repositoryID (Util.emptyID);
  } 
  public Object clone ()
  {
    return new SequenceEntry (this);
  } 
  public boolean isReferencable()
  {
    return type().isReferencable() ;
  }
  public void isReferencable( boolean value )
  {
  }
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    sequenceGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return sequenceGen;
  } 
  public void maxSize (Expression expr)
  {
    _maxSize = expr;
  } 
  public Expression maxSize ()
  {
    return _maxSize;
  } 
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } 
  public Vector contained ()
  {
    return _contained;
  } 
  static SequenceGen sequenceGen;
  private Expression _maxSize   = null;
  private Vector     _contained = new Vector ();
} 

public class ValueBoxEntry extends ValueEntry
{
  protected ValueBoxEntry ()
  {
    super ();
  } 
  protected ValueBoxEntry (ValueBoxEntry that)
  {
    super (that);
  } 
  protected ValueBoxEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
  } 
  public Object clone ()
  {
    return new ValueBoxEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
     valueBoxGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return valueBoxGen;
  } 
  static ValueBoxGen valueBoxGen;
} 

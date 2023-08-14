public class NativeEntry extends SymtabEntry
{
  protected NativeEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } 
  protected NativeEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  protected NativeEntry (NativeEntry that)
  {
    super (that);
  } 
  public Object clone ()
  {
    return new NativeEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    nativeGen.generate(symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return nativeGen;
  } 
  static NativeGen nativeGen;
} 

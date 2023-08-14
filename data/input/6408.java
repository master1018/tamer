public class ForwardEntry extends SymtabEntry implements InterfaceType
{
  protected ForwardEntry ()
  {
    super ();
  } 
  protected ForwardEntry (ForwardEntry that)
  {
    super (that);
  } 
  protected ForwardEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } 
  public Object clone ()
  {
    return new ForwardEntry (this);
  } 
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    forwardGen.generate (symbolTable, this, stream);
  } 
  public Generator generator ()
  {
    return forwardGen;
  } 
  static boolean replaceForwardDecl (InterfaceEntry interfaceEntry)
  {
    boolean result = true;
    try
    {
      ForwardEntry forwardEntry =
          (ForwardEntry)Parser.symbolTable.get (interfaceEntry.fullName ());
      if ( forwardEntry != null )
      {
        result = (interfaceEntry.getInterfaceType () ==
            forwardEntry.getInterfaceType ());
        forwardEntry.type (interfaceEntry);
        interfaceEntry.forwardedDerivers = forwardEntry.derivers;
        for ( Enumeration derivers = forwardEntry.derivers.elements();
              derivers.hasMoreElements(); )
          ((InterfaceEntry)derivers.nextElement ()).replaceForwardDecl (forwardEntry, interfaceEntry);
        for ( Enumeration types = forwardEntry.types.elements ();
              types.hasMoreElements (); )
          ((SymtabEntry)types.nextElement ()).type (interfaceEntry);
      }
    }
    catch (Exception exception)
    {}
    return result;
  } 
  public int getInterfaceType ()
  {
    return _type;
  }
  public void setInterfaceType (int type)
  {
    _type = type;
  }
  static ForwardGen forwardGen;
  Vector            derivers   = new Vector (); 
  Vector            types      = new Vector (); 
  private int   _type  = InterfaceType.NORMAL; 
} 

public class AttributeGen extends MethodGen implements com.sun.tools.corba.se.idl.AttributeGen
{
  public AttributeGen ()
  {
  } 
  private boolean unique (InterfaceEntry entry, String name)
  {
    Enumeration methods = entry.methods ().elements ();
    while (methods.hasMoreElements ())
    {
      SymtabEntry method = (SymtabEntry)methods.nextElement ();
      if (name.equals (method.name ()))
        return false;
    }
    Enumeration derivedFrom = entry.derivedFrom ().elements ();
    while (derivedFrom.hasMoreElements ())
      if (!unique ((InterfaceEntry)derivedFrom.nextElement (), name))
        return false;
    return true;
  } 
  public void generate (Hashtable symbolTable, AttributeEntry m, PrintWriter stream)
  {
  } 
  protected void interfaceMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    AttributeEntry a = (AttributeEntry)m;
    super.interfaceMethod (symbolTable, a, stream);
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.interfaceMethod (symbolTable, a, stream);
      clear ();
    }
  } 
  protected void stub (String className, boolean isAbstract, Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;
    super.stub (className, isAbstract, symbolTable, a, stream, index);
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.stub (className, isAbstract, symbolTable, a, stream, index + 1);
      clear ();
    }
  } 
  protected void skeleton (Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;
    super.skeleton (symbolTable, a, stream, index);
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.skeleton (symbolTable, a, stream, index + 1);
      clear ();
    }
  } 
  protected void dispatchSkeleton (Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;
    super.dispatchSkeleton (symbolTable, a, stream, index);
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.dispatchSkeleton (symbolTable, m, stream, index + 1);
      clear ();
    }
  } 
  private SymtabEntry realType = null;
  protected void setupForSetMethod ()
  {
    ParameterEntry parm = Compile.compiler.factory.parameterEntry ();
    parm.type (m.type ());
    parm.name ("new" + Util.capitalize (m.name ()));
    m.parameters ().addElement (parm);
    realType = m.type ();
    m.type (null);
  } 
  protected void clear ()
  {
    m.parameters ().removeAllElements ();
    m.type (realType);
  } 
} 

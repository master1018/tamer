public class AttributeGen24 extends MethodGenClone24
{
  public AttributeGen24 ()
  {
  } 
  protected void abstractMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    AttributeEntry a = (AttributeEntry)m;
    super.abstractMethod (symbolTable, a, stream);
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.abstractMethod (symbolTable, a, stream);
      clear ();
    }
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
} 

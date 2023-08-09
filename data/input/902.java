public class DefaultFactory implements AuxGen
{
  public DefaultFactory ()
  {
  } 
  public void generate (java.util.Hashtable symbolTable, com.sun.tools.corba.se.idl.SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.entry       = entry;
    init ();
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } 
  protected void init ()
  {
    factoryClass = entry.name () + "DefaultFactory";
    factoryInterface = entry.name () + "ValueFactory";
    factoryType = Util.javaName (entry);
    implType = entry.name () + "Impl"; 
  } 
  protected boolean hasFactoryMethods ()
  {
    Vector init = ((ValueEntry)entry).initializers ();
    if (init != null && init.size () > 0)
      return true;
    else
      return false;
  } 
  protected void openStream ()
  {
    stream = Util.stream (entry, "DefaultFactory.java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, entry, Util.TypeFile); 
    Util.writeProlog (stream, stream.name ());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.print ("public class " + factoryClass + " implements ");
    if (hasFactoryMethods ())
        stream.print (factoryInterface);
    else
        stream.print ("org.omg.CORBA.portable.ValueFactory");
    stream.println (" {");
  } 
  protected void writeBody ()
  {
    writeFactoryMethods ();
    stream.println ();
    writeReadValue ();
  } 
  protected void writeFactoryMethods ()
  {
    Vector init = ((ValueEntry)entry).initializers ();
    if (init != null)
    {
      for (int i = 0; i < init.size (); i++)
      {
        MethodEntry element = (MethodEntry) init.elementAt (i);
        element.valueMethod (true); 
        ((MethodGen24) element.generator ()).defaultFactoryMethod (symbolTable, element, stream);
      }
    }
  } 
  protected void writeReadValue ()
  {
     stream.println ("  public java.io.Serializable read_value (org.omg.CORBA_2_3.portable.InputStream is)");
     stream.println ("  {");
     stream.println ("    return is.read_value(new " + implType + " ());");
     stream.println ("  }");
  } 
  protected void writeClosing ()
  {
    stream.println ('}');
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected java.util.Hashtable     symbolTable;
  protected com.sun.tools.corba.se.idl.SymtabEntry entry;
  protected GenFileStream           stream;
  protected String factoryClass;
  protected String factoryInterface;
  protected String factoryType;
  protected String implType;
} 

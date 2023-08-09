public class ValueFactory implements AuxGen
{
  public ValueFactory ()
  {
  } 
  public void generate (java.util.Hashtable symbolTable, com.sun.tools.corba.se.idl.SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.entry       = entry;
    init ();
    if (hasFactoryMethods ()) {
        openStream ();
        if (stream == null)
          return;
        writeHeading ();
        writeBody ();
        writeClosing ();
        closeStream ();
    }
  } 
  protected void init ()
  {
    factoryClass = entry.name () + "ValueFactory";
    factoryType = Util.javaName (entry);
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
    stream = Util.stream (entry, "ValueFactory.java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, entry, Util.TypeFile); 
    Util.writeProlog (stream, stream.name ());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.println ("public interface " + factoryClass + " extends org.omg.CORBA.portable.ValueFactory");
    stream.println ('{');
  } 
  protected void writeBody ()
  {
    Vector init = ((ValueEntry)entry).initializers ();
    if (init != null)
    {
      for (int i = 0; i < init.size (); i++)
      {
        MethodEntry element = (MethodEntry) init.elementAt (i);
        element.valueMethod (true); 
        ((MethodGen) element.generator ()). interfaceMethod (symbolTable, element, stream);
      }
    }
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
  protected String factoryType;
} 

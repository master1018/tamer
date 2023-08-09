public class Holder implements AuxGen
{
  public Holder ()
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
    holderClass = entry.name () + "Holder";
    helperClass = Util.helperName (entry, true); 
    if (entry instanceof ValueBoxEntry)
    {
      ValueBoxEntry v = (ValueBoxEntry) entry;
      TypedefEntry member = ((InterfaceState) v.state ().elementAt (0)).entry;
      SymtabEntry mType =  member.type ();
      holderType = Util.javaName (mType);
    }
    else
      holderType = Util.javaName (entry);
  } 
  protected void openStream ()
  {
    stream = Util.stream (entry, "Holder.java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, entry, Util.HolderFile);
    Util.writeProlog (stream, stream.name ());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.println ("public final class " + holderClass + " implements org.omg.CORBA.portable.Streamable");
    stream.println ('{');
  } 
  protected void writeBody ()
  {
    if (entry instanceof ValueBoxEntry)
      stream.println ("  public " + holderType + " value;");
    else
      Util.writeInitializer ("  public ", "value", "", entry, stream);
    stream.println ();
    writeCtors ();
    writeRead ();
    writeWrite ();
    writeType ();
  } 
  protected void writeClosing ()
  {
    stream.println ('}');
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected void writeCtors ()
  {
    stream.println ("  public " + holderClass + " ()");
    stream.println ("  {");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public " + holderClass + " (" + holderType + " initialValue)");
    stream.println ("  {");
    stream.println ("    value = initialValue;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeRead ()
  {
    stream.println ("  public void _read (org.omg.CORBA.portable.InputStream i)");
    stream.println ("  {");
    if (entry instanceof ValueBoxEntry)
    {
      TypedefEntry member = ((InterfaceState) ((ValueBoxEntry) entry).state ().elementAt (0)).entry;
      SymtabEntry mType = member.type ();
      if (mType instanceof StringEntry)
        stream.println ("    value = i.read_string ();");
      else if (mType instanceof PrimitiveEntry)
        stream.println ("    value = " + helperClass + ".read (i).value;");
      else
        stream.println ("    value = " + helperClass + ".read (i);");
    }
    else
      stream.println ("    value = " + helperClass + ".read (i);");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeWrite ()
  {
    stream.println ("  public void _write (org.omg.CORBA.portable.OutputStream o)");
    stream.println ("  {");
    if (entry instanceof ValueBoxEntry)
    {
      TypedefEntry member = ((InterfaceState) ((ValueBoxEntry) entry).state ().elementAt (0)).entry;
      SymtabEntry mType = member.type ();
      if (mType instanceof StringEntry)
        stream.println ("    o.write_string (value);");
      else if (mType instanceof PrimitiveEntry)
      {
        String name = entry.name ();
        stream.println ("    " + name + " vb = new " + name + " (value);");
        stream.println ("    " + helperClass + ".write (o, vb);");
      }
      else
        stream.println ("    " + helperClass + ".write (o, value);");
    }
    else
      stream.println ("    " + helperClass + ".write (o, value);");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeType ()
  {
    stream.println ("  public org.omg.CORBA.TypeCode _type ()");
    stream.println ("  {");
    stream.println ("    return " + helperClass + ".type ();");
    stream.println ("  }");
    stream.println ();
  } 
  protected java.util.Hashtable     symbolTable;
  protected com.sun.tools.corba.se.idl.SymtabEntry entry;
  protected GenFileStream           stream;
  protected String holderClass;
  protected String helperClass;
  protected String holderType;
} 

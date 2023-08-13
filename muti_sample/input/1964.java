public class EnumGen implements com.sun.tools.corba.se.idl.EnumGen, JavaGenerator
{
  public EnumGen ()
  {
  } 
  public void generate (Hashtable symbolTable, EnumEntry e, PrintWriter s)
  {
    this.symbolTable = symbolTable;
    this.e           = e;
    init ();
    openStream ();
    if (stream == null) return;
    generateHolder ();
    generateHelper ();
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } 
  protected void init ()
  {
    className = e.name ();
    fullClassName = Util.javaName (e);
  }
  protected void openStream ()
  {
    stream = Util.stream (e, ".java");
  }
  protected void generateHolder ()
  {
    ((Factories)Compile.compiler.factories ()).holder ().generate (symbolTable, e);
  }
  protected void generateHelper ()
  {
    ((Factories)Compile.compiler.factories ()).helper ().generate (symbolTable, e);
  }
  protected void writeHeading ()
  {
    Util.writePackage (stream, e);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (e.comment () != null)
      e.comment ().generate ("", stream);
    stream.println ("public class " + className + " implements org.omg.CORBA.portable.IDLEntity");
    stream.println ("{");
  }
  protected void writeBody ()
  {
    stream.println ("  private        int __value;");
    stream.println ("  private static int __size = " + (e.elements ().size ()) + ';');
    stream.println ("  private static " + fullClassName + "[] __array = new " + fullClassName + " [__size];");
    stream.println ();
    for (int i = 0; i < e.elements ().size (); ++i)
    {
      String label = (String)e.elements ().elementAt (i);
      stream.println ("  public static final int _" + label + " = " + i + ';');
      stream.println ("  public static final " + fullClassName + ' ' + label + " = new " + fullClassName + "(_" + label + ");");
    }
    stream.println ();
    writeValue ();
    writeFromInt ();
    writeCtors ();
  }
  protected void writeValue ()
  {
    stream.println ("  public int value ()");
    stream.println ("  {");
    stream.println ("    return __value;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeFromInt ()
  {
    stream.println ("  public static " + fullClassName + " from_int (int value)");
    stream.println ("  {");
    stream.println ("    if (value >= 0 && value < __size)");
    stream.println ("      return __array[value];");
    stream.println ("    else");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("  }");
    stream.println ();
  }
  protected void writeCtors ()
  {
    stream.println ("  protected " + className + " (int value)");
    stream.println ("  {");
    stream.println ("    __value = value;");
    stream.println ("    __array[__value] = this;");
    stream.println ("  }");
  }
  protected void writeClosing ()
  {
    stream.println ("} 
  }
  protected void closeStream ()
  {
    stream.close ();
  }
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    tcoffsets.set (entry);
    EnumEntry enumEntry = (EnumEntry)entry;
    StringBuffer emit = new StringBuffer ("new String[] { ");
    Enumeration e = enumEntry.elements ().elements ();
    boolean firstTime = true;
    while (e.hasMoreElements ())
    {
      if (firstTime)
        firstTime = false;
      else
        emit.append (", ");
      emit.append ('"' + Util.stripLeadingUnderscores ((String)e.nextElement ()) + '"');
    }
    emit.append ("} ");
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_enum_tc ("
      + Util.helperName (enumEntry, true) + ".id (), \"" 
      + Util.stripLeadingUnderscores (entry.name ()) + "\", "
      + new String (emit) + ");");
    return index + 1;
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();"); 
    return index;
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    return " + Util.javaQualifiedName (entry) + ".from_int (istream.read_long ());");
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    ostream.write_long (value.value ());");
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + name + " = " + Util.javaQualifiedName (entry) + ".from_int (istream.read_long ());");
    return index;
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + "ostream.write_long (" + name + ".value ());");
    return index;
  } 
  protected Hashtable    symbolTable = null;
  protected EnumEntry    e           = null;
  protected PrintWriter  stream      = null;
  String className     = null;
  String fullClassName = null;
} 

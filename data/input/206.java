public class ValueBoxGen implements com.sun.tools.corba.se.idl.ValueBoxGen, JavaGenerator
{
  public ValueBoxGen ()
  {
  } 
  public void generate (Hashtable symbolTable, ValueBoxEntry v, PrintWriter str)
  {
    this.symbolTable = symbolTable;
    this.v = v;
    TypedefEntry member = ((InterfaceState) v.state ().elementAt (0)).entry;
    SymtabEntry mType = member.type ();
    if (mType instanceof PrimitiveEntry)
    {
      openStream ();
      if (stream == null)
        return;
      writeHeading ();
      writeBody ();
      writeClosing ();
      closeStream ();
    }
    else
    {
      Enumeration e = v.contained ().elements ();
      while (e.hasMoreElements ())
      {
        SymtabEntry contained = (SymtabEntry) e.nextElement ();
        if (contained.type () != null)
          contained.type ().generate (symbolTable, stream);
      }
    }
    generateHelper ();
    generateHolder ();
  } 
  protected void openStream ()
  {
    stream = Util.stream (v, ".java");
  } 
  protected void generateHelper ()
  {
    ((Factories)Compile.compiler.factories ()).helper ().generate (symbolTable, v);
  } 
  protected void generateHolder ()
  {
    ((Factories)Compile.compiler.factories ()).holder ().generate (symbolTable, v);
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, v);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (v.comment () != null)
      v.comment ().generate ("", stream);
    stream.println ("public class " + v.name () + " implements org.omg.CORBA.portable.ValueBase"); 
    stream.println ("{");
  } 
  protected void writeBody ()
  {
    InterfaceState member = (InterfaceState) v.state ().elementAt (0);
    SymtabEntry entry = (SymtabEntry) member.entry;
    Util.fillInfo (entry);
    if (entry.comment () != null)
      entry.comment ().generate (" ", stream);
    stream.println ("  public " +  Util.javaName (entry) + " value;");
    stream.println ("  public " +  v.name () + " (" + Util.javaName (entry) + " initial)");
    stream.println ("  {");
    stream.println ("    value = initial;");
    stream.println ("  }");
    stream.println ();
    writeTruncatable (); 
  } 
  protected void writeTruncatable () 
  {
      stream.println ("  public String[] _truncatable_ids() {");
      stream.println ("      return " + Util.helperName(v, true) + ".get_instance().get_truncatable_base_ids();"); 
      stream.println ("  }");
      stream.println ();
  } 
  protected void writeClosing ()
  {
    stream.println ("} 
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected void writeStreamableMethods ()
  {
    stream.println ("  public void _read (org.omg.CORBA.portable.InputStream istream)");
    stream.println ("  {");
    streamableRead ("this", v, stream);
    stream.println ("  }");
    stream.println ();
    stream.println ("  public void _write (org.omg.CORBA.portable.OutputStream ostream)");
    stream.println ("  {");
    write (0, "    ", "this", v, stream);
    stream.println ("  }");
    stream.println ();
    stream.println ("  public org.omg.CORBA.TypeCode _type ()");
    stream.println ("  {");
    stream.println ("    return " + Util.helperName (v, false) + ".type ();"); 
    stream.println ("  }");
  } 
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    ValueEntry vt = (ValueEntry) entry;
    TypedefEntry member = (TypedefEntry) ((InterfaceState) (vt.state ()).elementAt (0)).entry;
    SymtabEntry mType = Util.typeOf (member);
    index = ((JavaGenerator)mType.generator ()).type (index, indent, tcoffsets, name, mType, stream);
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_value_box_tc ("
      + "_id, "
      + '"' + entry.name () + "\", "
      + name
      + ");");
    return index;
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();"); 
    return index;
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    return index;
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    return (" + entryName +") ((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (get_instance());"); 
    stream.println ("  }");
    stream.println ();
    stream.println ("  public java.io.Serializable read_value (org.omg.CORBA.portable.InputStream istream)"); 
    stream.println ("  {");
    String indent = "    ";
    Vector vMembers = ((ValueBoxEntry) entry).state ();
    TypedefEntry member = ((InterfaceState) vMembers.elementAt (0)).entry;
    SymtabEntry mType = member.type ();
    if (mType instanceof PrimitiveEntry ||
        mType instanceof SequenceEntry ||
        mType instanceof TypedefEntry ||
        mType instanceof StringEntry ||
        !member.arrayInfo ().isEmpty ()) {
      stream.println (indent + Util.javaName (mType) + " tmp;"); 
      ((JavaGenerator)member.generator ()).read (0, indent, "tmp", member, stream);
    }
    else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
      stream.println (indent + Util.javaQualifiedName (mType) + " tmp = (" +
                      Util.javaQualifiedName (mType) + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value (" + Util.helperName (mType, true) + ".get_instance ());"); 
    else
      stream.println (indent + Util.javaName (mType) + " tmp = " +
                      Util.helperName ( mType, true ) + ".read (istream);"); 
    if (mType instanceof PrimitiveEntry)
      stream.println (indent + "return new " + entryName + " (tmp);");
    else
      stream.println (indent + "return tmp;");
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, get_instance());"); 
    stream.println ("  }");
    stream.println ();
    stream.println ("  public void write_value (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable obj)"); 
    stream.println ("  {");
    String entryName = Util.javaName(entry);
    stream.println ("    " + entryName + " value  = (" + entryName + ") obj;");
    write (0, "    ", "value", entry, stream);
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    Vector vMembers = ( (ValueEntry) entry ).state ();
    TypedefEntry member = ((InterfaceState) vMembers.elementAt (0)).entry;
    SymtabEntry mType = member.type ();
    if (mType instanceof PrimitiveEntry || !member.arrayInfo ().isEmpty ())
      index = ((JavaGenerator)member.generator ()).write (index, indent, name + ".value", member, stream);
    else if (mType instanceof SequenceEntry || mType instanceof StringEntry || mType instanceof TypedefEntry || !member.arrayInfo ().isEmpty ())
      index = ((JavaGenerator)member.generator ()).write (index, indent, name, member, stream);
    else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
      stream.println (indent
                      + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value ((java.io.Serializable) value, " 
                      +  Util.helperName (mType, true)  
                      + ".get_instance ());"); 
    else
      stream.println (indent + Util.helperName (mType, true) + ".write (ostream, " + name + ");"); 
    return index;
  } 
  protected void writeAbstract ()
  {
  } 
  protected void streamableRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    Vector vMembers = ( (ValueBoxEntry) entry ).state ();
    TypedefEntry member = ((InterfaceState) vMembers.elementAt (0)).entry;
    SymtabEntry mType = member.type ();
    if (mType instanceof PrimitiveEntry || mType instanceof SequenceEntry || mType instanceof TypedefEntry ||
        mType instanceof StringEntry || !member.arrayInfo ().isEmpty ())
    {
      SymtabEntry mEntry = (SymtabEntry) ((InterfaceState) vMembers.elementAt (0)).entry;
      ((JavaGenerator)member.generator ()).read (0, "    ", entryName + ".value", member, stream);
    }
    else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
      stream.println ("    " + entryName + ".value = (" + Util.javaQualifiedName (mType) + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value (" + Util.helperName(mType, true) + ".get_instance ());"); 
    else
      stream.println ("    " + entryName + ".value = " + Util.helperName (mType, true) + ".read (istream);"); 
  } 
  protected Hashtable  symbolTable = null;
  protected ValueBoxEntry v = null;
  protected PrintWriter stream = null;
} 

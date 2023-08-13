public class ValueBoxGen24 extends ValueBoxGen
{
  public ValueBoxGen24 ()
  {
  } 
  protected void writeTruncatable () 
  {
      stream.print   ("  private static String[] _truncatable_ids = {");
      stream.println (Util.helperName(v, true) + ".id ()};");
      stream.println ();
      stream.println ("  public String[] _truncatable_ids() {");
      stream.println ("    return _truncatable_ids;");
      stream.println ("  }");
      stream.println ();
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    if (!(istream instanceof org.omg.CORBA_2_3.portable.InputStream)) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM(); }");
    stream.println ("    return (" + entryName +") ((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (_instance);");
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
    else
      stream.println (indent + Util.javaName (mType) + " tmp = " +
                      Util.helperName ( mType, true ) + ".read (istream);");
    if (mType instanceof PrimitiveEntry)
      stream.println (indent + "return new " + entryName + " (tmp);");
    else
      stream.println (indent + "return (java.io.Serializable) tmp;");
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    if (!(ostream instanceof org.omg.CORBA_2_3.portable.OutputStream)) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM(); }");
    stream.println ("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, _instance);");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public void write_value (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable value)");
    stream.println ("  {");
    String entryName = Util.javaName(entry);
    stream.println ("    if (!(value instanceof " + entryName + ")) {");
    stream.println ("      throw new org.omg.CORBA.MARSHAL(); }");
    stream.println ("    " + entryName + " valueType = (" + entryName + ") value;");
    write (0, "    ", "valueType", entry, stream);
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
    else
      stream.println (indent + Util.helperName (mType, true) + ".write (ostream, " + name + ");"); 
    return index;
  } 
}

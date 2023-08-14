public class PrimitiveGen implements com.sun.tools.corba.se.idl.PrimitiveGen, JavaGenerator
{
  public PrimitiveGen ()
  {
  } 
  public void generate (Hashtable symbolTable, PrimitiveEntry e, PrintWriter stream)
  {
  } 
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return type (index, indent, tcoffsets, name, entry, stream);
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    tcoffsets.set (entry);
    String emit = "tk_null";
    if (entry.name ().equals ("null"))
      emit = "tk_null";
    else if (entry.name ().equals ("void"))
      emit = "tk_void";
    else if (entry.name ().equals ("short"))
      emit = "tk_short";
    else if (entry.name ().equals ("long"))
      emit = "tk_long";
    else if (entry.name ().equals ("long long"))
      emit = "tk_longlong";
    else if (entry.name ().equals ("unsigned short"))
      emit = "tk_ushort";
    else if (entry.name ().equals ("unsigned long"))
      emit = "tk_ulong";
    else if (entry.name ().equals ("unsigned long long"))
      emit = "tk_ulonglong";
    else if (entry.name ().equals ("float"))
      emit = "tk_float";
    else if (entry.name ().equals ("double"))
      emit = "tk_double";
    else if (entry.name ().equals ("boolean"))
      emit = "tk_boolean";
    else if (entry.name ().equals ("char"))
      emit = "tk_char";
    else if (entry.name ().equals ("octet"))
      emit = "tk_octet";
    else if (entry.name ().equals ("any"))
      emit = "tk_any";
    else if (entry.name ().equals ("TypeCode"))
      emit = "tk_TypeCode";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    else if (entry.name ().equals ("Principal")) 
      emit = "tk_Principal";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind." + emit + ");");
    return index;
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + name + " = " + "istream.read_" + Util.collapseName (entry.name ()) + " ();");
    return index;
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + "ostream.write_" + Util.collapseName (entry.name ()) + " (" + name + ");");
    return index;
  } 
} 

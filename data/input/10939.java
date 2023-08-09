public class SequenceGen implements com.sun.tools.corba.se.idl.SequenceGen, JavaGenerator
{
  public SequenceGen ()
  {
  } 
  public void generate (Hashtable symbolTable, SequenceEntry s, PrintWriter stream)
  {
  } 
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    int offsetOfType = tcoffsets.offset (entry.type ().fullName ());
    if (offsetOfType >= 0)
    {
      tcoffsets.set (null);
      Expression maxSize = ((SequenceEntry)entry).maxSize ();
      if (maxSize == null)
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_recursive_sequence_tc (0, " + (offsetOfType - tcoffsets.currentOffset ()) + ");");
      else
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_recursive_sequence_tc (" + Util.parseExpression (maxSize) + ", " + (offsetOfType - tcoffsets.currentOffset ()) + ");");
      tcoffsets.bumpCurrentOffset (4); 
    }
    else
    {
      tcoffsets.set (entry);
      index = ((JavaGenerator)entry.type ().generator ()).helperType (index + 1, indent, tcoffsets, name, entry.type (), stream);
      Expression maxSize = ((SequenceEntry)entry).maxSize ();
      if (maxSize == null)
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_sequence_tc (0, " + name + ");");
      else
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_sequence_tc (" + Util.parseExpression (maxSize) + ", " + name + ");");
    }
    tcoffsets.bumpCurrentOffset (4); 
    return index;
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    int offsetOfType = tcoffsets.offset (entry.type ().fullName ());
    if (offsetOfType >= 0)
    {
      tcoffsets.set (null);
      stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_recursive_tc (" + "\"\"" + ");");
      tcoffsets.bumpCurrentOffset (4); 
    }
    else
    {
      tcoffsets.set (entry);
      index = ((JavaGenerator)entry.type ().generator ()).type (index + 1, indent, tcoffsets, name, entry.type (), stream);
      Expression maxSize = ((SequenceEntry)entry).maxSize ();
      if (maxSize == null)
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_sequence_tc (0, " + name + ");");
      else
        stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_sequence_tc (" + Util.parseExpression (maxSize) + ", " + name + ");");
    }
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
    SequenceEntry seq = (SequenceEntry)entry;
    String length = "_len" + index++;
    stream.println (indent + "int " + length + " = istream.read_long ();");
    if (seq.maxSize () != null)
    {
      stream.println (indent + "if (" + length + " > (" + Util.parseExpression (seq.maxSize ()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    String seqOfName;
    try
    {
      seqOfName = Util.sansArrayInfo ((String)seq.dynamicVariable (Compile.typedefInfo));
    }
    catch (NoSuchFieldException e)
    {
      seqOfName = seq.name ();
    }
    int startArray = seqOfName.indexOf ('[');
    String arrayDcl = seqOfName.substring (startArray);
    seqOfName = seqOfName.substring (0, startArray);
    SymtabEntry seqOfEntry = (SymtabEntry)Util.symbolTable.get (seqOfName.replace ('.', '/'));
    if (seqOfEntry != null && seqOfEntry instanceof InterfaceEntry && ((InterfaceEntry)seqOfEntry).state () != null)
      seqOfName = Util.javaName ((InterfaceEntry)seqOfEntry);
    arrayDcl = arrayDcl.substring (2);
    stream.println (indent + name + " = new " + seqOfName + '[' + length + ']' + arrayDcl + ';');
    if (seq.type () instanceof PrimitiveEntry)
      if (seq.type ().name ().equals ("any") ||
          seq.type ().name ().equals ("TypeCode") ||
          seq.type ().name ().equals ("Principal"))
      {
        String loopIndex = "_o" + index;
        stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
        stream.println (indent + "  " + name + '[' + loopIndex + "] = istream.read_" + seq.type ().name () + " ();");
      }
      else
      { 
        String varName = name;
        int nameIndex = varName.indexOf (' ');
        if ( nameIndex != -1 )
          varName = varName.substring( nameIndex + 1 );
        stream.println (indent + "istream.read_" + Util.collapseName (entry.type ().name ()) + "_array (" + varName + ", 0, " + length + ");");
      }
    else if (entry.type () instanceof StringEntry)
    {
      String loopIndex = "_o" + index;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
      stream.println (indent + "  " + name + '[' + loopIndex + "] = istream.read_" + seq.type ().name () + " ();");
    }
    else if (entry.type () instanceof SequenceEntry)
    {
      String loopIndex = "_o" + index;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
      stream.println (indent + '{');
      index = ((JavaGenerator)seq.type ().generator ()).read (index, indent + "  ", name + '[' + loopIndex + ']', seq.type (), stream);
      stream.println (indent + '}');
    }
    else
    { 
      String varName = name;
      int nameIndex = varName.indexOf (' ');
      if ( nameIndex != -1 )
        varName = varName.substring( nameIndex + 1 );
      String loopIndex = "_o" + index;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + varName + ".length; ++" + loopIndex + ')');
      stream.println (indent + "  " + varName + '[' + loopIndex + "] = " + Util.helperName (seq.type (), true) + ".read (istream);"); 
    }
    return index;
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    SequenceEntry seq = (SequenceEntry)entry;
    if (seq.maxSize () != null)
    {
      stream.println (indent + "if (" + name + ".length > (" + Util.parseExpression (seq.maxSize ()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    stream.println (indent + "ostream.write_long (" + name + ".length);");
    if (entry.type () instanceof PrimitiveEntry)
      if (entry.type ().name ().equals ("any") ||
          entry.type ().name ().equals ("TypeCode") ||
          entry.type ().name ().equals ("Principal"))
      {
        String loopIndex = "_i" + index++;
        stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
        stream.println (indent + "  ostream.write_" + seq.type ().name () + " (" + name + '[' + loopIndex + "]);");
      }
      else
        stream.println (indent + "ostream.write_" + Util.collapseName (entry.type ().name ()) + "_array (" + name + ", 0, " + name + ".length);");
    else if (entry.type () instanceof StringEntry)
    {
      String loopIndex = "_i" + index++;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
      stream.println (indent + "  ostream.write_" + seq.type ().name () + " (" + name + '[' + loopIndex + "]);");
    }
    else if (entry.type () instanceof SequenceEntry)
    {
      String loopIndex = "_i" + index++;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
      stream.println (indent + '{');
      index = ((JavaGenerator)seq.type ().generator ()).write (index, indent + "  ", name + '[' + loopIndex + ']', seq.type (), stream);
      stream.println (indent + '}');
    }
    else
    {
      String loopIndex = "_i" + index++;
      stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < " + name + ".length; ++" + loopIndex + ')');
      stream.println (indent + "  " + Util.helperName (seq.type (), true) + ".write (ostream, " + name + '[' + loopIndex + "]);"); 
    }
    return index;
  } 
} 

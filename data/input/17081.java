public class StructGen implements com.sun.tools.corba.se.idl.StructGen, JavaGenerator
{
  public StructGen ()
  {
  } 
  protected StructGen (boolean exception)
  {
    thisIsReallyAnException = exception;
  } 
  public void generate (Hashtable symbolTable, StructEntry s, PrintWriter str)
  {
    this.symbolTable = symbolTable;
    this.s           = s;
    openStream ();
    if (stream == null)
      return;
    generateHelper ();
    generateHolder ();
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
    generateContainedTypes ();
  } 
  protected void init ()
  {
  } 
  protected void openStream ()
  {
    stream = Util.stream (s, ".java");
  } 
  protected void generateHelper ()
  {
    ((Factories)Compile.compiler.factories ()).helper ().generate (symbolTable, s);
  } 
  protected void generateHolder ()
  {
    ((Factories)Compile.compiler.factories ()).holder ().generate (symbolTable, s);
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, s);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (s.comment () != null)
      s.comment ().generate ("", stream);
    stream.print ("public final class " + s.name ());
    if (thisIsReallyAnException)
      stream.print (" extends org.omg.CORBA.UserException");
    else
      stream.print(" implements org.omg.CORBA.portable.IDLEntity");
    stream.println ();
    stream.println ("{");
  } 
  protected void writeBody ()
  {
    writeMembers ();
    writeCtors ();
  } 
  protected void writeClosing ()
  {
   stream.println ("} 
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected void generateContainedTypes ()
  {
    Enumeration e = s.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry entry = (SymtabEntry)e.nextElement ();
      if (!(entry instanceof SequenceEntry))
        entry.generate (symbolTable, stream);
    }
  } 
  protected void writeMembers ()
  {
    int size = s.members ().size ();
    memberIsPrimitive = new boolean [size];
    memberIsInterface = new boolean [size];
    memberIsTypedef   = new boolean [size];
    for (int i = 0; i < s.members ().size (); ++i)
    {
      SymtabEntry member = (SymtabEntry)s.members ().elementAt (i);
      memberIsPrimitive[i] = member.type () instanceof PrimitiveEntry;
      memberIsInterface[i] = member.type () instanceof InterfaceEntry;
      memberIsTypedef[i]   = member.type () instanceof TypedefEntry;
      Util.fillInfo (member);
      if (member.comment () != null)
         member.comment ().generate ("  ", stream);
      Util.writeInitializer ("  public ", member.name (), "", member, stream);
    }
  } 
  protected void writeCtors ()
  {
    stream.println ();
    stream.println ("  public " + s.name () + " ()");
    stream.println ("  {");
    if (thisIsReallyAnException)
        stream.println ("    super(" + s.name() + "Helper.id());");
    stream.println ("  } 
    writeInitializationCtor(true);
    if (thisIsReallyAnException) {
        writeInitializationCtor(false);
    }
  }
  private void writeInitializationCtor(boolean init)
  {
    if (!init || (s.members ().size () > 0))
    {
      stream.println ();
      stream.print ("  public " + s.name () + " (");
      boolean firstTime = true;
      if (!init) {
        stream.print ("String $reason");
        firstTime = false;
      }
      for (int i = 0; i < s.members ().size (); ++i)
      {
        SymtabEntry member = (SymtabEntry)s.members ().elementAt (i);
        if (firstTime)
          firstTime = false;
        else
          stream.print (", ");
        stream.print (Util.javaName (member) + " _" + member.name ());
      }
      stream.println (")");
      stream.println ("  {");
      if (thisIsReallyAnException) {
          if (init)
              stream.println ("    super(" + s.name() + "Helper.id());");
          else
              stream.println ("    super(" + s.name() + "Helper.id() + \"  \" + $reason);");
      }
      for (int i = 0; i < s.members ().size (); ++i)
      {
        SymtabEntry member = (SymtabEntry)s.members ().elementAt (i);
        stream.println ("    " + member.name () + " = _" + member.name () + ";");
      }
      stream.println ("  } 
    }
    stream.println ();
  } 
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    TCOffsets innerOffsets = new TCOffsets ();
    innerOffsets.set (entry);
    int offsetForStruct = innerOffsets.currentOffset ();
    StructEntry s = (StructEntry)entry;
    String membersName = "_members" + index++;
    stream.println (indent + "org.omg.CORBA.StructMember[] " + membersName + " = new org.omg.CORBA.StructMember [" + s.members ().size () + "];");
    String tcOfMembers = "_tcOf" + membersName;
    stream.println (indent + "org.omg.CORBA.TypeCode " + tcOfMembers + " = null;");
    for (int i = 0; i < s.members ().size (); ++i)
    {
      TypedefEntry member = (TypedefEntry)s.members ().elementAt (i);
      String memberName = member.name ();
      index = ((JavaGenerator)member.generator ()).type (index, indent, innerOffsets, tcOfMembers, member, stream);
      stream.println (indent + membersName + '[' + i + "] = new org.omg.CORBA.StructMember (");
      stream.println (indent + "  \"" + Util.stripLeadingUnderscores (memberName) + "\",");
      stream.println (indent + "  " + tcOfMembers + ',');
      stream.println (indent + "  null);");
      int offsetSoFar = innerOffsets.currentOffset ();
      innerOffsets = new TCOffsets ();
      innerOffsets.set (entry);
      innerOffsets.bumpCurrentOffset (offsetSoFar - offsetForStruct);
    }
    tcoffsets.bumpCurrentOffset (innerOffsets.currentOffset ());
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_" + (thisIsReallyAnException ? "exception" : "struct") + "_tc (" + Util.helperName (s, true) + ".id (), \"" + Util.stripLeadingUnderscores (entry.name ()) + "\", " + membersName + ");"); 
    return index;
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();"); 
    return index;
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    " + entryName + " value = new " + entryName + " ();");
    read (0, "    ", "value", entry, stream);
    stream.println ("    return value;");
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    if (thisIsReallyAnException)
    {
      stream.println (indent + "
      stream.println (indent + "istream.read_string ();");
    }
    Enumeration e = ((StructEntry)entry).members ().elements ();
    while (e.hasMoreElements ())
    {
      TypedefEntry member = (TypedefEntry)e.nextElement ();
      SymtabEntry  mType = member.type ();
      if (!member.arrayInfo ().isEmpty () || mType instanceof SequenceEntry ||
          mType instanceof PrimitiveEntry || mType instanceof StringEntry ||
          mType instanceof TypedefEntry)
        index = ((JavaGenerator)member.generator ()).read (index, indent, name + '.' + member.name (), member, stream);
      else if (mType instanceof ValueBoxEntry)
      {
        Vector st = ((ValueBoxEntry) mType).state ();
        TypedefEntry vbMember = ((InterfaceState) st.elementAt (0)).entry;
        SymtabEntry vbType = vbMember.type ();
        String jName = null;
        String jHelper = null;
        if (vbType instanceof SequenceEntry || vbType instanceof StringEntry ||
            !vbMember.arrayInfo ().isEmpty ())
        {
          jName = Util.javaName (vbType);      
          jHelper = Util.helperName (mType, true);
              }
        else
        {
          jName = Util.javaName (mType);       
          jHelper = Util.helperName (mType, true);
        }
        if (Util.corbaLevel (2.4f, 99.0f))
          stream.println (indent + name + '.' + member.name () + " = (" + jName + ") " + jHelper + ".read (istream);");
        else
          stream.println (indent + name + '.' + member.name () + " = (" + jName + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value (" + jHelper + ".get_instance ());"); 
      }
      else if ((mType instanceof ValueEntry) &&
          !Util.corbaLevel (2.4f, 99.0f)) 
      {
        stream.println (indent + name + '.' + member.name () + " = (" + Util.javaName (mType) + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value (" + Util.helperName (mType, false) + ".get_instance ());"); 
      }
      else
        stream.println (indent + name + '.' + member.name () + " = " + Util.helperName (member.type (), true) + ".read (istream);"); 
    }
    return index;
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    write (0, "    ", "value", entry, stream);
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    if (thisIsReallyAnException)
    {
      stream.println (indent + "
      stream.println (indent + "ostream.write_string (id ());");
    }
    Vector members = ((StructEntry)entry).members ();
    for (int i = 0; i < members.size (); ++i)
    {
      TypedefEntry member = (TypedefEntry)members.elementAt (i);
      SymtabEntry  mType = member.type ();
      if (!member.arrayInfo ().isEmpty () || mType instanceof SequenceEntry ||
           mType instanceof TypedefEntry || mType instanceof PrimitiveEntry ||
           mType instanceof StringEntry)
        index = ((JavaGenerator)member.generator ()).write (index, "    ", name + '.' + member.name (), member, stream);
      else if ((mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
                && !Util.corbaLevel (2.4f, 99.0f)) { 
        stream.println (indent + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value ((java.io.Serializable) " 
                        + name + '.' + member.name () + ", "
                        + Util.helperName (member.type (), true) 
                        + ".get_instance ());"); 
      }
      else
        stream.println (indent + Util.helperName (member.type (), true) + ".write (ostream, " + name + '.' + member.name () + ");"); 
    }
    return index;
  } 
  protected Hashtable   symbolTable = null;
  protected StructEntry s           = null;
  protected PrintWriter stream      = null;
  protected boolean     thisIsReallyAnException = false;
  private   boolean[]   memberIsPrimitive;
  private   boolean[]   memberIsInterface;
  private   boolean[]   memberIsTypedef;
} 

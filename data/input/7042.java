public class ValueGen24 extends ValueGen
{
  public ValueGen24 ()
  {
  } 
  protected void writeConstructor ()
  {
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, id ());");
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    return (" + entryName + ")((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (id ());");
  } 
  protected void writeInitializers ()
  {
  } 
  protected void writeTruncatable () 
  {
    if (!v.isAbstract ()) {
       stream.println ("  private static String[] _truncatable_ids = {");
       stream.print   ("    " + Util.helperName(v, true) + ".id ()");
       ValueEntry child = v;
       while (child.isSafe ())
       {
        stream.println(",");
        ValueEntry parent = (ValueEntry)child.derivedFrom ().elementAt (0);
        stream.print("    \"" + Util.stripLeadingUnderscoresFromID (parent.repositoryID ().ID ()) + "\"");
        child = parent;
      }
      stream.println();
      stream.println("  };");
      stream.println();
      stream.println ("  public String[] _truncatable_ids() {");
      stream.println ("    return _truncatable_ids;");
      stream.println ("  }");
      stream.println ();
    }
  } 
  class ImplStreamWriter {
    private boolean isImplementsWritten = false ;
    public void writeClassName( String name )
    {
        if (!isImplementsWritten) {
            stream.print( " implements " ) ;
            isImplementsWritten = true ;
        } else
            stream.print( ", " ) ;
        stream.print( name ) ;
    }
  }
  protected void writeHeading ()
  {
    ImplStreamWriter isw = new ImplStreamWriter() ;
    Util.writePackage (stream, v);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (v.comment () != null)
        v.comment ().generate ("", stream);
    if (v.isAbstract ()) {
        writeAbstract ();
        return;
    } else
        stream.print ("public abstract class " + v.name ());
    SymtabEntry parent = (SymtabEntry) v.derivedFrom ().elementAt (0);
    String parentName = Util.javaName (parent);
    boolean cv = false; 
    if (parentName.equals ("java.io.Serializable")) {
        if (((ValueEntry)v).isCustom ()) {
              isw.writeClassName( "org.omg.CORBA.portable.CustomValue" ) ;
              cv = true;
        } else
              isw.writeClassName( "org.omg.CORBA.portable.StreamableValue" ) ;
    } else if ( !((ValueEntry)parent).isAbstract ())
        stream.print (" extends " + parentName);
    for (int i = 0; i < v.derivedFrom ().size (); i++) {
        parent = (SymtabEntry) v.derivedFrom ().elementAt (i);
        if ( ((ValueEntry)parent).isAbstract ()) {
            isw.writeClassName( Util.javaName(parent) ) ;
        }
    }
    Enumeration enumeration = v.supports().elements();
    while (enumeration.hasMoreElements())  {
        InterfaceEntry ie = (InterfaceEntry)(enumeration.nextElement()) ;
        String cname = Util.javaName(ie) ;
        if (!ie.isAbstract())
            cname += "Operations" ;
        isw.writeClassName( cname ) ;
    }
    if ( v.isCustom () && !cv)
        isw.writeClassName( "org.omg.CORBA.portable.CustomValue" ) ;
    stream.println ();
    stream.println ("{");
  } 
  protected void writeMembers ()
  {
    if (v.state () == null)
      return;
    for (int i = 0; i < v.state ().size (); i ++)
    {
      InterfaceState member = (InterfaceState) v.state ().elementAt (i);
      SymtabEntry entry = (SymtabEntry) member.entry;
      Util.fillInfo (entry);
      if (entry.comment () != null)
        entry.comment ().generate (" ", stream);
      String modifier = "  ";
      if (member.modifier == InterfaceState.Public)
        modifier = "  public ";
      else
        modifier = "  protected ";
      Util.writeInitializer (modifier, entry.name (), "", entry, stream);
    }
    stream.println();
  } 
  protected void writeMethods ()
  {
    Enumeration e = v.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry contained = (SymtabEntry)e.nextElement ();
      if (contained instanceof AttributeEntry)
      {
        AttributeEntry element = (AttributeEntry)contained;
        ((AttributeGen24)element.generator ()).abstractMethod (symbolTable, element, stream);
      }
      else if (contained instanceof MethodEntry)
      {
        MethodEntry element = (MethodEntry)contained;
        ((MethodGen24)element.generator ()).abstractMethod (symbolTable, element, stream);
      }
      else
      {
        if (contained instanceof TypedefEntry)
          contained.type ().generate (symbolTable, stream);
        contained.generate (symbolTable, stream);
      }
    }
    if (v.isAbstract ())
        return;
  if (!(v.isCustom () || v.isAbstract ()))
      writeStreamableMethods ();
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    Vector vParents = ((ValueEntry) entry).derivedFrom ();
    if (vParents != null && vParents.size() != 0)
    {
      ValueEntry parent = (ValueEntry) vParents.elementAt (0);
      if (parent == null)
        return index;
      if ((!parent.isAbstract ()) && (! Util.javaQualifiedName(parent).equals ("java.io.Serializable"))) 
          stream.println(indent + "super._read (istream);");
    }
    Vector vMembers = ((ValueEntry) entry).state ();
    int noOfMembers = vMembers == null ? 0 : vMembers.size ();
    for (int k = 0; k < noOfMembers; k++)
    {
      TypedefEntry member = (TypedefEntry)((InterfaceState)vMembers.elementAt (k)).entry;
      String memberName = member.name ();
      SymtabEntry mType = member.type ();
      if (mType instanceof PrimitiveEntry ||
          mType instanceof TypedefEntry   ||
          mType instanceof SequenceEntry  ||
          mType instanceof StringEntry    ||
          !member.arrayInfo ().isEmpty ())
        index = ((JavaGenerator)member.generator ()).read (index, indent, name + '.' + memberName, member, stream);
      else
        stream.println (indent + name + '.' + memberName + " = " +
                        Util.helperName (mType, true) + ".read (istream);"); 
    }
    return index;
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    Vector vParents = ((ValueEntry)entry).derivedFrom ();
    if (vParents != null && vParents.size () != 0)
    {
      ValueEntry parent = (ValueEntry)vParents.elementAt (0);
      if (parent == null)
        return index;
      if ((!parent.isAbstract ()) && (! Util.javaQualifiedName(parent).equals ("java.io.Serializable"))) 
          stream.println(indent + "super._write (ostream);");
    }
    Vector vMembers = ((ValueEntry) entry ).state ();
    int noOfMembers = vMembers == null ? 0 : vMembers.size ();
    for (int k = 0; k < noOfMembers; k++)
    {
      TypedefEntry member = (TypedefEntry)((InterfaceState)vMembers.elementAt (k)).entry;
      String memberName = member.name ();
      SymtabEntry mType = member.type ();
      if (mType instanceof PrimitiveEntry ||
          mType instanceof TypedefEntry   ||
          mType instanceof SequenceEntry  ||
          mType instanceof StringEntry    ||
          !member.arrayInfo ().isEmpty ())
        index = ((JavaGenerator)member.generator ()).write (index, indent, name + '.' + memberName, member, stream);
      else
        stream.println (indent + Util.helperName (mType, true) + 
                              ".write (ostream, " + name + '.' + memberName + ");");
    }
    return index;
  } 
  public void generate (Hashtable symbolTable, ValueEntry v, PrintWriter str)
  {
    this.symbolTable = symbolTable;
    this.v = v;
    init ();
    openStream ();
    if (stream == null)
      return;
    generateTie ();
    generateHelper ();
    generateHolder ();
    if (!v.isAbstract ()) {
      generateValueFactory ();
      generateDefaultFactory ();
    }
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } 
  protected void generateValueFactory ()
  {
    ((Factories)Compile.compiler.factories ()).valueFactory ().generate (symbolTable, v);
  } 
  protected void generateDefaultFactory ()
  {
    ((Factories)Compile.compiler.factories ()).defaultFactory ().generate (symbolTable, v);
  } 
}

public class InterfaceGen implements com.sun.tools.corba.se.idl.InterfaceGen, JavaGenerator
{
  public InterfaceGen ()
  {
  } 
  public void generate (Hashtable symbolTable, InterfaceEntry i, PrintWriter stream)
  {
    if (!isPseudo(i))
    {
      this.symbolTable = symbolTable;
      this.i           = i;
      init ();
      if (! (i.isLocalSignature())) {
          if (! (i.isLocal())) {
              generateSkeleton ();
              Arguments theArguments = (Arguments)Compile.compiler.arguments;
              if( (theArguments.TIEServer == true )
                &&(theArguments.emit == theArguments.All ) )
              {
                  theArguments.TIEServer = false;
                  generateSkeleton ();
                  theArguments.TIEServer = true;
              }
              generateStub ();
          }
          generateHolder ();
          generateHelper ();
      }
      intfType = SIGNATURE;
      generateInterface ();
      intfType = OPERATIONS;
      generateInterface ();
      intfType = 0;
    }
  } 
  protected void init ()
  {
    emit = ((Arguments)Compile.compiler.arguments).emit;
    factories = (Factories)Compile.compiler.factories ();
  } 
  protected void generateSkeleton ()
  {
    if (emit != Arguments.Client)
      factories.skeleton ().generate (symbolTable, i);
  } 
  protected void generateStub ()
  {
    if (emit != Arguments.Server )
      factories.stub ().generate (symbolTable, i);
  } 
  protected void generateHelper ()
  {
    if (emit != Arguments.Server)
      factories.helper ().generate (symbolTable, i);
  } 
  protected void generateHolder ()
  {
    if (emit != Arguments.Server)
      factories.holder ().generate (symbolTable, i);
  } 
  protected void generateInterface ()
  {
    init ();
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    if (intfType == OPERATIONS)
      writeOperationsBody ();
    if (intfType == SIGNATURE)
      writeSignatureBody ();
    writeClosing ();
    closeStream ();
  } 
  protected void openStream ()
  {
    if (i.isAbstract () || intfType == SIGNATURE)
       stream = Util.stream (i, ".java");
    else if (intfType == OPERATIONS)
       stream = Util.stream (i, "Operations.java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, i, Util.TypeFile);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (i.comment () != null)
      i.comment ().generate ("", stream);
    String className = i.name ();
      if (intfType == SIGNATURE)
         writeSignatureHeading ();
      else if (intfType == OPERATIONS)
         writeOperationsHeading ();
    stream.println ();
    stream.println ('{');
  } 
  protected void writeSignatureHeading ()
  {
    String className = i.name ();
    stream.print ("public interface " + className + " extends " + className + "Operations, ");
    boolean firstTime = true;
    boolean hasNonAbstractParent = false; 
    for (int k = 0; k < i.derivedFrom ().size (); ++k)
    {
      if (firstTime)
        firstTime = false;
      else
        stream.print (", ");
      InterfaceEntry parent = (InterfaceEntry)i.derivedFrom ().elementAt (k);
      stream.print (Util.javaName (parent));
      if (! parent.isAbstract ()) 
        hasNonAbstractParent = true; 
    }
    if (!hasNonAbstractParent) {
      stream.print (", org.omg.CORBA.Object, org.omg.CORBA.portable.IDLEntity ");
    }
    else {
        if (i.derivedFrom ().size () == 1)
          stream.print (", org.omg.CORBA.portable.IDLEntity ");
    }
  } 
  protected void writeOperationsHeading ()
  {
    stream.print ("public interface " + i.name ());
    if ( !i.isAbstract ())
      stream.print ("Operations ");
    else {
        if (i.derivedFrom ().size () == 0)
          stream.print (" extends org.omg.CORBA.portable.IDLEntity");
    }
    boolean firstTime = true;
    for (int k = 0; k < i.derivedFrom ().size (); ++k)
    {
      InterfaceEntry parent = (InterfaceEntry) i.derivedFrom ().elementAt (k);
      String parentName = Util.javaName (parent);
      if (parentName.equals ("org.omg.CORBA.Object"))
          continue;
      if (firstTime)
      {
        firstTime = false;
        stream.print (" extends ");
      }
      else
        stream.print (", ");
      if (parent.isAbstract () || i.isAbstract ())
        stream.print (parentName);
      else
        stream.print (parentName + "Operations");
    }
  } 
  protected void writeOperationsBody ()
  {
    Enumeration e = i.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry contained = (SymtabEntry)e.nextElement ();
      if (contained instanceof MethodEntry)
      {
        MethodEntry element = (MethodEntry)contained;
        ((MethodGen)element.generator ()).interfaceMethod (symbolTable, element, stream);
      }
      else
        if ( !(contained instanceof ConstEntry))
          contained.generate (symbolTable, stream);
    }
  } 
  protected void writeSignatureBody ()
  {
    Enumeration e = i.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry contained = (SymtabEntry)e.nextElement ();
      if (contained instanceof ConstEntry)
        contained.generate (symbolTable, stream);
    }
  } 
  protected void writeClosing ()
  {
    String intfName = i.name ();
    if ( !i.isAbstract () && intfType == OPERATIONS)
      intfName = intfName + "Operations";
    stream.println ("} 
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    InterfaceEntry i = (InterfaceEntry)entry;
    tcoffsets.set (entry);
    if (entry.fullName ().equals ("org/omg/CORBA/Object"))
      stream.println (indent + name
          + " = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_objref);");
    else
      stream.println (indent + name
          + " = org.omg.CORBA.ORB.init ().create_interface_tc (" + Util.helperName (i, true) + ".id (), " 
          + '\"' + Util.stripLeadingUnderscores (entry.name ()) + "\");");
    return index;
  } 
  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();"); 
    return index;
  } 
  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    InterfaceEntry i = (InterfaceEntry)entry;
    if (i.isAbstract ())
      stream.println ("    return narrow (((org.omg.CORBA_2_3.portable.InputStream)istream).read_abstract_interface (_" + i.name () + "Stub.class));"); 
    else
      stream.println ("    return narrow (istream.read_Object (_" + i.name () + "Stub.class));");
  } 
  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    write (0, "    ", "value", entry, stream);
  } 
  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    InterfaceEntry i = (InterfaceEntry)entry;
    if (entry.fullName ().equals ("org/omg/CORBA/Object"))
      stream.println (indent + name + " = istream.read_Object (_" + i.name () + "Stub.class);");
    else
      stream.println (indent + name + " = " + Util.helperName (entry, false) + ".narrow (istream.read_Object (_" + i.name () + "Stub.class));"); 
    return index;
  } 
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    InterfaceEntry i = (InterfaceEntry)entry;
    if (i.isAbstract ())
      stream.println (indent + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_abstract_interface ((java.lang.Object) " + name + ");"); 
    else
      stream.println (indent + "ostream.write_Object ((org.omg.CORBA.Object) " + name + ");");
    return index;
  } 
  private boolean isPseudo(InterfaceEntry i) {
    java.lang.String fullname = i.fullName();
    if (fullname.equalsIgnoreCase("CORBA/TypeCode"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/Principal"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/ORB"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/Any"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/Context"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/ContextList"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/DynamicImplementation"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/Environment"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/ExceptionList"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/NVList"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/NamedValue"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/Request"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/ServerRequest"))
        return true;
    if (fullname.equalsIgnoreCase("CORBA/UserException"))
        return true;
    return false;
  }
  protected int            emit        = 0;
  protected Factories      factories   = null;
  protected Hashtable      symbolTable = null;
  protected InterfaceEntry i           = null;
  protected PrintWriter    stream      = null;
  protected static final   int SIGNATURE  = 1;
  protected static final   int OPERATIONS = 2;
  protected                int intfType   = 0;
} 

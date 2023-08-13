public class Stub implements AuxGen
{
  public Stub ()
  {
  } 
  public void generate (Hashtable symbolTable, SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.i           = (InterfaceEntry)entry;
    this.localStub   = i.isLocalServant();
    this.isAbstract  = i.isAbstract( );
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
    classSuffix = "Stub";
  } 
  protected void openStream ()
  {
    String name = '_' + i.name () + classSuffix;
    String pkg = Util.containerFullName (i.container ());
    if (pkg != null && !pkg.equals (""))
    {
      Util.mkdir (pkg);
      name = pkg + '/' + name;
    }
    stream = Util.getStream (name.replace ('/', File.separatorChar) + ".java", i);
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, i, Util.StubFile);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (i.comment () != null)
      i.comment ().generate ("", stream);
    writeClassDeclaration ();
    stream.println ('{');
  } 
  protected void writeClassDeclaration ()
  {
    stream.print ("public class _" + i.name () + classSuffix + " extends org.omg.CORBA.portable.ObjectImpl");
    stream.println (" implements " + Util.javaName (i));
  } 
  protected void writeBody ()
  {
    writeCtors ();
    buildMethodList ();
    writeMethods ();
    writeCORBAObjectMethods ();
    writeSerializationMethods ();
  } 
  protected void writeClosing ()
  {
    stream.println ("} 
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected void writeCtors ()
  {
    String name = i.name ();
    if (localStub) {
        stream.println ("  final public static java.lang.Class _opsClass = " +
            name + "Operations.class;");
        stream.println ();
    }
    stream.println ();
  } 
  protected void buildMethodList ()
  {
    methodList = new Vector ();
    buildMethodList (i);
  } 
  private void buildMethodList (InterfaceEntry entry)
  {
    Enumeration locals = entry.methods ().elements ();
    while (locals.hasMoreElements ())
      addMethod ((MethodEntry)locals.nextElement ());
    Enumeration parents = entry.derivedFrom ().elements ();
    while (parents.hasMoreElements ())
    {
      InterfaceEntry parent = (InterfaceEntry)parents.nextElement ();
      if (!parent.name ().equals ("Object"))
        buildMethodList (parent);
    }
  } 
  private void addMethod (MethodEntry method)
  {
    if (!methodList.contains (method))
      methodList.addElement (method);
  } 
  protected void writeMethods ()
  {
    int count = methodList.size ();
    Enumeration e = methodList.elements ();
    while (e.hasMoreElements ())
    {
      Object method = e.nextElement ();
      if (method instanceof AttributeEntry && !((AttributeEntry)method).readOnly ())
        ++count;
    }
    if( (((Arguments)Compile.compiler.arguments).LocalOptimization )
      && !isAbstract )
    {
        stream.println( "    final public static java.lang.Class _opsClass =" );
        stream.println( "        " + this.i.name() + "Operations.class;" );
    }
    int realI = 0;
    for (int i = 0; i < methodList.size (); ++i)
    {
      MethodEntry method = (MethodEntry)methodList.elementAt (i);
      if (!localStub) {
      ((MethodGen)method.generator ()).stub (this.i.name(), isAbstract, symbolTable, method, stream, realI);
      } else {
      ((MethodGen)method.generator ()).localstub (symbolTable, method, stream, realI, this.i);
      }
      if (method instanceof AttributeEntry && !((AttributeEntry)method).readOnly ())
        realI += 2;
      else
        ++realI;
    }
  } 
  private void buildIDList (InterfaceEntry entry, Vector list)
  {
    if (!entry.fullName ().equals ("org/omg/CORBA/Object"))
    {
      String id = Util.stripLeadingUnderscoresFromID (entry.repositoryID ().ID ());
      if (!list.contains (id))
        list.addElement (id);
      Enumeration e = entry.derivedFrom ().elements ();
      while (e.hasMoreElements ())
        buildIDList ((InterfaceEntry)e.nextElement (), list);
    }
  } 
  private void writeIDs ()
  {
    Vector list = new Vector ();
    buildIDList (i, list);
    Enumeration e = list.elements ();
    boolean first = true;
    while (e.hasMoreElements ())
    {
      if (first)
        first = false;
      else
        stream.println (", ");
      stream.print ("    \"" + (String)e.nextElement () + '"');
    }
  } 
  protected void writeCORBAObjectMethods ()
  {
    stream.println ("  
    stream.println ("  private static String[] __ids = {");
    writeIDs ();
    stream.println ("};");
    stream.println ();
    stream.println ("  public String[] _ids ()");
    stream.println ("  {");
    stream.println ("    return (String[])__ids.clone ();");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeSerializationMethods ()
  {
    stream.println ("  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException");
    stream.println ("  {");
    stream.println ("     String str = s.readUTF ();");
    stream.println ("     String[] args = null;");
    stream.println ("     java.util.Properties props = null;");
    stream.println ("     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);");
    stream.println ("   try {");
    stream.println ("     org.omg.CORBA.Object obj = orb.string_to_object (str);");
    stream.println ("     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();");
    stream.println ("     _set_delegate (delegate);");
    stream.println ("   } finally {");
    stream.println ("     orb.destroy() ;");
    stream.println ("   }");
    stream.println ("  }");
    stream.println ();
    stream.println ("  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException");
    stream.println ("  {");
    stream.println ("     String[] args = null;");
    stream.println ("     java.util.Properties props = null;");
    stream.println ("     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);");
    stream.println ("   try {");
    stream.println ("     String str = orb.object_to_string (this);");
    stream.println ("     s.writeUTF (str);");
    stream.println ("   } finally {");
    stream.println ("     orb.destroy() ;");
    stream.println ("   }");
    stream.println ("  }");
  }
  protected Hashtable      symbolTable = null;
  protected InterfaceEntry i           = null;
  protected PrintWriter    stream      = null;
  protected Vector         methodList  = null;
  protected String         classSuffix = "";
  protected boolean        localStub = false;
  private   boolean        isAbstract = false;
} 

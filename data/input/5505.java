public class Skeleton implements AuxGen
{
  private NameModifier skeletonNameModifier ;
  private NameModifier tieNameModifier ;
  public Skeleton ()
  {
  }
  public void generate (Hashtable symbolTable, SymtabEntry entry)
  {
    if (entry instanceof ValueEntry)
    {
      ValueEntry v = (ValueEntry) entry;
      if ((v.supports ().size () == 0) ||
          ((InterfaceEntry) v.supports ().elementAt (0)).isAbstract ()) {
        return;
        }
    }
    if (((InterfaceEntry) entry).isAbstract ()) {
        return;
    }
    this.symbolTable = symbolTable;
    this.i           = (InterfaceEntry)entry;
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
    tie = ((Arguments)Compile.compiler.arguments).TIEServer ;
    poa = ((Arguments)Compile.compiler.arguments).POAServer ;
    skeletonNameModifier =
        ((Arguments)Compile.compiler.arguments).skeletonNameModifier ;
    tieNameModifier =
        ((Arguments)Compile.compiler.arguments).tieNameModifier ;
    tieClassName = tieNameModifier.makeName( i.name() ) ;
    skeletonClassName = skeletonNameModifier.makeName( i.name() ) ;
    intfName = Util.javaName (i);
    if (i instanceof ValueEntry)
    {
      ValueEntry v = (ValueEntry) i;
      InterfaceEntry intf = (InterfaceEntry) v.supports ().elementAt (0);
      intfName = Util.javaName (intf);
    }
  } 
  protected void openStream ()
  {
    if (tie)
        stream = Util.stream( i, tieNameModifier, ".java" ) ;
    else
        stream = Util.stream( i, skeletonNameModifier, ".java" ) ;
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, i, Util.StubFile);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    if (i.comment () != null)
      i.comment ().generate ("", stream);
    writeClassDeclaration ();
    stream.println ('{');
    stream.println ();
  } 
  protected void writeClassDeclaration ()
  {
    if (tie){
        stream.println ("public class " + tieClassName +
            " extends " + skeletonClassName ) ;
    } else {
        if (poa) {
            stream.println ("public abstract class " + skeletonClassName +
                            " extends org.omg.PortableServer.Servant");
            stream.print   (" implements " + intfName + "Operations, ");
            stream.println ("org.omg.CORBA.portable.InvokeHandler");
        } else {
            stream.println ("public abstract class " + skeletonClassName +
                            " extends org.omg.CORBA.portable.ObjectImpl");
            stream.print   ("                implements " + intfName + ", ");
            stream.println ("org.omg.CORBA.portable.InvokeHandler");
        }
    }
  } 
  protected void writeBody ()
  {
    writeCtors ();
    if (i instanceof ValueEntry)
    {
      ValueEntry v = (ValueEntry) i;
      this.i = (InterfaceEntry) v.supports ().elementAt (0);
    }
    buildMethodList ();
    if (tie){ 
        if (poa) {
            writeMethods ();
            stream.println ("  private " + intfName + "Operations _impl;");
            stream.println ("  private org.omg.PortableServer.POA _poa;");
        } else {
            writeMethods ();
            stream.println ("  private " + intfName + "Operations _impl;");
        }
    } else { 
        if (poa) {
            writeMethodTable ();
            writeDispatchMethod ();
            writeCORBAOperations ();
        } else {
            writeMethodTable ();
            writeDispatchMethod ();
            writeCORBAOperations ();
        }
    }
    writeOperations ();
  } 
  protected void writeClosing ()
  {
    stream.println ();
    if (tie){
        stream.println ("} 
    } else {
        stream.println ("} 
    }
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected void writeCtors ()
  {
    stream.println ("  
    if (!poa) {
        if (tie){
            stream.println ("  public " + tieClassName + " ()");
            stream.println ("  {");
            stream.println ("  }");
        } else {
            stream.println ("  public " + skeletonClassName + " ()");
            stream.println ("  {");
            stream.println ("  }");
        }
    }
    stream.println ();
    if (tie){
        if (poa) {
            writePOATieCtors();
            writePOATieFieldAccessMethods();
        } else {
            stream.println ("  public " + tieClassName +
                            " (" + intfName + "Operations impl)");
            stream.println ("  {");
            if (((InterfaceEntry)i.derivedFrom ().firstElement ()).state () != null)
                stream.println ("    super (impl);");
            else
                stream.println ("    super ();");
            stream.println ("    _impl = impl;");
            stream.println ("  }");
            stream.println ();
        }
    } else { 
        if (poa) {
        } else {
        }
    }
  } 
  private void writePOATieCtors(){
    stream.println ("  public " + tieClassName + " ( " + intfName + "Operations delegate ) {");
    stream.println ("      this._impl = delegate;");
    stream.println ("  }");
    stream.println ("  public " + tieClassName + " ( " + intfName +
                    "Operations delegate , org.omg.PortableServer.POA poa ) {");
    stream.println ("      this._impl = delegate;");
    stream.println ("      this._poa      = poa;");
    stream.println ("  }");
  }
  private void writePOATieFieldAccessMethods(){
    stream.println ("  public " + intfName+ "Operations _delegate() {");
    stream.println ("      return this._impl;");
    stream.println ("  }");
    stream.println ("  public void _delegate (" + intfName + "Operations delegate ) {");
    stream.println ("      this._impl = delegate;");
    stream.println ("  }");
    stream.println ("  public org.omg.PortableServer.POA _default_POA() {");
    stream.println ("      if(_poa != null) {");
    stream.println ("          return _poa;");
    stream.println ("      }");
    stream.println ("      else {");
    stream.println ("          return super._default_POA();");
    stream.println ("      }");
    stream.println ("  }");
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
  protected void writeDispatchMethod ()
  {
    String indent = "                                ";
    stream.println ("  public org.omg.CORBA.portable.OutputStream _invoke (String $method,");
    stream.println (indent + "org.omg.CORBA.portable.InputStream in,");
    stream.println (indent + "org.omg.CORBA.portable.ResponseHandler $rh)");
    stream.println ("  {");
    boolean isLocalInterface = false;
    if (i instanceof InterfaceEntry) {
        isLocalInterface = i.isLocalServant();
    }
    if (!isLocalInterface) {
        stream.println ("    org.omg.CORBA.portable.OutputStream out = null;");
        stream.println ("    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);");
        stream.println ("    if (__method == null)");
        stream.println ("      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
        stream.println ();
        if (methodList.size () > 0)
        {
          stream.println ("    switch (__method.intValue ())");
          stream.println ("    {");
          int realI = 0;
          for (int i = 0; i < methodList.size (); ++i)
          {
            MethodEntry method = (MethodEntry)methodList.elementAt (i);
            ((MethodGen)method.generator ()).dispatchSkeleton (symbolTable, method, stream, realI);
            if (method instanceof AttributeEntry && !((AttributeEntry)method).readOnly ())
              realI += 2;
            else
              ++realI;
          }
          indent = "       ";
          stream.println (indent + "default:");
          stream.println (indent + "  throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
          stream.println ("    }");
          stream.println ();
        }
        stream.println ("    return out;");
    } else {
        stream.println("    throw new org.omg.CORBA.BAD_OPERATION();");
    }
    stream.println ("  } 
    stream.println ();
  } 
  protected void writeMethodTable ()
  {
    stream.println ("  private static java.util.Hashtable _methods = new java.util.Hashtable ();");
    stream.println ("  static");
    stream.println ("  {");
    int count = -1;
    Enumeration e = methodList.elements ();
    while (e.hasMoreElements ())
    {
      MethodEntry method = (MethodEntry)e.nextElement ();
      if (method instanceof AttributeEntry)
      {
        stream.println ("    _methods.put (\"_get_" + Util.stripLeadingUnderscores (method.name ()) + "\", new java.lang.Integer (" + (++count) + "));");
        if (!((AttributeEntry)method).readOnly ())
          stream.println ("    _methods.put (\"_set_" + Util.stripLeadingUnderscores (method.name ()) + "\", new java.lang.Integer (" + (++count) + "));");
      }
      else
        stream.println ("    _methods.put (\"" + Util.stripLeadingUnderscores (method.name ()) + "\", new java.lang.Integer (" + (++count) + "));");
    }
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeMethods ()
  {
      int realI = 0;
      for (int i = 0; i < methodList.size (); ++i)
          {
              MethodEntry method = (MethodEntry)methodList.elementAt (i);
              ((MethodGen)method.generator ()).skeleton
                  (symbolTable, method, stream, realI);
              if (method instanceof AttributeEntry &&
                  !((AttributeEntry)method).readOnly ())
                  realI += 2;
              else
                  ++realI;
              stream.println ();
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
  protected void writeCORBAOperations ()
  {
    stream.println ("  
    stream.println ("  private static String[] __ids = {");
    writeIDs ();
    stream.println ("};");
    stream.println ();
    if (poa)
        writePOACORBAOperations();
    else
        writeNonPOACORBAOperations();
  } 
  protected void writePOACORBAOperations(){
      stream.println ("  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)");
      stream.println ("  {");
      stream.println ("    return (String[])__ids.clone ();");
      stream.println ("  }");
      stream.println ();
      stream.println ("  public "+ i.name() +" _this() ");
      stream.println ("  {");
      stream.println ("    return "+ i.name() +"Helper.narrow(" );
      stream.println ("    super._this_object());");
      stream.println ("  }");
      stream.println ();
      stream.println ("  public "+ i.name() +" _this(org.omg.CORBA.ORB orb) ");
      stream.println ("  {");
      stream.println ("    return "+ i.name() +"Helper.narrow(" );
      stream.println ("    super._this_object(orb));");
      stream.println ("  }");
      stream.println ();
  }
  protected void writeNonPOACORBAOperations(){
      stream.println ("  public String[] _ids ()");
      stream.println ("  {");
      stream.println ("    return (String[])__ids.clone ();");
      stream.println ("  }");
      stream.println ();
  }
  protected void writeOperations ()
  {
  } 
  protected Hashtable      symbolTable = null;
  protected InterfaceEntry i           = null;
  protected PrintWriter    stream      = null;
  protected String         tieClassName   = null;
  protected String         skeletonClassName   = null;
  protected boolean        tie         = false;
  protected boolean        poa         = false;
  protected Vector         methodList  = null;
  protected String         intfName    = "";
} 

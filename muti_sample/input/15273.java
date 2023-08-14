public class Helper implements AuxGen
{
  public Helper ()
  {
  } 
  public void generate (java.util.Hashtable symbolTable, com.sun.tools.corba.se.idl.SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.entry       = entry;
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
    helperClass = entry.name () + "Helper";
    if (entry instanceof ValueBoxEntry)
    {
      ValueBoxEntry v = (ValueBoxEntry) entry;
      TypedefEntry member = ((InterfaceState) v.state ().elementAt (0)).entry;
      SymtabEntry mType =  member.type ();
      if (mType instanceof PrimitiveEntry)
        helperType = Util.javaName (entry);
      else
        helperType = Util.javaName (mType);
    }
    else
      helperType = Util.javaName (entry);
  } 
  protected void openStream ()
  {
    stream = Util.stream (entry, "Helper.java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, entry, Util.HelperFile);
    Util.writeProlog (stream, stream.name ());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.print ("public final class " + helperClass);
    if (entry instanceof ValueEntry)
      stream.println (" implements org.omg.CORBA.portable.ValueHelper");
    else
      stream.println ();
    stream.println ('{');
  }
  protected void writeBody ()
  {
    writeInstVars ();
    writeCtors ();
    writeInsert ();
    writeExtract ();
    writeType ();
    writeID ();
    writeRead ();
    writeWrite ();
    if (entry instanceof InterfaceEntry && !(entry instanceof ValueEntry)) {
      writeNarrow ();
      writeUncheckedNarrow ();
    }
    writeHelperInterface ();
    if (entry instanceof ValueEntry)
      writeValueHelperInterface ();
  } 
  protected void writeHelperInterface ()
  {
  } 
  protected void writeValueHelperInterface ()
  {
    writeGetID ();       
    writeGetType ();     
    writeGetInstance (); 
    writeGetClass ();
    writeGetSafeBaseIds ();
  } 
  protected void writeClosing ()
  {
    stream.println ('}');
  }
  protected void closeStream ()
  {
    stream.close ();
  }
  protected void writeInstVars ()
  {
    stream.println ("  private static String  _id = \"" + Util.stripLeadingUnderscoresFromID (entry.repositoryID ().ID ()) + "\";");
    if (entry instanceof ValueEntry)
    {
      stream.println ();
      stream.println ("  private static " + helperClass + " helper = new " + helperClass + " ();");
      stream.println ();
      stream.println ("  private static String[] _truncatable_ids = {");
      stream.print   ("    _id");
      ValueEntry child = (ValueEntry) entry;
      while (child.isSafe ())
      {
        stream.println(",");
        ValueEntry parent = (ValueEntry)child.derivedFrom ().elementAt (0);
        stream.print("    \"" + Util.stripLeadingUnderscoresFromID (parent.repositoryID ().ID ()) + "\"");
        child = parent;
      }
      stream.println("   };");
    }
    stream.println ();
  } 
  protected void writeCtors ()
  {
    stream.println ("  public " + helperClass + "()");
    stream.println ("  {");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeInsert ()
  {
    stream.println ("  public static void insert (org.omg.CORBA.Any a, " + helperType + " that)");
    stream.println ("  {");
    stream.println ("    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();");
    stream.println ("    a.type (type ());");
    stream.println ("    write (out, that);");
    stream.println ("    a.read_value (out.create_input_stream (), type ());");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeExtract ()
  {
    stream.println ("  public static " + helperType + " extract (org.omg.CORBA.Any a)");
    stream.println ("  {");
    stream.println ("    return read (a.create_input_stream ());");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeType ()
  {
    boolean canRecurse = entry instanceof ValueEntry
        || entry instanceof ValueBoxEntry
        || entry instanceof StructEntry;
    stream.println ("  private static org.omg.CORBA.TypeCode __typeCode = null;");
    if (canRecurse)
      stream.println ("  private static boolean __active = false;");
    stream.println ("  synchronized public static org.omg.CORBA.TypeCode type ()");
    stream.println ("  {");
    stream.println ("    if (__typeCode == null)");
    stream.println ("    {");
    if (canRecurse) {
    stream.println ("      synchronized (org.omg.CORBA.TypeCode.class)");
    stream.println ("      {");
    stream.println ("        if (__typeCode == null)");
    stream.println ("        {");
    stream.println ("          if (__active)");
    stream.println ("          {");
    stream.println ("            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );");
    stream.println ("          }");
    stream.println ("          __active = true;");
    ((JavaGenerator)entry.generator ()).helperType (0, "          ", new TCOffsets (), "__typeCode", entry, stream);
    }
    else
    ((JavaGenerator)entry.generator ()).helperType (0, "      ", new TCOffsets (), "__typeCode", entry, stream);
    if (canRecurse) {
    stream.println ("          __active = false;");
    stream.println ("        }");
    stream.println ("      }");
    }
    stream.println ("    }");
    stream.println ("    return __typeCode;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeID ()
  {
    stream.println ("  public static String id ()");
    stream.println ("  {");
    stream.println ("    return _id;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeRead ()
  {
    boolean isLocalInterface = false;
    if (entry instanceof InterfaceEntry) {
        InterfaceEntry ie = (InterfaceEntry) entry;
        isLocalInterface = ie.isLocal() | ie.isLocalServant();
    }
    stream.println ("  public static " + helperType + " read (org.omg.CORBA.portable.InputStream istream)");
    stream.println ("  {");
    if ( !isLocalInterface ) { 
      ((JavaGenerator)entry.generator ()).helperRead (helperType, entry, stream);
    } else { 
      stream.println ("      throw new org.omg.CORBA.MARSHAL ();");
    }
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeWrite ()
  {
    boolean isLocalInterface = false;
    if (entry instanceof InterfaceEntry) {
        InterfaceEntry ie = (InterfaceEntry) entry;
        isLocalInterface = ie.isLocal() | ie.isLocalServant();
    }
    stream.println ("  public static void write (org.omg.CORBA.portable.OutputStream ostream, " + helperType + " value)");
    stream.println ("  {");
    if ( !isLocalInterface ) { 
      ((JavaGenerator)entry.generator ()).helperWrite (entry, stream);
    } else { 
      stream.println ("      throw new org.omg.CORBA.MARSHAL ();");
    }
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeNarrow ()
  {
    writeRemoteNarrow ();
    stream.println ();
  }
  protected void writeRemoteNarrow ()
  {
    InterfaceEntry ie = (InterfaceEntry) entry;
    if (ie.isLocal ()) {
        writeRemoteNarrowForLocal (false);
        return;
    }
    if (ie.isAbstract ()) {
        writeRemoteNarrowForAbstract (false);
        return;
    } else {
        for (int i = 0; i < ie.derivedFrom ().size (); i++) {
            SymtabEntry parent = (SymtabEntry) ie.derivedFrom ().elementAt (i);
            if (((InterfaceEntry) parent).isAbstract ()) {
                writeRemoteNarrowForAbstract (true);
                break;
            }
        }
    }
    stream.println ("  public static " + helperType + " narrow (org.omg.CORBA.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    stream.println ("    else if (obj instanceof " + helperType + ')');
    stream.println ("      return (" + helperType + ")obj;");
    stream.println ("    else if (!obj._is_a (id ()))");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("    else");
    stream.println ("    {");
    stream.println ("      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();");
    String stubNameofEntry = stubName ((InterfaceEntry)entry);
    stream.println ("      " + stubNameofEntry + " stub = new " + stubNameofEntry + " ();");
    stream.println ("      stub._set_delegate(delegate);");
    stream.println ("      return stub;");
    stream.println ("    }");
    stream.println ("  }");
  } 
  private void writeRemoteNarrowForLocal (boolean hasAbstractParent)
  {
    stream.println ("  public static " + helperType + " narrow (org.omg.CORBA.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    stream.println ("    else if (obj instanceof " + helperType + ')');
    stream.println ("      return (" + helperType + ")obj;");
    stream.println ("    else");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("  }");
  } 
  private void writeRemoteNarrowForAbstract (boolean hasAbstractParent)
  {
    stream.print ("  public static " + helperType + " narrow (java.lang.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    if (hasAbstractParent)
    {
      stream.println ("    else if (obj instanceof org.omg.CORBA.Object)");
      stream.println ("      return narrow ((org.omg.CORBA.Object) obj);");
    }
    else
    {
      stream.println ("    else if (obj instanceof " + helperType + ')');
      stream.println ("      return (" + helperType + ")obj;");
    }
    if (!hasAbstractParent) { 
      String stubNameofEntry = stubName ((InterfaceEntry)entry);
      stream.println ("    else if ((obj instanceof org.omg.CORBA.portable.ObjectImpl) &&");
      stream.println ("             (((org.omg.CORBA.Object)obj)._is_a (id ()))) {");
      stream.println ("      org.omg.CORBA.portable.ObjectImpl impl = (org.omg.CORBA.portable.ObjectImpl)obj ;" ) ;
      stream.println ("      org.omg.CORBA.portable.Delegate delegate = impl._get_delegate() ;" ) ;
      stream.println ("      " + stubNameofEntry + " stub = new " + stubNameofEntry + " ();");
      stream.println ("      stub._set_delegate(delegate);");
      stream.println ("      return stub;" ) ;
      stream.println ("    }" ) ;
    };
    stream.println ("    throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeUncheckedNarrow ()
  {
    writeUncheckedRemoteNarrow ();
    stream.println ();
  }
  protected void writeUncheckedRemoteNarrow ()
  {
    InterfaceEntry ie = (InterfaceEntry) entry;
    if (ie.isLocal ()) {
        writeRemoteUncheckedNarrowForLocal (false);
        return;
    }
    if (ie.isAbstract ()) {
        writeRemoteUncheckedNarrowForAbstract (false);
        return;
    } else {
        for (int i = 0; i < ie.derivedFrom ().size (); i++) {
            SymtabEntry parent = (SymtabEntry) ie.derivedFrom ().elementAt (i);
            if (((InterfaceEntry) parent).isAbstract ()) {
                writeRemoteUncheckedNarrowForAbstract (true);
                break;
            }
        }
    }
    stream.println ("  public static " + helperType + " unchecked_narrow (org.omg.CORBA.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    stream.println ("    else if (obj instanceof " + helperType + ')');
    stream.println ("      return (" + helperType + ")obj;");
    stream.println ("    else");
    stream.println ("    {");
    stream.println ("      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();");
    String stubNameofEntry = stubName ((InterfaceEntry)entry);
    stream.println ("      " + stubNameofEntry + " stub = new " + stubNameofEntry + " ();");
    stream.println ("      stub._set_delegate(delegate);");
    stream.println ("      return stub;");
    stream.println ("    }");
    stream.println ("  }");
  } 
  private void writeRemoteUncheckedNarrowForLocal (boolean hasAbstractParent)
  {
    stream.println ("  public static " + helperType + " unchecked_narrow (org.omg.CORBA.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    stream.println ("    else if (obj instanceof " + helperType + ')');
    stream.println ("      return (" + helperType + ")obj;");
    stream.println ("    else");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("  }");
  } 
  private void writeRemoteUncheckedNarrowForAbstract (boolean hasAbstractParent)
  {
    stream.print ("  public static " + helperType + " unchecked_narrow (java.lang.Object obj)");
    stream.println ("  {");
    stream.println ("    if (obj == null)");
    stream.println ("      return null;");
    if (hasAbstractParent)
    {
      stream.println ("    else if (obj instanceof org.omg.CORBA.Object)");
      stream.println ("      return unchecked_narrow ((org.omg.CORBA.Object) obj);");
    }
    else
    {
      stream.println ("    else if (obj instanceof " + helperType + ')');
      stream.println ("      return (" + helperType + ")obj;");
    }
    if (!hasAbstractParent) {
      String stubNameofEntry = stubName ((InterfaceEntry)entry);
      stream.println ("    else if (obj instanceof org.omg.CORBA.portable.ObjectImpl) {");
      stream.println ("      org.omg.CORBA.portable.ObjectImpl impl = (org.omg.CORBA.portable.ObjectImpl)obj ;" ) ;
      stream.println ("      org.omg.CORBA.portable.Delegate delegate = impl._get_delegate() ;" ) ;
      stream.println ("      " + stubNameofEntry + " stub = new " + stubNameofEntry + " ();");
      stream.println ("      stub._set_delegate(delegate);");
      stream.println ("      return stub;" ) ;
      stream.println ("    }" ) ;
    };
    stream.println ("    throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeGetID ()
  {
    if ( !Util.IDLEntity (entry))
      return;
    stream.println ("  public String get_id ()");
    stream.println ("  {");
    stream.println ("    return _id;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeGetType ()
  {
    if ( !Util.IDLEntity (entry))
      return;
    stream.println ("  public org.omg.CORBA.TypeCode get_type ()");
    stream.println ("  {");
    stream.println ("    return type ();");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeGetClass ()
  {
    stream.println ("  public Class get_class ()");
    stream.println ("  {");
    stream.println ("    return " + helperType + ".class;"); 
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeGetInstance ()
  {
    stream.println ("  public static org.omg.CORBA.portable.ValueHelper get_instance ()");
    stream.println ("  {");
    stream.println ("    return helper;");
    stream.println ("  }");
    stream.println ();
  } 
  protected void writeGetSafeBaseIds ()
  {
    stream.println ("  public String[] get_truncatable_base_ids ()");
    stream.println ("  {");
    stream.println ("    return _truncatable_ids;");
    stream.println ("  }");
    stream.println ();
  } 
  protected String stubName (InterfaceEntry entry)
  {
    String name;
    if (entry.container ().name ().equals (""))
      name =  '_' + entry.name () + "Stub";
    else
    {
      name = Util.containerFullName (entry.container ()) + "._" + entry.name () + "Stub";
    }
    return name.replace ('/', '.');
  } 
  protected java.util.Hashtable     symbolTable;
  protected com.sun.tools.corba.se.idl.SymtabEntry entry;
  protected GenFileStream           stream;
  protected String helperClass;
  protected String helperType;
} 

public class UnionGen implements com.sun.tools.corba.se.idl.UnionGen, JavaGenerator
{
  public UnionGen ()
  {
  } 
  public void generate (Hashtable symbolTable, UnionEntry u, PrintWriter s)
  {
    this.symbolTable = symbolTable;
    this.u           = u;
    init ();
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
    utype       = Util.typeOf (u.type ());
    unionIsEnum = utype instanceof EnumEntry;
  } 
  protected void openStream ()
  {
    stream = Util.stream (u, ".java");
  } 
  protected void generateHelper ()
  {
    ((Factories)Compile.compiler.factories ()).helper ().generate (symbolTable, u);
  } 
  protected void generateHolder ()
  {
   ((Factories)Compile.compiler.factories ()).holder ().generate (symbolTable, u);
  } 
  protected void writeHeading ()
  {
    if (unionIsEnum)
      typePackage = Util.javaQualifiedName (utype) + '.';
    else
      typePackage = "";
    Util.writePackage (stream, u);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    String className = u.name ();
    stream.println ("public final class " + u.name () + " implements org.omg.CORBA.portable.IDLEntity");
    stream.println ("{");
  } 
  protected void writeBody ()
  {
    int size = u.branches ().size () + 1;
    Enumeration e = u.branches ().elements ();
    int i = 0;
    while (e.hasMoreElements ())
    {
      UnionBranch branch = (UnionBranch)e.nextElement ();
      Util.fillInfo (branch.typedef);
      stream.println ("  private " + Util.javaName (branch.typedef) + " ___" + branch.typedef.name () + ";");
      ++i;
    }
    stream.println ("  private " + Util.javaName (utype) + " __discriminator;");
    stream.println ("  private boolean __uninitialized = true;");
    stream.println ();
    stream.println ("  public " + u.name () + " ()");
    stream.println ("  {");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public " + Util.javaName (utype) + " " + safeName (u, "discriminator") + " ()");
    stream.println ("  {");
    stream.println ("    if (__uninitialized)");
    stream.println ("      throw new org.omg.CORBA.BAD_OPERATION ();");
    stream.println ("    return __discriminator;");
    stream.println ("  }");
    e = u.branches ().elements ();
    i = 0;
    while (e.hasMoreElements ())
    {
      UnionBranch branch = (UnionBranch)e.nextElement ();
      writeBranchMethods (stream, u, branch, i++);
    }
    if (u.defaultBranch () == null && !coversAll (u))
    {
      stream.println ();
      stream.println ("  public void _default ()");
      stream.println ("  {");
      stream.println ("    __discriminator = " + defaultDiscriminator (u) + ';');
      stream.println ("    __uninitialized = false;");
      stream.println ("  }");
      stream.println ();
      stream.println ("  public void _default (" + Util.javaName(utype) +
        " discriminator)");
      stream.println ("  {");
      stream.println ("    verifyDefault( discriminator ) ;" );
      stream.println ("    __discriminator = discriminator ;");
      stream.println ("    __uninitialized = false;");
      stream.println ("  }");
      writeVerifyDefault() ;
    }
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
  protected void generateContainedTypes ()
  {
    Enumeration e = u.contained ().elements ();
    while (e.hasMoreElements ())
    {
      SymtabEntry entry = (SymtabEntry)e.nextElement ();
      if (!(entry instanceof SequenceEntry))
        entry.generate (symbolTable, stream);
    }
  } 
  private void writeVerifyDefault()
  {
    Vector labels = vectorizeLabels (u.branches (), true);
    stream.println( "" ) ;
    stream.println( "  private void verifyDefault( " + Util.javaName(utype) +
        " value )" ) ;
    stream.println( "  {" ) ;
    if (unionIsEnum)
        stream.println( "    switch (value.value()) {" ) ;
    else
        stream.println( "    switch (value) {" ) ;
    Enumeration e = labels.elements() ;
    while (e.hasMoreElements()) {
        String str = (String)(e.nextElement()) ;
        stream.println( "      case " + str + ":" ) ;
    }
    stream.println( "        throw new org.omg.CORBA.BAD_OPERATION() ;" ) ;
    stream.println( "" ) ;
    stream.println( "      default:" ) ;
    stream.println( "        return;" ) ;
    stream.println( "    }" ) ;
    stream.println( "  }" ) ;
  }
  private String defaultDiscriminator (UnionEntry u)
  {
    Vector labels = vectorizeLabels (u.branches (), false );
    String ret = null;
    SymtabEntry utype = Util.typeOf (u.type ());
    if (utype instanceof PrimitiveEntry  && utype.name ().equals ("boolean")) {
        if (labels.contains ("true"))
            ret = "false";
        else
            ret = "true";
    } else if (utype.name ().equals ("char")) {
        int def = 0;
        String string = "'\\u0000'";
        while (def != 0xFFFF && labels.contains (string))
            if (++def / 0x10 == 0)
                string = "'\\u000" + def + "'";
            else if (def / 0x100 == 0)
                string = "\\u00" + def + "'";
            else if (def / 0x1000 == 0)
                string = "\\u0" + def + "'";
            else
                string = "\\u" + def + "'";
        ret = string;
    } else if (utype instanceof EnumEntry) {
        Enumeration e = labels.elements ();
        EnumEntry enumEntry = (EnumEntry)utype;
        Vector enumList = (Vector)enumEntry.elements ().clone ();
        while (e.hasMoreElements ())
            enumList.removeElement (e.nextElement ());
        if (enumList.size () == 0)
            ret = typePackage + (String)enumEntry.elements ().lastElement ();
        else
            ret = typePackage + (String)enumList.firstElement ();
    } else if (utype.name ().equals ("octet")) {
        short def = Byte.MIN_VALUE;
        while (def != Byte.MAX_VALUE && labels.contains (Integer.toString (def)))
            ++def;
        ret = Integer.toString (def);
    } else if (utype.name ().equals ("short")) {
        short def = Short.MIN_VALUE;
        while (def != Short.MAX_VALUE && labels.contains (Integer.toString (def)))
            ++def;
        ret = Integer.toString (def);
    } else if (utype.name ().equals ("long")) {
        int def = Integer.MIN_VALUE;
        while (def != Integer.MAX_VALUE && labels.contains (Integer.toString (def)))
            ++def;
        ret = Integer.toString (def);
    } else if (utype.name ().equals ("long long")) {
        long def = Long.MIN_VALUE;
        while (def != Long.MAX_VALUE && labels.contains (Long.toString (def)))
            ++def;
        ret = Long.toString (def);
    } else if (utype.name ().equals ("unsigned short")) {
        short def = 0;
        while (def != Short.MAX_VALUE && labels.contains (Integer.toString (def)))
            ++def;
        ret = Integer.toString (def);
    } else if (utype.name ().equals ("unsigned long")) {
        int def = 0;
        while (def != Integer.MAX_VALUE && labels.contains (Integer.toString (def)))
            ++def;
        ret = Integer.toString (def);
    } else if (utype.name ().equals ("unsigned long long")) {
        long def = 0;
        while (def != Long.MAX_VALUE && labels.contains (Long.toString (def)))
            ++def;
        ret = Long.toString (def);
    }
    return ret;
  } 
  private Vector vectorizeLabels (Vector branchVector, boolean useIntsForEnums )
  {
    Vector mergedLabels = new Vector ();
    Enumeration branches = branchVector.elements ();
    while (branches.hasMoreElements ())
    {
      UnionBranch branch = (UnionBranch)branches.nextElement ();
      Enumeration labels = branch.labels.elements ();
      while (labels.hasMoreElements ())
      {
        Expression expr = (Expression)labels.nextElement ();
        String str ;
        if (unionIsEnum)
          if (useIntsForEnums)
            str = typePackage + "_" + Util.parseExpression( expr ) ;
          else
            str = typePackage + Util.parseExpression( expr ) ;
        else
          str = Util.parseExpression( expr ) ;
        mergedLabels.addElement (str);
      }
    }
    return mergedLabels;
  } 
  private String safeName (UnionEntry u, String name)
  {
    Enumeration e = u.branches ().elements ();
    while (e.hasMoreElements ())
      if (((UnionBranch)e.nextElement ()).typedef.name ().equals (name))
      {
        name = '_' + name;
        break;
      }
    return name;
  } 
  private boolean coversAll (UnionEntry u)
  {
    SymtabEntry utype = Util.typeOf (u.type ());
    boolean coversAll = false;
    if (utype.name ().equals ("boolean")) {
      if (u.branches ().size () == 2)
        coversAll = true;
    } else if (utype instanceof EnumEntry) {
      Vector labels = vectorizeLabels (u.branches (), true);
      if (labels.size () == ((EnumEntry)utype).elements ().size ())
        coversAll = true;
    }
    return coversAll;
  } 
  private void writeBranchMethods (PrintWriter stream, UnionEntry u, UnionBranch branch, int i)
  {
    stream.println ();
    stream.println ("  public " + Util.javaName (branch.typedef) + " " + branch.typedef.name () + " ()");
    stream.println ("  {");
    stream.println ("    if (__uninitialized)");
    stream.println ("      throw new org.omg.CORBA.BAD_OPERATION ();");
    stream.println ("    verify" + branch.typedef.name () + " (__discriminator);");
    stream.println ("    return ___" + branch.typedef.name () + ";");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public void " + branch.typedef.name () + " (" + Util.javaName (branch.typedef) + " value)");
    stream.println ("  {");
    if (branch.labels.size () == 0)
    {
      stream.println ("    __discriminator = " + defaultDiscriminator (u) + ";");
    }
    else
    {
      if (unionIsEnum)
        stream.println ("    __discriminator = " + typePackage + Util.parseExpression ((Expression)branch.labels.firstElement ()) + ";");
      else
        stream.println ("    __discriminator = " + cast ((Expression)branch.labels.firstElement (), u.type ()) + ";");
    }
    stream.println ("    ___" + branch.typedef.name () + " = value;");
    stream.println ("    __uninitialized = false;");
    stream.println ("  }");
    SymtabEntry utype = Util.typeOf (u.type ());
    if (branch.labels.size () > 0 || branch.isDefault)
    {
      stream.println ();
      stream.println ("  public void " + branch.typedef.name () + " (" + Util.javaName (utype) + " discriminator, " + Util.javaName (branch.typedef) + " value)");
      stream.println ("  {");
      stream.println ("    verify" + branch.typedef.name () + " (discriminator);");
      stream.println ("    __discriminator = discriminator;");
      stream.println ("    ___" + branch.typedef.name () + " = value;");
      stream.println ("    __uninitialized = false;");
      stream.println ("  }");
    }
    stream.println ();
    stream.println ("  private void verify" + branch.typedef.name () + " (" + Util.javaName (utype) + " discriminator)");
    stream.println ("  {");
    boolean onlyOne = true;
    if (branch.isDefault && u.branches ().size () == 1)
      ;
    else
    {
      stream.print ("    if (");
      if (branch.isDefault)
      {
        Enumeration eBranches = u.branches ().elements ();
        while (eBranches.hasMoreElements ())
        {
          UnionBranch b = (UnionBranch)eBranches.nextElement ();
          if (b != branch)
          {
            Enumeration eLabels = b.labels.elements ();
            while (eLabels.hasMoreElements ())
            {
              Expression label = (Expression)eLabels.nextElement ();
              if (!onlyOne)
                stream.print (" || ");
              if (unionIsEnum)
                stream.print ("discriminator == " + typePackage + Util.parseExpression (label));
              else
                stream.print ("discriminator == " + Util.parseExpression (label));
              onlyOne = false;
            }
          }
        }
      }
      else
      {
        Enumeration e = branch.labels.elements ();
        while (e.hasMoreElements ())
        {
          Expression label = (Expression)e.nextElement ();
          if (!onlyOne)
            stream.print (" && ");
          if (unionIsEnum)
            stream.print ("discriminator != " + typePackage + Util.parseExpression (label));
          else
            stream.print ("discriminator != " + Util.parseExpression (label));
          onlyOne = false;
        }
      }
      stream.println (")");
      stream.println ("      throw new org.omg.CORBA.BAD_OPERATION ();");
    }
    stream.println ("  }");
  } 
  private int unionLabelSize( UnionEntry un )
  {
    int size = 0 ;
    Vector branches = un.branches() ;
    for (int i = 0; i < branches.size (); ++i) {
        UnionBranch branch = (UnionBranch)(branches.get(i)) ;
        int branchSize = branch.labels.size() ;
        size += ((branchSize == 0) ? 1 : branchSize) ;
    }
    return size ;
  }
  public int helperType (int index, String indent, TCOffsets tcoffsets,
    String name, SymtabEntry entry, PrintWriter stream)
  {
    TCOffsets innerOffsets = new TCOffsets ();
    UnionEntry u = (UnionEntry)entry;
    String discTypeCode = "_disTypeCode" + index;
    String membersName = "_members" + index;
    stream.println (indent + "org.omg.CORBA.TypeCode " + discTypeCode + ';');
    index = ((JavaGenerator)u.type ().generator ()).type (index + 1, indent,
        innerOffsets, discTypeCode, u.type (), stream);
    tcoffsets.bumpCurrentOffset (innerOffsets.currentOffset ());
    stream.println (indent + "org.omg.CORBA.UnionMember[] " + membersName +
        " = new org.omg.CORBA.UnionMember [" + unionLabelSize(u) + "];");
    String tcOfMembers = "_tcOf" + membersName;
    String anyOfMembers = "_anyOf" + membersName;
    stream.println (indent + "org.omg.CORBA.TypeCode " + tcOfMembers + ';');
    stream.println (indent + "org.omg.CORBA.Any " + anyOfMembers + ';');
    innerOffsets = new TCOffsets ();
    innerOffsets.set (entry);
    int offsetForUnion = innerOffsets.currentOffset ();
    for (int i = 0; i < u.branches ().size (); ++i) {
        UnionBranch branch = (UnionBranch)u.branches ().elementAt (i);
        TypedefEntry member = branch.typedef;
        Vector labels = branch.labels;
        String memberName = Util.stripLeadingUnderscores (member.name ());
        if (labels.size() == 0) {
            stream.println ();
            stream.println (indent + "
                " (Default case)" );
            SymtabEntry utype = Util.typeOf (u.type ());
            stream.println (indent + anyOfMembers + " = org.omg.CORBA.ORB.init ().create_any ();");
            stream.println (indent + anyOfMembers + ".insert_octet ((byte)0); 
            innerOffsets.bumpCurrentOffset (4); 
            index = ((JavaGenerator)member.generator ()).type (index, indent, innerOffsets, tcOfMembers, member, stream);
            int offsetSoFar = innerOffsets.currentOffset ();
            innerOffsets = new TCOffsets ();
            innerOffsets.set (entry);
            innerOffsets.bumpCurrentOffset (offsetSoFar - offsetForUnion);
            stream.println (indent + membersName + '[' + i + "] = new org.omg.CORBA.UnionMember (");
            stream.println (indent + "  \"" + memberName + "\",");
            stream.println (indent + "  " + anyOfMembers + ',');
            stream.println (indent + "  " + tcOfMembers + ',');
            stream.println (indent + "  null);");
        } else {
            Enumeration enumeration = labels.elements() ;
            while (enumeration.hasMoreElements()) {
                Expression expr = (Expression)(enumeration.nextElement()) ;
                String elem = Util.parseExpression( expr ) ;
                stream.println ();
                stream.println (indent + "
                    " (case label " + elem + ")" );
                SymtabEntry utype = Util.typeOf (u.type ());
                stream.println (indent + anyOfMembers + " = org.omg.CORBA.ORB.init ().create_any ();");
                if (utype instanceof PrimitiveEntry)
                    stream.println (indent + anyOfMembers + ".insert_" +
                    Util.collapseName (utype.name ()) + " ((" + Util.javaName (utype) +
                        ')' + elem + ");");
                else { 
                    String enumClass = Util.javaName (utype);
                    stream.println (indent + Util.helperName (utype, false) + ".insert (" +
                        anyOfMembers + ", " + enumClass + '.' + elem + ");"); 
                }
                innerOffsets.bumpCurrentOffset (4); 
                index = ((JavaGenerator)member.generator ()).type (index, indent, innerOffsets, tcOfMembers, member, stream);
                int offsetSoFar = innerOffsets.currentOffset ();
                innerOffsets = new TCOffsets ();
                innerOffsets.set (entry);
                innerOffsets.bumpCurrentOffset (offsetSoFar - offsetForUnion);
                stream.println (indent + membersName + '[' + i + "] = new org.omg.CORBA.UnionMember (");
                stream.println (indent + "  \"" + memberName + "\",");
                stream.println (indent + "  " + anyOfMembers + ',');
                stream.println (indent + "  " + tcOfMembers + ',');
                stream.println (indent + "  null);");
            }
        }
    }
    tcoffsets.bumpCurrentOffset (innerOffsets.currentOffset ());
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_union_tc (" +
        Util.helperName (u, true) + ".id (), \"" + entry.name () + "\", " +
        discTypeCode + ", " + membersName + ");");
    return index;
  } 
    public int type (int index, String indent, TCOffsets tcoffsets, String name,
        SymtabEntry entry, PrintWriter stream)
    {
        stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();");
        return index;
    }
    public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
    {
        stream.println ("    " + entryName + " value = new " + entryName + " ();");
        read (0, "    ", "value", entry, stream);
        stream.println ("    return value;");
    }
    public void helperWrite (SymtabEntry entry, PrintWriter stream)
    {
        write (0, "    ", "value", entry, stream);
    }
    public int read (int index, String indent, String name,
        SymtabEntry entry, PrintWriter stream)
    {
        UnionEntry u = (UnionEntry)entry;
        String disName = "_dis" + index++;
        SymtabEntry utype = Util.typeOf (u.type ());
        Util.writeInitializer (indent, disName, "", utype, stream);
        if (utype instanceof PrimitiveEntry)
            index = ((JavaGenerator)utype.generator ()).read (index, indent, disName, utype, stream);
        else
            stream.println (indent + disName + " = " + Util.helperName (utype, true) + ".read (istream);");
        if (utype.name ().equals ("boolean"))
            index = readBoolean (disName, index, indent, name, u, stream);
        else
            index = readNonBoolean (disName, index, indent, name, u, stream);
        return index;
    }
    private int readBoolean (String disName, int index, String indent,
        String name, UnionEntry u, PrintWriter stream)
    {
        UnionBranch firstBranch = (UnionBranch)u.branches ().firstElement ();
        UnionBranch secondBranch;
        if (u.branches ().size () == 2)
            secondBranch = (UnionBranch)u.branches ().lastElement ();
        else
            secondBranch = null;
        boolean firstBranchIsTrue = false;
        boolean noCases = false;
        try {
            if (u.branches ().size () == 1 &&
                (u.defaultBranch () != null || firstBranch.labels.size () == 2)) {
                noCases = true;
            } else {
                Expression expr = (Expression)(firstBranch.labels.firstElement()) ;
                Boolean bool = (Boolean)(expr.evaluate()) ;
                firstBranchIsTrue = bool.booleanValue ();
            }
        } catch (EvaluationException ex) {
        }
        if (noCases) {
            index = readBranch (index, indent, firstBranch.typedef.name (), "",  firstBranch.typedef, stream);
        } else {
            if (!firstBranchIsTrue) {
                UnionBranch tmp = firstBranch;
                firstBranch = secondBranch;
                secondBranch = tmp;
            }
            stream.println (indent + "if (" + disName + ')');
            if (firstBranch == null)
                stream.println (indent + "  throw new org.omg.CORBA.BAD_OPERATION ();");
            else {
                stream.println (indent + '{');
                index = readBranch (index, indent + "  ", firstBranch.typedef.name (),
                    disName, firstBranch.typedef, stream);
                stream.println (indent + '}');
            }
            stream.println (indent + "else");
            if (secondBranch == null)
                stream.println (indent + "  throw new org.omg.CORBA.BAD_OPERATION ();");
            else {
                stream.println (indent + '{');
                index = readBranch (index, indent + "  ", secondBranch.typedef.name (),
                    disName, secondBranch.typedef, stream);
                stream.println (indent + '}');
            }
        }
        return index;
    }
    private int readNonBoolean (String disName, int index, String indent,
        String name, UnionEntry u, PrintWriter stream)
    {
        SymtabEntry utype = Util.typeOf (u.type ());
        if (utype instanceof EnumEntry)
            stream.println (indent + "switch (" + disName + ".value ())");
        else
            stream.println (indent + "switch (" + disName + ')');
        stream.println (indent + '{');
        String typePackage = Util.javaQualifiedName (utype) + '.';
        Enumeration e = u.branches ().elements ();
        while (e.hasMoreElements ()) {
            UnionBranch branch = (UnionBranch)e.nextElement ();
            Enumeration labels = branch.labels.elements ();
            while (labels.hasMoreElements ()) {
                Expression label = (Expression)labels.nextElement ();
                if (utype instanceof EnumEntry) {
                    String key = Util.parseExpression (label);
                    stream.println (indent + "  case " + typePackage + '_' + key + ':');
                } else
                    stream.println (indent + "  case " + cast (label, utype) + ':');
            }
            if (!branch.typedef.equals (u.defaultBranch ())) {
                index = readBranch (index, indent + "    ", branch.typedef.name (),
                    branch.labels.size() > 1 ? disName : "" ,
                    branch.typedef, stream);
                stream.println (indent + "    break;");
            }
        }
        if (!coversAll(u)) {
            stream.println( indent + "  default:") ;
            if (u.defaultBranch () == null) {
                stream.println( indent + "    value._default( " + disName + " ) ;" ) ;
            } else {
                index = readBranch (index, indent + "    ", u.defaultBranch ().name (), disName,
                    u.defaultBranch (), stream);
            }
            stream.println (indent + "    break;");
        }
        stream.println (indent + '}');
        return index;
    }
    private int readBranch (int index, String indent, String name, String disName, TypedefEntry entry, PrintWriter stream)
    {
        SymtabEntry type = entry.type ();
        Util.writeInitializer (indent, '_' + name, "", entry, stream);
        if (!entry.arrayInfo ().isEmpty () ||
            type instanceof SequenceEntry ||
            type instanceof PrimitiveEntry ||
            type instanceof StringEntry) {
            index = ((JavaGenerator)entry.generator ()).read (index, indent, '_' + name, entry, stream);
        } else {
            stream.println (indent + '_' + name + " = " + Util.helperName (type, true) + ".read (istream);");
        }
        stream.print (indent + "value." + name + " (");
        if( disName == "" )
            stream.println("_" + name + ");");
        else
            stream.println(disName + ", " + "_" + name + ");");
        return index;
    }
  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    UnionEntry u = (UnionEntry)entry;
    SymtabEntry utype = Util.typeOf (u.type ());
    if (utype instanceof PrimitiveEntry)
      index = ((JavaGenerator)utype.generator ()).write (index, indent, name + ".discriminator ()", utype, stream);
    else
      stream.println (indent + Util.helperName (utype, true) + ".write (ostream, " + name + ".discriminator ());"); 
    if (utype.name ().equals ("boolean"))
      index = writeBoolean (name + ".discriminator ()", index, indent, name, u, stream);
    else
      index = writeNonBoolean (name + ".discriminator ()", index, indent, name, u, stream);
    return index;
  } 
  private int writeBoolean (String disName, int index, String indent, String name, UnionEntry u, PrintWriter stream)
  {
    SymtabEntry utype = Util.typeOf (u.type ());
    UnionBranch firstBranch = (UnionBranch)u.branches ().firstElement ();
    UnionBranch secondBranch;
    if (u.branches ().size () == 2)
      secondBranch = (UnionBranch)u.branches ().lastElement ();
    else
      secondBranch = null;
    boolean firstBranchIsTrue = false;
    boolean noCases = false;
    try
    {
      if (u.branches ().size () == 1 && (u.defaultBranch () != null || firstBranch.labels.size () == 2))
        noCases = true;
      else
        firstBranchIsTrue = ((Boolean)((Expression)firstBranch.labels.firstElement ()).evaluate ()).booleanValue ();
    }
    catch (EvaluationException ex)
    {}
    if (noCases)
    {
      index = writeBranch (index, indent, name, firstBranch.typedef, stream);
    }
    else
    {
      if (!firstBranchIsTrue)
      {
        UnionBranch tmp = firstBranch;
        firstBranch = secondBranch;
        secondBranch = tmp;
      }
      stream.println (indent + "if (" + disName + ')');
      if (firstBranch == null)
        stream.println (indent + "  throw new org.omg.CORBA.BAD_OPERATION ();");
      else
      {
        stream.println (indent + '{');
        index = writeBranch (index, indent + "  ", name, firstBranch.typedef, stream);
        stream.println (indent + '}');
      }
      stream.println (indent + "else");
      if (secondBranch == null)
        stream.println (indent + "  throw new org.omg.CORBA.BAD_OPERATION ();");
      else
      {
        stream.println (indent + '{');
        index = writeBranch (index, indent + "  ", name, secondBranch.typedef, stream);
        stream.println (indent + '}');
      }
    }
    return index;
  } 
  private int writeNonBoolean (String disName, int index, String indent, String name, UnionEntry u, PrintWriter stream)
  {
    SymtabEntry utype = Util.typeOf (u.type ());
    if (utype instanceof EnumEntry)
      stream.println (indent + "switch (" + name + ".discriminator ().value ())");
    else
      stream.println (indent + "switch (" + name + ".discriminator ())");
    stream.println (indent + "{");
    String typePackage = Util.javaQualifiedName (utype) + '.';
    Enumeration e = u.branches ().elements ();
    while (e.hasMoreElements ())
    {
      UnionBranch branch = (UnionBranch)e.nextElement ();
      Enumeration labels = branch.labels.elements ();
      while (labels.hasMoreElements ())
      {
        Expression label = (Expression)labels.nextElement ();
        if (utype instanceof EnumEntry)
        {
          String key = Util.parseExpression (label);
          stream.println (indent + "  case " + typePackage + '_' + key + ":");
        }
        else
          stream.println (indent + "  case " + cast (label, utype) + ':');
      }
      if (!branch.typedef.equals (u.defaultBranch ()))
      {
        index = writeBranch (index, indent + "    ", name, branch.typedef, stream);
        stream.println (indent + "    break;");
      }
    }
    if (u.defaultBranch () != null) {
      stream.println (indent + "  default:");
      index = writeBranch (index, indent + "    ", name, u.defaultBranch (), stream);
      stream.println (indent + "    break;");
    }
    stream.println (indent + "}");
    return index;
  } 
  private int writeBranch (int index, String indent, String name, TypedefEntry entry, PrintWriter stream)
  {
    SymtabEntry type = entry.type ();
    if (!entry.arrayInfo ().isEmpty () || type instanceof SequenceEntry || type instanceof PrimitiveEntry || type instanceof StringEntry)
      index = ((JavaGenerator)entry.generator ()).write (index, indent, name + '.' + entry.name () + " ()", entry, stream);
    else
      stream.println (indent + Util.helperName (type, true) + ".write (ostream, " + name + '.' + entry.name () + " ());"); 
    return index;
  } 
  private String cast (Expression expr, SymtabEntry type)
  {
    String ret = Util.parseExpression (expr);
    if (type.name ().indexOf ("short") >= 0)
    {
      if (expr.value () instanceof Long)
      {
        long value = ((Long)expr.value ()).longValue ();
        if (value > Short.MAX_VALUE)
          ret = "(short)(" + ret + ')';
      }
      else if (expr.value () instanceof Integer)
      {
        int value = ((Integer)expr.value ()).intValue ();
        if (value > Short.MAX_VALUE)
          ret = "(short)(" + ret + ')';
      }
    }
    else if (type.name ().indexOf ("long") >= 0)
    {
      if (expr.value () instanceof Long)
      {
        long value = ((Long)expr.value ()).longValue ();
        if (value > Integer.MAX_VALUE || value == Integer.MIN_VALUE)
          ret = "(int)(" + ret + ')';
      }
      else if (expr.value () instanceof Integer)
      {
        int value = ((Integer)expr.value ()).intValue ();
        if (value > Integer.MAX_VALUE || value == Integer.MIN_VALUE)
          ret = "(int)(" + ret + ')';
      }
    }
    return ret;
  } 
  protected Hashtable   symbolTable = null;
  protected UnionEntry  u           = null;
  protected PrintWriter stream      = null;
  protected SymtabEntry utype       = null;
  protected boolean     unionIsEnum;
  protected String      typePackage = "";
} 

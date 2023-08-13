public class MethodGen24 extends MethodGen
{
  public MethodGen24 ()
  {
  } 
  protected void writeParmList (MethodEntry m, boolean listTypes, PrintWriter stream) {
    boolean firstTime = true;
    Enumeration e = m.parameters ().elements ();
    while (e.hasMoreElements ())
    {
      if (firstTime)
        firstTime = false;
      else
        stream.print (", ");
      ParameterEntry parm = (ParameterEntry)e.nextElement ();
      if (listTypes) {
        writeParmType (parm.type (), parm.passType ());
        stream.print (' ');
      }
      stream.print (parm.name ());
    }
  }
  protected void helperFactoryMethod (Hashtable symbolTable, MethodEntry m, SymtabEntry t, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m = m;
    this.stream = stream;
    String initializerName = m.name ();
    String typeName = Util.javaName (t);
    String factoryName = typeName + "ValueFactory";
    stream.print  ("  public static " + typeName + " " + initializerName +
            " (org.omg.CORBA.ORB $orb");
    if (!m.parameters ().isEmpty ())
      stream.print (", "); 
    writeParmList (m, true, stream);
    stream.println (")");
    stream.println ("  {");
    stream.println ("    try {");
    stream.println ("      " + factoryName + " $factory = (" + factoryName + ")");
    stream.println ("          ((org.omg.CORBA_2_3.ORB) $orb).lookup_value_factory(id());");
    stream.print   ("      return $factory." + initializerName + " (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("    } catch (ClassCastException $ex) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("    }");
    stream.println ("  }");
    stream.println ();
  } 
  protected void abstractMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    stream.print ("public abstract ");
    writeMethodSignature ();
    stream.println (";");
    stream.println ();
  } 
  protected void defaultFactoryMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    String typeName = m.container (). name ();
    stream.println ();
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print   ("  public " + typeName + " " + m.name () + " (");
    writeParmList  (m, true, stream);
    stream.println (")");
    stream.println ("  {");
    stream.print   ("    return new " + typeName + "Impl (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("  }");
  } 
  protected void writeMethodSignature ()
  {
    if (m.type () == null)
    {
        if (isValueInitializer ())
            stream.print (m.container ().name ());
        else
            stream.print ("void");
    }
    else
    {
      stream.print (Util.javaName (m.type ()));
    }
    stream.print (' ' + m.name () + " (");
    boolean firstTime = true;
    Enumeration e = m.parameters ().elements ();
    while (e.hasMoreElements ())
    {
      if (firstTime)
        firstTime = false;
      else
        stream.print (", ");
      ParameterEntry parm = (ParameterEntry)e.nextElement ();
      writeParmType (parm.type (), parm.passType ());
      stream.print (' ' + parm.name ());
    }
    if (m.contexts ().size () > 0)
    {
      if (!firstTime)
        stream.print (", ");
      stream.print ("org.omg.CORBA.Context $context");
    }
    if (m.exceptions ().size () > 0)
    {
      stream.print (") throws ");
      e = m.exceptions ().elements ();
      firstTime = true;
      while (e.hasMoreElements ())
      {
        if (firstTime)
          firstTime = false;
        else
          stream.print (", ");
        stream.print (Util.javaName ((SymtabEntry)e.nextElement ()));
      }
    }
    else
      stream.print (')');
  } 
  protected void interfaceMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    writeMethodSignature ();
    stream.println (";");
  } 
}

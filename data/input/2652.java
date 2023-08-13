public class Compile extends com.sun.tools.corba.se.idl.Compile
{
  public static void main (String[] args)
  {
    compiler = new Compile ();
    compiler.start (args);
  } 
  public void start (String[] args)
  {
    try
    {
      Util.registerMessageFile ("com/sun/tools/corba/se/idl/toJavaPortable/toJavaPortable.prp");
      init (args);
      if (arguments.versionRequest)
        displayVersion ();
      else
      {
        preParse ();
        Enumeration e = parse ();
        if (e != null)
        {
          preEmit (e);
          generate ();
        }
      }
    }
    catch (InvalidArgument e)
    {
      System.err.println (e);
    }
    catch (IOException e)
    {
      System.err.println (e);
    }
  } 
  protected Compile ()
  {
    factory = factories ().symtabFactory ();
  } 
  public Factories _factories = new Factories ();  
  protected com.sun.tools.corba.se.idl.Factories factories ()
  {
    return _factories;
  } 
  ModuleEntry org;
  ModuleEntry omg;
  ModuleEntry corba;
  InterfaceEntry object;
  protected void preParse ()
  {
    Util.setSymbolTable (symbolTable);
    Util.setPackageTranslation( ((Arguments)arguments).packageTranslation ) ;
    org = factory.moduleEntry ();
    org.emit (false);
    org.name ("org");
    org.container (null);
    omg = factory.moduleEntry ();
    omg.emit (false); 
    omg.name ("omg");
    omg.module ("org");
    omg.container (org);
    org.addContained (omg);
    corba = factory.moduleEntry ();
    corba.emit (false); 
    corba.name ("CORBA");
    corba.module ("org/omg");
    corba.container (omg);
    omg.addContained (corba);
    symbolTable.put ("org", org);
    symbolTable.put ("org/omg", omg);
    symbolTable.put ("org/omg/CORBA", corba);
    object = (InterfaceEntry)symbolTable.get ("Object");
    object.module ("org/omg/CORBA");
    object.container (corba);
    symbolTable.put ("org/omg/CORBA/Object", object);
    PrimitiveEntry pEntry = factory.primitiveEntry ();
    pEntry.name ("TypeCode");
    pEntry.module ("org/omg/CORBA");
    pEntry.container (corba);
    symbolTable.put ("org/omg/CORBA/TypeCode", pEntry);
    symbolTable.put ("CORBA/TypeCode", pEntry);                      
    overrideNames.put ("CORBA/TypeCode", "org/omg/CORBA/TypeCode");  
    overrideNames.put ("org/omg/CORBA/TypeCode", "CORBA/TypeCode");  
    pEntry = factory.primitiveEntry ();
    pEntry.name ("Principal");
    pEntry.module ("org/omg/CORBA");
    pEntry.container (corba);
    symbolTable.put ("org/omg/CORBA/Principle", pEntry);
    symbolTable.put ("CORBA/Principal", pEntry);
    overrideNames.put ("CORBA/Principal", "org/omg/CORBA/Principal");
    overrideNames.put ("org/omg/CORBA/Principal", "CORBA/Principal");
    overrideNames.put ("TRUE", "true");
    overrideNames.put ("FALSE", "false");
    symbolTable.put ("CORBA", corba);  
    overrideNames.put ("CORBA", "org/omg/CORBA");  
    overrideNames.put ("org/omg/CORBA", "CORBA");  
  } 
  protected void preEmit (Enumeration emitList)
  {
    typedefInfo = SymtabEntry.getVariableKey ();
    Hashtable tempST = (Hashtable)symbolTable.clone ();
    for (Enumeration e = tempST.elements (); e.hasMoreElements ();)
    {
      SymtabEntry element = (SymtabEntry)e.nextElement ();
      preEmitSTElement (element);
    }
    Enumeration elements = symbolTable.elements ();
    while (elements.hasMoreElements ())
    {
      SymtabEntry element = (SymtabEntry)elements.nextElement ();
      if (element instanceof TypedefEntry || element instanceof SequenceEntry)
        Util.fillInfo (element);
      else if (element instanceof StructEntry)
      {
        Enumeration members = ((StructEntry)element).members ().elements ();
        while (members.hasMoreElements ())
          Util.fillInfo ((SymtabEntry)members.nextElement ());
      }
      else if (element instanceof InterfaceEntry && ((InterfaceEntry)element).state () != null)
      {
        Enumeration members = ((InterfaceEntry)element).state ().elements ();
        while (members.hasMoreElements ())
          Util.fillInfo (((InterfaceState)members.nextElement ()).entry);
      }
      else if (element instanceof UnionEntry)
      {
        Enumeration branches = ((UnionEntry)element).branches ().elements ();
        while (branches.hasMoreElements ())
          Util.fillInfo (((UnionBranch)branches.nextElement ()).typedef);
      }
      if (element.module ().equals ("") && !(element instanceof ModuleEntry || element instanceof IncludeEntry || element instanceof PrimitiveEntry))
        importTypes.addElement (element);
    }
    while (emitList.hasMoreElements ())
    {
      SymtabEntry entry = (SymtabEntry)emitList.nextElement ();
      preEmitELElement (entry);
    }
  } 
  protected void preEmitSTElement (SymtabEntry entry)
  {
    Hashtable packages = ((Arguments)arguments).packages;
    if (packages.size () > 0)
    {
      String substr = (String)packages.get (entry.fullName ());
      if (substr != null)
      {
        String pkg = null;
        ModuleEntry mod = null;
        ModuleEntry prev = null;
        while (substr != null)
        {
          int dot = substr.indexOf ('.');
          if (dot < 0)
          {
            pkg = substr;
            substr = null;
          }
          else
          {
            pkg = substr.substring (0, dot);
            substr = substr.substring (dot + 1);
          }
          String fullName = prev == null ? pkg : prev.fullName () + '/' + pkg;
          mod = (ModuleEntry)symbolTable.get (fullName);
          if (mod == null)
          {
            mod = factory.moduleEntry ();
            mod.name (pkg);
            mod.container (prev);
            if (prev != null) mod.module (prev.fullName ());
            symbolTable.put (pkg, mod);
          }
          prev = mod;
        }
        entry.module (mod.fullName ());
        entry.container (mod);
      }
    }
  } 
  protected void preEmitELElement (SymtabEntry entry)
  {
  } 
  public        Vector        importTypes  = new Vector ();
  public        SymtabFactory factory;
  public static int           typedefInfo;
  public        Hashtable     list         = new Hashtable ();
  public static Compile       compiler     = null;  
} 

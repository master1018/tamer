public class ConstGen implements com.sun.tools.corba.se.idl.ConstGen
{
  public ConstGen ()
  {
  } 
  public void generate (Hashtable symbolTable, ConstEntry c, PrintWriter s)
  {
    this.symbolTable = symbolTable;
    this.c           = c;
    this.stream      = s;
    init ();
    if (c.container () instanceof ModuleEntry)
      generateConst ();
    else if (stream != null)
      writeConstExpr ();
  } 
  protected void init ()
  {
  } 
  protected void generateConst ()
  {
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } 
  protected void openStream ()
  {
    stream = Util.stream (c, ".java");
  } 
  protected void writeHeading ()
  {
    Util.writePackage (stream, c);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());
    stream.println ("public interface " + c.name ());
    stream.println ("{");
  } 
  protected void writeBody ()
  {
    writeConstExpr ();
  } 
  protected void writeConstExpr ()
  {
    if (c.comment () != null)
      c.comment ().generate ("  ", stream);
    if (c.container () instanceof ModuleEntry) {
      stream.print ("  public static final " + Util.javaName (c.type ()) + " value = ");
    } else {
      stream.print ("  public static final " + Util.javaName (c.type ()) + ' ' + c.name () + " = ");
    }
    writeConstValue (c.type ());
  } 
  private void writeConstValue (SymtabEntry type)
  {
    if (type instanceof PrimitiveEntry)
      stream.println ('(' + Util.javaName (type) + ")(" + Util.parseExpression (c.value ()) + ");");
    else if (type instanceof StringEntry)
      stream.println (Util.parseExpression (c.value ()) + ';');
    else if (type instanceof TypedefEntry)
    {
      while (type instanceof TypedefEntry)
        type = type.type ();
      writeConstValue (type);
    }
    else
      stream.println (Util.parseExpression (c.value ()) + ';');
  } 
  protected void writeClosing ()
  {
    stream.println ("}");
  } 
  protected void closeStream ()
  {
    stream.close ();
  } 
  protected java.util.Hashtable  symbolTable = null;
  protected ConstEntry           c           = null;
  protected PrintWriter          stream      = null;
} 

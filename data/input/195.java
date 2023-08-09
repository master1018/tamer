public class MC {
  private static final String VERSION = "1.0";
  private static final List<String> SUN_EXCEPTION_GROUPS = Arrays.asList(new String[]
    { "SUNBASE", "ORBUTIL", "ACTIVATION", "NAMING", "INTERCEPTORS", "POA", "IOR", "UTIL" });
  private static final List<String> EXCEPTIONS = Arrays.asList(new String[]
    { "UNKNOWN", "BAD_PARAM", "NO_MEMORY", "IMP_LIMIT", "COMM_FAILURE", "INV_OBJREF", "NO_PERMISSION",
      "INTERNAL", "MARSHAL", "INITIALIZE", "NO_IMPLEMENT", "BAD_TYPECODE", "BAD_OPERATION", "NO_RESOURCES",
      "NO_RESPONSE", "PERSIST_STORE", "BAD_INV_ORDER", "TRANSIENT", "FREE_MEM", "INV_IDENT", "INV_FLAG",
      "INTF_REPOS", "BAD_CONTEXT", "OBJ_ADAPTER", "DATA_CONVERSION", "OBJECT_NOT_EXIST", "TRANSACTION_REQUIRED",
      "TRANSACTION_ROLLEDBACK", "INVALID_TRANSACTION", "INV_POLICY", "CODESET_INCOMPATIBLE", "REBIND",
      "TIMEOUT", "TRANSACTION_UNAVAILABLE", "BAD_QOS", "INVALID_ACTIVITY", "ACTIVITY_COMPLETED",
      "ACTIVITY_REQUIRED" });
  private void makeResource(String inFile, String outDir)
  throws FileNotFoundException, IOException {
    writeResource(outDir, new Input(inFile));
  }
  private void makeClass(String inFile, String outDir)
  throws FileNotFoundException, IOException {
    writeClass(inFile, outDir, new Input(inFile));
  }
  private void writeClass(String inFile, String outDir, Input input)
    throws FileNotFoundException {
    String packageName = input.getPackageName();
    String className = input.getClassName();
    String groupName = input.getGroupName();
    Queue<InputException> exceptions = input.getExceptions();
    FileOutputStream file = new FileOutputStream(outDir + File.separator + className + ".java");
    IndentingPrintWriter pw = new IndentingPrintWriter(file);
    writeClassHeader(inFile, groupName, pw);
    pw.printMsg("package @ ;", packageName);
    pw.println();
    pw.println("import java.util.logging.Logger ;");
    pw.println("import java.util.logging.Level ;");
    pw.println();
    pw.println("import org.omg.CORBA.OMGVMCID ;");
    pw.println( "import com.sun.corba.se.impl.util.SUNVMCID ;");
    pw.println( "import org.omg.CORBA.CompletionStatus ;");
    pw.println( "import org.omg.CORBA.SystemException ;");
    pw.println();
    pw.println( "import com.sun.corba.se.spi.orb.ORB ;");
    pw.println();
    pw.println( "import com.sun.corba.se.spi.logging.LogWrapperFactory;");
    pw.println();
    pw.println( "import com.sun.corba.se.spi.logging.LogWrapperBase;");
    pw.println();
    writeImports(exceptions, pw);
    pw.println();
    pw.indent();
    pw.printMsg("public class @ extends LogWrapperBase {", className);
    pw.println();
    pw.printMsg("public @( Logger logger )", className);
    pw.indent();
    pw.println( "{");
    pw.undent();
    pw.println( "super( logger ) ;");
    pw.println( "}");
    pw.println();
    pw.flush();
    writeFactoryMethod(className, groupName, pw);
    writeExceptions(groupName, exceptions, className, pw);
    pw.undent();
    pw.println( );
    pw.println( "}");
    pw.flush();
    pw.close();
  }
  private void writeClassHeader(String inFile, String groupName,
                                IndentingPrintWriter pw) {
    if (groupName.equals("OMG"))
      pw.println("
    else
      pw.printMsg("
                  groupName);
    pw.println("
    pw.printMsg("
    pw.printMsg("
    pw.println();
  }
  private void writeImports(Queue<InputException> exceptions,
                            IndentingPrintWriter pw) {
    if (exceptions == null)
      return;
    for (InputException e : exceptions)
      pw.println("import org.omg.CORBA." + e.getName() + " ;");
  }
  private void writeFactoryMethod(String className, String groupName,
                                  IndentingPrintWriter pw) {
    pw.indent();
    pw.println( "private static LogWrapperFactory factory = new LogWrapperFactory() {");
    pw.println( "public LogWrapperBase create( Logger logger )" );
    pw.indent();
    pw.println( "{");
    pw.undent();
    pw.printMsg("return new @( logger ) ;", className);
    pw.undent();
    pw.println( "}" );
    pw.println( "} ;" );
    pw.println();
    pw.printMsg("public static @ get( ORB orb, String logDomain )", className);
    pw.indent();
    pw.println( "{");
    pw.indent();
    pw.printMsg( "@ wrapper = ", className);
    pw.indent();
    pw.printMsg( "(@) orb.getLogWrapper( logDomain, ", className);
    pw.undent();
    pw.undent();
    pw.printMsg( "\"@\", factory ) ;", groupName);
    pw.undent();
    pw.println( "return wrapper ;" );
    pw.println( "} " );
    pw.println();
    pw.printMsg( "public static @ get( String logDomain )", className);
    pw.indent();
    pw.println( "{");
    pw.indent();
    pw.printMsg( "@ wrapper = ", className);
    pw.indent();
    pw.printMsg( "(@) ORB.staticGetLogWrapper( logDomain, ", className);
    pw.undent();
    pw.undent();
    pw.printMsg( "\"@\", factory ) ;", groupName);
    pw.undent();
    pw.println( "return wrapper ;" );
    pw.println( "} " );
    pw.println();
  }
  private void writeExceptions(String groupName, Queue<InputException> exceptions,
                               String className, IndentingPrintWriter pw) {
    for (InputException e : exceptions) {
      pw.println("
      pw.printMsg("
      pw.println("
      pw.println();
      for (InputCode c : e.getCodes())
        writeMethods(groupName, e.getName(), c.getName(), c.getCode(),
                     c.getLogLevel(), className, StringUtil.countArgs(c.getMessage()), pw);
      pw.flush();
    }
  }
  private void writeMethods(String groupName, String exceptionName, String errorName,
                            int code, String level, String className, int numParams,
                            IndentingPrintWriter pw) {
    String ident = StringUtil.toMixedCase(errorName);
    pw.printMsg("public static final int @ = @ ;", errorName, getBase(groupName, code));
    pw.println();
    pw.flush();
    writeMethodStatusCause(groupName, exceptionName, errorName, ident, level,
                           numParams, className, pw);
    pw.println();
    pw.flush();
    writeMethodStatus(exceptionName, ident, numParams, pw);
    pw.println();
    pw.flush();
    writeMethodCause(exceptionName, ident, numParams, pw);
    pw.println();
    pw.flush();
    writeMethodNoArgs(exceptionName, ident, numParams, pw);
    pw.println();
    pw.flush();
  }
  private void writeMethodStatusCause(String groupName, String exceptionName,
                                      String errorName, String ident,
                                      String logLevel, int numParams,
                                      String className, IndentingPrintWriter pw) {
    pw.indent();
    pw.printMsg( "public @ @( CompletionStatus cs, Throwable t@) {", exceptionName,
                 ident, makeDeclArgs(true, numParams));
    pw.printMsg( "@ exc = new @( @, cs ) ;", exceptionName, exceptionName, errorName);
    pw.indent();
    pw.println( "if (t != null)" );
    pw.undent();
    pw.println( "exc.initCause( t ) ;" );
    pw.println();
    pw.indent();
    pw.printMsg( "if (logger.isLoggable( Level.@ )) {", logLevel);
    if (numParams > 0) {
      pw.printMsg( "Object[] parameters = new Object[@] ;", numParams);
      for (int a = 0; a < numParams; ++a)
        pw.printMsg("parameters[@] = arg@ ;", a, a);
    } else
      pw.println( "Object[] parameters = null ;");
    pw.indent();
    pw.printMsg( "doLog( Level.@, \"@.@\",", logLevel, groupName, ident);
    pw.undent();
    pw.undent();
    pw.printMsg( "parameters, @.class, exc ) ;", className);
    pw.println( "}");
    pw.println();
    pw.undent();
    pw.println( "return exc ;");
    pw.println( "}");
  }
  private void writeMethodStatus(String exceptionName, String ident,
                                 int numParams, IndentingPrintWriter pw) {
    pw.indent();
    pw.printMsg("public @ @( CompletionStatus cs@) {", exceptionName,
                ident, makeDeclArgs(true, numParams));
    pw.undent();
    pw.printMsg("return @( cs, null@ ) ;", ident, makeCallArgs(true, numParams));
    pw.println("}");
  }
  private void writeMethodCause(String exceptionName, String ident,
                                int numParams, IndentingPrintWriter pw) {
    pw.indent();
    pw.printMsg("public @ @( Throwable t@) {", exceptionName, ident,
                makeDeclArgs(true, numParams));
    pw.undent();
    pw.printMsg("return @( CompletionStatus.COMPLETED_NO, t@ ) ;", ident,
                makeCallArgs(true, numParams));
    pw.println("}");
  }
  private void writeMethodNoArgs(String exceptionName, String ident,
                                 int numParams, IndentingPrintWriter pw) {
    pw.indent();
    pw.printMsg("public @ @( @) {", exceptionName, ident,
                makeDeclArgs(false, numParams));
    pw.undent();
    pw.printMsg("return @( CompletionStatus.COMPLETED_NO, null@ ) ;",
                ident, makeCallArgs(true, numParams));
    pw.println("}");
  }
  private String makeDeclArgs(boolean leadingComma, int numArgs) {
    return makeArgString("Object arg", leadingComma, numArgs);
  }
  private String makeCallArgs(boolean leadingComma, int numArgs) {
    return makeArgString("arg", leadingComma, numArgs);
  }
  private String makeArgString(String prefixString, boolean leadingComma,
                               int numArgs) {
    if (numArgs == 0)
      return " ";
    if (numArgs == 1) {
      if (leadingComma)
        return ", " + prefixString + (numArgs - 1);
      else
        return " " + prefixString + (numArgs - 1);
    }
    return makeArgString(prefixString, leadingComma, numArgs - 1) +
      ", " + prefixString + (numArgs - 1);
  }
  private String getBase(String groupName, int code) {
    if (groupName.equals("OMG"))
      return "OMGVMCID.value + " + code;
    else
      return "SUNVMCID.value + " + (code + getSunBaseNumber(groupName));
  }
  private int getSunBaseNumber(String groupName) {
    return 200 * SUN_EXCEPTION_GROUPS.indexOf(groupName);
  }
  private void writeResource(String outDir, Input input)
    throws FileNotFoundException {
    FileOutputStream file = new FileOutputStream(outDir + File.separator +
                                                 input.getClassName() + ".resource");
    IndentingPrintWriter pw = new IndentingPrintWriter(file);
    String groupName = input.getGroupName();
    for (InputException e : input.getExceptions()) {
      String exName = e.getName();
      for (InputCode c : e.getCodes()) {
        String ident = StringUtil.toMixedCase(c.getName());
        pw.printMsg("@.@=\"@: (@) @\"", groupName, ident,
                    getMessageID(groupName, exName, c.getCode()), exName, c.getMessage());
      }
      pw.flush();
    }
    pw.close();
  }
  private String getMessageID(String groupName, String exceptionName, int code) {
    if (groupName.equals("OMG"))
      return getStandardMessageID(exceptionName, code);
    else
      return getSunMessageID(groupName, exceptionName, code);
  }
  private String getStandardMessageID(String exceptionName, int code) {
    return new Formatter().format("IOP%s0%04d", getExceptionID(exceptionName),
                                  code).toString();
  }
  private String getSunMessageID(String groupName, String exceptionName, int code) {
    return new Formatter().format("IOP%s1%04d", getExceptionID(exceptionName),
                                  getSunBaseNumber(groupName) + code).toString();
  }
  private String getExceptionID(String exceptionName) {
    return new Formatter().format("%03d", EXCEPTIONS.indexOf(exceptionName)).toString();
  }
  public static void main(String[] args)
    throws FileNotFoundException, IOException
  {
    if (args.length < 3)
      {
        System.err.println("(make-class|make-resource) <input file> <output dir>");
        System.exit(-1);
      }
    if (args[0].equals("make-class"))
      new MC().makeClass(args[1], args[2]);
    else if (args[0].equals("make-resource"))
      new MC().makeResource(args[1], args[2]);
    else
      System.err.println("Invalid command: " + args[0]);
  }
}

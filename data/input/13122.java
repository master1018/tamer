public class Arguments
{
  protected void parseOtherArgs (String[] args, Properties properties) throws InvalidArgument
  {
    if (args.length > 0)
      throw new InvalidArgument (args[0]);
  } 
    protected void setDebugFlags( String args )
    {
        StringTokenizer st = new StringTokenizer( args, "," ) ;
        while (st.hasMoreTokens()) {
            String token = st.nextToken() ;
            try {
                Field fld = this.getClass().getField( token + "DebugFlag" ) ;
                int mod = fld.getModifiers() ;
                if (Modifier.isPublic( mod ) && !Modifier.isStatic( mod ))
                    if (fld.getType() == boolean.class)
                        fld.setBoolean( this, true ) ;
            } catch (Exception exc) {
            }
        }
    }
    void parseArgs (String[] args) throws InvalidArgument {
        Vector unknownArgs = new Vector ();
        int    i           = 0;
        try {
            for (i = 0; i < args.length - 1; ++i) {
                String lcArg = args[i].toLowerCase ();
                if (lcArg.charAt (0) != '-' && lcArg.charAt (0) != '/')
                    throw new InvalidArgument (args[i]);
                if (lcArg.charAt (0) == '-' ) {
                    lcArg = lcArg.substring (1);
                }
                if (lcArg.equals ("i")) {
                    includePaths.addElement (args[++i]);
                } else if (lcArg.startsWith ("i")) {
                    includePaths.addElement (args[i].substring (2));
                } else if (lcArg.equals ("v") || lcArg.equals ("verbose")) {
                    verbose = true;
                } else if (lcArg.equals ("d")) {
                    definedSymbols.put (args[++i], "");
                } else if (lcArg.equals( "debug" )) {
                    setDebugFlags( args[++i] ) ;
                } else if (lcArg.startsWith ("d")) {
                    definedSymbols.put (args[i].substring (2), "");
                } else if (lcArg.equals ("emitall")) {
                    emitAll = true;
                } else if (lcArg.equals ("keep")) {
                    keepOldFiles = true;
                } else if (lcArg.equals ("nowarn")) {
                    noWarn = true;
                } else if (lcArg.equals ("trace")) {
                    Runtime.getRuntime ().traceMethodCalls (true);
                }
                else if ( lcArg.equals ("cppmodule")) {
                    cppModule = true;
                } else if (lcArg.equals ("version"))  {
                    versionRequest = true;
                } else if (lcArg.equals ("corba"))  {
                    if (i + 1 >= args.length)
                        throw new InvalidArgument (args[i]);
                    String level = args[++i];
                    if (level.charAt (0) == '-')
                        throw new InvalidArgument (args[i - 1]);
                    try {
                        corbaLevel = new Float (level).floatValue ();
                    } catch (NumberFormatException e) {
                        throw new InvalidArgument (args[i]);
                    }
                } else {
                    unknownArgs.addElement (args[i]);
                    ++i;
                    while (i < (args.length - 1) &&
                        args[i].charAt (0) != '-' &&
                        args[i].charAt (0) != '/') {
                        unknownArgs.addElement (args[i++]);
                    }
                    --i;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidArgument (args[args.length - 1]);
        }
        if (i == args.length - 1) {
            if (args[i].toLowerCase ().equals ("-version"))
                versionRequest = true;
            else
                file = args[i];
        } else
            throw new InvalidArgument ();
        Properties props = new Properties ();
        try {
          DataInputStream stream = FileLocator.locateFileInClassPath ("idl.config");
          props.load (stream);
          addIncludePaths (props);
        } catch (IOException e) {
        }
        String[] otherArgs;
        if (unknownArgs.size () > 0) {
            otherArgs = new String[unknownArgs.size ()];
            unknownArgs.copyInto (otherArgs);
        } else
            otherArgs = new String[0];
        parseOtherArgs (otherArgs, props);
    } 
  private void addIncludePaths (Properties props)
  {
    String paths = props.getProperty ("includes");
    if (paths != null)
    {
      String separator = System.getProperty ("path.separator");
      int end = -separator.length (); 
      do
      {
        paths = paths.substring (end + separator.length ());
        end = paths.indexOf (separator);
        if (end < 0)
          end = paths.length ();
        includePaths.addElement (paths.substring (0, end));
      }
      while (end != paths.length ());
    }
  } 
  public String file = null;
  public boolean verbose = false;
  public boolean keepOldFiles = false;
  public boolean emitAll = false;
  public Vector includePaths = new Vector ();
  public Hashtable definedSymbols = new Hashtable ();
  public boolean cppModule = false;
  public boolean versionRequest = false;  
  public float corbaLevel = 2.2f;
  public boolean noWarn = false;  
    public boolean scannerDebugFlag = false ;
    public boolean tokenDebugFlag = false ;
} 

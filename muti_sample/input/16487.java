public class NamingUtils {
    private NamingUtils() {};
    public static boolean debug = false;
    public static void dprint(String msg) {
        if (debug && debugStream != null)
            debugStream.println(msg);
    }
    public static void errprint(String msg) {
        if (errStream != null)
            errStream.println(msg);
        else
            System.err.println(msg);
    }
    public static void printException(java.lang.Exception e) {
        if (errStream != null)
            e.printStackTrace(errStream);
        else
            e.printStackTrace();
    }
    public static void makeDebugStream(File logFile)
        throws java.io.IOException {
        java.io.OutputStream logOStream =
            new java.io.FileOutputStream(logFile);
        java.io.DataOutputStream logDStream =
            new java.io.DataOutputStream(logOStream);
        debugStream = new java.io.PrintStream(logDStream);
        debugStream.println("Debug Stream Enabled.");
    }
    public static void makeErrStream(File errFile)
        throws java.io.IOException {
        if (debug) {
            java.io.OutputStream errOStream =
                new java.io.FileOutputStream(errFile);
            java.io.DataOutputStream errDStream =
                new java.io.DataOutputStream(errOStream);
            errStream = new java.io.PrintStream(errDStream);
            dprint("Error stream setup completed.");
        }
    }
    static String getDirectoryStructuredName( NameComponent[] name ) {
        StringBuffer directoryStructuredName = new StringBuffer("/");
        for( int i = 0; i < name.length; i++ ) {
            directoryStructuredName.append( name[i].id + "." + name[i].kind );
        }
        return directoryStructuredName.toString( );
    }
    public static java.io.PrintStream debugStream;
    public static java.io.PrintStream errStream;
}

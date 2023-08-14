public class LocaleTestFmwk {
    protected LocaleTestFmwk() {
        testMethods = new Hashtable();
        Method[] methods = getClass().getDeclaredMethods();
        for( int i=0; i<methods.length; i++ ) {
            if( methods[i].getName().startsWith("Test")
                    || methods[i].getName().startsWith("test")) {
                testMethods.put( methods[i].getName(), methods[i] );
            }
        }
    }
    protected void run(String[] args) throws Exception
    {
        System.out.println(getClass().getName() + " {");
        indentLevel++;
        log = new PrintWriter(System.out,true);
        Vector testsToRun = new Vector( args.length );
        for( int i=0; i<args.length; i++ ) {
            if( args[i].equals("-verbose") ) {
                verbose = true;
            }
            else if( args[i].equals("-prompt") ) {
                prompt = true;
            } else if (args[i].equals("-nothrow")) {
                nothrow = true;
            } else if (args[i].equals("-exitcode")) {
                exitcode = true;
            } else {
                Object m = testMethods.get( args[i] );
                if( m != null ) {
                    testsToRun.addElement( m );
                }
                else {
                    usage();
                    return;
                }
            }
        }
        if( testsToRun.size() == 0 ) {
            Enumeration methodNames = testMethods.elements();
            while( methodNames.hasMoreElements() ) {
                testsToRun.addElement( methodNames.nextElement() );
            }
        }
        for( int i=0; i<testsToRun.size(); i++ ) {
            int oldCount = errorCount;
            Method testMethod = (Method)testsToRun.elementAt(i);
            writeTestName(testMethod.getName());
            try {
                testMethod.invoke(this, new Object[0]);
            }
            catch( IllegalAccessException e ) {
                errln("Can't acces test method " + testMethod.getName());
            }
            catch( InvocationTargetException e ) {
                errln("Uncaught exception thrown in test method "
                               + testMethod.getName());
                e.getTargetException().printStackTrace(this.log);
            }
            writeTestResult(errorCount - oldCount);
        }
        indentLevel--;
        writeTestResult(errorCount);
        if (prompt) {
            System.out.println("Hit RETURN to exit...");
            try {
                System.in.read();
            }
            catch (IOException e) {
                System.out.println("Exception: " + e.toString() + e.getMessage());
            }
        }
        if (nothrow) {
            if (exitcode) {
                System.exit(errorCount);
            }
            if (errorCount > 0) {
                throw new IllegalArgumentException("encountered " + errorCount + " errors");
            }
        }
    }
    protected void log( String message ) {
        if( verbose ) {
            indent(indentLevel + 1);
            log.print( message );
        }
    }
    protected void logln( String message ) {
        log(message + System.getProperty("line.separator"));
    }
    protected void err( String message ) {
        errorCount++;
        indent(indentLevel + 1);
        log.print( message );
        log.flush();
        if (!nothrow) {
            throw new RuntimeException(message);
        }
    }
    protected void errln( String message ) {
        err(message + System.getProperty("line.separator"));
    }
    protected void writeTestName(String testName) {
        indent(indentLevel);
        log.print(testName);
        log.flush();
        needLineFeed = true;
    }
    protected void writeTestResult(int count) {
        if (!needLineFeed) {
            indent(indentLevel);
            log.print("}");
        }
        needLineFeed = false;
        if (count != 0)
            log.println(" FAILED");
        else
            log.println(" Passed");
    }
    private final void indent(int distance) {
        if (needLineFeed) {
            log.println(" {");
            needLineFeed = false;
        }
        log.print(spaces.substring(0, distance * 2));
    }
    void usage() {
        System.out.println(getClass().getName() +
                            ": [-verbose] [-nothrow] [-exitcode] [-prompt] [test names]");
        System.out.println("test names:");
        Enumeration methodNames = testMethods.keys();
        while( methodNames.hasMoreElements() ) {
            System.out.println("\t" + methodNames.nextElement() );
        }
    }
    private boolean     prompt = false;
    private boolean     nothrow = false;
    private boolean     exitcode = false;
    protected boolean   verbose = false;
    private PrintWriter log;
    private int         indentLevel = 0;
    private boolean     needLineFeed = false;
    private int         errorCount = 0;
    private Hashtable testMethods;
    private final String spaces = "                                          ";
}

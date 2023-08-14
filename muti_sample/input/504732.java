public class StatTestRunner extends BaseTestRunner {
    private ResultPrinter fPrinter;
    private PerfStatCollector fPerfStatCollector;
    public static final int SUCCESS_EXIT= 0;
    public static final int FAILURE_EXIT= 1;
    public static final int EXCEPTION_EXIT= 2;
    public static final String DEFAULT_DATABASE = "sqlite:/coretests.db";
    public static final String DEFAULT_DRIVER = "SQLite.JDBCDriver";
    public static String connectionURL;
    public static String jdbcDriver;
    public StatTestRunner() {
        this(System.out);
    }
    public StatTestRunner(PrintStream writer) {
        this(new ResultPrinter(writer));
    }
    public StatTestRunner(ResultPrinter printer) {
        fPrinter= printer;
        fPerfStatCollector = new PerfStatCollector(printer.getWriter());
    }
    static public void run(Class testClass) {
        run(new TestSuite(testClass));
    }
    static public TestResult run(Test test) {
        StatTestRunner runner= new StatTestRunner();
        try {
            return runner.doRun(test, false);
        }
        catch (Exception e) {
            return null;
        }
    }
    static public void runAndWait(Test suite) {
        StatTestRunner aTestRunner= new StatTestRunner();
        try {
            aTestRunner.doRun(suite, true);
        }
        catch (Exception e) {}
    }
    public TestSuiteLoader getLoader() {
        return new StandardTestSuiteLoader();
    }
    public void testFailed(int status, Test test, Throwable t) {
    }
    public void testStarted(String testName) {
    }
    public void testEnded(String testName) {
    }
    public TestResult doRun(Test suite, boolean wait) throws Exception {
        StatsStore.open(jdbcDriver, connectionURL);
        TestResult result = new TestResult();
        result.addListener(fPrinter);
        result.addListener(fPerfStatCollector);
        long startTime= System.currentTimeMillis();
        StatsStore.now = startTime;
        suite.run(result);
        long endTime= System.currentTimeMillis();
        long runTime= endTime-startTime;
        fPrinter.print(result, runTime);
        fPerfStatCollector.digest();
        StatsStore.close();
        pause(wait);
        return result;
    }
    protected void pause(boolean wait) {
        if (!wait) return;
        fPrinter.printWaitPrompt();
        try {
            System.in.read();
        }
        catch(Exception e) {
        }
    }
    public static void main(String args[]) {
        StatTestRunner aTestRunner= new StatTestRunner();
        try {
            TestResult r= aTestRunner.start(args);
            if (!r.wasSuccessful())
                System.exit(FAILURE_EXIT);
            System.exit(SUCCESS_EXIT);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            System.exit(EXCEPTION_EXIT);
        }
    }
    protected TestResult start(String args[]) throws Exception {
        String testCase= "";
        boolean wait= false;
        jdbcDriver = System.getProperty("android.coretests.driver", DEFAULT_DRIVER); 
        connectionURL = System.getProperty("android.coretests.database", "jdbc:" + DEFAULT_DATABASE); 
        for (int i= 0; i < args.length; i++) {
            if (args[i].equals("--all"))
                fPerfStatCollector.listAll = true;
            else if (args[i].equals("--bad"))
                fPerfStatCollector.listBad = true;
            else if (args[i].equals("--nobig"))
                fPerfStatCollector.bigMarking = false;
            else if (args[i].equals("--s")) {
                fPerfStatCollector.thresholdDuration =
                    Integer.valueOf(args[++i]);
            } else if (args[i].equals("-wait"))
                wait= true;
            else if (args[i].equals("-c")) 
                testCase= extractClassName(args[++i]);
            else if (args[i].equals("-v"))
                System.err.println("JUnit "+Version.id()+" (plus Android performance stats)");
            else
                testCase= args[i];
        }
        if (testCase.equals("")) 
            throw new Exception("Usage: TestRunner [-wait] testCaseName, where name is the name of the TestCase class");
        try {
            Test suite= getTest(testCase);
            return doRun(suite, wait);
        }
        catch (Exception e) {
            throw new Exception("Exception: " + e);
        }
    }
    protected void runFailed(String message) {
        System.err.println(message);
        System.exit(FAILURE_EXIT);
    }
    public void setPrinter(ResultPrinter printer) {
        fPrinter= printer;
    }
}

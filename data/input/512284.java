public class CoreTestIsolator extends TestRunner {
    public CoreTestIsolator() {
        super(new PrintStream(new OutputStream() {
            @Override
            public void write(int oneByte) throws IOException {
            }
        }));
    }
    @Override
    protected TestResult createTestResult() {
        return new TestResult();
    }
    public static void main(String args[]) {
        Logger.global.setLevel(Level.OFF);
        CoreTestIsolator testRunner = new CoreTestIsolator();
        try {
            TestResult r = testRunner.start(args);
            if (!r.wasSuccessful()) {
                Throwable failure = r.failureCount() != 0 ? 
                        ((TestFailure)r.failures().nextElement()).
                                thrownException() :
                        ((TestFailure)r.errors().nextElement()).
                                thrownException();
                saveStackTrace(failure, args[2]);
                System.exit(FAILURE_EXIT);
            } else {
                System.exit(SUCCESS_EXIT);
            }
        } catch(Exception e) {
            saveStackTrace(e, args[2]);
            System.exit(EXCEPTION_EXIT);
        }
    }
    private static void saveStackTrace(Throwable throwable, String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(throwable);
            oos.flush();
            oos.close();
        } catch (IOException ex) {
        }
    }
    @Override
    protected TestResult start(String args[]) {
        try {
            Test suite = TestSuite.createTest(Class.forName(args[0]), args[1]);
            return doRun(suite);
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to launch test", e);
        }
    }
}

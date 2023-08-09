public class CoreTestRunnable implements Runnable {
    private static boolean IS_DALVIK = "Dalvik".equals(
            System.getProperty("java.vm.name"));
    private TestCase fTest;
    private TestResult fResult;
    private Protectable fProtectable;
    private boolean fInvert;
    private boolean fIsolate;
    private Process fProcess;
    public CoreTestRunnable(TestCase test, TestResult result,
            Protectable protectable, boolean invert, boolean isolate) {
        this.fTest = test;
        this.fProtectable = protectable;
        this.fResult = result;
        this.fInvert = invert;
        this.fIsolate = isolate;
    }
    public void run() {
        try {
            if (fIsolate) {
                runExternally();
            } else {
                runInternally();
            }
            if (fInvert) {
                fInvert = false;
                throw new AssertionFailedError(
                        "@KnownFailure expected to fail, but succeeded");
            }
        } catch (AssertionFailedError e) {
            if (!fInvert) {
                fResult.addFailure(fTest, e);
            }
        } catch (ThreadDeath e) { 
            throw e;
        } catch (Throwable e) {
            if (!fInvert) {
                fResult.addError(fTest, e);
            }
        }
    }
    public void stop() {
        if (fProcess != null) {
            fProcess.destroy();
        }
    }
    private void runInternally() throws Throwable {
        fProtectable.protect();        
    }
    private void runExternally() throws Throwable {
        Throwable throwable = null;
        File file = File.createTempFile("isolation", ".tmp");
        String program = (IS_DALVIK ? "dalvikvm" : "java") +
                " -classpath " + System.getProperty("java.class.path") +
                " -Djava.home=" + System.getProperty("java.home") +
                " -Duser.home=" + System.getProperty("user.home") +
                " -Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir") +
                " com.google.coretests.CoreTestIsolator" +
                " " + fTest.getClass().getName() +
                " " + fTest.getName() +
                " " + file.getAbsolutePath();
        fProcess = Runtime.getRuntime().exec(program);
        int result = fProcess.waitFor();
        if (result != TestRunner.SUCCESS_EXIT) {
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                throwable = (Throwable)ois.readObject();
                ois.close();
            } catch (Exception ex) {
                throwable = new RuntimeException("Error isolating test: " + program, ex);
            }
        }
        file.delete();
        if (throwable != null) {
            throw throwable;
        }
    }
}

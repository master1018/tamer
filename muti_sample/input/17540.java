public class SourceDebugExtensionTest extends TestScaffold {
    ReferenceType targetClass;
    SourceDebugExtensionTest (String args[]) {
        super(args);
    }
    public static void main(String[] args)      throws Exception {
        testSetUp();
        new SourceDebugExtensionTest(args).startTests();
    }
    static void testSetUp() throws Exception {
        InstallSDE.install(new File(System.getProperty("test.classes", "."),
                                    "SourceDebugExtensionTarg.class"),
                           new File(System.getProperty("test.src", "."),
                                    "testString"));
    }
    protected void runTests() throws Exception {
        BreakpointEvent bpe = startToMain("SourceDebugExtensionTarg");
        targetClass = bpe.location().declaringType();
        if (!vm().canGetSourceDebugExtension()) {
            failure("FAIL: canGetSourceDebugExtension() is false");
        } else {
            println("canGetSourceDebugExtension() is true");
        }
        String expected = "An expected attribute string";
        String sde = targetClass.sourceDebugExtension();
        if (!sde.equals(expected)) {
            failure("FAIL: got '" + sde +
                    "' expected: '" + expected + "'");
        }
        listenUntilVMDisconnect();
        if (!testFailed) {
            println("SourceDebugExtensionTest: passed");
        } else {
            throw new Exception("SourceDebugExtensionTest: failed");
        }
    }
}

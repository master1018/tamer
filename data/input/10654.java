public class
AppendToBootstrapClassPathTest
    extends ASimpleInstrumentationTestCase
{
    public AppendToBootstrapClassPathTest(String name) {
        super(name);
    }
    public static void main (String[] args)  throws Throwable {
        ATestCaseScaffold   test = new AppendToBootstrapClassPathTest(args[0]);
        test.runTest();
    }
    protected final void doRunTest() {
        testAppendToBootstrapClassPath();
    }
    public void  testAppendToBootstrapClassPath() {
        Object instance = loadExampleClass();
        assertTrue(instance.getClass().getClassLoader() == null);
    }
    Object loadExampleClass() {
        ExampleForBootClassPath instance = new ExampleForBootClassPath();
        assertTrue(instance.fifteen() == 15);
        return instance;
    }
}

public class
AppendToClassPathTest
    extends ASimpleInstrumentationTestCase
{
    public AppendToClassPathTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new AppendToClassPathTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testAppendToClassPath();
    }
    public void
    testAppendToClassPath()
        throws  IOException,
                ClassNotFoundException
    {
        Class sentinelClass;
        ClassLoader loader = this.getClass().getClassLoader();
        sentinelClass = loader.loadClass("ExampleForClassPath");
        assertTrue(sentinelClass.getClassLoader() == ClassLoader.getSystemClassLoader());
    }
}

public class
GetInitiatedClassesTest
    extends ASimpleInstrumentationTestCase
{
    public GetInitiatedClassesTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new GetInitiatedClassesTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testGetInitiatedClasses();
    }
    public void
    testGetInitiatedClasses()
        throws  Throwable
    {
        ClassLoader loader = getClass().getClassLoader();
        Class[] classes = fInst.getInitiatedClasses(loader);
        assertNotNull(classes);
        Class dummy = loader.loadClass("DummyClass");
        assertEquals("DummyClass", dummy.getName());
        Object testInstance = dummy.newInstance();
        Class[] classes2 = fInst.getInitiatedClasses(loader);
        assertNotNull(classes2);
        assertClassArrayContainsClass(classes2, dummy);
    }
}

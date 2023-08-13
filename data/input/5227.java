public class
GetAllLoadedClassesTest
    extends ASimpleInstrumentationTestCase
{
    public GetAllLoadedClassesTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new GetAllLoadedClassesTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testGetAllLoadedClasses();
    }
    public void
    testGetAllLoadedClasses()
        throws  Throwable
    {
        ClassLoader loader = getClass().getClassLoader();
        Class[] classes = fInst.getAllLoadedClasses();
        assertNotNull(classes);
        assertClassArrayDoesNotContainClassByName(classes, "DummyClass");
        assertClassArrayContainsClass(classes, Object.class);
        Class dummy = loader.loadClass("DummyClass");
        assertEquals("DummyClass", dummy.getName());
        Object testInstance = dummy.newInstance();
        Class[] classes2 = fInst.getAllLoadedClasses();
        assertNotNull(classes2);
        assertClassArrayContainsClass(classes2, dummy);
    }
}

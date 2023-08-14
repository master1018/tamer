public class
RedefineClassesDisabledTest
    extends ASimpleInstrumentationTestCase
{
    public RedefineClassesDisabledTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new RedefineClassesDisabledTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testIsRedefineClassesSupported();
        testSimpleRedefineClasses();
    }
    public void
    testIsRedefineClassesSupported()
    {
        boolean canRedef = fInst.isRedefineClassesSupported();
        assertTrue("Can redefine classes flag set incorrectly (true)", !canRedef);
    }
    public void
    testSimpleRedefineClasses()
        throws Throwable
    {
        ExampleRedefine ex = new ExampleRedefine();
        int firstGet = ex.get();
        ex.doSomething();
        int secondGet = ex.get();
        assertEquals(firstGet, secondGet);
        File f = new File("Different_ExampleRedefine.class");
        System.out.println("Reading test class from " + f);
        InputStream redefineStream = new FileInputStream(f);
        byte[] redefineBuffer = NamedBuffer.loadBufferFromStream(redefineStream);
        ClassDefinition redefineParamBlock = new ClassDefinition(   ExampleRedefine.class,
                                                                    redefineBuffer);
        boolean caught = false;
        try {
            fInst.redefineClasses(new ClassDefinition[] {redefineParamBlock});
        } catch (UnsupportedOperationException uoe) {
            caught = true;
        }
        assertTrue(caught);
    }
}

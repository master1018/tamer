public class
RedefineClassesTests
    extends ASimpleInstrumentationTestCase
{
    public RedefineClassesTests(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new RedefineClassesTests(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testIsRedefineClassesSupported();
        testSimpleRedefineClasses();
        testUnmodifiableClassException();
    }
    public void
    testIsRedefineClassesSupported()
    {
        boolean canRedef = fInst.isRedefineClassesSupported();
        assertTrue("Cannot redefine classes", canRedef);
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
        fInst.redefineClasses(new ClassDefinition[] {redefineParamBlock});
        int thirdGet = ex.get();
        ex.doSomething();
        int fourthGet = ex.get();
        assertEquals(thirdGet + 1, fourthGet);
    }
    public void
    testUnmodifiableClassException()
        throws Throwable
    {
        System.out.println("Testing UnmodifiableClassException");
        File f = new File("Different_ExampleRedefine.class");
        InputStream redefineStream = new FileInputStream(f);
        byte[] redefineBuffer = NamedBuffer.loadBufferFromStream(redefineStream);
        System.out.println("Try to redefine class for primitive type");
        try {
            ClassDefinition redefineParamBlock =
                new ClassDefinition( byte.class, redefineBuffer );
            fInst.redefineClasses(new ClassDefinition[] {redefineParamBlock});
            fail();
        } catch (UnmodifiableClassException x) {
        }
        System.out.println("Try to redefine class for array type");
        try {
            ClassDefinition redefineParamBlock =
                new ClassDefinition( byte[].class, redefineBuffer );
            fInst.redefineClasses(new ClassDefinition[] {redefineParamBlock});
            fail();
        } catch (UnmodifiableClassException x) {
        }
    }
}

public class
NullRedefineClassesTests
    extends ASimpleInstrumentationTestCase
{
    public NullRedefineClassesTests(String name) {
        super(name);
    }
    public static void
    main (String[] args) throws Throwable {
        ATestCaseScaffold   test = new NullRedefineClassesTests(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest() throws ClassNotFoundException, UnmodifiableClassException  {
        testNullRedefineClasses();
    }
    public void
    testNullRedefineClasses() throws ClassNotFoundException, UnmodifiableClassException {
        boolean caught;
        caught = false;
        try {
            fInst.redefineClasses(null);
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            fInst.redefineClasses(new ClassDefinition[]{ null });
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        ClassDefinition cd = new ClassDefinition(DummyClass.class, new byte[] {1, 2, 3});
        try {
            fInst.redefineClasses(new ClassDefinition[]{ cd, null });
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            new ClassDefinition(null, new byte[] {1, 2, 3});
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
        caught = false;
        try {
            new ClassDefinition(DummyClass.class, null);
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
    }
}

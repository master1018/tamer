public class
NullGetObjectSizeTest
    extends ASimpleInstrumentationTestCase
{
    public NullGetObjectSizeTest(String name) {
        super(name);
    }
    public static void
    main (String[] args) throws Throwable {
        ATestCaseScaffold   test = new NullGetObjectSizeTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest() {
        testNullGetObjectSize();
    }
    public void
    testNullGetObjectSize() {
        boolean caught = false;
        try {
            fInst.getObjectSize(null);
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
    }
}

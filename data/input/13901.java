public class
NullTransformerRemoveTest
    extends ATransformerManagementTestCase
{
    public NullTransformerRemoveTest(String name) {
        super(name);
    }
    public static void
    main (String[] args)  throws Throwable {
        ATestCaseScaffold   test = new NullTransformerRemoveTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest() {
        testNullTransformerRemove();
    }
    public void
    testNullTransformerRemove() {
        boolean caught = false;
        try {
            fInst.removeTransformer(null);
        } catch (NullPointerException npe) {
            caught = true;
        }
        assertTrue(caught);
    }
}

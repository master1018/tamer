public class
NoTransformerAddedTest
    extends ATransformerManagementTestCase
{
    public NoTransformerAddedTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new NoTransformerAddedTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testNoTransformersAdded();
    }
    public void
    testNoTransformersAdded()
    {
        verifyTransformers(fInst);
    }
}

public class
SingleTransformerTest
    extends ATransformerManagementTestCase
{
    public SingleTransformerTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new SingleTransformerTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        beVerbose(); 
        testOneTransformer();
    }
    public void
    testOneTransformer()
    {
        addTransformerToManager(fInst, getRandomTransformer());
        verifyTransformers(fInst);
    }
}

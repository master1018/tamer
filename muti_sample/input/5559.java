public class
AddTransformerTest
    extends ATransformerManagementTestCase
{
    public AddTransformerTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new AddTransformerTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testAddTransformer();
    }
    public void
    testAddTransformer()
    {
        for (int i = 0; i < kTransformerSamples.length; i++)
        {
            addTransformerToManager(fInst, kTransformerSamples[i]);
        }
        verifyTransformers(fInst);
    }
}

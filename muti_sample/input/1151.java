public class
RemoveTransformerTest
    extends ATransformerManagementTestCase
{
    public RemoveTransformerTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new RemoveTransformerTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testRemoveTransformer();
    }
    public void
    testRemoveTransformer()
    {
        for (int i = 0; i < kTransformerSamples.length; i++)
        {
            addTransformerToManager(fInst, kTransformerSamples[i]);
            if (i % kModSamples == 1)
            {
                removeTransformerFromManager(fInst, kTransformerSamples[i]);
            }
        }
        verifyTransformers(fInst);
    }
}

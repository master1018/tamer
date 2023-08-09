public class
TransformMethodTest
    extends ATransformerManagementTestCase
{
    public TransformMethodTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new TransformMethodTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testTransform();
    }
    public void
    testTransform()
    {
        for (int i = 0; i < kTransformerSamples.length; i++)
        {
            ClassFileTransformer transformer = getRandomTransformer();
            addTransformerToManager(fInst, transformer);
            verifyTransformers(fInst);
            removeTransformerFromManager(fInst, transformer, true);
        }
    }
}

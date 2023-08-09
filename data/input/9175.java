public class
RemoveAbsentTransformerTest
    extends ATransformerManagementTestCase
{
    public RemoveAbsentTransformerTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new RemoveAbsentTransformerTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testRemoveNonExistentTransformer();
    }
    public void
    testRemoveNonExistentTransformer()
    {
        boolean result;
        ClassFileTransformer moreThanMeetsTheEye = new MyClassFileTransformer("NonExistent");
        addTransformerToManager(fInst, moreThanMeetsTheEye);
        removeTransformerFromManager(fInst, moreThanMeetsTheEye);
        result = fInst.removeTransformer(new MyClassFileTransformer("NonExistent2"));
        assertTrue(!result);
        result = fInst.removeTransformer(moreThanMeetsTheEye);
        assertTrue(!result);
        verifyTransformers(fInst);
    }
}

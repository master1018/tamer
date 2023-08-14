public class
NullTransformerAddTest
    extends ATransformerManagementTestCase
{
    public NullTransformerAddTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new NullTransformerAddTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testNullTransformerAdd();
    }
    public void
    testNullTransformerAdd()
    {
        boolean caughtIt = false;
        try
            {
            addTransformerToManager(fInst, null);
            }
        catch (NullPointerException npe)
            {
            caughtIt = true;
            }
        assertTrue(caughtIt);
    }
}

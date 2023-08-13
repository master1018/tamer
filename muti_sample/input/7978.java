public class
GetObjectSizeTest
    extends ASimpleInstrumentationTestCase
{
    public GetObjectSizeTest(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new GetObjectSizeTest(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testGetObjectSize();
    }
    public void
    testGetObjectSize()
    {
        Object[] objects = new Object[] {
            "Hello World",
            new Integer(8),
            this,
            new StringBuffer("Another test object"),
            new Vector(99),
            };
        for (int i = 0; i < objects.length; i++)
        {
            Object o = objects[i];
            long size = fInst.getObjectSize(o);
            assertTrue(size > 0);
        }
    }
}

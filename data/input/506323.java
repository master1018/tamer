@TestTargetClass(LogPrinter.class)
public class LogPrinterTest extends AndroidTestCase {
    private final String mTag="LogPrinterTest";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "LogPrinter",
        args = {int.class, String.class}
    )
    public void testConstructor() {
        int[] priorities = { Log.ASSERT, Log.DEBUG, Log.ERROR, Log.INFO,
                Log.VERBOSE, Log.WARN };
        for (int i = 0; i < priorities.length; i++) {
            new LogPrinter(priorities[i], mTag);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "println",
        args = {String.class}
    )
    public void testPrintln() {
        LogPrinter logPrinter = new LogPrinter(Log.DEBUG, mTag);
        String mMessage = "testMessage";
        logPrinter.println(mMessage);
    }
}

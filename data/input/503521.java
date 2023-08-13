@TestTargetClass(PrintStreamPrinter.class)
public class PrintStreamPrinterTest extends AndroidTestCase {
    private File mFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFile = new File(getContext().getFilesDir(), "PrintStreamPrinter.log");
        if (!mFile.exists()) {
            mFile.createNewFile();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PrintStreamPrinter",
        args = {PrintStream.class}
    )
    public void testConstructor() throws FileNotFoundException {
        new PrintStreamPrinter(new PrintStream(mFile));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "println",
        args = {String.class}
    )
    public void testPrintln() throws FileNotFoundException, SecurityException, IOException {
        PrintStreamPrinter printStreamPrinter = null;
        final String message = "testMessageOfPrintStreamPrinter";
        InputStream is = null;
        PrintStream ps = new PrintStream(mFile);
        printStreamPrinter = new PrintStreamPrinter(ps);
        printStreamPrinter.println(message);
        ps.flush();
        ps.close();
        String mLine;
        try {
            is = new FileInputStream(mFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            mLine = reader.readLine();
            assertEquals(message, mLine);
            reader.close();
        } finally {
            is.close();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        if (mFile.exists()) {
            mFile.delete();
        }
        super.tearDown();
    }
}

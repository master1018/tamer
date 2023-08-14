@TestTargetClass(PrintWriterPrinter.class)
public class PrintWriterPrinterTest extends AndroidTestCase {
    private File mFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File dbDir = getContext().getDir("tests", Context.MODE_PRIVATE);
        mFile = new File(dbDir,"print.log");
        if (!mFile.exists())
            mFile.createNewFile();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of PrintWriterPrinter.",
        method = "PrintWriterPrinter",
        args = {java.io.PrintWriter.class}
    )
    public void testConstructor() {
        PrintWriterPrinter printWriterPrinter = null;
        try {
            PrintWriter pw = new PrintWriter(mFile);
            printWriterPrinter = new PrintWriterPrinter(pw);
        } catch (FileNotFoundException e) {
            fail("shouldn't throw exception");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test println(String x).",
        method = "println",
        args = {java.lang.String.class}
    )
    public void testPrintln() {
        PrintWriterPrinter printWriterPrinter = null;
        String mMessage = "testMessage";
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(mFile);
            printWriterPrinter = new PrintWriterPrinter(pw);
        } catch (FileNotFoundException e) {
            fail("shouldn't throw exception");
        }
        printWriterPrinter.println(mMessage);
        pw.flush();
        pw.close();
        String mLine = "";
        try {
            InputStream is = new FileInputStream(mFile);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            mLine = reader.readLine();
        } catch (Exception e) {
        }
        assertEquals(mMessage, mLine);
    }
    @Override
    protected void tearDown() throws Exception {
        if (mFile.exists())
            mFile.delete();
    }
}

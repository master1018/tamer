public class FileUtilsTest extends AndroidTestCase {
    private static final String TEST_DATA =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private File mTestFile;
    private File mCopyFile;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File testDir = getContext().getDir("testing", Context.MODE_PRIVATE);
        mTestFile = new File(testDir, "test.file");
        mCopyFile = new File(testDir, "copy.file");
        FileWriter writer = new FileWriter(mTestFile);
        try {
            writer.write(TEST_DATA, 0, TEST_DATA.length());
        } finally {
            writer.close();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        if (mTestFile.exists()) mTestFile.delete();
        if (mCopyFile.exists()) mCopyFile.delete();
    }
    @LargeTest
    public void testGetFileStatus() {
        final byte[] MAGIC = { 0xB, 0xE, 0x0, 0x5 };
        try {
            FileOutputStream os = new FileOutputStream(mTestFile, false);
            os.write(MAGIC, 0, 4);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            Assert.fail("File was removed durning test" + e);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
        Assert.assertTrue(mTestFile.exists());
        Assert.assertTrue(FileUtils.getFileStatus(mTestFile.getPath(), null));
        FileStatus status1 = new FileStatus();
        FileUtils.getFileStatus(mTestFile.getPath(), status1);
        Assert.assertEquals(4, status1.size);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        try {
            FileOutputStream os = new FileOutputStream(mTestFile, true);
            os.write(MAGIC, 0, 4);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            Assert.fail("File was removed durning test" + e);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
        FileStatus status2 = new FileStatus();
        FileUtils.getFileStatus(mTestFile.getPath(), status2);
        Assert.assertEquals(8, status2.size);
        Assert.assertTrue(status2.mtime > status1.mtime);
        mTestFile.delete();
        Assert.assertFalse(mTestFile.exists());
        Assert.assertFalse(FileUtils.getFileStatus(mTestFile.getPath(), null));
    }
    @MediumTest
    public void testCopyFile() throws Exception {
        assertFalse(mCopyFile.exists());
        FileUtils.copyFile(mTestFile, mCopyFile);
        assertTrue(mCopyFile.exists());
        assertEquals(TEST_DATA, FileUtils.readTextFile(mCopyFile, 0, null));
    }
    @MediumTest
    public void testCopyToFile() throws Exception {
        final String s = "Foo Bar";
        assertFalse(mCopyFile.exists());
        FileUtils.copyToFile(new ByteArrayInputStream(s.getBytes()), mCopyFile);        assertTrue(mCopyFile.exists());
        assertEquals(s, FileUtils.readTextFile(mCopyFile, 0, null));
    }
    @MediumTest
    public void testIsFilenameSafe() throws Exception {
        assertTrue(FileUtils.isFilenameSafe(new File("foobar")));
        assertTrue(FileUtils.isFilenameSafe(new File("a_b-c=d.e/0,1+23")));
        assertFalse(FileUtils.isFilenameSafe(new File("foo*bar")));
        assertFalse(FileUtils.isFilenameSafe(new File("foo\nbar")));
    }
    @MediumTest
    public void testReadTextFile() throws Exception {
        assertEquals(TEST_DATA, FileUtils.readTextFile(mTestFile, 0, null));
        assertEquals("ABCDE", FileUtils.readTextFile(mTestFile, 5, null));
        assertEquals("ABCDE<>", FileUtils.readTextFile(mTestFile, 5, "<>"));
        assertEquals(TEST_DATA.substring(0, 51) + "<>",
                FileUtils.readTextFile(mTestFile, 51, "<>"));
        assertEquals(TEST_DATA, FileUtils.readTextFile(mTestFile, 52, "<>"));
        assertEquals(TEST_DATA, FileUtils.readTextFile(mTestFile, 100, "<>"));
        assertEquals("vwxyz", FileUtils.readTextFile(mTestFile, -5, null));
        assertEquals("<>vwxyz", FileUtils.readTextFile(mTestFile, -5, "<>"));
        assertEquals("<>" + TEST_DATA.substring(1, 52),
                FileUtils.readTextFile(mTestFile, -51, "<>"));
        assertEquals(TEST_DATA, FileUtils.readTextFile(mTestFile, -52, "<>"));
        assertEquals(TEST_DATA, FileUtils.readTextFile(mTestFile, -100, "<>"));
    }
    @MediumTest
    public void testReadTextFileWithZeroLengthFile() throws Exception {
        new FileOutputStream(mTestFile).close();  
        assertEquals("", FileUtils.readTextFile(mTestFile, 0, null));
        assertEquals("", FileUtils.readTextFile(mTestFile, 1, "<>"));
        assertEquals("", FileUtils.readTextFile(mTestFile, 10, "<>"));
        assertEquals("", FileUtils.readTextFile(mTestFile, -1, "<>"));
        assertEquals("", FileUtils.readTextFile(mTestFile, -10, "<>"));
    }
}

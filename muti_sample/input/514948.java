@TestTargetClass(FileReader.class) 
public class FileReaderTest extends junit.framework.TestCase {
    FileReader br;
    BufferedWriter bw;
    FileInputStream fis;
    File f;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileReader",
        args = {java.io.File.class}
    )     
    public void test_ConstructorLjava_io_File() {
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new FileReader(f);
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Test 1: Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
        File noFile = new File(System.getProperty("java.io.tmpdir"), "noreader.tst");
        try {
            br = new FileReader(noFile);
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies FileReader(java.io.FileDescriptor) constructor.",
        method = "FileReader",
        args = {java.io.FileDescriptor.class}
    )     
    public void test_ConstructorLjava_io_FileDescriptor() {
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            FileInputStream fis = new FileInputStream(f.getPath());
            br = new FileReader(fis.getFD());
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            fis.close();
            assertEquals("Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileReader",
        args = {java.lang.String.class}
    )         
    public void test_ConstructorLjava_lang_String() {
        try {
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new FileReader(f.getPath());
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Test 1: Failed to read correct chars", 
                    " After test string", new String(buf, 0, r));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
        try {
            br = new FileReader(System.getProperty("java.io.tmpdir") + "/noreader.tst");
            fail("Test 2: FileNotFoundException expected.");
        } catch (FileNotFoundException e) {
        }
      }
    protected void setUp() {
        f = new File(System.getProperty("java.io.tmpdir"), "reader.tst");
        if (f.exists()) {
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
        }
    }
    protected void tearDown() {
        try {
            bw.close();
            br.close();
        } catch (Exception e) {
        }
        try {
            if (fis != null)
                fis.close();
        } catch (Exception e) {
        }
        f.delete();
    }
}

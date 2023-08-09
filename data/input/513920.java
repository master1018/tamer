@TestTargetClass(FileWriter.class) 
public class FileWriterTest extends junit.framework.TestCase {
    FileWriter fw;
    FileInputStream fis;
    BufferedWriter bw;
    File f;
    FileOutputStream fos;
    BufferedReader br;
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.io.File.class}
    )      
    public void test_ConstructorLjava_io_File() {
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to write correct chars", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.io.File.class}
    )      
    public void test_ConstructorLjava_io_File_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        try {
            fw = new FileWriter(dir);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies FileWriter(java.io.File, boolean) constructor.",
        method = "FileWriter",
        args = {java.io.File.class, boolean.class}
    )              
    public void test_ConstructorLjava_io_FileZ() throws Exception {
        {
            FileWriter fileWriter = new FileWriter(f);
            String first = "The first string for testing. ";
            fileWriter.write(first);
            fileWriter.close();
            fileWriter = new FileWriter(f, true);
            String second = "The second String for testing.";
            fileWriter.write(second);
            fileWriter.close();
            FileReader fileReader = new FileReader(f);
            char[] out = new char[first.length() + second.length() + 10];
            int length = fileReader.read(out);
            fileReader.close();
            assertEquals(first + second, new String(out, 0, length));
        }
        {
            FileWriter fileWriter = new FileWriter(f);
            String first = "The first string for testing. ";
            fileWriter.write(first);
            fileWriter.close();
            fileWriter = new FileWriter(f, false);
            String second = "The second String for testing.";
            fileWriter.write(second);
            fileWriter.close();
            FileReader fileReader = new FileReader(f);
            char[] out = new char[first.length() + second.length() + 10];
            int length = fileReader.read(out);
            fileReader.close();
            assertEquals(second, new String(out, 0, length));
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.io.File.class, boolean.class}
    )      
    public void test_ConstructorLjava_io_FileZ_IOException() {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        try {
            fw = new FileWriter(dir, true);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies FileWriter(java.io.FileDescriptor) constructor.",
        method = "FileWriter",
        args = {java.io.FileDescriptor.class}
    )          
    public void test_ConstructorLjava_io_FileDescriptor() {
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            fis = new FileInputStream(f.getPath());
            br = new BufferedReader(new FileReader(fis.getFD()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            fis.close();
            assertTrue("Failed to write correct chars: "
                    + new String(buf, 0, r), new String(buf, 0, r)
                    .equals("Test String"));
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.lang.String.class}
    )     
    public void test_ConstructorLjava_lang_String() {
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath()));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to write correct chars", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.lang.String.class}
    )      
    public void test_ConstructorLjava_lang_String_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"));
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        method = "FileWriter",
        args = {java.lang.String.class, boolean.class}
    )       
    public void test_ConstructorLjava_lang_StringZ() {
        try {
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath(), true));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            char[] buf = new char[100];
            int r = br.read(buf);
            br.close();
            assertEquals("Failed to append to file", "Test String After test string", new String(buf, 0, r)
                    );
            fos = new FileOutputStream(f.getPath());
            fos.write("Test String".getBytes());
            fos.close();
            bw = new BufferedWriter(new FileWriter(f.getPath(), false));
            bw.write(" After test string", 0, 18);
            bw.close();
            br = new BufferedReader(new FileReader(f.getPath()));
            buf = new char[100];
            r = br.read(buf);
            br.close();
            assertEquals("Failed to overwrite file", " After test string", new String(buf, 0, r)
                    );
        } catch (Exception e) {
            fail("Exception during Constructor test " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Checks IOException.",
        method = "FileWriter",
        args = {java.lang.String.class, boolean.class}
    )      
    public void test_ConstructorLjava_lang_StringZ_IOException() {
        try {
            fw = new FileWriter(System.getProperty("java.io.tmpdir"), false);
            fail("Test 1: IOException expected.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "write",
        args = {char[].class, int.class, int.class}
    )
    public void test_handleEarlyEOFChar_1() {
        String str = "All work and no play makes Jack a dull boy\n"; 
        int NUMBER = 2048;
        int j = 0;
        int len = str.length() * NUMBER;
    protected void setUp() throws Exception {
        f = File.createTempFile("writer", ".tst");
        if (f.exists())
            if (!f.delete()) {
                fail("Unable to delete test file");
            }
    }
    protected void tearDown() {
        try {
            bw.close();
        } catch (Exception e) {
        }
        try {
            fis.close();
        } catch (Exception e) {
        }
        f.delete();
    }
}

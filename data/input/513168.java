@TestTargetClass(RandomAccessFile.class) 
public class OpenRandomFileTest extends TestCase {
    public static void main(String[] args) {
        new OpenRandomFileTest().testOpenEmptyFile();
    }
    public OpenRandomFileTest() {
        super();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "RandomAccessFile",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testOpenNonEmptyFile() {
        try {
            File file = File.createTempFile("test", "tmp");
            assertTrue(file.exists());
            file.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
            fos.close();
            String fileName = file.getCanonicalPath();
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.close();
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "RandomAccessFile",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testOpenEmptyFile() {
        try {
            File file = File.createTempFile("test", "tmp");
            assertTrue(file.exists());
            file.deleteOnExit();
            String fileName = file.getCanonicalPath();
            RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
            raf.close();
        } catch (IOException ex) {
            fail(ex.getLocalizedMessage());
        }
    }
}

@TestTargetClass(FileDescriptor.class) 
public class FileDescriptorTest extends junit.framework.TestCase {
    private static String platformId = "JDK"
            + System.getProperty("java.vm.version").replace('.', '-');
    FileOutputStream fos;
    BufferedOutputStream os;
    FileInputStream fis;
    File f;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "FileDescriptor",
        args = {}
    )     
    public void test_Constructor() {
        FileDescriptor fd = new FileDescriptor();
        assertTrue("Failed to create FileDescriptor",
                fd instanceof FileDescriptor);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "SyncFailedException not checked since it is only thrown" +
                "by the native implementation of sync().",
        method = "sync",
        args = {}
    )    
       public void test_sync() throws Exception {
        f = File.createTempFile("fd" + platformId, ".tst");
        f.delete();
        fos = new FileOutputStream(f.getPath());
        fos.write("Test String".getBytes());
        fis = new FileInputStream(f.getPath());
        FileDescriptor fd = fos.getFD();
        fd.sync();
        int length = "Test String".length();
        assertEquals("Bytes were not written after sync", length, fis
                .available());
        fd = fis.getFD();
        fd.sync();
        assertEquals("Bytes were not written after sync", length, fis
                .available());
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        fd = raf.getFD(); 
        fd.sync();
        raf.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valid",
        args = {}
    )        
    public void test_valid() {
        try {
            f = new File(System.getProperty("java.io.tmpdir"), "fd.tst");
            f.delete();
            os = new BufferedOutputStream(fos = new FileOutputStream(f
                    .getPath()), 4096);
            FileDescriptor fd = fos.getFD();
            assertTrue("Valid fd returned false", fd.valid());
            os.close();
            assertTrue("Invalid fd returned true", !fd.valid());
        } catch (Exception e) {
            fail("Exception during test : " + e.getMessage());
        }
    }
    protected void setUp() {
    }
    protected void tearDown() {
        try {
            os.close();
        } catch (Exception e) {
        }
        try {
            fis.close();
        } catch (Exception e) {
        }
        try {
            fos.close();
        } catch (Exception e) {
        }
        try {
            f.delete();
        } catch (Exception e) {
        }
    }
    protected void doneSuite() {
    }
}

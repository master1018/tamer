@TestTargetClass(java.io.FileInputStream.class)
public class JavaIoFileInputStreamTest extends TestCase {
    SecurityManager old;
    @Override
    protected void setUp() throws Exception {
        old = System.getSecurityManager();
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        System.setSecurityManager(old);
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileInputStream() constructor calls checkRead method on security manager.",
            method = "FileInputStream",
            args = {java.io.FileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileInputStream() constructor calls checkRead method on security manager.",
            method = "FileInputStream",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileInputStream() constructor calls checkRead method on security manager.",
            method = "FileInputStream",
            args = {java.io.File.class}
        )
    })
    public void test_FileInputStream() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String file;
            FileDescriptor fd;
            void reset(){
                called = false;
                file = null;
                fd = null;
            }
            @Override
            public void checkRead(FileDescriptor fd) {
                called = true;
                this.fd = fd;
                super.checkRead(fd);
            }
            @Override
            public void checkRead(String file){
                called = true;
                this.file = file;
                super.checkRead(file);
            }
            @Override
            public void checkPermission(Permission p) {
            }
        }
        long id = new java.util.Date().getTime();
        String filename = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        f.deleteOnExit();
        filename = f.getCanonicalPath();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        FileDescriptor fd = new FileDescriptor();
        new FileInputStream(fd);
        assertTrue("FileInputStream(FileDescriptor) ctor must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", fd, s.fd);
        s.reset();
        new FileInputStream(filename);
        assertTrue("FileInputStream(String) ctor must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        s.reset();
        new FileInputStream(f);
        assertTrue("FileInputStream(File) ctor must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
    }
}

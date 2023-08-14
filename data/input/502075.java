@TestTargetClass(java.io.FileOutputStream.class)
public class JavaIoFileOutputStreamTest extends TestCase {
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
            notes = "Verifies that FileOutputStream constructor calls checkRead on security manager.",
            method = "FileOutputStream",
            args = {java.io.FileDescriptor.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileOutputStream constructor calls checkRead on security manager.",
            method = "FileOutputStream",
            args = {java.io.File.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that FileOutputStream constructor calls checkRead on security manager.",
                method = "FileOutputStream",
                args = {java.io.File.class, boolean.class}
            ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileOutputStream constructor calls checkRead on security manager.",
            method = "FileOutputStream",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that FileOutputStream constructor calls checkRead on security manager.",
            method = "FileOutputStream",
            args = {java.lang.String.class, boolean.class}
        )
    })    
    public void test_FileOutputStream1() throws IOException {
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
            public void checkWrite(FileDescriptor fd) {
                called = true;
                this.fd = fd;
                super.checkWrite(fd);
            }
            @Override
            public void checkWrite(String file){
                called = true;
                this.file = file;
                super.checkWrite(file);
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
        new FileOutputStream(fd);
        assertTrue("FileOutputStream(FileDescriptor) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", fd, s.fd);
        s.reset();
        new FileOutputStream(f);
        assertTrue("FileOutputStream(File) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file); 
        s.reset();
        new FileOutputStream(f, true);
        assertTrue("FileOutputStream(File) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file); 
        s.reset();
        new FileOutputStream(filename);
        assertTrue("FileOutputStream(String) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file);
        s.reset();
        new FileOutputStream(filename, true);
        assertTrue("FileOutputStream(String,boolean) ctor must call checkWrite on security manager", s.called);
        assertEquals("Argument of checkWrite is not correct", filename, s.file); 
    }
}

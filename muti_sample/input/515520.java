@TestTargetClass(java.io.RandomAccessFile.class)
public class JavaIoRandomAccessFileTest extends TestCase {
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
            notes = "Verifies that RandomAccessFile constructor calls checkRead method of security manager.",
            method = "RandomAccessFile",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies that RandomAccessFile constructor calls checkRead method of security manager.",
            method = "RandomAccessFile",
            args = {java.io.File.class, java.lang.String.class}
        )
    })
    public void test_RandomAccessFile1() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            String file;
            void reset(){
                called = false;
                file = null;
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
        new RandomAccessFile(filename, "r");
        assertTrue("RandomAccessFile(String,String) ctor must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
        s.reset();
        new RandomAccessFile(f, "r");
        assertTrue("RandomAccessFile(File, String) ctor must call checkRead on security manager", s.called);
        assertEquals("Argument of checkRead is not correct", filename, s.file);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that RandomAccessFile constructor calls checkRead and checkWrite on security manager.",
        method = "RandomAccessFile",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_RandomAccessFile2() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean checkReadCalled;
            boolean checkWriteCalled;
            String checkReadFile;
            String checkWriteFile;
            void reset(){
                checkReadCalled = false;
                checkWriteCalled = false;
                checkReadFile = null;
                checkWriteFile = null;
            }
            @Override
            public void checkRead(String file) {
                checkReadCalled = true;
                this.checkReadFile = file;
                super.checkRead(file);
            }
            @Override
            public void checkWrite(String file) {
                checkWriteCalled = true;
                this.checkWriteFile = file;
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
        new RandomAccessFile(filename, "rw");
        assertTrue("RandomAccessFile(String,String) ctor must call checkRead on security manager", s.checkReadCalled);
        assertTrue("RandomAccessFile(String,String) ctor must call checkWrite on security manager", s.checkWriteCalled);
        assertEquals("Argument of checkRead is not correct", filename, s.checkReadFile);
        assertEquals("Argument of checkWrite is not correct", filename, s.checkWriteFile);
    }
}

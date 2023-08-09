@TestTargetClass(java.util.zip.ZipFile.class)
public class JavaUtilZipZipFile extends TestCase {
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
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that the constructor java.util.zip.ZipFile() calls checkRead on the security manager.",
        method = "ZipFile",
        args = {java.lang.String.class}
    )
    public void test_ZipFile() throws IOException {
        class TestSecurityManager extends SecurityManager {
            private boolean called = false;
            private String name = null;
            void reset(){
                called = false;
                name = null;
            }
            String getName(){return name;}
            @Override
            public void checkRead(String name) {
                called = true;
                this.name = name;
                super.checkRead(name);
            }
            @Override
            public void checkPermission(Permission permission) {
            }
        }
        File file = File.createTempFile("foo", "zip");
        String filename = file.getAbsolutePath();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        try {
            new ZipFile(filename);
        } catch (ZipException ex) {
        }
        assertTrue("java.util.zip.ZipFile() construcor must call checkRead on security permissions", s.called);
        assertEquals("Argument of checkPermission is not correct", filename, s.getName());
    }
}

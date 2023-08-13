@TestTargetClass(java.io.ObjectInputStream.class)
public class JavaIoObjectInputStreamTest extends TestCase {
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
    private static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        public Node(){}
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that ObjectInputStream.enableResolveObject method calls checkPermission on security manager.",
        method = "enableResolveObject",
        args = {boolean.class}
    )
    public void test_ObjectInputStream() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission permission;
            void reset(){
                called = false;
                permission = null;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof SerializablePermission){
                    called = true;
                    this.permission = permission;
                }
            }
        }
        class TestObjectInputStream extends ObjectInputStream  {
            TestObjectInputStream(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public boolean enableResolveObject(boolean enable) throws SecurityException {
                return super.enableResolveObject(enable);
            }
        }
        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(new Node());
        oos.flush();
        oos.close();
        f.deleteOnExit();
        TestObjectInputStream ois = new TestObjectInputStream(new FileInputStream(f));
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        ois.enableResolveObject(true);
        assertTrue("ObjectInputStream.enableResolveObject(boolean) must call checkPermission on security manager", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubstitution", s.permission.getName());
    }
    @TestTargets({
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                notes = "Verifies that the ObjectInputStream constructor calls checkPermission on security manager.",
                method = "ObjectInputStream",
                args = {InputStream.class}
        )
    })
    public void test_ObjectInputStream2() throws IOException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            Permission permission;
            void reset(){
                called = false;
                permission = null;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof SerializablePermission){
                    called = true;
                    this.permission = permission;
                }
            }
        }
        class TestObjectInputStream extends ObjectInputStream  {
            TestObjectInputStream(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
        }
        class TestObjectInputStream_readFields extends ObjectInputStream  {
            TestObjectInputStream_readFields(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public GetField readFields() throws IOException, ClassNotFoundException, NotActiveException {
                return super.readFields();
            }
        }
        class TestObjectInputStream_readUnshared extends ObjectInputStream  {
            TestObjectInputStream_readUnshared(InputStream s) throws StreamCorruptedException, IOException {
                super(s);
            }
            @Override
            public Object readUnshared() throws IOException, ClassNotFoundException {
                return super.readUnshared();
            }   
        }
        long id = new java.util.Date().getTime();
        String filename  = "SecurityPermissionsTest_"+id;
        File f = File.createTempFile(filename, null);
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(new Node());
        oos.flush();
        oos.close();
        f.deleteOnExit();
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        new ObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        s.reset();
        new TestObjectInputStream(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must not call checkPermission on security manager on a class which neither overwrites methods readFields nor readUnshared", !s.called);
        s.reset();
        new TestObjectInputStream_readFields(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must call checkPermission on security manager on a class which overwrites method readFields", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
        s.reset();
        new TestObjectInputStream_readUnshared(new FileInputStream(f));
        assertTrue("ObjectInputStream(InputStream) ctor must call checkPermission on security manager on a class which overwrites method readUnshared", s.called);
        assertEquals("Name of SerializablePermission is not correct", "enableSubclassImplementation", s.permission.getName());
    }
}

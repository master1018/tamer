@TestTargetClass(java.lang.ClassLoader.class)
public class JavaLangClassLoaderTest extends TestCase {
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
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader constructor calls checkCreateClassLoader on security manager.",
            method = "ClassLoader",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader constructor calls checkCreateClassLoader on security manager.",
            method = "ClassLoader",
            args = {java.lang.ClassLoader.class}
        )
    })
    public void test_ClassLoaderCtor () {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkCreateClassLoader(){
                called = true;
            }
            @Override
            public void checkPermission(Permission p) {
            }
        }
        class MyClassLoader extends ClassLoader { 
            MyClassLoader(){super();}
            MyClassLoader(ClassLoader parent){super(parent);}            
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        s.reset();
        ClassLoader c1 = new MyClassLoader();
        assertTrue("ClassLoader ctor must call checkCreateClassLoader on security manager", s.called);
        s.reset();
        ClassLoader c2 = new MyClassLoader(c1);
        assertTrue("ClassLoader ctor must call checkCreateClassLoader on security manager", s.called);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader.getSystemClassLoader() calls checkPermission on security manager.",
            method = "getSystemClassLoader",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Verifies that ClassLoader.getParent() calls checkPermission on security manager.",
            method = "getParent",
            args = {}
        )
    })
    @AndroidOnly("uses DexFile")
    public void test_getSystemClassLoader () throws IOException,
            IllegalAccessException, InstantiationException {
        class TestSecurityManager extends SecurityManager {
            boolean called;
            void reset(){
                called = false;
            }
            @Override
            public void checkPermission(Permission permission){
                if(permission instanceof RuntimePermission &&
                        "getClassLoader".equals(permission.getName())){
                    called = true;
                }
            }
        }
        TestSecurityManager s = new TestSecurityManager();
        System.setSecurityManager(s);
        File tempFile = Support_Resources.createTempFile(".jar");
        tempFile.delete();
        tempFile.deleteOnExit();
        File tempCache = Support_Resources.createTempFile(".dex");
        tempCache.delete();
        tempCache.deleteOnExit();
        InputStream is = Support_Resources.getResourceStream("testdex.jar");
        Support_Resources.copyLocalFileto(tempFile, is);
        DexFile dexfile = DexFile.loadDex(tempFile.getAbsolutePath(),
                tempCache.getAbsolutePath(), 0);
        ClassLoader pcl = Support_ClassLoader.getInstance(
                new URL(Support_Resources.getResourceURL("testdex.jar")),
                ClassLoader.getSystemClassLoader());
        Class<?> testClass = dexfile.loadClass(
                "tests/security/permissions/resources/TestClass1", pcl);
        assertNotNull("failed to load TestlClass1", testClass);
        s.reset();
        testClass.newInstance();
        assertTrue("ClassLoader.getSystemClassLoader() must call "
                + "checkPermission on security manager", s.called);
        testClass = dexfile.loadClass(
                "tests/security/permissions/resources/TestClass2", pcl);
        assertNotNull("failed to load TestClass2", testClass);
        s.reset();
        testClass.newInstance();
        assertTrue("Method getParent on a class loader must call "
                + "checkPermission on security manager", s.called);
    }
}

@TestTargetClass(value=SecureClassLoader.class,
        untestedMethods={
            @TestTargetNew(
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "cannot be tested",
                    method = "defineClass",
                    args = {
                        java.lang.String.class, byte[].class, int.class, 
                        int.class, java.security.CodeSource.class}
            ),
            @TestTargetNew(
                    level = TestLevel.NOT_FEASIBLE,
                    notes = "cannot be tested",
                    method = "defineClass",
                    args = {
                        java.lang.String.class, java.nio.ByteBuffer.class,
                        java.security.CodeSource.class}
            )           
})
public class SecureClassLoaderTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(SecureClassLoaderTest.class);
    }
    private static final String klassName = "HiWorld";
    private static final byte[] klassData = { (byte) 0xCA, (byte) 0xFE,
            (byte) 0xBA, (byte) 0xBE, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x2E, (byte) 0x00, (byte) 0x22, (byte) 0x01, (byte) 0x00,
            (byte) 0x07, (byte) 0x48, (byte) 0x69, (byte) 0x57, (byte) 0x6F,
            (byte) 0x72, (byte) 0x6C, (byte) 0x64, (byte) 0x07, (byte) 0x00,
            (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x6A,
            (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C,
            (byte) 0x61, (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x4F,
            (byte) 0x62, (byte) 0x6A, (byte) 0x65, (byte) 0x63, (byte) 0x74,
            (byte) 0x07, (byte) 0x00, (byte) 0x03, (byte) 0x01, (byte) 0x00,
            (byte) 0x06, (byte) 0x3C, (byte) 0x69, (byte) 0x6E, (byte) 0x69,
            (byte) 0x74, (byte) 0x3E, (byte) 0x01, (byte) 0x00, (byte) 0x03,
            (byte) 0x28, (byte) 0x29, (byte) 0x56, (byte) 0x01, (byte) 0x00,
            (byte) 0x04, (byte) 0x43, (byte) 0x6F, (byte) 0x64, (byte) 0x65,
            (byte) 0x0C, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x06,
            (byte) 0x0A, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x08,
            (byte) 0x01, (byte) 0x00, (byte) 0x0F, (byte) 0x4C, (byte) 0x69,
            (byte) 0x6E, (byte) 0x65, (byte) 0x4E, (byte) 0x75, (byte) 0x6D,
            (byte) 0x62, (byte) 0x65, (byte) 0x72, (byte) 0x54, (byte) 0x61,
            (byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x01, (byte) 0x00,
            (byte) 0x12, (byte) 0x4C, (byte) 0x6F, (byte) 0x63, (byte) 0x61,
            (byte) 0x6C, (byte) 0x56, (byte) 0x61, (byte) 0x72, (byte) 0x69,
            (byte) 0x61, (byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x54,
            (byte) 0x61, (byte) 0x62, (byte) 0x6C, (byte) 0x65, (byte) 0x01,
            (byte) 0x00, (byte) 0x04, (byte) 0x74, (byte) 0x68, (byte) 0x69,
            (byte) 0x73, (byte) 0x01, (byte) 0x00, (byte) 0x09, (byte) 0x4C,
            (byte) 0x48, (byte) 0x69, (byte) 0x57, (byte) 0x6F, (byte) 0x72,
            (byte) 0x6C, (byte) 0x64, (byte) 0x3B, (byte) 0x01, (byte) 0x00,
            (byte) 0x04, (byte) 0x6D, (byte) 0x61, (byte) 0x69, (byte) 0x6E,
            (byte) 0x01, (byte) 0x00, (byte) 0x16, (byte) 0x28, (byte) 0x5B,
            (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61,
            (byte) 0x2F, (byte) 0x6C, (byte) 0x61, (byte) 0x6E, (byte) 0x67,
            (byte) 0x2F, (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x69,
            (byte) 0x6E, (byte) 0x67, (byte) 0x3B, (byte) 0x29, (byte) 0x56,
            (byte) 0x01, (byte) 0x00, (byte) 0x10, (byte) 0x6A, (byte) 0x61,
            (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61,
            (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x53, (byte) 0x79,
            (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x6D, (byte) 0x07,
            (byte) 0x00, (byte) 0x10, (byte) 0x01, (byte) 0x00, (byte) 0x03,
            (byte) 0x6F, (byte) 0x75, (byte) 0x74, (byte) 0x01, (byte) 0x00,
            (byte) 0x15, (byte) 0x4C, (byte) 0x6A, (byte) 0x61, (byte) 0x76,
            (byte) 0x61, (byte) 0x2F, (byte) 0x69, (byte) 0x6F, (byte) 0x2F,
            (byte) 0x50, (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x74,
            (byte) 0x53, (byte) 0x74, (byte) 0x72, (byte) 0x65, (byte) 0x61,
            (byte) 0x6D, (byte) 0x3B, (byte) 0x0C, (byte) 0x00, (byte) 0x12,
            (byte) 0x00, (byte) 0x13, (byte) 0x09, (byte) 0x00, (byte) 0x11,
            (byte) 0x00, (byte) 0x14, (byte) 0x01, (byte) 0x00, (byte) 0x0A,
            (byte) 0x48, (byte) 0x69, (byte) 0x2C, (byte) 0x20, (byte) 0x77,
            (byte) 0x6F, (byte) 0x72, (byte) 0x6C, (byte) 0x64, (byte) 0x21,
            (byte) 0x08, (byte) 0x00, (byte) 0x16, (byte) 0x01, (byte) 0x00,
            (byte) 0x13, (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61,
            (byte) 0x2F, (byte) 0x69, (byte) 0x6F, (byte) 0x2F, (byte) 0x50,
            (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x74, (byte) 0x53,
            (byte) 0x74, (byte) 0x72, (byte) 0x65, (byte) 0x61, (byte) 0x6D,
            (byte) 0x07, (byte) 0x00, (byte) 0x18, (byte) 0x01, (byte) 0x00,
            (byte) 0x07, (byte) 0x70, (byte) 0x72, (byte) 0x69, (byte) 0x6E,
            (byte) 0x74, (byte) 0x6C, (byte) 0x6E, (byte) 0x01, (byte) 0x00,
            (byte) 0x15, (byte) 0x28, (byte) 0x4C, (byte) 0x6A, (byte) 0x61,
            (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C, (byte) 0x61,
            (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x53, (byte) 0x74,
            (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x67, (byte) 0x3B,
            (byte) 0x29, (byte) 0x56, (byte) 0x0C, (byte) 0x00, (byte) 0x1A,
            (byte) 0x00, (byte) 0x1B, (byte) 0x0A, (byte) 0x00, (byte) 0x19,
            (byte) 0x00, (byte) 0x1C, (byte) 0x01, (byte) 0x00, (byte) 0x04,
            (byte) 0x61, (byte) 0x72, (byte) 0x67, (byte) 0x73, (byte) 0x01,
            (byte) 0x00, (byte) 0x13, (byte) 0x5B, (byte) 0x4C, (byte) 0x6A,
            (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x2F, (byte) 0x6C,
            (byte) 0x61, (byte) 0x6E, (byte) 0x67, (byte) 0x2F, (byte) 0x53,
            (byte) 0x74, (byte) 0x72, (byte) 0x69, (byte) 0x6E, (byte) 0x67,
            (byte) 0x3B, (byte) 0x01, (byte) 0x00, (byte) 0x0A, (byte) 0x53,
            (byte) 0x6F, (byte) 0x75, (byte) 0x72, (byte) 0x63, (byte) 0x65,
            (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65, (byte) 0x01,
            (byte) 0x00, (byte) 0x0C, (byte) 0x48, (byte) 0x69, (byte) 0x57,
            (byte) 0x6F, (byte) 0x72, (byte) 0x6C, (byte) 0x64, (byte) 0x2E,
            (byte) 0x6A, (byte) 0x61, (byte) 0x76, (byte) 0x61, (byte) 0x00,
            (byte) 0x21, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x04,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x05,
            (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x2F,
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x2A, (byte) 0xB7,
            (byte) 0x00, (byte) 0x09, (byte) 0xB1, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0A, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x14, (byte) 0x00,
            (byte) 0x0B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C,
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x05, (byte) 0x00, (byte) 0x0C, (byte) 0x00, (byte) 0x0D,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x00,
            (byte) 0x0E, (byte) 0x00, (byte) 0x0F, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x37, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0xB2,
            (byte) 0x00, (byte) 0x15, (byte) 0x12, (byte) 0x17, (byte) 0xB6,
            (byte) 0x00, (byte) 0x1D, (byte) 0xB1, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0A, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x00, (byte) 0x02,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x17, (byte) 0x00,
            (byte) 0x08, (byte) 0x00, (byte) 0x18, (byte) 0x00, (byte) 0x0B,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09,
            (byte) 0x00, (byte) 0x1E, (byte) 0x00, (byte) 0x1F, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x20,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00,
            (byte) 0x21, };
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SecureClassLoader",
        args = {}
    )
    public void testSecureClassLoader() {
        new MyClassLoader();
        class TestSecurityManager extends SecurityManager {
            boolean called;
            @Override
            public void checkCreateClassLoader() {
                called = true;
                super.checkCreateClassLoader();
            }
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof RuntimePermission) {
                    if (permission.getName().equals("createClassLoader")) {
                        throw new SecurityException();
                    }
                }
            }
        }
        TestSecurityManager sm = new TestSecurityManager();
        try {
            System.setSecurityManager(sm);
            new MyClassLoader();
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertTrue("checkCreateClassLoader was not called", sm.called);
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verification with null parameter missed",
        method = "SecureClassLoader",
        args = {java.lang.ClassLoader.class}
    )
    @KnownFailure("Android doesn't allow null parent.")
    public void testSecureClassLoaderClassLoader() throws Exception {
        URL[] urls = new URL[] { new URL("http:
        URLClassLoader ucl = URLClassLoader.newInstance(urls);
        new MyClassLoader(ucl);
        try {
            new MyClassLoader(null);
        } catch (Exception e) {
            fail("unexpected exception: " + e);
        }
        class TestSecurityManager extends SecurityManager {
            boolean called;
            @Override
            public void checkCreateClassLoader() {
                called = true;
                super.checkCreateClassLoader();
            }
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof RuntimePermission) {
                    if (permission.getName().equals("createClassLoader")) {
                        throw new SecurityException();
                    }
                }
            }
        }
        TestSecurityManager sm = new TestSecurityManager();
        try {
            System.setSecurityManager(sm);
            new MyClassLoader(ucl);
            fail("expected SecurityException");
        } catch (SecurityException e) {
            assertTrue("checkCreateClassLoader was not called", sm.called);
        } finally {
            System.setSecurityManager(null);
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "",
        method = "getPermissions",
        args = {java.security.CodeSource.class}
    )
    public void testGetPermissions() throws Exception {
        URL url = new URL("http:
        CodeSource cs = new CodeSource(url, (Certificate[]) null);
        MyClassLoader ldr = new MyClassLoader();
        ldr.getPerms(null);
        ldr.getPerms(cs);
    }
    class MyClassLoader extends SecureClassLoader {
        public MyClassLoader() {
            super();
        }
        public MyClassLoader(ClassLoader parent) {
            super(parent);
        }
        public PermissionCollection getPerms(CodeSource codesource) {
            return super.getPermissions(codesource);
        }
        public Class define(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length,
                    (ProtectionDomain) null);
        }
        public Class define(String name, ByteBuffer b, CodeSource cs) {
            return defineClass(name, b, cs);
        }
        public Class define(String name, byte[] b, int off, int len,
                CodeSource cs) {
            return defineClass(name, b, off, len, cs);
        }
    }
}

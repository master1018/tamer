@TestTargetClass(CharsetProvider.class)
public class CharsetProviderTest extends TestCase {
    static String PROP_CONFIG_FILE1 = "clear.tests.cp1";
    static String CONFIG_FILE1 = null;
    static MockCharset charset1 = new MockCharset("mockCharset00",
            new String[] { "mockCharset01", "mockCharset02" });
    static MockCharset charset2 = new MockCharset("mockCharset10",
            new String[] { "mockCharset11", "mockCharset12" });
    @Override
    protected void setUp() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir == null) {
            fail("java.io.tmpdir not set");
        }
        File tmpdir = new File(tmpDir);
        if (!tmpdir.isDirectory()) {
            fail("java.io.tmpdir is not a directory");
        }
        String sep = System.getProperty("file.separator");
        if (!tmpDir.endsWith(sep)) {
            tmpDir += sep;
        }
        CONFIG_FILE1 = tmpDir +  "META-INF" + sep + "services" + sep
                + "java.nio.charset.spi.CharsetProvider";
        URL url = null;
        try {
            url = new URL("file:
        } catch (MalformedURLException e) {
            fail("unexpected exception: " + e);
        }
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }
        URLClassLoader urlc = new URLClassLoader(new URL[] { url }, parent);
        Thread.currentThread().setContextClassLoader(urlc);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Thread.currentThread().setContextClassLoader(null);
    }
    private void setupFile(String path, String content) throws Exception {
        String sep = System.getProperty("file.separator");
        int sepIndex = path.lastIndexOf(sep);
        File f = new File(path.substring(0, sepIndex));
        f.mkdirs();
        FileOutputStream fos = new FileOutputStream(path);
        OutputStreamWriter writer = new OutputStreamWriter(fos);
        try {
            writer.write(content);
        } finally {
            writer.close();
        }
    }
    private void cleanupFile(String path) throws Exception {
        File f = new File(path);
        f.delete();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void testIsSupported_And_ForName_NormalProvider() throws Exception {
        try {
            assertFalse(Charset.isSupported("mockCharset10"));
            assertFalse(Charset.isSupported("mockCharset11"));
            assertFalse(Charset.isSupported("mockCharset12"));
            try {
                Charset.forName("mockCharset10");
                fail("Should throw UnsupportedCharsetException!");
            } catch (UnsupportedCharsetException e) {
            }
            try {
                Charset.forName("mockCharset11");
                fail("Should throw UnsupportedCharsetException!");
            } catch (UnsupportedCharsetException e) {
            }
            try {
                Charset.forName("mockCharset12");
                fail("Should throw UnsupportedCharsetException!");
            } catch (UnsupportedCharsetException e) {
            }
            StringBuffer sb = new StringBuffer();
            sb.append("#comment\r");
            sb.append("\n");
            sb.append("\r\n");
            sb.append(" \ttests.api.java.nio.charset."
                    + "CharsetTest$MockCharsetProvider \t\n\r");
            sb.append(" \ttests.api.java.nio.charset."
                    + "CharsetTest$MockCharsetProvider \t");
            setupFile(CONFIG_FILE1, sb.toString());
            sb = new StringBuffer();
            sb.append(" #comment\r");
            sb.append("\n");
            sb.append("\r\n");
            sb.append(" \ttests.api.java.nio.charset."
                    + "CharsetProviderTest$MockCharsetProvider \t\n\r");
            setupFile(CONFIG_FILE1, sb.toString());
            assertTrue(Charset.isSupported("mockCharset10"));
            assertTrue(Charset.isSupported("MockCharset11"));
            assertTrue(Charset.isSupported("MockCharset12"));
            assertTrue(Charset.isSupported("MOCKCharset10"));
            assertTrue(Charset.isSupported("MOCKCharset11"));
            assertTrue(Charset.isSupported("MOCKCharset12"));
            assertTrue(Charset.forName("mockCharset10") instanceof MockCharset);
            assertTrue(Charset.forName("mockCharset11") instanceof MockCharset);
            assertTrue(Charset.forName("mockCharset12") instanceof MockCharset);
            assertTrue(Charset.forName("mockCharset10") == charset2);
            Charset.forName("mockCharset11");
            assertTrue(Charset.forName("mockCharset12") == charset2);
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    public void testIsSupported_NonExistingClass() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("impossible\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.isSupported("impossible");
            fail("Should throw Error!");
        } catch (Error e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")
    public void testIsSupported_NotCharsetProviderClass() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("java.lang.String\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.isSupported("impossible");
            fail("Should throw ClassCastException!");
        } catch (ClassCastException e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    public void testIsSupported_InsufficientPrivilege() throws Exception {
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            Charset.isSupported("UTF-8");
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("tests.api.java.nio.charset."
                        + "CharsetProviderTest$MockCharsetProvider\r");
                setupFile(CONFIG_FILE1, sb.toString());
                assertFalse(Charset.isSupported("gb180300000"));
            } catch (SecurityException e) {
                fail("unexpected SecurityException!:" + e);
            } finally {
                cleanupFile(CONFIG_FILE1);
            }
        } finally {
            System.setSecurityManager(oldMan);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    @KnownFailure("Fails in CTS but passes under run-core-tests")
    public void testForName_InsufficientPrivilege() throws Exception {
        SecurityManager oldMan = System.getSecurityManager();
        System.setSecurityManager(new MockSecurityManager());
        try {
            Charset.forName("UTF-8");
            try {
                StringBuffer sb = new StringBuffer();
                sb.append("tests.api.java.nio.charset."
                        + "CharsetProviderTest$MockCharsetProvider\r");
                setupFile(CONFIG_FILE1, sb.toString());
                Charset.forName("gb180300000");
                fail("expected UnsupportedCharsetException!");
            } catch (SecurityException e) {
                fail("unexpected SecurityException!:" + e);
            } catch (UnsupportedCharsetException e) {
            } finally {
                cleanupFile(CONFIG_FILE1);
            }
        } finally {
            System.setSecurityManager(oldMan);
        }
    }    
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "charsets",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "charsetForName",
            args = {String.class}
        )
    }) 
    public void testForName_DuplicateWithBuiltInCharset() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("tests.api.java.nio.charset." +
                    "CharsetProviderTest$MockCharsetProviderACSII\r");
            setupFile(CONFIG_FILE1, sb.toString());
            assertFalse(Charset.forName("us-ascii") instanceof MockCharset);
            Charset charset = Charset.availableCharsets().get("us-ascii");
            assertFalse(charset instanceof MockCharset);
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")    
    public void testForName_NonExistingClass() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("impossible\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.forName("impossible");
            fail("Should throw Error!");
        } catch (Error e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsetForName",
        args = {String.class}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")    
    public void testForName_NotCharsetProviderClass() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("java.lang.String\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.forName("impossible");
            fail("Should throw ClassCastException!");
        } catch (ClassCastException e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsets",
        args = {}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")    
    public void testAvailableCharsets_NormalProvider() throws Exception {
        try {
            assertFalse(Charset.availableCharsets()
                    .containsKey("mockCharset10"));
            assertFalse(Charset.availableCharsets()
                    .containsKey("mockCharset11"));
            assertFalse(Charset.availableCharsets()
                    .containsKey("mockCharset12"));
            StringBuffer sb = new StringBuffer();
            sb.append("#comment\r");
            sb.append("\n");
            sb.append("\r\n");
            sb.append("\ttests.api.java.nio.charset."
                    + "CharsetTest$MockCharsetProvider \t\n\r");
            sb.append("\ttests.api.java.nio.charset."
                    + "CharsetTest$MockCharsetProvider \t");
            sb.append("#comment\r");
            sb.append("\n");
            sb.append("\r\n");
            sb.append(" \ttests.api.java.nio.charset."
                    + "CharsetProviderTest$MockCharsetProvider \t\n\r");
            setupFile(CONFIG_FILE1, sb.toString());
            SortedMap<String, Charset> availableCharsets = Charset
                    .availableCharsets();
            assertTrue(availableCharsets.containsKey("mockCharset00"));
            assertTrue(availableCharsets.containsKey("MOCKCharset00"));
            Charset charset = availableCharsets.get("mockCharset00");
            assertTrue(charset instanceof MockCharset);
            charset = availableCharsets.get("MOCKCharset00");
            assertTrue(charset instanceof MockCharset);
            assertFalse(availableCharsets.containsKey("mockCharset01"));
            assertFalse(availableCharsets.containsKey("mockCharset02"));
            charset = availableCharsets.get("mockCharset10");
            assertTrue(charset == charset2);
            charset = availableCharsets.get("MOCKCharset10");
            assertTrue(charset == charset2);
            assertFalse(availableCharsets.containsKey("mockCharset11"));
            assertFalse(availableCharsets.containsKey("mockCharset12"));
            assertTrue(availableCharsets.containsKey("mockCharset10"));
            assertTrue(availableCharsets.containsKey("MOCKCharset10"));
            charset = availableCharsets.get("mockCharset10");
            assertTrue(charset == charset2);
            assertFalse(availableCharsets.containsKey("mockCharset11"));
            assertFalse(availableCharsets.containsKey("mockCharset12"));
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsets",
        args = {}
    )
    public void testAvailableCharsets_NonExistingClass() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("impossible\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.availableCharsets();
            fail("Should throw Error!");
        } catch (Error e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsets",
        args = {}
    )
    @AndroidOnly("Looks like RI doesn't use current thread's context class "+
    "loader to lookup charset providers")    
    public void testAvailableCharsets_NotCharsetProviderClass()
            throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("java.lang.String\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.availableCharsets();
            fail("Should throw ClassCastException!");
        } catch (ClassCastException e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "charsets",
        args = {}
    )
    public void testAvailableCharsets_IllegalString() throws Exception {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append("java String\r");
            setupFile(CONFIG_FILE1, sb.toString());
            Charset.availableCharsets();
            fail("Should throw Error!");
        } catch (Error e) {
        } finally {
            cleanupFile(CONFIG_FILE1);
        }
    }
    public static class MockCharsetProvider extends CharsetProvider {
        public Charset charsetForName(String charsetName) {
            if ("MockCharset10".equalsIgnoreCase(charsetName)
                    || "MockCharset11".equalsIgnoreCase(charsetName)
                    || "MockCharset12".equalsIgnoreCase(charsetName)) {
                return charset2;
            }
            return null;
        }
        public Iterator<Charset> charsets() {
            Vector<Charset> v = new Vector<Charset>();
            v.add(charset2);
            return v.iterator();
        }
    }
    public static class MockCharsetProviderACSII extends CharsetProvider {
        public Charset charsetForName(String charsetName) {
            if ("US-ASCII".equalsIgnoreCase(charsetName)
                    || "ASCII".equalsIgnoreCase(charsetName)) {
                return new MockCharset("US-ASCII", new String[] { "ASCII" });
            }
            return null;
        }
        public Iterator<Charset> charsets() {
            Vector<Charset> v = new Vector<Charset>();
            v.add(new MockCharset("US-ASCII", new String[] { "ASCII" }));
            return v.iterator();
        }
    }
}

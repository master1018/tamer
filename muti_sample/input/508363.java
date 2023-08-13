@TestTargetClass(JarEntry.class)
public class JarEntryTest extends TestCase {
    private ZipEntry zipEntry;
    private JarEntry jarEntry;
    private JarFile jarFile;
    private final String jarName = "hyts_patch.jar";
    private final String entryName = "foo/bar/A.class";
    private final String entryName2 = "Blah.txt";
    private final String attJarName = "hyts_att.jar";
    private final String attEntryName = "HasAttributes.txt";
    private final String attEntryName2 = "NoAttributes.txt";
    private File resources;
    @Override
    protected void setUp() throws Exception {
        resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, jarName);
        jarFile = new JarFile(new File(resources, jarName));
    }
    @Override
    protected void tearDown() throws Exception {
        if (jarFile != null) {
            jarFile.close();
        }
    }
    @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "JarEntry",
            args = {java.util.jar.JarEntry.class}
    )
    public void test_ConstructorLjava_util_jar_JarEntry_on_null() throws IOException {
        JarEntry newJarEntry = new JarEntry(jarFile.getJarEntry(entryName));
        assertNotNull(newJarEntry);
        jarEntry = null;
        try {
            newJarEntry = new JarEntry(jarEntry);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarEntry",
        args = {java.util.zip.ZipEntry.class}
    )
    public void test_ConstructorLjava_util_zip_ZipEntry() {
        assertNotNull("Jar file is null", jarFile);
        zipEntry = jarFile.getEntry(entryName);
        assertNotNull("Zip entry is null", zipEntry);
        jarEntry = new JarEntry(zipEntry);
        assertNotNull("Jar entry is null", jarEntry);
        assertEquals("Wrong entry constructed--wrong name", entryName, jarEntry
                .getName());
        assertEquals("Wrong entry constructed--wrong size", 311, jarEntry
                .getSize());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAttributes",
        args = {}
    )
    public void test_getAttributes() {
        JarFile attrJar = null;
        File file = null;
        try {
            Support_Resources.copyFile(resources, null, attJarName);
            file = new File(resources, attJarName);
            attrJar = new JarFile(file);
        } catch (Exception e) {
            assertTrue(file + " does not exist", file.exists());
            fail("Exception opening file: " + e.toString());
        }
        try {
            jarEntry = attrJar.getJarEntry(attEntryName);
            assertNotNull("Should have Manifest attributes", jarEntry
                    .getAttributes());
        } catch (Exception e) {
            fail("Exception during 2nd test: " + e.toString());
        }
        try {
            jarEntry = attrJar.getJarEntry(attEntryName2);
            assertNull("Shouldn't have any Manifest attributes", jarEntry
                    .getAttributes());
            attrJar.close();
        } catch (Exception e) {
            fail("Exception during 1st test: " + e.toString());
        }
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        try {
            attrJar = new JarFile(new File(resources, "Broken_manifest.jar"));
            jarEntry = attrJar.getJarEntry("META-INF/");
            jarEntry.getAttributes();
            fail("IOException expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCertificates",
        args = {}
    )
    public void test_getCertificates() throws Exception {
        zipEntry = jarFile.getEntry(entryName2);
        jarEntry = new JarEntry(zipEntry);
        assertNull("Shouldn't have any Certificates", jarEntry
                .getCertificates());
        String jarFileName = "TestCodeSigners.jar";
        Support_Resources.copyFile(resources, null, jarFileName);
        File file = new File(resources, jarFileName);
        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry1 = jarFile.getJarEntry("Test.class");
        JarEntry jarEntry2 = jarFile.getJarEntry("Test.class");
        InputStream in = jarFile.getInputStream(jarEntry1);
        byte[] buffer = new byte[1024];
        while (in.read(buffer) >= 0);
        in.close();
        assertEquals("the file is fully read", -1, in.read());
        assertNotNull(jarEntry1.getCertificates());
        assertNotNull(jarEntry2.getCertificates());
        in.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCodeSigners",
        args = {}
    )
    public void test_getCodeSigners() throws IOException {
        String jarFileName = "TestCodeSigners.jar";
        Support_Resources.copyFile(resources, null, jarFileName);
        File file = new File(resources, jarFileName);
        JarFile jarFile = new JarFile(file);
        JarEntry jarEntry = jarFile.getJarEntry("Test.class");
        InputStream in = jarFile.getInputStream(jarEntry);
        byte[] buffer = new byte[1024];
        while (in.available() > 0) {
            in.read(buffer);
        }
        assertEquals("the file is fully read", -1, in.read());
        CodeSigner[] codeSigners = jarEntry.getCodeSigners();
        assertEquals(2, codeSigners.length);
        List<?> certs_bob = codeSigners[0].getSignerCertPath()
                .getCertificates();
        List<?> certs_alice = codeSigners[1].getSignerCertPath()
                .getCertificates();
        if (1 == certs_bob.size()) {
            List<?> temp = certs_bob;
            certs_bob = certs_alice;
            certs_alice = temp;
        }
        assertEquals(2, certs_bob.size());
        assertEquals(1, certs_alice.size());
        assertNull(
                "getCodeSigners() of a primitive JarEntry should return null",
                new JarEntry("aaa").getCodeSigners());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "JarEntry",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        assertNotNull("Jar file is null", jarFile);
        zipEntry = jarFile.getEntry(entryName);
        assertNotNull("Zip entry is null", zipEntry);
        jarEntry = new JarEntry(entryName);
        assertNotNull("Jar entry is null", jarEntry);
        assertEquals("Wrong entry constructed--wrong name", entryName, jarEntry
                .getName());
        try {
            jarEntry = new JarEntry((String) null);
            fail("NullPointerException expected");
        } catch (NullPointerException ee) {
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 0x10000; i++) {
            sb.append('3');
        }
        try {
            jarEntry = new JarEntry(new String(sb));
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "JarEntry",
        args = {java.util.jar.JarEntry.class}
    )
    public void test_ConstructorLjava_util_jar_JarEntry() {
        assertNotNull("Jar file is null", jarFile);
        JarEntry je = jarFile.getJarEntry(entryName);
        assertNotNull("Jar entry is null", je);
        jarEntry = new JarEntry(je);
        assertNotNull("Jar entry is null", jarEntry);
        assertEquals("Wrong entry constructed--wrong name", entryName, jarEntry
                .getName());
        assertEquals("Wrong entry constructed--wrong size", 311, jarEntry
                .getSize());
    }
}

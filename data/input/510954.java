@TestTargetClass(ZipEntry.class)
public class ZipEntryTest extends junit.framework.TestCase {
    public byte[] getAllBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        byte[] buf = new byte[512];
        int iRead;
        int off;
        while (is.available() > 0) {
            iRead = is.read(buf, 0, buf.length);
            if (iRead > 0) bs.write(buf, 0, iRead);
        }
        return bs.toByteArray();
    }
    java.util.zip.ZipEntry zentry;
    java.util.zip.ZipFile zfile;
    private static final String platformId = System.getProperty(
            "com.ibm.oti.configuration", "JDK")
            + System.getProperty("java.vm.version");
    static final String tempFileName = platformId + "zipentrytest.zip";
    long orgSize;
    long orgCompressedSize;
    long orgCrc;
    long orgTime;
    String orgComment;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipEntry",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        zentry = zfile.getEntry("File3.txt");
        assertNotNull("Failed to create ZipEntry", zentry);
        try {
            zentry = zfile.getEntry(null);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 65535; i++) {
            s.append('a');
        }
        try {
            zentry = new ZipEntry(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry = new ZipEntry(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            String n = null;
            zentry = new ZipEntry(n);
            fail("NullPointerException not thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getComment",
        args = {}
    )
    public void test_getComment() {
        ZipEntry zipEntry = new ZipEntry("zippy.zip");
        assertNull("Incorrect Comment Returned.", zipEntry.getComment());
        zipEntry.setComment("This Is A Comment");
        assertEquals("Incorrect Comment Returned.", "This Is A Comment",
                zipEntry.getComment());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCompressedSize",
        args = {}
    )
    public void test_getCompressedSize() {
        assertTrue("Incorrect compressed size returned", zentry
                .getCompressedSize() == orgCompressedSize);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCrc",
        args = {}
    )
    public void test_getCrc() {
        assertTrue("Failed to get Crc", zentry.getCrc() == orgCrc);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getExtra",
        args = {}
    )
    public void test_getExtra() {
        assertNull("Incorrect extra information returned", zentry.getExtra());
        byte[] ba = {'T', 'E', 'S', 'T'};
        zentry = new ZipEntry("test.tst");
        zentry.setExtra(ba);
        assertTrue("Incorrect Extra Information Returned.",
                zentry.getExtra() == ba);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getMethod",
        args = {}
    )
    public void test_getMethod() {
        zentry = zfile.getEntry("File1.txt");
        assertTrue("Incorrect compression method returned",
                zentry.getMethod() == java.util.zip.ZipEntry.STORED);
        zentry = zfile.getEntry("File3.txt");
        assertTrue("Incorrect compression method returned",
                zentry.getMethod() == java.util.zip.ZipEntry.DEFLATED);
        zentry = new ZipEntry("test.tst");
        assertEquals("Incorrect Method Returned.", -1, zentry.getMethod());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        assertEquals(
                "Incorrect name returned - Note return result somewhat ambiguous in spec",
                "File1.txt", zentry.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSize",
        args = {}
    )
    public void test_getSize() {
        assertTrue("Incorrect size returned", zentry.getSize() == orgSize);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTime",
        args = {}
    )
    public void test_getTime() {
        assertTrue("Failed to get time", zentry.getTime() == orgTime);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isDirectory",
        args = {}
    )
    public void test_isDirectory() {
        assertTrue("Entry should not answer true to isDirectory", !zentry
                .isDirectory());
        zentry = new ZipEntry("Directory/");
        assertTrue("Entry should answer true to isDirectory", zentry
                .isDirectory());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setComment",
        args = {java.lang.String.class}
    )
    public void test_setCommentLjava_lang_String() {
        zentry = zfile.getEntry("File1.txt");
        zentry.setComment("Set comment using api");
        assertEquals("Comment not correctly set", "Set comment using api",
                zentry.getComment());
        String n = null;
        zentry.setComment(n);
        assertNull("Comment not correctly set", zentry.getComment());
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < 0xFFFF; i++) {
            s.append('a');
        }
        try {
            zentry.setComment(s.toString());
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException During Test.");
        }
        try {
            s.append('a');
            zentry.setComment(s.toString());
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCompressedSize",
        args = {long.class}
    )
    public void test_setCompressedSizeJ() {
        zentry.setCompressedSize(orgCompressedSize + 10);
        assertTrue("Set compressed size failed",
                zentry.getCompressedSize() == (orgCompressedSize + 10));
        zentry.setCompressedSize(0);
        assertEquals("Set compressed size failed", 0, zentry
                .getCompressedSize());
        zentry.setCompressedSize(-25);
        assertEquals("Set compressed size failed", -25, zentry
                .getCompressedSize());
        zentry.setCompressedSize(4294967296l);
        assertTrue("Set compressed size failed",
                zentry.getCompressedSize() == 4294967296l);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setCrc",
        args = {long.class}
    )
    public void test_setCrcJ() {
        zentry.setCrc(orgCrc + 100);
        assertTrue("Failed to set Crc", zentry.getCrc() == (orgCrc + 100));
        zentry.setCrc(0);
        assertEquals("Failed to set Crc", 0, zentry.getCrc());
        try {
            zentry.setCrc(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setExtra",
        args = {byte[].class}
    )
    public void test_setExtra$B() {
        zentry = zfile.getEntry("File1.txt");
        zentry.setExtra("Test setting extra information".getBytes());
        assertEquals("Extra information not written properly",
                "Test setting extra information", new String(zentry.getExtra(),
                        0, zentry.getExtra().length));
        zentry = new ZipEntry("test.tst");
        byte[] ba = new byte[0xFFFF];
        try {
            zentry.setExtra(ba);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            ba = new byte[0xFFFF + 1];
            zentry.setExtra(ba);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = {'a', 'b', 'd', 'e'};
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);
        ZipEntry zeOutput = new ZipEntry(zeInput);
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMethod",
        args = {int.class}
    )
    public void test_setMethodI() {
        zentry = zfile.getEntry("File3.txt");
        zentry.setMethod(ZipEntry.STORED);
        assertTrue("Failed to set compression method",
                zentry.getMethod() == ZipEntry.STORED);
        zentry.setMethod(ZipEntry.DEFLATED);
        assertTrue("Failed to set compression method",
                zentry.getMethod() == ZipEntry.DEFLATED);
        try {
            int error = 1;
            zentry = new ZipEntry("test.tst");
            zentry.setMethod(error);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSize",
        args = {long.class}
    )
    public void test_setSizeJ() {
        zentry.setSize(orgSize + 10);
        assertTrue("Set size failed", zentry.getSize() == (orgSize + 10));
        zentry.setSize(0);
        assertEquals("Set size failed", 0, zentry.getSize());
        try {
            zentry.setSize(-25);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            zentry.setCrc(4294967295l);
        } catch (IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException during test");
        }
        try {
            zentry.setCrc(4294967296l);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setTime",
        args = {long.class}
    )
    public void test_setTimeJ() {
        zentry.setTime(orgTime + 10000);
        assertTrue("Test 1: Failed to set time: " + zentry.getTime(), zentry
                .getTime() == (orgTime + 10000));
        zentry.setTime(orgTime - 10000);
        assertTrue("Test 2: Failed to set time: " + zentry.getTime(), zentry
                .getTime() == (orgTime - 10000));
        TimeZone zone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            zentry.setTime(0);
            assertTrue("Test 3: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            assertTrue("Test 3a: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315532800000L);
            zentry.setTime(0);
            TimeZone.setDefault(TimeZone.getTimeZone("EST"));
            assertTrue("Test 3b: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
            zentry.setTime(-25);
            assertTrue("Test 4: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
            zentry.setTime(4354837200000L);
            assertTrue("Test 5: Failed to set time: " + zentry.getTime(),
                    zentry.getTime() == 315550800000L);
        } finally {
            TimeZone.setDefault(zone);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertTrue("Returned incorrect entry name", zentry.toString().indexOf(
                "File1.txt") >= 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ZipEntry",
        args = {java.util.zip.ZipEntry.class}
    )
    public void test_ConstructorLjava_util_zip_ZipEntry() {
        zentry.setSize(2);
        zentry.setCompressedSize(4);
        zentry.setComment("Testing");
        ZipEntry zentry2 = new ZipEntry(zentry);
        assertEquals("ZipEntry Created With Incorrect Size.", 2, zentry2
                .getSize());
        assertEquals("ZipEntry Created With Incorrect Compressed Size.", 4,
                zentry2.getCompressedSize());
        assertEquals("ZipEntry Created With Incorrect Comment.", "Testing",
                zentry2.getComment());
        assertTrue("ZipEntry Created With Incorrect Crc.",
                zentry2.getCrc() == orgCrc);
        assertTrue("ZipEntry Created With Incorrect Time.",
                zentry2.getTime() == orgTime);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        Object obj = zentry.clone();
        assertTrue("toString()", obj.toString().equals(zentry.toString()));
        assertTrue("hashCode()", obj.hashCode() == zentry.hashCode());
        ZipEntry zeInput = new ZipEntry("InputZIP");
        byte[] extraB = {'a', 'b', 'd', 'e'};
        zeInput.setExtra(extraB);
        assertEquals(extraB, zeInput.getExtra());
        assertEquals(extraB[3], zeInput.getExtra()[3]);
        assertEquals(extraB.length, zeInput.getExtra().length);
        ZipEntry zeOutput = (ZipEntry) zeInput.clone();
        assertEquals(zeInput.getExtra()[3], zeOutput.getExtra()[3]);
        assertEquals(zeInput.getExtra().length, zeOutput.getExtra().length);
        assertEquals(extraB[3], zeOutput.getExtra()[3]);
        assertEquals(extraB.length, zeOutput.getExtra().length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {}
    )
    public void test_hashCode() {
        try {
            zentry.hashCode();
        } catch (Exception ee) {
            fail("Unexpected exception " + ee);
        }
    }
    @Override
    protected void setUp() {
        java.io.File f = null;
        try {
            f = File.createTempFile(tempFileName, ".zip");
            f = new java.io.File(f.getAbsolutePath());
            f.delete();
            java.io.InputStream is = Support_Resources
                    .getStream("hyts_ZipFile.zip");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
            byte[] rbuf = getAllBytesFromStream(is);
            fos.write(rbuf, 0, rbuf.length);
            is.close();
            fos.close();
            zfile = new java.util.zip.ZipFile(f);
            zentry = zfile.getEntry("File1.txt");
            orgSize = zentry.getSize();
            orgCompressedSize = zentry.getCompressedSize();
            orgCrc = zentry.getCrc();
            orgTime = zentry.getTime();
            orgComment = zentry.getComment();
        } catch (Exception e) {
            System.out.println("Exception during ZipFile setup <"
                    + f.getAbsolutePath() + ">: ");
            e.printStackTrace();
        }
    }
    @Override
    protected void tearDown() {
        try {
            if (zfile != null) {
                zfile.close();
            }
            java.io.File f = new java.io.File(tempFileName);
            f.delete();
        } catch (java.io.IOException e) {
            System.out.println("Exception during tearDown");
        }
    }
}

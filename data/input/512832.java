@TestTargetClass(ZipOutputStream.class)
public class ZipOutputStreamTest extends junit.framework.TestCase {
    ZipOutputStream zos;
    ByteArrayOutputStream bos;
    ZipInputStream zis;
    static final String data = "HelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorldHelloWorld";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Can not check IOException.",
        method = "close",
        args = {}
    )
    public void test_close() throws Exception {
        try {
            zos.close();
            fail("Close on empty stream failed to throw exception");
        } catch (ZipException e) {
        }
        zos = new ZipOutputStream(bos);
        zos.putNextEntry(new ZipEntry("XX"));
        zos.closeEntry();
        zos.close();
        ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
        zos.putNextEntry(new ZipEntry("myFile"));
        zos.close();
        zos.close(); 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "ZipException can not be checked.",
        method = "closeEntry",
        args = {}
    )
    public void test_closeEntry() throws IOException {
        ZipEntry ze = new ZipEntry("testEntry");
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes("UTF-8"));
        zos.closeEntry();
        assertTrue("closeEntry failed to update required fields",
                ze.getSize() == 11 && ze.getCompressedSize() == 13);
        ze = new ZipEntry("testEntry1");
        zos.close();
        try {
            zos.closeEntry();
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "ZipException can not be checked.",
        method = "finish",
        args = {}
    )
    public void test_finish() throws Exception {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes());
        zos.finish();
        assertEquals("Finish failed to closeCurrentEntry", 11, ze.getSize());
        ZipOutputStream zos = new ZipOutputStream(new ByteArrayOutputStream());
        zos.putNextEntry(new ZipEntry("myFile"));
        zos.finish();
        zos.close();
        try {
            zos.finish();
            fail("Assert 0: Expected IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "ZipException can not be checked.",
        method = "putNextEntry",
        args = {java.util.zip.ZipEntry.class}
    )
    public void test_putNextEntryLjava_util_zip_ZipEntry() throws IOException {
        ZipEntry ze = new ZipEntry("testEntry");
        ze.setTime(System.currentTimeMillis());
        zos.putNextEntry(ze);
        zos.write("Hello World".getBytes());
        zos.closeEntry();
        zos.close();
        zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipEntry ze2 = zis.getNextEntry();
        zis.closeEntry();
        assertTrue("Failed to write correct entry", ze.getName().equals(
                ze2.getName())
                && ze.getCrc() == ze2.getCrc());
        try {
            zos.putNextEntry(ze);
            fail("Entry with incorrect setting failed to throw exception");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setComment",
        args = {java.lang.String.class}
    )
    public void test_setCommentLjava_lang_String() {
        zos.setComment("test setComment");
        try {
            zos.setComment(new String(new byte[0xFFFF + 1]));
            fail("Comment over 0xFFFF in length should throw exception");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLevel",
        args = {int.class}
    )
    public void test_setLevelI() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        long csize = ze.getCompressedSize();
        zos.setLevel(9); 
        zos.putNextEntry(ze = new ZipEntry("test2"));
        zos.write(data.getBytes());
        zos.closeEntry();
        assertTrue("setLevel failed", csize <= ze.getCompressedSize());
        try {
            zos.setLevel(-9); 
            fail("IllegalArgumentException ecpected.");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setMethod",
        args = {int.class}
    )
    public void test_setMethodI() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.setMethod(ZipOutputStream.STORED);
        CRC32 tempCrc = new CRC32();
        tempCrc.update(data.getBytes());
        ze.setCrc(tempCrc.getValue());
        ze.setSize(new String(data).length());
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        long csize = ze.getCompressedSize();
        zos.setMethod(ZipOutputStream.DEFLATED);
        zos.putNextEntry(ze = new ZipEntry("test2"));
        zos.write(data.getBytes());
        zos.closeEntry();
        assertTrue("setLevel failed", csize >= ze.getCompressedSize());
        try {
            zos.setMethod(-ZipOutputStream.DEFLATED);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() throws IOException {
        ZipEntry ze = new ZipEntry("test");
        zos.putNextEntry(ze);
        zos.write(data.getBytes());
        zos.closeEntry();
        zos.close();
        zos = null;
        zis = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        zis.getNextEntry();
        byte[] b = new byte[data.length()];
        int r = 0;
        int count = 0;
        while (count != b.length && (r = zis.read(b, count, b.length)) != -1) {
            count += r;
        }
        zis.closeEntry();
        assertTrue("Write failed to write correct bytes", new String(b)
                .equals(data));
        File f = File.createTempFile("testZip", "tst");
        f.deleteOnExit();
        FileOutputStream stream = new FileOutputStream(f);
        ZipOutputStream zip = new ZipOutputStream(stream);
        zip.setMethod(ZipEntry.STORED);
        try {
            zip.putNextEntry(new ZipEntry("Second"));
            fail("Not set an entry. Should have thrown ZipException.");
        } catch (ZipException e) {
        }
        try {
            zip.write(new byte[2]);
            fail("Writing data without an entry. Should have thrown IOException");
        } catch (IOException e) {
        }
        try {
            zip.write(new byte[2], 0, 12);
            fail("Writing data without an entry. Should have thrown IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        try {
            zip.write(null, 0, -2);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
        }
        stream.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII_2() throws IOException {
        File f1 = File.createTempFile("testZip1", "tst");
        f1.deleteOnExit();
        FileOutputStream stream1 = new FileOutputStream(f1);
        ZipOutputStream zip1 = new ZipOutputStream(stream1);
        zip1.putNextEntry(new ZipEntry("one"));
        zip1.setMethod(ZipOutputStream.STORED);
        zip1.setMethod(ZipEntry.STORED);
        zip1.write(new byte[2]);
        try {
            zip1.putNextEntry(new ZipEntry("Second"));
            fail("ZipException expected");
        } catch (ZipException e) {
        }
        try {
            zip1.write(new byte[2]); 
            fail("expected IOE there");
        } catch (IOException e2) {
        }
        zip1.close();
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        zos = new ZipOutputStream(bos = new ByteArrayOutputStream());
    }
    @Override
    protected void tearDown() throws Exception {
        try {
            if (zos != null) {
                zos.close();
            }
            if (zis != null) {
                zis.close();
            }
        } catch (Exception e) {
        }
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "See setUp procedure for more info.",
        method = "ZipOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void test_ConstructorLjava_io_OutputStream() {
        assertTrue(true);
    }
}

@TestTargetClass(InflaterInputStream.class)
public class InflaterInputStreamTest extends TestCase {
    byte outPutBuf[] = new byte[500];
    class MyInflaterInputStream extends InflaterInputStream {
        MyInflaterInputStream(InputStream in) {
            super(in);
        }
        MyInflaterInputStream(InputStream in, Inflater infl) {
            super(in, infl);
        }
        MyInflaterInputStream(InputStream in, Inflater infl, int size) {
            super(in, infl, size);
        }
        void myFill() throws IOException {
            fill();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InflaterInputStream",
        args = {java.io.InputStream.class}
    )
    public void test_ConstructorLjava_io_InputStream() throws IOException {
        byte byteArray[] = new byte[100];
        InputStream infile = Support_Resources.getStream("hyts_constru_OD.txt");
        InflaterInputStream inflatIP = new InflaterInputStream(infile);
        inflatIP.read(byteArray, 0, 5);
        inflatIP.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "InflaterInputStream",
        args = {java.io.InputStream.class, java.util.zip.Inflater.class}
    )
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_Inflater()
            throws IOException {
        byte byteArray[] = new byte[100];
        InputStream infile = Support_Resources.getStream("hyts_constru_OD.txt");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile, inflate);
        inflatIP.read(byteArray, 0, 5);
        inflatIP.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IllegalArgumentException checking missed.",
        method = "InflaterInputStream",
        args = {java.io.InputStream.class, java.util.zip.Inflater.class, int.class}
    )
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_InflaterI()
            throws IOException {
        int result = 0;
        int buffer[] = new int[500];
        InputStream infile = Support_Resources
                .getStream("hyts_constru_ODI.txt");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile, inflate,
                1);
        int i = 0;
        while ((result = inflatIP.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP.close();
        try {
            inflatIP = new InflaterInputStream(infile, inflate, -1);
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "mark",
        args = {int.class}
    )
    public void test_markI() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        iis.mark(0);
        iis.mark(-1);
        iis.mark(10000000);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "markSupported",
        args = {}
    )
    public void test_markSupported() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        assertFalse(iis.markSupported());
        assertTrue(is.markSupported());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public void test_read() throws IOException {
        int result = 0;
        int buffer[] = new int[500];
        byte orgBuffer[] = {1, 3, 4, 7, 8};
        InputStream infile = Support_Resources.getStream("hyts_constru_OD.txt");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile, inflate);
        int i = 0;
        while ((result = inflatIP.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP.close();
        for (int j = 0; j < orgBuffer.length; j++) {
            assertTrue(
                    "original compressed data did not equal decompressed data",
                    buffer[j] == orgBuffer[j]);
        }
        inflatIP.close();
        try {
            inflatIP.read();
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    public void testAvailableNonEmptySource() throws Exception {
        byte[] deflated = { 72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        assertEquals(1, in.read());
        assertEquals(1, in.available());
        assertEquals(3, in.read());
        assertEquals(1, in.available());
        assertEquals(4, in.read());
        assertEquals(1, in.available());
        assertEquals(6, in.read());
        assertEquals(0, in.available());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
    }
    public void testAvailableSkip() throws Exception {
        byte[] deflated = { 72, -119, 99, 100, 102, 97, 3, 0, 0, 31, 0, 15, 0 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        assertEquals(1, in.available());
        assertEquals(4, in.skip(4));
        assertEquals(0, in.available());
    }
    public void testAvailableEmptySource() throws Exception {
        byte[] deflated = { 120, -100, 3, 0, 0, 0, 0, 1 };
        InputStream in = new InflaterInputStream(new ByteArrayInputStream(deflated));
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
        assertEquals(0, in.available());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException & ZipException checking missed. Additional tests for fill method is not needed.",
            method = "read",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException & ZipException checking missed. Additional tests for fill method is not needed.",
            method = "fill",
            args = {}
        )
    })
    public void test_read$BII() throws IOException {
        byte[] test = new byte[507];
        for (int i = 0; i < 256; i++) {
            test[i] = (byte) i;
        }
        for (int i = 256; i < test.length; i++) {
            test[i] = (byte) (256 - i);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        dos.write(test);
        dos.close();
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        InflaterInputStream iis = new InflaterInputStream(is);
        byte[] outBuf = new byte[530];
        int result = 0;
        while (true) {
            result = iis.read(outBuf, 0, 5);
            if (result == -1) {
                break;
            }
        }
        try {
            iis.read(outBuf, -1, 10);
            fail("should throw IOOBE.");
        } catch (IndexOutOfBoundsException e) {
        }
        iis.close();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException checking.",
            method = "read",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException checking.",
            method = "fill",
            args = {}
        )
    })
    public void test_read$BII2() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));
        InflaterInputStream iis = new InflaterInputStream(fis);
        byte[] outBuf = new byte[530];
        iis.close();
        try {
            iis.read(outBuf, 0, 5);
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException checking.",
            method = "read",
            args = {byte[].class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "IOException checking.",
            method = "fill",
            args = {}
        )
    })
    public void test_read$BII3() throws IOException {
        File resources = Support_Resources.createTempFolder();
        Support_Resources.copyFile(resources, null, "Broken_manifest.jar");
        FileInputStream fis = new FileInputStream(new File(resources,
                "Broken_manifest.jar"));
        InflaterInputStream iis = new InflaterInputStream(fis);
        byte[] outBuf = new byte[530];
        try {
            iis.read();
            fail("IOException expected.");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        InputStream is = new ByteArrayInputStream(new byte[10]);
        InflaterInputStream iis = new InflaterInputStream(is);
        try {
            iis.reset();
            fail("Should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException checking missed.",
        method = "skip",
        args = {long.class}
    )
    public void test_skipJ() throws IOException {
        InputStream is = Support_Resources.getStream("hyts_available.tst");
        InflaterInputStream iis = new InflaterInputStream(is);
        try {
            iis.skip(-3);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        assertEquals("Incorrect Byte Returned.", 5, iis.read());
        try {
            iis.skip(Integer.MIN_VALUE);
            fail("IllegalArgumentException not thrown");
        } catch (IllegalArgumentException e) {
        }
        assertEquals("Incorrect Byte Returned.", 4, iis.read());
        assertEquals("Incorrect Number Of Bytes Skipped.", 3, iis.skip(3));
        assertEquals("Incorrect Byte Returned.", 7, iis.read());
        assertEquals("Incorrect Number Of Bytes Skipped.", 0, iis.skip(0));
        assertEquals("Incorrect Byte Returned.", 0, iis.read());
        assertEquals("Incorrect Number Of Bytes Skipped.", 2, iis.skip(4));
        assertEquals("Incorrect Byte Returned.", -1, iis.read());
        iis.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException checking missed.",
        method = "skip",
        args = {long.class}
    )
    public void test_skipJ2() throws IOException {
        int result = 0;
        int buffer[] = new int[100];
        byte orgBuffer[] = {1, 3, 4, 7, 8};
        InputStream infile = Support_Resources.getStream("hyts_constru_OD.txt");
        Inflater inflate = new Inflater();
        InflaterInputStream inflatIP = new InflaterInputStream(infile, inflate,
                10);
        long skip;
        try {
            skip = inflatIP.skip(Integer.MIN_VALUE);
            fail("Expected IllegalArgumentException when skip() is called with negative parameter");
        } catch (IllegalArgumentException e) {
        }
        inflatIP.close();
        InputStream infile2 = Support_Resources
                .getStream("hyts_constru_OD.txt");
        InflaterInputStream inflatIP2 = new InflaterInputStream(infile2);
        skip = inflatIP2.skip(Integer.MAX_VALUE);
        assertEquals("method skip() returned wrong number of bytes skipped", 5,
                skip);
        InputStream infile3 = Support_Resources
                .getStream("hyts_constru_OD.txt");
        InflaterInputStream inflatIP3 = new InflaterInputStream(infile3);
        skip = inflatIP3.skip(2);
        assertEquals(
                "the number of bytes returned by skip did not correspond with its input parameters",
                2, skip);
        int i = 0;
        result = 0;
        while ((result = inflatIP3.read()) != -1) {
            buffer[i] = result;
            i++;
        }
        inflatIP2.close();
        for (int j = 2; j < orgBuffer.length; j++) {
            assertTrue(
                    "original compressed data did not equal decompressed data",
                    buffer[j - 2] == orgBuffer[j]);
        }
        try {
            inflatIP2.skip(4);
            fail("IOException expected.");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "available",
        args = {}
    )
    public void test_available() throws IOException {
        InputStream is = Support_Resources.getStream("hyts_available.tst");
        InflaterInputStream iis = new InflaterInputStream(is);
        int available;
        for (int i = 0; i < 11; i++) {
            iis.read();
            available = iis.available();
            if (available == 0) {
                assertEquals("Expected no more bytes to read", -1, iis.read());
            } else {
                assertEquals("Bytes Available Should Return 1.", 1, available);
            }
        }
        iis.close();
        try {
            iis.available();
            fail("available after close should throw IOException.");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "IOException can not be tested.",
        method = "close",
        args = {}
    )
    public void test_close() throws IOException {
        InflaterInputStream iin = new InflaterInputStream(
                new ByteArrayInputStream(new byte[0]));
        iin.close();
        iin.close();
    }
}

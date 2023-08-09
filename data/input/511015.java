@TestTargetClass(GZIPOutputStream.class)
public class GZIPOutputStreamTest extends junit.framework.TestCase {
    class TestGZIPOutputStream extends GZIPOutputStream {
        TestGZIPOutputStream(OutputStream out) throws IOException {
            super(out);
        }
        TestGZIPOutputStream(OutputStream out, int size) throws IOException {
            super(out, size);
        }
        Checksum getChecksum() {
            return crc;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPOutputStream",
        args = {java.io.OutputStream.class}
    )
    public void test_ConstructorLjava_io_OutputStream() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0,
                    outGZIP.getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPOutputStream",
        args = {java.io.OutputStream.class, int.class}
    )
    public void test_ConstructorLjava_io_OutputStreamI() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile,
                    100);
            assertNotNull("the constructor for GZIPOutputStream is null",
                    outGZIP);
            assertEquals("the CRC value of the outputStream is not zero", 0,
                    outGZIP.getChecksum().getValue());
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "finish",
        args = {}
    )
    public void test_finish() {
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        TestGZIPOutputStream outGZIP = null;
        FileOutputStream outFile = null;
        try {
            outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.finish();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals(
                    "GZIP instance can still be used after finish is called",
                    1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
        try {
            outFile = new FileOutputStream("GZIPOutFinish.txt");
            outGZIP = new TestGZIPOutputStream(outFile);
            outFile.close();
            outGZIP.finish();
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "IOException checking missed.",
        method = "close",
        args = {}
    )
    public void test_close() {
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            TestGZIPOutputStream outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.close();
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals(
                    "GZIP instance can still be used after close is called", 1,
                    r);
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() {
        byte byteArray[] = {3, 5, 2, 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        TestGZIPOutputStream outGZIP = null;
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("GZIPOutCon", ".txt"));
            outGZIP = new TestGZIPOutputStream(outFile);
            outGZIP.write(byteArray, 0, 10);
            assertEquals(
                    "the checksum value was incorrect result of write from GZIP",
                    3097700292L, outGZIP.getChecksum().getValue());
            int r = 0;
            try {
                outGZIP.write(byteArray, 0, 11);
            } catch (IndexOutOfBoundsException ee) {
                r = 1;
            }
            assertEquals("out of bounds exception is not present", 1, r);
            outGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to find the output file or creating GZIP constructor");
        }
        try {
            outGZIP.write(byteArray, 0, 10);
            fail("Expected IOException");
        } catch (IOException e) {
        }
    }
    @Override
    protected void setUp() {
    }
    @Override
    protected void tearDown() {
        try {
            File dFile = new File("GZIPOutCon.txt");
            dFile.delete();
            File dFile2 = new File("GZIPOutFinish.txt");
            dFile2.delete();
            File dFile3 = new File("GZIPOutWrite.txt");
            dFile3.delete();
            File dFile4 = new File("GZIPOutClose2.txt");
            dFile4.delete();
        } catch (SecurityException e) {
            fail("Cannot delete file for security reasons");
        }
    }
}

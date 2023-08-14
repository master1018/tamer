@TestTargetClass(CheckedOutputStream.class)
public class CheckedOutputStreamTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CheckedOutputStream",
        args = {java.io.OutputStream.class, java.util.zip.Checksum.class}
    )
    public void test_ConstructorLjava_io_OutputStreamLjava_util_zip_Checksum() {
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("chkOut", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new CRC32());
            assertEquals("the checkSum value of the constructor is not 0", 0,
                    chkOut.getChecksum().getValue());
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail("file cannot be opened for writing due to security reasons");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getChecksum",
        args = {}
    )
    public void test_getChecksum() {
        byte byteArray[] = {1, 2, 3, 'e', 'r', 't', 'g', 3, 6};
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("chkOut", ".txt"));
            CheckedOutputStream chkOut = new CheckedOutputStream(outFile,
                    new Adler32());
            chkOut.write(byteArray[4]);
            assertEquals("the checkSum value for writeI is incorrect", 7536755,
                    chkOut.getChecksum().getValue());
            chkOut.getChecksum().reset();
            chkOut.write(byteArray, 5, 4);
            assertEquals("the checkSum value for writeBII is incorrect ",
                    51708133, chkOut.getChecksum().getValue());
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail("file cannot be opened for writing due to security reasons");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {int.class}
    )
    public void test_writeI() {
        CheckedOutputStream chkOut = null;
        byte byteArray[] = {1, 2, 3, 'e', 'r', 't', 'g', 3, 6};
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("chkOut", ".txt"));
            chkOut = new CheckedOutputStream(outFile, new CRC32());
            for (byte element : byteArray) {
                chkOut.write(element);
            }
            assertTrue(
                    "the checkSum value is zero, no bytes are written to the output file",
                    chkOut.getChecksum().getValue() != 0);
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail("File cannot be opened for writing due to security reasons");
        }
        try {
            chkOut.write(0);
            fail("IOException expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "write",
        args = {byte[].class, int.class, int.class}
    )
    public void test_write$BII() {
        CheckedOutputStream chkOut = null;
        byte byteArray[] = {1, 2, 3, 'e', 'r', 't', 'g', 3, 6};
        try {
            FileOutputStream outFile = new FileOutputStream(
                    File.createTempFile("chkOut", ".txt"));
            chkOut = new CheckedOutputStream(outFile, new CRC32());
            chkOut.write(byteArray, 4, 5);
            assertTrue(
                    "the checkSum value is zero, no bytes are written to the output file",
                    chkOut.getChecksum().getValue() != 0);
            int r = 0;
            try {
                chkOut.write(byteArray, 4, 6);
            } catch (IndexOutOfBoundsException e) {
                r = 1;
            }
            assertEquals("boundary check is not performed", 1, r);
            outFile.close();
        } catch (IOException e) {
            fail("Unable to find file");
        } catch (SecurityException e) {
            fail("file cannot be opened for writing due to security reasons");
        } catch (IndexOutOfBoundsException e) {
            fail("Index for write is out of bounds");
        }
        try {
            chkOut.write(byteArray, 4, 5);
            fail("IOException expected");
        } catch (IOException e) {
        }
    }
    @Override
    protected void setUp() {
    }
    @Override
    protected void tearDown() {
        try {
            File deletedFile = new File("chkOut.txt");
            deletedFile.delete();
        } catch (SecurityException e) {
            fail("Cannot delete file for security reasons");
        }
    }
}

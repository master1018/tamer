@TestTargetClass(CheckedInputStream.class)
public class CheckedInputStreamTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CheckedInputStream",
        args = {java.io.InputStream.class, java.util.zip.Checksum.class}
    )
    public void test_ConstructorLjava_io_InputStreamLjava_util_zip_Checksum()
            throws Exception {
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        assertEquals("constructor of checkedInputStream has failed", 0, checkIn
                .getChecksum().getValue());
        checkInput.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getChecksum",
        args = {}
    )
    public void test_getChecksum() throws Exception {
        byte outBuf[] = new byte[100];
        File empty = File.createTempFile("empty", ".txt");
        empty.deleteOnExit();
        FileOutputStream outEmp = new FileOutputStream(empty);
        outEmp.close();
        InputStream inEmp = new FileInputStream(empty);
        CheckedInputStream checkEmpty = new CheckedInputStream(inEmp,
                new CRC32());
        while (checkEmpty.read() >= 0) {
        }
        assertEquals("the checkSum value of an empty file is not zero", 0,
                checkEmpty.getChecksum().getValue());
        inEmp.close();
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        while (checkIn.read() >= 0) {
        }
        assertEquals("the checksum value is incorrect", 2036203193, checkIn
                .getChecksum().getValue());
        checkInput.close();
        checkInput = Support_Resources.getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn2 = new CheckedInputStream(checkInput,
                new CRC32());
        checkIn2.read(outBuf, 0, 10);
        assertEquals("the checksum value is incorrect", 2235765342L, checkIn2
                .getChecksum().getValue());
        checkInput.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "skip",
        args = {long.class}
    )
    public void test_skipJ() throws Exception {
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        long skipValue = 5;
        assertEquals(
                "the value returned by skip(n) is not the same as its parameter",
                skipValue, checkIn.skip(skipValue));
        checkIn.skip(skipValue);
        assertEquals("checkSum value is not correct", 2235765342L, checkIn
                .getChecksum().getValue());
        checkInput.close();
        try {
            checkInput.skip(33);
            fail("IOException expected");
        } catch (IOException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {}
    )
    public void test_read() throws Exception {
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        checkIn.read();
        checkIn.close();
        try {
            checkIn.read();
            fail("IOException expected.");
        } catch (IOException ee) {
        }
        long skipValue = 5;
        checkInput.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$byteII() throws Exception {
        InputStream checkInput = Support_Resources
                .getStream("hyts_checkInput.txt");
        CheckedInputStream checkIn = new CheckedInputStream(checkInput,
                new CRC32());
        byte buff[] = new byte[50];
        checkIn.read(buff, 10, 5);
        checkIn.close();
        try {
            checkIn.read(buff, 10, 5);
            fail("IOException expected.");
        } catch (IOException ee) {
        }
        long skipValue = 5;
        checkInput.close();
    }
}

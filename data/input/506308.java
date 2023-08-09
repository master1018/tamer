@TestTargetClass(CRC32.class)
public class CRC32Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CRC32",
        args = {}
    )
    public void test_Constructor() {
        CRC32 crc = new CRC32();
        assertEquals("Constructor of CRC32 failed", 0, crc.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {}
    )
    public void test_getValue() {
        CRC32 crc = new CRC32();
        assertEquals(
                "getValue() should return a zero as a result of constructing a CRC32 instance",
                0, crc.getValue());
        crc.reset();
        crc.update(Integer.MAX_VALUE);
        assertEquals(
                "update(max) failed to update the checksum to the correct value ",
                4278190080L, crc.getValue());
        crc.reset();
        byte byteEmpty[] = new byte[10000];
        crc.update(byteEmpty);
        assertEquals(
                "update(byte[]) failed to update the checksum to the correct value ",
                1295764014L, crc.getValue());
        crc.reset();
        crc.update(1);
        crc.reset();
        assertEquals("reset failed to reset the checksum value to zero", 0, crc
                .getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        CRC32 crc = new CRC32();
        crc.update(1);
        assertEquals(
                "update(int) failed to update the checksum to the correct value ",
                2768625435L, crc.getValue());
        crc.reset();
        assertEquals("reset failed to reset the checksum value to zero", 0, crc
                .getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {int.class}
    )
    public void test_updateI() {
        CRC32 crc = new CRC32();
        crc.update(1);
        assertEquals(
                "update(1) failed to update the checksum to the correct value ",
                2768625435L, crc.getValue());
        crc.reset();
        crc.update(Integer.MAX_VALUE);
        assertEquals(
                "update(max) failed to update the checksum to the correct value ",
                4278190080L, crc.getValue());
        crc.reset();
        crc.update(Integer.MIN_VALUE);
        assertEquals(
                "update(min) failed to update the checksum to the correct value ",
                3523407757L, crc.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class}
    )
    public void test_update$B() {
        byte byteArray[] = {1, 2};
        CRC32 crc = new CRC32();
        crc.update(byteArray);
        assertEquals(
                "update(byte[]) failed to update the checksum to the correct value ",
                3066839698L, crc.getValue());
        crc.reset();
        byte byteEmpty[] = new byte[10000];
        crc.update(byteEmpty);
        assertEquals(
                "update(byte[]) failed to update the checksum to the correct value ",
                1295764014L, crc.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void test_update$BII() {
        byte[] byteArray = {1, 2, 3};
        CRC32 crc = new CRC32();
        int off = 2;
        int len = 1;
        int lenError = 3;
        int offError = 4;
        crc.update(byteArray, off, len);
        assertEquals(
                "update(byte[],int,int) failed to update the checksum to the correct value ",
                1259060791L, crc.getValue());
        int r = 0;
        try {
            crc.update(byteArray, off, lenError);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        }
        assertEquals(
                "update(byte[],int,int) failed b/c lenError>byte[].length-off",
                1, r);
        try {
            crc.update(byteArray, offError, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 2;
        }
        assertEquals(
                "update(byte[],int,int) failed b/c offError>byte[].length", 2,
                r);
    }
    @Override
    protected void setUp() {
    }
    @Override
    protected void tearDown() {
    }
}

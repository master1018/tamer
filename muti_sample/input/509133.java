@TestTargetClass(Adler32.class)
public class Adler32Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Adler32",
        args = {}
    )
    public void test_Constructor() {
        Adler32 adl = new Adler32();
        assertEquals("Constructor of adl32 failed", 1, adl.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getValue",
        args = {}
    )
    public void test_getValue() {
        Adler32 adl = new Adler32();
        assertEquals(
                "GetValue should return a zero as a result of construction an object of Adler32",
                1, adl.getValue());
        adl.reset();
        adl.update(1);
        assertEquals(
                "update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());
        adl.reset();
        assertEquals("reset failed to reset the checksum value to zero", 1, adl
                .getValue());
        adl.reset();
        adl.update(Integer.MIN_VALUE);
        assertEquals(
                "update(min) failed to update the checksum to the correct value ",
                65537L, adl.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "reset",
        args = {}
    )
    public void test_reset() {
        Adler32 adl = new Adler32();
        adl.update(1);
        assertEquals(
                "update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());
        adl.reset();
        assertEquals("reset failed to reset the checksum value to zero", 1, adl
                .getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {int.class}
    )
    public void test_updateI() {
        Adler32 adl = new Adler32();
        adl.update(1);
        assertEquals(
                "update(int) failed to update the checksum to the correct value ",
                131074, adl.getValue());
        adl.reset();
        adl.update(Integer.MAX_VALUE);
        assertEquals(
                "update(max) failed to update the checksum to the correct value ",
                16777472L, adl.getValue());
        adl.reset();
        adl.update(Integer.MIN_VALUE);
        assertEquals(
                "update(min) failed to update the checksum to the correct value ",
                65537L, adl.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class}
    )
    public void test_update$B() {
        byte byteArray[] = {1, 2};
        Adler32 adl = new Adler32();
        adl.update(byteArray);
        assertEquals(
                "update(byte[]) failed to update the checksum to the correct value ",
                393220, adl.getValue());
        adl.reset();
        byte byteEmpty[] = new byte[10000];
        adl.update(byteEmpty);
        assertEquals(
                "update(byte[]) failed to update the checksum to the correct value ",
                655360001L, adl.getValue());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "update",
        args = {byte[].class, int.class, int.class}
    )
    public void test_update$BII() {
        byte[] byteArray = {1, 2, 3};
        Adler32 adl = new Adler32();
        int off = 2;
        int len = 1;
        int lenError = 3;
        int offError = 4;
        adl.update(byteArray, off, len);
        assertEquals(
                "update(byte[],int,int) failed to update the checksum to the correct value ",
                262148, adl.getValue());
        int r = 0;
        try {
            adl.update(byteArray, off, lenError);
        } catch (ArrayIndexOutOfBoundsException e) {
            r = 1;
        }
        assertEquals(
                "update(byte[],int,int) failed b/c lenError>byte[].length-off",
                1, r);
        try {
            adl.update(byteArray, offError, len);
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

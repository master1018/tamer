@TestTargetClass(IvParameterSpec.class)
public class IvParameterSpecTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IvParameterSpec",
        args = {byte[].class}
    )
    public void testIvParameterSpec1() {
        try {
            new IvParameterSpec(null);
            fail("Should raise an NullPointerException "
                    + "in the case of null byte array.");
        } catch(NullPointerException e) {
        }
        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv);
        iv[0] ++;
        assertFalse("The change of input array's content should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IvParameterSpec",
        args = {byte[].class, int.class, int.class}
    )
    public void testIvParameterSpec2() {
        try {
            new IvParameterSpec(null, 1, 1);
            fail("Should raise an IllegalArgumentException "
                    + "in the case of null byte array.");
        } catch(ArrayIndexOutOfBoundsException e) {
            fail("Unexpected ArrayIndexOutOfBoundsException was thrown");
        } catch(IllegalArgumentException e) {
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }
        try {
            new IvParameterSpec(new byte[] {1, 2, 3}, 2, 2);
            fail("Should raise an IllegalArgumentException "
                    + "if (iv.length - offset < len).");
        } catch(ArrayIndexOutOfBoundsException e) {
            fail("Unexpected ArrayIndexOutOfBoundsException was thrown");
        } catch(IllegalArgumentException e) {
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }
        try {
            new IvParameterSpec(new byte[] {1, 2, 3}, -1, 1);
            fail("Should raise an ArrayIndexOutOfBoundsException "
                    + "if offset index bytes outside the iv.");
        } catch(ArrayIndexOutOfBoundsException e) {
        } catch(IllegalArgumentException e) {
            fail("Unexpected IllegalArgumentException was thrown");
        } catch(NullPointerException e) {
            fail("Unexpected NullPointerException was thrown");
        }
        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv, 0, iv.length);
        iv[0] ++;
        assertFalse("The change of input array's content should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);
        try {
            new IvParameterSpec(new byte[2], 2,  -1);
            fail("ArrayIndexOutOfBoundsException expected");
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getIV",
        args = {}
    )
    public void testGetIV() {
        byte[] iv = new byte[] {1, 2, 3, 4, 5};
        IvParameterSpec ivps = new IvParameterSpec(iv);
        iv = ivps.getIV();
        iv[0] ++;
        assertFalse("The change of returned array should not cause "
                    + "the change of internal array", iv[0] == ivps.getIV()[0]);
    }
    public static Test suite() {
        return new TestSuite(IvParameterSpecTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

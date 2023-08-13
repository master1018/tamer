@TestTargetClass(DESedeKeySpec.class)
public class DESedeKeySpecTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks both constructors.",
            method = "DESedeKeySpec",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks both constructors.",
            method = "DESedeKeySpec",
            args = {byte[].class, int.class}
        )
    })
    public void testDESedeKeySpec() {
        try {
            new DESedeKeySpec((byte []) null);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESedeKeySpec(new byte [] {1, 2, 3});
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESedeKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                          1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                          1, 2, 3, 4});
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
        try {
            new DESedeKeySpec((byte []) null, 1);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESedeKeySpec(new byte []  {1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                            1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                            1, 2, 3, 4}, 1);
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESedeKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                          1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                                          1, 2, 3, 4, 5}, 1);
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getKey",
        args = {}
    )
    public void testGetKey() {
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2,
                      1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2};
        DESedeKeySpec ks;
        try {
            ks = new DESedeKeySpec(key);
        } catch (InvalidKeyException e) {
            fail("InvalidKeyException should not be thrown.");
            return;
        }
        byte[] res = ks.getKey();
        assertTrue("The returned array should be equal to the specified "
                    + "in constructor.", Arrays.equals(key, res));
        res[0] += 1;
        assertFalse("The modification of returned key should not affect"
                    + "the underlying key.", key[0] == res[0]);
        byte[] key1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3,
                       1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3};
        try {
            ks = new DESedeKeySpec(key1, 2);
        } catch (InvalidKeyException e) {
            fail("InvalidKeyException should not be thrown.");
            return;
        }
        res = ks.getKey();
        assertNotSame("The returned array should not be the same object "
                    + "as specified in a constructor.", key1, res);
        byte[] exp = new byte[24];
        System.arraycopy(key1, 2, exp, 0, 24);
        assertTrue("The returned array should be equal to the specified "
                    + "in constructor.", Arrays.equals(exp, res));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isParityAdjusted",
        args = {byte[].class, int.class}
    )
    public void testIsParityAdjusted() {
        try {
            DESedeKeySpec.isParityAdjusted(null, 1);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
                      1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        try {
            DESedeKeySpec.isParityAdjusted(key, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        byte[] key_not_pa = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2,
                             1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2};
        try {
            assertFalse("Method returns true when false is expected.",
                        DESedeKeySpec.isParityAdjusted(key_not_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
        byte[] key_pa = {(byte) 128, (byte) 131, (byte) 133, (byte) 134,
                         (byte) 137, (byte) 138, (byte) 140, (byte) 143,
                         (byte) 145, (byte) 146, (byte) 148, (byte) 151,
                         (byte) 152, (byte) 155, (byte) 157, (byte) 158,
                         (byte) 161, (byte) 162, (byte) 164, (byte) 167,
                         (byte) 168, (byte) 171, (byte) 173, (byte) 174};
        try {
            assertTrue("Method returns false when true is expected.",
                        DESedeKeySpec.isParityAdjusted(key_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
    }
    public static Test suite() {
        return new TestSuite(DESedeKeySpecTest.class);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}

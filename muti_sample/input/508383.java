@TestTargetClass(DESKeySpec.class)
public class DESKeySpecTest extends TestCase {
    private static final byte[][] semiweaks = {
                {(byte) 0xE0, (byte) 0x01, (byte) 0xE0, (byte) 0x01,
                 (byte) 0xF1, (byte) 0x01, (byte) 0xF1, (byte) 0x01},
                {(byte) 0x01, (byte) 0xE0, (byte) 0x01, (byte) 0xE0,
                 (byte) 0x01, (byte) 0xF1, (byte) 0x01, (byte) 0xF1},
                {(byte) 0xFE, (byte) 0x1F, (byte) 0xFE, (byte) 0x1F,
                 (byte) 0xFE, (byte) 0x0E, (byte) 0xFE, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0xFE, (byte) 0x1F, (byte) 0xFE,
                 (byte) 0x0E, (byte) 0xFE, (byte) 0x0E, (byte) 0xFE},
                {(byte) 0xE0, (byte) 0x1F, (byte) 0xE0, (byte) 0x1F,
                 (byte) 0xF1, (byte) 0x0E, (byte) 0xF1, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0xE0, (byte) 0x1F, (byte) 0xE0,
                 (byte) 0x0E, (byte) 0xF1, (byte) 0x0E, (byte) 0xF1},
                {(byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE,
                 (byte) 0x01, (byte) 0xFE, (byte) 0x01, (byte) 0xFE},
                {(byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01,
                 (byte) 0xFE, (byte) 0x01, (byte) 0xFE, (byte) 0x01},
                {(byte) 0x01, (byte) 0x1F, (byte) 0x01, (byte) 0x1F,
                 (byte) 0x01, (byte) 0x0E, (byte) 0x01, (byte) 0x0E},
                {(byte) 0x1F, (byte) 0x01, (byte) 0x1F, (byte) 0x01,
                 (byte) 0x0E, (byte) 0x01, (byte) 0x0E, (byte) 0x01},
                {(byte) 0xE0, (byte) 0xFE, (byte) 0xE0, (byte) 0xFE,
                 (byte) 0xF1, (byte) 0xFE, (byte) 0xF1, (byte) 0xFE},
                {(byte) 0xFE, (byte) 0xE0, (byte) 0xFE, (byte) 0xE0,
                 (byte) 0xFE, (byte) 0xF1, (byte) 0xFE, (byte) 0xF1},
                {(byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01, 
                 (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x01},
                {(byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
                 (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE},
                {(byte) 0xE0, (byte) 0xE0, (byte) 0xE0, (byte) 0xE0,
                 (byte) 0xF1, (byte) 0xF1, (byte) 0xF1, (byte) 0xF1},
                {(byte) 0x1F, (byte) 0x1F, (byte) 0x1F, (byte) 0x1F,
                 (byte) 0x0E, (byte) 0x0E, (byte) 0x0E, (byte) 0x0E},
            };
    private static final byte[][] notsemiweaks = {
                {(byte) 0x1f, (byte) 0x1f, (byte) 0x1f, (byte) 0x1f,
                 (byte) 0x1f, (byte) 0x1f, (byte) 0x1f, (byte) 0x1f},
                {(byte) 0xe0, (byte) 0xe0, (byte) 0xe0, (byte) 0xe0,
                 (byte) 0xe0, (byte) 0xe0, (byte) 0xe0, (byte) 0xe0}
            };
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks both constructors.",
            method = "DESKeySpec",
            args = {byte[].class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Checks both constructors.",
            method = "DESKeySpec",
            args = {byte[].class, int.class}
        )
    })
    public void testDESKeySpec() {
        try {
            new DESKeySpec((byte []) null);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESKeySpec(new byte [] {1, 2, 3});
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8});
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
        try {
            new DESKeySpec((byte []) null, 1);
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
        } catch (InvalidKeyException e) {
            fail("Should raise an NullPointerException "
                    + "in case of null byte array.");
        }
        try {
            new DESKeySpec(new byte []  {1, 2, 3, 4, 5, 6, 7, 8}, 1);
            fail("Should raise an InvalidKeyException on a short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        try {
            new DESKeySpec(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9}, 1);
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
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        DESKeySpec ks;
        try {
            ks = new DESKeySpec(key);
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
        byte[] key1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        try {
            ks = new DESKeySpec(key1, 2);
        } catch (InvalidKeyException e) {
            fail("InvalidKeyException should not be thrown.");
            return;
        }
        res = ks.getKey();
        assertNotSame("The returned array should not be the same object "
                    + "as specified in a constructor.", key1, res);
        byte[] exp = new byte[8];
        System.arraycopy(key1, 2, exp, 0, 8);
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
            DESKeySpec.isParityAdjusted(null, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            DESKeySpec.isParityAdjusted(key, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        byte[] key_not_pa = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            assertFalse("Method returns true when false is expected.",
                        DESKeySpec.isParityAdjusted(key_not_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
        byte[] key_pa = {(byte) 128, (byte) 131, (byte) 133, (byte) 134,
                         (byte) 137, (byte) 138, (byte) 140, (byte) 143};
        try {
            assertTrue("Method returns false when true is expected.",
                        DESKeySpec.isParityAdjusted(key_pa, 0));
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
            fail("Unexpected InvalidKeyException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isWeak",
        args = {byte[].class, int.class}
    )
    public void testIsWeak() {
        try {
            DESKeySpec.isWeak(null, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of null byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        byte[] key = {1, 2, 3, 4, 5, 6, 7, 8};
        try {
            DESKeySpec.isWeak(key, 1);
            fail("Should raise an InvalidKeyException "
                    + "in case of short byte array.");
        } catch (NullPointerException e) {
            fail("Unexpected NullPointerException was thrown.");
        } catch (InvalidKeyException e) {
        }
        for (int i=0; i<semiweaks.length; i++) {
            try {
                assertTrue("Method returns false when true is expected",
                        DESKeySpec.isWeak(semiweaks[i], 0));
            } catch (InvalidKeyException e) {
                fail("Unexpected InvalidKeyException was thrown.");
            }
        }
        for (int i=0; i<notsemiweaks.length; i++) {
            try {
                assertFalse("Method returns true when false is expected",
                        DESKeySpec.isWeak(notsemiweaks[i], 0));
            } catch (InvalidKeyException e) {
                fail("Unexpected InvalidKeyException was thrown.");
            }
        }
    }
    public static Test suite() {
        return new TestSuite(DESKeySpecTest.class);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
}

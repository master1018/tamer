@TestTargetClass(DSAKeyPairGenerator.class)
public class DSAKeyPairGeneratorTest extends TestCase {
    private final BigInteger p = new BigInteger("4");
    private final BigInteger q = BigInteger.TEN;
    private final BigInteger g = BigInteger.ZERO;
    class MyDSA extends DSAKeyPairGeneratorImpl {
        public MyDSA(DSAParams dsaParams) {
            super(dsaParams);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initialize",
        args = {java.security.interfaces.DSAParams.class, java.security.SecureRandom.class}
    )
    public void test_DSAKeyPairGenerator01() {
        DSAParams dsaParams = new DSAParameterSpec(p, q, g);
        SecureRandom random = null;
        MyDSA dsa = new MyDSA(dsaParams);
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            fail("Unexpected exception for SecureRandom: " + e);
        }
        try {
            dsa.initialize(dsaParams, random);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
        try {
            dsa.initialize(dsaParams, null);
            fail("InvalidParameterException was not thrown");
        } catch (InvalidParameterException ipe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of InvalidParameterException");
        }
        try {
            dsa.initialize(null, random);
            fail("InvalidParameterException was not thrown");
        } catch (InvalidParameterException ipe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of InvalidParameterException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "initialize",
        args = {int.class, boolean.class, java.security.SecureRandom.class}
    )
    public void test_DSAKeyPairGenerator02() {
        int[] invalidLen = {-1, 0, 511, 513, 650, 1023, 1025};
        DSAParams dsaParams = new DSAParameterSpec(p, q, g);
        SecureRandom random = null;
        MyDSA dsa = new MyDSA(null);
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            fail("Unexpected exception for SecureRandom: " + e);
        }
        try {
            dsa.initialize(520, false, random);
            fail("InvalidParameterException was not thrown");
        } catch (InvalidParameterException ipe) {
            String str = ipe.getMessage();
            if (!str.equals("there are not precomputed parameters")) {
                fail("Incorrect exception's message: " + str);
            }
        } catch (Exception e) {
            fail(e + " was thrown instead of InvalidParameterException");
        }
        for (int i = 0; i < invalidLen.length; i++) {
            try {
                dsa.initialize(invalidLen[i], true, random);
                fail("InvalidParameterException was not thrown");
            } catch (InvalidParameterException ipe) {
                String str = ipe.getMessage();
                if (!str.equals("Incorrect modlen")) {
                    fail("Incorrect exception's message: " + str);
                }
            } catch (Exception e) {
                fail(e + " was thrown instead of InvalidParameterException");
            }            
        }
        dsa = new MyDSA(dsaParams);
        try {
            dsa.initialize(520, true, random);
        } catch (Exception e) {
            fail(e + " was thrown for subcase 1");
        }
        try {
            dsa.initialize(520, false, random);
        } catch (Exception e) {
            fail(e + " was thrown for subcase 1");
        }
    }
}

@TestTargetClass(AlgorithmParameterGenerator.class)
public class AlgorithmParameterGenerator3Test extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "generateParameters",
        args = {}
    )
    @BrokenTest("Takes too long due to math implementation, disabling for now")
    public void test_generateParameters() throws Exception {
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        gen.init(1024);
        AlgorithmParameters params = gen.generateParameters();
        assertNotNull("params is null", params);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void test_getAlgorithm() throws Exception {
        String alg = AlgorithmParameterGenerator.getInstance("DSA")
                .getAlgorithm();
        assertEquals("getAlgorithm ok", "DSA", alg);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies getInstance with parameter",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_String() throws Exception {
        AlgorithmParameterGenerator.getInstance("DSA");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Test NoSuchAlgorithmException is missed",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_getInstanceLjava_lang_StringLjava_lang_String() throws Exception {
               Provider[] provs = Security
                       .getProviders("AlgorithmParameterGenerator.DSA");
        for (int i = 0; i < provs.length; i++) {
                AlgorithmParameterGenerator.getInstance("DSA", provs[i].getName());
            }
        try {
            AlgorithmParameterGenerator.getInstance("DSA", (String) null);
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            AlgorithmParameterGenerator.getInstance("DSA", "");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            AlgorithmParameterGenerator.getInstance("DSA", "IDontExist");
            fail("Should have thrown NoSuchProviderException");
        } catch (NoSuchProviderException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies provider with null parameter",
        method = "getProvider",
        args = {}
    )
    public void test_getProvider() throws Exception {
        Provider p = AlgorithmParameterGenerator.getInstance("DSA")
                .getProvider();
        assertNotNull("provider is null", p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class}
    )
    public void test_initI() throws Exception {
        int[] valid = {512, 576, 640, 960, 1024};
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        for (int i = 0; i < valid.length; i++) {
            try {
                gen.init(valid[i]);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid parameter" + valid[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {int.class, java.security.SecureRandom.class}
    )
    public void test_initILjava_security_SecureRandom() throws Exception {
        int[] valid = {512, 576, 640, 960, 1024};
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        for (int i = 0; i < valid.length; i++) {
            try {
                gen.init(valid[i], new SecureRandom());
                gen.init(valid[i], null);
            } catch (Exception e) {
                fail("Exception should not be thrown for valid parameter" + valid[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies InvalidAlgorithmParameterException exception only",
        method = "init",
        args = {java.security.spec.AlgorithmParameterSpec.class}
    )
    public void test_initLjava_security_spec_AlgorithmParameterSpec() throws Exception {
        DSAParameterSpec spec = new DSAParameterSpec(BigInteger.ONE,
                BigInteger.ONE, BigInteger.ONE);
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        try {
            gen.init(spec);
            fail("No expected InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies InvalidAlgorithmParameterException exception only",
        method = "init",
        args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
    )
    public void test_initLjava_security_spec_AlgorithmParameterSpecLjava_security_SecureRandom() throws Exception {
        DSAParameterSpec spec = new DSAParameterSpec(BigInteger.ONE,
                BigInteger.ONE, BigInteger.ONE);
        AlgorithmParameterGenerator gen = AlgorithmParameterGenerator
                .getInstance("DSA");
        try {
            gen.init(spec, new SecureRandom());
            fail("No expected InvalidAlgorithmParameterException");
        } catch (InvalidAlgorithmParameterException e) {
        }
    }
}

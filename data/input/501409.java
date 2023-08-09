@TestTargetClass(AlgorithmParameterGenerator.class)
public class AlgorithmParameterGenerator2Test extends TestCase {
    private static final String AlgorithmParameterGeneratorProviderClass = "org.apache.harmony.security.tests.support.MyAlgorithmParameterGeneratorSpi";
    private static final String defaultAlg = "APG";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "apG";
        validValues[3] = "ApG";
    }
    Provider mProv;
    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MyAPGProvider", "Testing provider", 
                AlgorithmParameterGenerator1Test.srvAlgorithmParameterGenerator.concat(".").concat(defaultAlg),
                AlgorithmParameterGeneratorProviderClass);
        Security.insertProviderAt(mProv, 1);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    private void checkResult(AlgorithmParameterGenerator algParGen) 
            throws InvalidAlgorithmParameterException {
        AlgorithmParameters param = algParGen.generateParameters();
        assertNull("Not null parameters", param);
        AlgorithmParameterSpec pp = null;
        algParGen.init(pp, new SecureRandom());
        algParGen.init(pp);
        try {
            algParGen.init(pp, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        pp = new tmpAlgorithmParameterSpec("Proba");
        algParGen.init(pp, new SecureRandom());
        algParGen.init(pp);
        algParGen.init(0, null);
        algParGen.init(0, new SecureRandom());        
        try {
            algParGen.init(-10, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        try {
            algParGen.init(-10, new SecureRandom());
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance01() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider(), mProv);
            checkResult(apG);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testGetInstance02() throws NoSuchAlgorithmException,
            NoSuchProviderException, IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null, mProv.getName());
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i], mProv
                        .getName());
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    AlgorithmParameterGenerator.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (algorithm: "
                            .concat(invalidValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i], mProv
                    .getName());
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider().getName(),
                    mProv.getName());
            checkResult(apG);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance03() throws NoSuchAlgorithmException,
            IllegalArgumentException,
            InvalidAlgorithmParameterException {
        try {
            AlgorithmParameterGenerator.getInstance(null, mProv);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        AlgorithmParameterGenerator apG;
        for (int i = 0; i < validValues.length; i++) {
            apG = AlgorithmParameterGenerator.getInstance(validValues[i], mProv);
            assertEquals("Incorrect algorithm", apG.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider", apG.getProvider(), mProv);
            checkResult(apG);
        }
    }
    class tmpAlgorithmParameterSpec implements AlgorithmParameterSpec {
        private final String type;
        public tmpAlgorithmParameterSpec(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }
}

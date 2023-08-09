@TestTargetClass(AlgorithmParameterGenerator.class)
public class AlgorithmParameterGenerator1Test extends TestCase {
    private static String[] invalidValues = SpiEngUtils.invalidValues;
    private static String validAlgName = "DSA";
    private static String[] algs =  {
            "DSA", "dsa", "Dsa", "DsA", "dsA" }; 
    public static final String srvAlgorithmParameterGenerator = "AlgorithmParameterGenerator";
    private static String validProviderName = null;
    private static Provider validProvider = null;
    private static boolean DSASupported = false;
    static {
        validProvider = SpiEngUtils.isSupport(
                validAlgName,
                srvAlgorithmParameterGenerator);
        DSASupported = (validProvider != null);
        validProviderName = (DSASupported ? validProvider.getName() : null);
    }
    protected AlgorithmParameterGenerator[] createAPGen() {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return null;
        }
        AlgorithmParameterGenerator[] apg = new AlgorithmParameterGenerator[3];
        try {
            apg[0] = AlgorithmParameterGenerator.getInstance(validAlgName);
            apg[1] = AlgorithmParameterGenerator.getInstance(validAlgName,
                    validProvider);
            apg[2] = AlgorithmParameterGenerator.getInstance(validAlgName,
                    validProviderName);
            return apg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator01()
            throws NoSuchAlgorithmException {
        try {
            AlgorithmParameterGenerator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException should be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator02()
            throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        AlgorithmParameterGenerator apg;
        for (int i = 0; i < algs.length; i++) {
            apg = AlgorithmParameterGenerator.getInstance(algs[i]);
            assertEquals("Incorrect algorithm", apg.getAlgorithm(), algs[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator03()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        String provider = null;
        for (int i = 0; i < algs.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(algs[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
            try {
                AlgorithmParameterGenerator.getInstance(algs[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator04()
            throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        for (int i = 0; i < algs.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    AlgorithmParameterGenerator.getInstance(algs[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (provider: "
                            .concat(invalidValues[j]));
                } catch (NoSuchProviderException e) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator05()
            throws NoSuchProviderException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        try {
            AlgorithmParameterGenerator.getInstance(null, validProviderName);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i],
                        validProviderName);
                fail("NoSuchAlgorithmException must be thrown when (algorithm: "
                        .concat(invalidValues[i].concat(")")));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testAlgorithmParameterGenerator06()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        AlgorithmParameterGenerator apg;
        for (int i = 0; i < algs.length; i++) {
            apg = AlgorithmParameterGenerator.getInstance(algs[i],
                    validProviderName);
            assertEquals("Incorrect algorithm", algs[i], apg.getAlgorithm());
            assertEquals("Incorrect provider", apg.getProvider().getName(),
                    validProviderName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testAlgorithmParameterGenerator07()
            throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        Provider provider = null;
        for (int i = 0; i < algs.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(algs[i], provider);
                fail("IllegalArgumentException must be thrown when provider is null");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testAlgorithmParameterGenerator08() {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        try {
            AlgorithmParameterGenerator.getInstance(null, validProvider);
            fail("NullPointerException or NoSuchAlgorithmException should be thrown");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                AlgorithmParameterGenerator.getInstance(invalidValues[i],
                        validProvider);
                fail("NoSuchAlgorithmException must be thrown (algorithm: "
                        .concat(invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testAlgorithmParameterGenerator09()
            throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        AlgorithmParameterGenerator apg;
        for (int i = 0; i < algs.length; i++) {
            apg = AlgorithmParameterGenerator.getInstance(algs[i],
                    validProvider);
            assertEquals("Incorrect algorithm", apg.getAlgorithm(), algs[i]);
            assertEquals("Incorrect provider", apg.getProvider(), validProvider);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "generateParameters",
        args = {}
    )
    public void testAlgorithmParameterGenerator10()
            throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        AlgorithmParameterGenerator apg = AlgorithmParameterGenerator
                .getInstance(validAlgName);
        apg.init(512);
        AlgorithmParameters ap = apg.generateParameters();
        assertEquals("Incorrect algorithm", ap.getAlgorithm().toUpperCase(),
                apg.getAlgorithm().toUpperCase());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Only invalid argument cases are verified. This is " +
            "sufficient since the effects of calling init with valid " +
            "parameters can not be observed.",
            method = "init",
            args = {java.security.spec.AlgorithmParameterSpec.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Only invalid argument cases are verified. This is " +
            "sufficient since the effects of calling init with valid " +
            "parameters can not be observed.",
            method = "init",
            args = {java.security.spec.AlgorithmParameterSpec.class, java.security.SecureRandom.class}
        )
    })
    public void testAlgorithmParameterGenerator12() {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        SecureRandom random = new SecureRandom();
        AlgorithmParameterSpec aps = null;
        AlgorithmParameterGenerator[] apgs = createAPGen();
        assertNotNull("AlgorithmParameterGenerator objects were not created",
                apgs);
        for (int i = 0; i < apgs.length; i++) {
            try {
                apgs[i].init(aps);
                fail("InvalidAlgorithmParameterException expected for null argument.");
            } catch (InvalidAlgorithmParameterException e) {      
            }
            try {
                apgs[i].init(aps, random);
                fail("InvalidAlgorithmParameterException expected for null argument.");
            } catch (InvalidAlgorithmParameterException e) {    
            }
        }        
    }
    @TestTargets ({
        @TestTargetNew(
                level = TestLevel.COMPLETE,
                notes = "Validity of arguments is not checked in the constructor; " +
                "this is done during init.",
                method = "AlgorithmParameterGenerator",
                args = {java.security.AlgorithmParameterGeneratorSpi.class, java.security.Provider.class, java.lang.String.class}
        ),
        @TestTargetNew(
                level = TestLevel.PARTIAL_COMPLETE,
                method = "init",
                args = {int.class, SecureRandom.class}
        )
    })
    public void testConstructor() throws NoSuchAlgorithmException {
        if (!DSASupported) {
            fail(validAlgName + " algorithm is not supported");
            return;
        }
        AlgorithmParameterGeneratorSpi spi = new MyAlgorithmParameterGeneratorSpi();
        AlgorithmParameterGenerator apg = 
                new myAlgPG(spi, validProvider, validAlgName);
        assertEquals("Incorrect algorithm", apg.getAlgorithm(), validAlgName);
        assertEquals("Incorrect provider",apg.getProvider(),validProvider);
        try {
            apg.init(-10, null);
            fail("IllegalArgumentException must be thrown");
        } catch (IllegalArgumentException e) {
        }
        apg = new myAlgPG(null, null, null);
        assertNull("Incorrect algorithm", apg.getAlgorithm());
        assertNull("Incorrect provider", apg.getProvider());
        try {
            apg.init(-10, null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(AlgorithmParameterGenerator1Test.class);
    }
}
class myAlgPG extends AlgorithmParameterGenerator {
    public myAlgPG(AlgorithmParameterGeneratorSpi spi, Provider prov, String alg) {
        super(spi, prov, alg);
    }
}

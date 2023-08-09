@TestTargetClass(CertPathBuilder.class)
public class CertPathBuilder2Test extends TestCase {
    private static final String defaultAlg = "CertPB";
    private static final String CertPathBuilderProviderClass = "org.apache.harmony.security.tests.support.cert.MyCertPathBuilderSpi";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "CeRtPb";
        validValues[3] = "cERTpb";
    }
    Provider mProv;
    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MyCertPathBuilderProvider",
                "Provider for testing", CertPathBuilder1Test.srvCertPathBuilder
                        + "." + defaultAlg,
                CertPathBuilderProviderClass);
        Security.insertProviderAt(mProv, 1);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    private void checkResult(CertPathBuilder certBuild)
            throws InvalidAlgorithmParameterException,
            CertPathBuilderException {
        String dt = CertPathBuilder.getDefaultType();
        String propName = CertPathBuilder1Test.DEFAULT_TYPE_PROPERTY;
        String dtN;
        for (int i = 0; i <invalidValues.length; i++) {
            Security.setProperty(propName, invalidValues[i]);
            dtN = CertPathBuilder.getDefaultType();
            if (!dtN.equals(invalidValues[i]) && !dtN.equals(dt)) {
                fail("Incorrect default type: ".concat(dtN));
            }
        }
        Security.setProperty(propName, dt);
        assertEquals("Incorrect default type", CertPathBuilder.getDefaultType(),
                dt);
        try {
            certBuild.build(null);
            fail("CertPathBuilderException must be thrown");
        } catch (CertPathBuilderException e) {
        }    
        CertPathBuilderResult cpbResult = certBuild.build(null);
        assertNull("Not null CertPathBuilderResult", cpbResult);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance01() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, CertPathBuilderException {
        try {
            CertPathBuilder.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        CertPathBuilder cerPB;
        for (int i = 0; i < validValues.length; i++) {
            cerPB = CertPathBuilder.getInstance(validValues[i]);
            assertEquals("Incorrect type", cerPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider", cerPB.getProvider(), mProv);
            checkResult(cerPB);
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
            InvalidAlgorithmParameterException, CertPathBuilderException {
        try {
            CertPathBuilder.getInstance(null, mProv.getName());
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], mProv
                        .getName());
                fail("NoSuchAlgorithmException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertPathBuilder.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
            try {
                CertPathBuilder.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    CertPathBuilder.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (type: "
                            .concat(validValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        CertPathBuilder cerPB;
        for (int i = 0; i < validValues.length; i++) {
            cerPB = CertPathBuilder.getInstance(validValues[i], mProv
                    .getName());
            assertEquals("Incorrect type", cerPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider", cerPB.getProvider().getName(),
                    mProv.getName());
            checkResult(cerPB);
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
            InvalidAlgorithmParameterException, CertPathBuilderException {
        try {
            CertPathBuilder.getInstance(null, mProv);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathBuilder.getInstance(invalidValues[i], mProv);
                fail("NoSuchAlgorithmException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (NoSuchAlgorithmException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertPathBuilder.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        CertPathBuilder cerPB;
        for (int i = 0; i < validValues.length; i++) {
            cerPB = CertPathBuilder.getInstance(validValues[i], mProv);
            assertEquals("Incorrect type", cerPB.getAlgorithm(), validValues[i]);
            assertEquals("Incorrect provider", cerPB.getProvider(), mProv);
            checkResult(cerPB);
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(CertPathBuilder2Test.class);
    }  
}

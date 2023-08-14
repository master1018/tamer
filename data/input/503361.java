@TestTargetClass(CertificateFactory.class)
public class CertificateFactory2Test extends TestCase {
    private static final String defaultAlg = "CertFac";
    private static final String CertificateFactoryProviderClass = "org.apache.harmony.security.tests.support.cert.MyCertificateFactorySpi";
    private static final String[] invalidValues = SpiEngUtils.invalidValues;
    private static final String[] validValues;
    static {
        validValues = new String[4];
        validValues[0] = defaultAlg;
        validValues[1] = defaultAlg.toLowerCase();
        validValues[2] = "CeRtFaC";
        validValues[3] = "cerTFac";
    }
    Provider mProv;
    protected void setUp() throws Exception {
        super.setUp();
        mProv = (new SpiEngUtils()).new MyProvider("MyCFProvider",
                "Provider for testing", CertificateFactory1Test.srvCertificateFactory
                        .concat(".").concat(defaultAlg),
                CertificateFactoryProviderClass);
        Security.insertProviderAt(mProv, 1);
    }
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(mProv.getName());
    }
    private void checkResult(CertificateFactory certFactory, boolean mode)
            throws CertificateException, CRLException {
        MyCertificateFactorySpi.putMode(mode);
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
        DataInputStream dis = new DataInputStream(bais);
        try {
            certFactory.generateCertPath(bais);
            fail("CertificateException must be thrown");
        } catch (CertificateException e) {
        }
        try {
            certFactory.generateCertPath(dis);
            if (!mode) {
                fail("CertificateException must be thrown because encodings list is empty");
            }
        } catch (CertificateException e) {
            if (mode) {
                fail("Unexpected CertificateFactoryException was thrown");                
            }
        }
        try {
            certFactory.generateCertPath(bais, "aa");
            fail("CertificateException must be thrown");
        } catch (CertificateException e) {
        }
        try {
            certFactory.generateCertPath(dis, "");
            if (mode) {
                fail("IllegalArgumentException must be thrown");
            }
        } catch (IllegalArgumentException e) {
            if (!mode) {
                fail("Unexpected IllegalArgumentException was thrown");
            }
        }
        certFactory.generateCertPath(dis, "ss");
        try {
            certFactory.generateCertificate(bais);
            fail("CertificateException must be thrown");
        } catch (CertificateException e) {
        }
        try {
            certFactory.generateCertificates(null);
            fail("CertificateException must be thrown");
        } catch (CertificateException e) {
        }
        Certificate cert = certFactory.generateCertificate(dis);
        assertNull("Result must be null", cert);
        Collection<? extends Certificate> col = certFactory.generateCertificates(dis);
        assertNull("Result must be null", col);
        try {
            certFactory.generateCRL(bais);
            fail("CRLException must be thrown");
        } catch (CRLException e) {
        }
        try {
            certFactory.generateCRLs(null);
            fail("CRLException must be thrown");
        } catch (CRLException e) {
        }
        CRL crl = certFactory.generateCRL(dis);
        assertNull("Result must be null", crl);
        Collection<? extends CRL> colc = certFactory.generateCRLs(dis);
        assertNull("Result must be null", colc);
        List<Certificate> list = null;
        CertPath cp;
        try {
            cp = certFactory.generateCertPath(list);
            if (mode) {
                fail("NullPointerException must be thrown");
            } else {
                assertNull("Must be null", cp);                
            }
        } catch (NullPointerException e) {
            if (!mode) {
                fail("Unexpected NullPointerException was thrown");
            }
        }
        Iterator<String> it = certFactory.getCertPathEncodings();
        if (mode) {
            assertTrue(it.hasNext());
        } else {
            assertFalse(it.hasNext());            
        }
    }
    public void GetInstance01(boolean mode) throws CertificateException, CRLException {
        try {
            CertificateFactory.getInstance(null);
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i]);
                fail("CertificateException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (CertificateException e) {
            }
        }
        CertificateFactory cerF;
        for (int i = 0; i < validValues.length; i++) {
            cerF = CertificateFactory.getInstance(validValues[i]);
            assertEquals("Incorrect type", cerF.getType(), validValues[i]);
            assertEquals("Incorrect provider", cerF.getProvider(), mProv);
            checkResult(cerF, mode);
        }
    }
    public void GetInstance02(boolean mode) throws CertificateException,
            NoSuchProviderException, IllegalArgumentException, CRLException {
        try {
            CertificateFactory.getInstance(null, mProv.getName());
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i], mProv
                        .getName());
                fail("CertificateException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (CertificateException e) {
            }
        }
        String prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertificateFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
            try {
                CertificateFactory.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown when provider is empty (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        for (int i = 0; i < validValues.length; i++) {
            for (int j = 1; j < invalidValues.length; j++) {
                try {
                    CertificateFactory.getInstance(validValues[i],
                            invalidValues[j]);
                    fail("NoSuchProviderException must be thrown (type: "
                            .concat(validValues[i]).concat(" provider: ")
                            .concat(invalidValues[j]).concat(")"));
                } catch (NoSuchProviderException e) {
                }
            }
        }
        CertificateFactory cerF;
        for (int i = 0; i < validValues.length; i++) {
            cerF = CertificateFactory.getInstance(validValues[i], mProv
                    .getName());
            assertEquals("Incorrect type", cerF.getType(), validValues[i]);
            assertEquals("Incorrect provider", cerF.getProvider().getName(),
                    mProv.getName());
            checkResult(cerF, mode);
        }
    }
    public void GetInstance03(boolean mode) throws CertificateException,
            IllegalArgumentException, CRLException {
        try {
            CertificateFactory.getInstance(null, mProv);
            fail("NullPointerException or CertificateException must be thrown when type is null");
        } catch (CertificateException e) {
        } catch (NullPointerException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertificateFactory.getInstance(invalidValues[i], mProv);
                fail("CertificateException must be thrown (type: ".concat(
                        invalidValues[i]).concat(")"));
            } catch (CertificateException e) {
            }
        }
        Provider prov = null;
        for (int i = 0; i < validValues.length; i++) {
            try {
                CertificateFactory.getInstance(validValues[i], prov);
                fail("IllegalArgumentException must be thrown when provider is null (type: "
                        .concat(validValues[i]).concat(")"));
            } catch (IllegalArgumentException e) {
            }
        }
        CertificateFactory cerF;
        for (int i = 0; i < validValues.length; i++) {
            cerF = CertificateFactory.getInstance(validValues[i], mProv);
            assertEquals("Incorrect type", cerF.getType(), validValues[i]);
            assertEquals("Incorrect provider", cerF.getProvider(), mProv);            
            checkResult(cerF,  mode);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance01() throws CertificateException, CRLException {
        GetInstance01(true);   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance02() throws CertificateException,
        NoSuchProviderException, IllegalArgumentException, CRLException {
        GetInstance02(true);   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )    
    public void testGetInstance03() throws CertificateException,
        IllegalArgumentException, CRLException {
        GetInstance03(true);   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testGetInstance04() throws CertificateException, CRLException {
        GetInstance01(false);   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testGetInstance05() throws CertificateException,
        NoSuchProviderException, IllegalArgumentException, CRLException {
        GetInstance02(false);   
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )    
    public void testGetInstance06() throws CertificateException,
        IllegalArgumentException, CRLException {
        GetInstance03(false);   
    }
}

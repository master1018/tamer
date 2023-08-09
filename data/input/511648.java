@TestTargetClass(CertPathValidator.class)
public class CertPathValidator1Test extends TestCase {
    public static final String srvCertPathValidator = "CertPathValidator";
    private static final String defaultType = "PKIX";    
    public static final String [] validValues = {
            "PKIX", "pkix", "PkiX", "pKiX" };
    private static String [] invalidValues = SpiEngUtils.invalidValues;
    private static boolean PKIXSupport = false;
    private static Provider defaultProvider;
    private static String defaultProviderName;
    private static String NotSupportMsg = "";
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                srvCertPathValidator);
        PKIXSupport = (defaultProvider != null);
        defaultProviderName = (PKIXSupport ? defaultProvider.getName() : null);
        NotSupportMsg = defaultType.concat(" is not supported");
    }    
    private static CertPathValidator[] createCPVs() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return null;
        }
        try {
            CertPathValidator[] certPVs = new CertPathValidator[3];
            certPVs[0] = CertPathValidator.getInstance(defaultType);
            certPVs[1] = CertPathValidator.getInstance(defaultType,
                    defaultProviderName);
            certPVs[2] = CertPathValidator.getInstance(defaultType,
                    defaultProvider);
            return certPVs;
        } catch (Exception e) {
            return null;
        }
    }    
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultType",
        args = {}
    )
    public void testCertPathValidator01() {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        String propName = "certpathvalidator.type";
        String defCPV = Security.getProperty(propName);
        String dt = CertPathValidator.getDefaultType();
        String resType = defCPV; 
        if (resType == null) {
            resType = defaultType;
        }
        assertNotNull("Default type have not be null", dt);
        assertEquals("Incorrect default type", dt, resType);
        if (defCPV == null) {
            Security.setProperty(propName, defaultType);
            dt = CertPathValidator.getDefaultType();
            resType = Security.getProperty(propName);
            assertNotNull("Incorrect default type", resType);
            assertNotNull("Default type have not be null", dt);
            assertEquals("Incorrect default type", dt, resType);            
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NoSuchAlgorithmException.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testCertPathValidator02() {
        try {
            CertPathValidator.getInstance(null);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathValidator.getInstance(invalidValues[i]);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e) {
            }
        }
    }   
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies positive case.",
        method = "getInstance",
        args = {java.lang.String.class}
    )
    public void testCertPathValidator03() throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator certPV;
        for (int i = 0; i < validValues.length; i++) {
            certPV = CertPathValidator.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", certPV.getAlgorithm(), validValues[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance method throws IllegalArgumentException when provider parameter is null or empty.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator04()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        String provider = null;
        for (int i = 0; i < validValues.length; i++) {        
            try {
                CertPathValidator.getInstance(validValues[i], provider);
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
            try {
                CertPathValidator.getInstance(validValues[i], "");
                fail("IllegalArgumentException must be thrown thrown");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance method throws NoSuchProviderException when provider parameter has invalid value.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator05() throws NoSuchAlgorithmException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        for (int t = 0; t < validValues.length; t++) {
            for (int i = 1; i < invalidValues.length; i++) {
                try {
                    CertPathValidator.getInstance(validValues[t],
                            invalidValues[i]);
                    fail("NoSuchProviderException must be thrown");
                } catch (NoSuchProviderException e1) {
                }
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance method throws NullPointerException when algorithm is null, and NoSuchAlgorithmException when algorithm  is not available",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator06()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathValidator.getInstance(null, defaultProviderName);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathValidator.getInstance(invalidValues[i], defaultProviderName);
                fail("NoSuchAlgorithmException must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance mwthod returns CertPathValidator object.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator07() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator certPV;
        for (int i = 0; i < validValues.length; i++) {
            certPV = CertPathValidator.getInstance(validValues[i],
                    defaultProviderName);
            assertEquals("Incorrect algorithm", certPV.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider name", certPV.getProvider()
                    .getName(), defaultProviderName);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Verifies that getInstance method throws IllegalArgumentException when provider parameter is null.",
        method = "getInstance",
        args = {java.lang.String.class, java.security.Provider.class}
    )
    public void testCertPathValidator08()
            throws NoSuchAlgorithmException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        Provider prov = null;
        for (int t = 0; t < validValues.length; t++ ) {
            try {
                CertPathValidator.getInstance(validValues[t], prov);
                fail("IllegalArgumentException must be thrown");
            } catch (IllegalArgumentException e1) {
            }
        }        
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance method throws NullPointerException when algorithm is null, and  NoSuchAlgorithmException when algorithm  is not available.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator09()
            throws NoSuchAlgorithmException, NoSuchProviderException  {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        try {
            CertPathValidator.getInstance(null, defaultProvider);
            fail("NullPointerException or NoSuchAlgorithmException must be thrown when algorithm is null");
        } catch (NullPointerException e) {
        } catch (NoSuchAlgorithmException e) {
        }
        for (int i = 0; i < invalidValues.length; i++) {
            try {
                CertPathValidator.getInstance(invalidValues[i], defaultProvider);
                fail("NoSuchAlgorithm must be thrown");
            } catch (NoSuchAlgorithmException e1) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getInstance method returns CertPathValidator object.",
        method = "getInstance",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void testCertPathValidator10() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator certPV;
        for (int i = 0; i < invalidValues.length; i++) {
            certPV = CertPathValidator.getInstance(validValues[i],
                    defaultProvider);
            assertEquals("Incorrect algorithm", certPV.getAlgorithm(),
                    validValues[i]);
            assertEquals("Incorrect provider name", certPV.getProvider(),
                    defaultProvider);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that validate method throws InvalidAlgorithmParameterException if param is not instance of PKIXParameters or null.",
        method = "validate",
        args = {java.security.cert.CertPath.class, java.security.cert.CertPathParameters.class}
    )
    public void testCertPathValidator11()
            throws NoSuchAlgorithmException, NoSuchProviderException, CertPathValidatorException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator [] certPV = createCPVs();
        assertNotNull("CertPathValidator objects were not created", certPV);
        MyCertPath mCP = new MyCertPath(new byte[0]);
        invalidParams mPar = new invalidParams();
        for (int i = 0; i < certPV.length; i++) {
            try {
                certPV[i].validate(mCP, mPar);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
            try {
                certPV[i].validate(mCP, null);
                fail("InvalidAlgorithmParameterException must be thrown");
            } catch(InvalidAlgorithmParameterException e) {
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "CertPathValidator",
        args = {java.security.cert.CertPathValidatorSpi.class, java.security.Provider.class, java.lang.String.class}
    )
    public void testCertPathValidator12()
            throws CertificateException, NoSuchProviderException, NoSuchAlgorithmException,
            CertPathValidatorException, InvalidAlgorithmParameterException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidatorSpi spi = new MyCertPathValidatorSpi();
        CertPathValidator certPV = new myCertPathValidator(spi, 
                    defaultProvider, defaultType);
        assertEquals("Incorrect algorithm", certPV.getAlgorithm(), defaultType);
        assertEquals("Incorrect provider", certPV.getProvider(), defaultProvider);
        certPV.validate(null, null);
        try {
            certPV.validate(null, null);
            fail("CertPathValidatorException must be thrown");
        } catch (CertPathValidatorException e) {
        }        
        certPV = new myCertPathValidator(null, null, null);
        assertNull("Incorrect algorithm", certPV.getAlgorithm());
        assertNull("Incorrect provider", certPV.getProvider());
        try {
            certPV.validate(null, null);
            fail("NullPointerException must be thrown");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithm",
        args = {}
    )
    public void testCertPathValidator13() throws NoSuchAlgorithmException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator certPV;
        for (int i = 0; i < validValues.length; i++) {
            certPV = CertPathValidator.getInstance(validValues[i]);
            assertEquals("Incorrect algorithm", certPV.getAlgorithm(),
                    validValues[i]);
            try {
                certPV = CertPathValidator.getInstance(validValues[i],
                        defaultProviderName);
                assertEquals("Incorrect algorithm", certPV.getAlgorithm(),
                        validValues[i]);
            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchAlgorithmException " + e.getMessage());
            }
            certPV = CertPathValidator.getInstance(validValues[i],
                    defaultProvider);
            assertEquals("Incorrect algorithm", certPV.getAlgorithm(),
                    validValues[i]);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProvider",
        args = {}
    )
    public void testCertPathValidator14() throws NoSuchAlgorithmException {
        if (!PKIXSupport) {
            fail(NotSupportMsg);
            return;
        }
        CertPathValidator certPV;
        for (int i = 0; i < validValues.length; i++) {
            try {
                certPV = CertPathValidator.getInstance(validValues[i],
                        defaultProviderName);
                assertEquals("Incorrect provider", certPV.getProvider(),
                        defaultProvider);
            } catch (NoSuchProviderException e) {
                fail("Unexpected NoSuchProviderException " + e.getMessage());
            }
            certPV = CertPathValidator.getInstance(validValues[i],
                    defaultProvider);
            assertEquals("Incorrect provider", certPV.getProvider(),
                    defaultProvider);
        }
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(CertPathValidator1Test.class);
    }  
}
class myCertPathValidator extends CertPathValidator {
    public myCertPathValidator(CertPathValidatorSpi spi, Provider prov, String type) {
        super(spi, prov, type);
    }
}
class invalidParams implements CertPathParameters {
    public Object clone() {
        return new invalidParams();
    }
}

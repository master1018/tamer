@TestTargetClass(X509Certificate.class)
public class X509CertificateTest extends TestCase {
    private static String base64cert = "-----BEGIN CERTIFICATE-----\n"
            + "MIIC+jCCAragAwIBAgICAiswDAYHKoZIzjgEAwEBADAdMRswGQYDVQQKExJDZXJ0a"
            + "WZpY2F0ZSBJc3N1ZXIwIhgPMTk3MDAxMTIxMzQ2NDBaGA8xOTcwMDEyNDAzMzMyMF"
            + "owHzEdMBsGA1UEChMUU3ViamVjdCBPcmdhbml6YXRpb24wGTAMBgcqhkjOOAQDAQE"
            + "AAwkAAQIDBAUGBwiBAgCqggIAVaOCAhQwggIQMA8GA1UdDwEB/wQFAwMBqoAwEgYD"
            + "VR0TAQH/BAgwBgEB/wIBBTAUBgNVHSABAf8ECjAIMAYGBFUdIAAwZwYDVR0RAQH/B"
            + "F0wW4EMcmZjQDgyMi5OYW1lggdkTlNOYW1lpBcxFTATBgNVBAoTDE9yZ2FuaXphdG"
            + "lvboYaaHR0cDovL3VuaWZvcm0uUmVzb3VyY2UuSWSHBP
            + "DVR0eAQH/BAIwADAMBgNVHSQBAf8EAjAAMIGZBgNVHSUBAf8EgY4wgYsGBFUdJQAG"
            + "CCsGAQUFBwMBBggrBgEFBQcDAQYIKwYBBQUHAwIGCCsGAQUFBwMDBggrBgEFBQcDB"
            + "AYIKwYBBQUHAwUGCCsGAQUFBwMGBggrBgEFBQcDBwYIKwYBBQUHAwgGCCsGAQUFBw"
            + "MJBggrBgEFBQgCAgYKKwYBBAGCNwoDAwYJYIZIAYb4QgQBMA0GA1UdNgEB/wQDAgE"
            + "BMA4GBCpNhgkBAf8EAwEBATBkBgNVHRIEXTBbgQxyZmNAODIyLk5hbWWCB2ROU05h"
            + "bWWkFzEVMBMGA1UEChMMT3JnYW5pemF0aW9uhhpodHRwOi8vdW5pZm9ybS5SZXNvd"
            + "XJjZS5JZIcE
            + "UdDgQDAQEBMAoGA1UdIQQDAQEBMAwGByqGSM44BAMBAQADMAAwLQIUAL4QvoazNWP"
            + "7jrj84/GZlhm09DsCFQCBKGKCGbrP64VtUt4JPmLjW1VxQA==\n"
            + "-----END CERTIFICATE-----";
   private static final String selfSignedCert = "-----BEGIN CERTIFICATE-----\n" +
   "MIIDPzCCAqigAwIBAgIBADANBgkqhkiG9w0BAQUFADB5MQswCQYDVQQGEwJBTjEQ" +
   "MA4GA1UECBMHQW5kcm9pZDEQMA4GA1UEChMHQW5kcm9pZDEQMA4GA1UECxMHQW5k" +
   "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDEiMCAGCSqGSIb3DQEJARYTYW5kcm9pZEBh" +
   "bmRyb2lkLmNvbTAeFw0wOTAzMjAxNzAwMDZaFw0xMjAzMTkxNzAwMDZaMHkxCzAJ" +
   "BgNVBAYTAkFOMRAwDgYDVQQIEwdBbmRyb2lkMRAwDgYDVQQKEwdBbmRyb2lkMRAw" +
   "DgYDVQQLEwdBbmRyb2lkMRAwDgYDVQQDEwdBbmRyb2lkMSIwIAYJKoZIhvcNAQkB" +
   "FhNhbmRyb2lkQGFuZHJvaWQuY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB" +
   "gQCqQkDtkiEXmV8O5EK4y2Y9YyoWNDx70z4fqD+9muuzJGuM5NovMbxhBycuKHF3" +
   "WK60iXzrsAYkB1c8VHHbcUEFqz2fBdLKyxy/nYohlo8TYSVpEjt3vfc0sgmp4FKU" +
   "RDHO2z3rZPHWysV9L9ZvjeQpiwaYipU9epdBmvFmxQmCDQIDAQABo4HWMIHTMB0G" +
   "A1UdDgQWBBTnm32QKeqQC38IQXZOQSPoQyypAzCBowYDVR0jBIGbMIGYgBTnm32Q" +
   "KeqQC38IQXZOQSPoQyypA6F9pHsweTELMAkGA1UEBhMCQU4xEDAOBgNVBAgTB0Fu" +
   "ZHJvaWQxEDAOBgNVBAoTB0FuZHJvaWQxEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNV" +
   "BAMTB0FuZHJvaWQxIjAgBgkqhkiG9w0BCQEWE2FuZHJvaWRAYW5kcm9pZC5jb22C" +
   "AQAwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOBgQAUmDApQu+r5rglS1WF" +
   "BKXE3R2LasFvbBwdw2E0MAc0TWqLVW91VW4VWMX4r+C+c7rZpYXXtRqFRCuI/czL" +
   "0e1GaUP/Wa6bXBcm2u7Iv2dVAaAOELmFSVTZeR57Lm9lT9kQLp24kmNndIsiDW3T" +
   "XZ4pY/k2kxungOKx8b8pGYE9Bw==\n" +
   "-----END CERTIFICATE-----";
    private java.security.cert.X509Certificate cert;
    private javax.security.cert.X509Certificate tbt_cert;
    private java.security.cert.X509Certificate javaCert;
    private Provider myProvider;
    private javax.security.cert.X509Certificate javaxCert;
    private java.security.cert.Certificate javaSSCert;
    private Provider mySSProvider;
    private Certificate javaxSSCert;
    @Override
    protected void setUp() throws Exception {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(base64cert
                    .getBytes());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            this.cert = (java.security.cert.X509Certificate) cf
                    .generateCertificate(bais);
            this.tbt_cert = X509Certificate.getInstance(cert.getEncoded());
            this.javaCert = (java.security.cert.X509Certificate)cf
                    .generateCertificate(new ByteArrayInputStream(selfSignedCert.getBytes()));
            this.javaxCert = X509Certificate.getInstance(javaCert.getEncoded());
            myProvider = cf.getProvider();
            Security.addProvider(myProvider);
            this.javaSSCert = cf.generateCertificate(new ByteArrayInputStream(
                    selfSignedCert.getBytes()));
            this.javaxSSCert = X509Certificate.getInstance(javaCert
                    .getEncoded());
            mySSProvider = cf.getProvider();
            Security.addProvider(mySSProvider);
        } catch (java.security.cert.CertificateException e) {
            this.cert = null;
            Logger.global.warning("Error in test setup: Certificate type not supported");
        } catch (javax.security.cert.CertificateException e) {
            this.cert = null;
            Logger.global.warning("Error in test setup: Certificate type not supported");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509Certificate",
        args = {}
    )
    public void testConstructor() {
        X509Certificate cert = new MyCertificate();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInstance",
        args = {java.io.InputStream.class}
    )
    public void testGetInstance1() {
        if (this.cert == null) {
            return;
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(cert
                    .getEncoded());
            X509Certificate.getInstance(bais);
        } catch (java.security.cert.CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException was thrown.");
        } catch (CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException was thrown.");
        } catch (CertificateException e) {
        }
        try {
            X509Certificate.getInstance((InputStream) null);
            fail("No expected CertificateException");
        } catch (CertificateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies CertificateException.",
        method = "getInstance",
        args = {byte[].class}
    )
    public void testGetInstance2() throws java.security.cert.CertificateEncodingException, CertificateEncodingException {
        boolean certificateException = false;
        X509Certificate c = null;
        if (this.cert == null) {
            return;
        }
        try {
            c = X509Certificate.getInstance(cert.getEncoded());
        } catch (java.security.cert.CertificateEncodingException e) {
            fail("Unexpected CertificateEncodingException was thrown.");
        } catch (CertificateException e) {
            certificateException = true;
        }
        if (! certificateException) {
            assertNotNull(c);
            assertTrue(Arrays.equals(c.getEncoded(),cert.getEncoded() ));
        }
        try {
            X509Certificate.getInstance(new byte[]{(byte) 1 });
        } catch (CertificateException e) {
        }
        try {
            X509Certificate.getInstance((byte[]) null);
            fail("No expected CertificateException");
        } catch (CertificateException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Doesn't verify exceptions.",
        method = "checkValidity",
        args = {}
    )
    public void testCheckValidity1() throws CertificateExpiredException, CertificateNotYetValidException, java.security.cert.CertificateExpiredException, java.security.cert.CertificateNotYetValidException {
        if (this.cert == null) {
            return;
        }
        Date date = new Date();
        Date nb_date = tbt_cert.getNotBefore();
        Date na_date = tbt_cert.getNotAfter();
        try {
            tbt_cert.checkValidity();
            assertFalse("CertificateExpiredException expected", date
                    .compareTo(na_date) > 0);
            assertFalse("CertificateNotYetValidException expected", date
                    .compareTo(nb_date) < 0);
        } catch (CertificateExpiredException e) {
            assertTrue("Unexpected CertificateExpiredException was thrown",
                    date.compareTo(na_date) > 0);
        } catch (CertificateNotYetValidException e) {
            assertTrue("Unexpected CertificateNotYetValidException was thrown",
                    date.compareTo(nb_date) < 0);
        }
       try {
       tbt_cert.checkValidity();
       } catch (CertificateExpiredException e) {
       }
       try {
            cert.checkValidity();
        } catch (java.security.cert.CertificateExpiredException e) {
        } 
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Doesn't verify exceptions.",
        method = "checkValidity",
        args = {java.util.Date.class}
    )
    public void testCheckValidity2() throws CertificateNotYetValidException, CertificateExpiredException {
        if (this.cert == null) {
            return;
        }
        Date[] date = new Date[4];
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < date.length; i++) {
            calendar.set(i * 50, Calendar.JANUARY, 1);
            date[i] = calendar.getTime();
        }
        Date nb_date = tbt_cert.getNotBefore();
        Date na_date = tbt_cert.getNotAfter();
        for (int i = 0; i < date.length; i++) {
            try {
                tbt_cert.checkValidity(date[i]);
                assertFalse("CertificateExpiredException expected", date[i]
                        .compareTo(na_date) > 0);
                assertFalse("CertificateNotYetValidException expected", date[i]
                        .compareTo(nb_date) < 0);
            } catch (CertificateExpiredException e) {
                assertTrue("Unexpected CertificateExpiredException was thrown",
                        date[i].compareTo(na_date) > 0);
            } catch (CertificateNotYetValidException e) {
                assertTrue("Unexpected CertificateNotYetValidException "
                        + "was thrown", date[i].compareTo(nb_date) < 0);
            }
        }
        Calendar calendarNow = Calendar.getInstance();
        try {
            tbt_cert.checkValidity(calendarNow.getTime());
        } catch (CertificateExpiredException e) {
        }
        Calendar calendarPast = GregorianCalendar.getInstance();
        calendarPast.clear();
        try {
            tbt_cert.checkValidity(calendarPast.getTime());
        } catch (CertificateNotYetValidException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getVersion",
        args = {}
    )
    public void testGetVersion() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The version is not correct.", tbt_cert.getVersion(), 2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSerialNumber",
        args = {}
    )
    public void testGetSerialNumber() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The serial number is not correct.", tbt_cert
                .getSerialNumber(), cert.getSerialNumber());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Denigrated API",
        method = "getIssuerDN",
        args = {}
    )
    public void testGetIssuerDN() {
        if (this.cert == null) {
            Logger.global.warning("testGetIssuerDN: error in test setup.");
        }
        assertEquals("The issuer DN is not correct.", tbt_cert.getIssuerDN(),
                cert.getIssuerDN());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSubjectDN",
        args = {}
    )
    public void testGetSubjectDN() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The subject DN is not correct.", tbt_cert.getSubjectDN(),
                cert.getSubjectDN());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNotBefore",
        args = {}
    )
    public void testGetNotBefore() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The NotBefore date is not correct.", tbt_cert
                .getNotBefore(), cert.getNotBefore());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getNotAfter",
        args = {}
    )
    public void testGetNotAfter() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The NotAfter date is not correct.", tbt_cert
                .getNotAfter(), cert.getNotAfter());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSigAlgName",
        args = {}
    )
    public void testGetSigAlgName() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The name of signature algorithm is not correct.",
                tbt_cert.getSigAlgName(), cert.getSigAlgName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSigAlgOID",
        args = {}
    )
    public void testGetSigAlgOID() {
        if (this.cert == null) {
            return;
        }
        assertEquals("The name of OID of signature algorithm is not correct.",
                tbt_cert.getSigAlgOID(), cert.getSigAlgOID());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSigAlgParams",
        args = {}
    )
    public void testGetSigAlgParams() {
        if (this.cert == null) {
            return;
        }
        assertTrue("The byte array with encoded algorithm parameters "
                + "is not correct.", Arrays.equals(tbt_cert.getSigAlgParams(),
                cert.getSigAlgParams()));
    }
    private class MyCertificate extends X509Certificate {
        public MyCertificate() {
            super();
        }
        @Override
        public void checkValidity() throws CertificateExpiredException,
                CertificateNotYetValidException {
        }
        @Override
        public void checkValidity(Date arg0)
                throws CertificateExpiredException,
                CertificateNotYetValidException {
        }
        @Override
        public Principal getIssuerDN() {
            return null;
        }
        @Override
        public Date getNotAfter() {
            return null;
        }
        @Override
        public Date getNotBefore() {
            return null;
        }
        @Override
        public BigInteger getSerialNumber() {
            return null;
        }
        @Override
        public String getSigAlgName() {
            return null;
        }
        @Override
        public String getSigAlgOID() {
            return null;
        }
        @Override
        public byte[] getSigAlgParams() {
            return null;
        }
        @Override
        public Principal getSubjectDN() {
            return null;
        }
        @Override
        public int getVersion() {
            return 0;
        }
        @Override
        public byte[] getEncoded() throws CertificateEncodingException {
            return null;
        }
        @Override
        public PublicKey getPublicKey() {
            return null;
        }
        @Override
        public String toString() {
            return null;
        }
        @Override
        public void verify(PublicKey key) throws CertificateException,
                NoSuchAlgorithmException, InvalidKeyException,
                NoSuchProviderException, SignatureException {
        }
        @Override
        public void verify(PublicKey key, String sigProvider)
                throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException,
                SignatureException {
        }
    }
    public class MyModifiablePublicKey implements PublicKey {
        private PublicKey key;
        private boolean modifiedAlgo;
        private String algo;
        private String format;
        private boolean modifiedFormat;
        private boolean modifiedEncoding;
        private byte[] encoding;
        public MyModifiablePublicKey(PublicKey k) {
            super();
            this.key = k;
        }
        public String getAlgorithm() {
            if (modifiedAlgo) {
                return algo;
            } else {
                return key.getAlgorithm();
            }
        }
        public String getFormat() {
            if (modifiedFormat) {
                return this.format;
            } else {
                return key.getFormat();
            }
        }
        public byte[] getEncoded() {
            if (modifiedEncoding) {
                return this.encoding;
            } else {
                return key.getEncoded();
            }
        }
        public long getSerVerUID() {
            return key.serialVersionUID;
        }
        public void setAlgorithm(String myAlgo) {
            modifiedAlgo = true;
            this.algo = myAlgo;
        }
        public void setFormat(String myFormat) {
            modifiedFormat = true;
            format = myFormat;
        }
        public void setEncoding(byte[] myEncoded) {
            modifiedEncoding = true;
            encoding = myEncoded;
        }
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "No ASN1/DER encoder available. Exception is not supported.",
      method = "getEncoded",
      args = {}
    )
    public void testGetEncoded()
            throws CertificateEncodingException, java.security.cert.CertificateException {
        assertTrue(Arrays.equals(cert.getEncoded(), tbt_cert.getEncoded()));
        assertFalse(Arrays.equals(javaxCert.getEncoded(), tbt_cert.getEncoded()));
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getPublicKey",
      args = {}
    )
    public void testGetPublicKey() {
       PublicKey key = javaxCert.getPublicKey();
       assertNotNull(key);
       assertEquals(javaxCert.getPublicKey(), javaCert.getPublicKey());
       assertEquals(key.getAlgorithm(),"RSA");
       key = javaxSSCert.getPublicKey();
       assertNotNull(key);
       assertEquals(key.getAlgorithm(),"RSA");
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = " CertificateException not supported."+
              "NoSuchAlgorithmException, NoSuchProviderException can be "+
              "implemented only with working Cert. Verification fails "+
              "(see failing) precondition assertions",
      method = "verify",
      args = {java.security.PublicKey.class}
    )
    @SideEffect("Destroys MD5 provider, hurts succeeding tests")
    public void testVerifyPublicKey() throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchProviderException,
            SignatureException, CertificateException {
        assertNotNull(javaxCert.getPublicKey());
        assertNotNull(javaxSSCert.getPublicKey());
        try {
            javaxCert.verify(javaxCert.getPublicKey());
        } catch (SignatureException e) {
        }
        PublicKey k = javaxCert.getPublicKey();
        MyModifiablePublicKey changedEncoding = new MyModifiablePublicKey(k);
        changedEncoding
                .setEncoding(new byte[javaxCert.getEncoded().length - 1]);
        try {
            javaxCert.verify(tbt_cert.getPublicKey());
        } catch (InvalidKeyException e) {
        }
        try {
            javaxCert.verify(null);
        } catch (Exception e) {
        }
        try {
            javaxCert.verify(changedEncoding);
            fail("Exception expected");
        } catch (Exception e) {
        }
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "",
      method = "verify",
      args = {java.security.PublicKey.class, java.lang.String.class}
    )
    @SideEffect("Destroys MD5 provider, hurts succeeding tests")
    public void testVerifyPublicKeyString() throws InvalidKeyException,
            java.security.cert.CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException, IOException,
            CertificateException {
        try {
            javaxCert.verify(javaxCert.getPublicKey(), myProvider.getName());
        } catch (NoSuchAlgorithmException e) {
        }
        Security.removeProvider(myProvider.getName());
        try {
            javaxCert.verify(javaxCert.getPublicKey(), myProvider.getName());
        } catch (NoSuchProviderException e) {
        }
        Security.addProvider(myProvider);
        Provider[] providers = Security.getProviders("Signature.MD5withRSA");
        if (providers == null || providers.length == 0) {
            fail("no Provider for Signature.MD5withRSA");
            return;
        }
        try {
            javaxSSCert.verify(javaxSSCert.getPublicKey(),
                    providers[0].getName());
        } catch (SignatureException e) {
            fail("blu");
        }
    }
    public static Test suite() {
        return new TestSuite(X509CertificateTest.class);
    }
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}

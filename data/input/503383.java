@TestTargetClass(CertificateFactory.class)
public class CertificateFactory3Test extends TestCase {
    private static String defaultProviderName = null;
    private static Provider defaultProvider = null;
    private static String defaultType = CertificateFactory1Test.defaultType;
    public static String fileCertPathPki = "java/security/cert/CertPath.PkiPath";
    private static boolean X509Support = false;
    private static String NotSupportMsg = "";
    static {
        defaultProvider = SpiEngUtils.isSupport(defaultType,
                CertificateFactory1Test.srvCertificateFactory);
        X509Support = defaultProvider != null;
        defaultProviderName = X509Support ? defaultProvider.getName() : null;
        NotSupportMsg = defaultType.concat(" is not supported");
    }
    private static CertificateFactory[] initCertFs() throws Exception {
        if (!X509Support) {
            fail(NotSupportMsg);
        }
        CertificateFactory[] certFs = new CertificateFactory[3];
        certFs[0] = CertificateFactory.getInstance(defaultType);
        certFs[1] = CertificateFactory.getInstance(defaultType,
                defaultProviderName);
        certFs[2] = CertificateFactory
                .getInstance(defaultType, defaultProvider);
        return certFs;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CertificateException.",
        method = "generateCertificate",
        args = {java.io.InputStream.class}
    )
    public void testGenerateCertificate() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Certificate[] certs = new Certificate[3];
        for (int i = 0; i < certFs.length; i++) {
            certs[i] = certFs[i].generateCertificate(new ByteArrayInputStream(
                    TestUtils.getEncodedX509Certificate()));
        }
        assertEquals(certs[0], certs[1]);
        assertEquals(certs[0], certs[2]);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CertificateException.",
        method = "generateCertificates",
        args = {java.io.InputStream.class}
    )
    public void testGenerateCertificates() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Certificate cert = certFs[0]
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getEncodedX509Certificate()));
        for (int i = 0; i < certFs.length; i++) {
            Collection<? extends Certificate> col = null;
            col = certFs[i].generateCertificates(new ByteArrayInputStream(
                    TestUtils.getEncodedX509Certificate()));
            Iterator<? extends Certificate> it = col.iterator();
            assertEquals("Incorrect Collection size", col.size(), 1);
            assertEquals("Incorrect Certificate in Collection", cert, it.next());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CertificateException.",
        method = "generateCertPath",
        args = {java.util.List.class}
    )
    public void testGenerateCertPath01() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        Certificate cert = certFs[0]
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getEncodedX509Certificate()));
        List<Certificate> list = new Vector<Certificate>();
        list.add(cert);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            certPath = certFs[i].generateCertPath(list);
            assertEquals(cert.getType(), certPath.getType());
            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
            Iterator<? extends Certificate> it = list1.iterator();
            assertEquals("Incorrect Certificate in CertPath", cert, it.next());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CertificateException.",
        method = "generateCertPath",
        args = {java.io.InputStream.class, java.lang.String.class}
    )
    public void testGenerateCertPath02() throws Exception {
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            CertPath certPath = null;
            InputStream fis = Support_Resources
                    .getResourceStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis, "PkiPath");
            fis.close();
            assertEquals(defaultType, certPath.getType());
            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify CertificateException.",
        method = "generateCertPath",
        args = {java.io.InputStream.class}
    )
    public void testGenerateCertPath03() throws Exception {
        String certPathEncoding = "PkiPath";
        CertificateFactory[] certFs = initCertFs();
        assertNotNull("CertificateFactory objects were not created", certFs);
        for (int i = 0; i < certFs.length; i++) {
            Iterator<String> it = certFs[0].getCertPathEncodings();
            assertTrue("no CertPath encodings", it.hasNext());
            assertEquals("Incorrect default encoding", certPathEncoding, it
                    .next());
            CertPath certPath = null;
            InputStream fis = Support_Resources
                    .getResourceStream(fileCertPathPki);
            certPath = certFs[i].generateCertPath(fis);
            fis.close();
            assertEquals(defaultType, certPath.getType());
            List<? extends Certificate> list1 = certPath.getCertificates();
            assertFalse("Result list is empty", list1.isEmpty());
        }
    }
}

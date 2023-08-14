public abstract class CertificateFactoryTest extends TestCase {
    private final String algorithmName;
    private final byte[] certificateData;
    public CertificateFactoryTest(String algorithmName, byte[] certificateData) {
        this.algorithmName = algorithmName;
        this.certificateData = certificateData;
    }
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargets({
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="getInstance",
                args={String.class}
        ),
        @TestTargetNew(
                level=TestLevel.ADDITIONAL,
                method="generateCertificate",
                args={InputStream.class}
        ),
        @TestTargetNew(
                level=TestLevel.COMPLETE,
                method="method",
                args={}
        )
    })
    public void testCertificateFactory() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance(
                algorithmName);
        Certificate certificate = certificateFactory.generateCertificate(
                new ByteArrayInputStream(certificateData));
        assertNotNull(certificate);
    }
}

@TestTargetClass(java.security.cert.Certificate.class)
public class CertificateCertificateRepTest extends TestCase {
    private static final byte[] testEncoding = new byte[] { (byte) 1, (byte) 2,
            (byte) 3, (byte) 4, (byte) 5 };
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Certificate.CertificateRep.CertificateRep", 
        args = { String.class, byte[].class}
    )
    public final void testCertificateCertificateRep() {
        MyCertificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        MyCertificateRep rep = c1.new MyCertificateRep("TEST_TYPE", new byte[] {
                (byte) 1, (byte) 2, (byte) 3 });
        assertTrue(Arrays.equals(new byte[] { (byte) 1, (byte) 2, (byte) 3 },
                rep.getData()));
        assertEquals("TEST_TYPE", rep.getType());
        try {
            c1.new MyCertificateRep(null, null);
        } catch (Exception e) {
            fail("Unexpected exeption " + e.getMessage());
        }
        try {
            MyCertificate.MyCertificateRep rep1 = c1.new MyCertificateRep(
                    "X509", TestUtils.getX509Certificate_v3());
            assertEquals("X509", rep1.getType());
            assertTrue(Arrays.equals(TestUtils.getX509Certificate_v3(), rep1.getData()));
        } catch (Exception e) {
            fail("Unexpected exeption " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "Certificate.CertificateRep.readResolve",
        args = {}
    )
    public final void testReadResolve() {
        MyCertificate c1 = new MyCertificate("TEST_TYPE", testEncoding);
        MyCertificateRep rep = c1.new MyCertificateRep("TEST_TYPE", new byte[] {
                (byte) 1, (byte) 2, (byte) 3 });
        try {
            rep.readResolve();
            fail("ObjectStreamException expected");
        } catch (ObjectStreamException e) {
        }
        MyCertificateRep rep1 = c1.new MyCertificateRep("X509", TestUtils
                .getX509Certificate_v3());
        try {
            Certificate obj = (Certificate) rep1.readResolve();
            assertEquals("0.3.5", obj.getPublicKey().getAlgorithm());
            assertEquals("X.509", obj.getPublicKey().getFormat());
            assertEquals("X.509", obj.getType());
        } catch (ObjectStreamException e) {
            fail("Unexpected ObjectStreamException " + e.getMessage());
        }
    }
}

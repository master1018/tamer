@TestTargetClass(KeyStore.TrustedCertificateEntry.class)
public class KSTrustedCertificateEntryTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "TrustedCertificateEntry",
        args = {java.security.cert.Certificate.class}
    )
    public void testTrustedCertificateEntry() {
        Certificate cert = null;
        try {
            new KeyStore.TrustedCertificateEntry(cert);
            fail("NullPointerException must be thrown when trustCert is null");
        } catch (NullPointerException e) {
        }
        cert = new MyCertificate("TEST", new byte[10]);
        try {
            KeyStore.TrustedCertificateEntry ksTCE = new KeyStore.TrustedCertificateEntry(cert);
            assertNotNull(ksTCE);
            assertTrue(ksTCE instanceof KeyStore.TrustedCertificateEntry);
        } catch (Exception e) {
            fail("Unexpected exception was thrown when trustCert is not null");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTrustedCertificate",
        args = {}
    )
    public void testGetTrustedCertificate() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
        KeyStore.TrustedCertificateEntry ksTCE = 
                new KeyStore.TrustedCertificateEntry(cert);
        assertEquals("Incorrect certificate", cert, ksTCE.getTrustedCertificate());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void testToString() {
        Certificate cert = new MyCertificate("TEST", new byte[10]);
        KeyStore.TrustedCertificateEntry ksTCE = 
                new KeyStore.TrustedCertificateEntry(cert);
        assertNotNull("toString() returns null string", ksTCE.toString());
    }
}

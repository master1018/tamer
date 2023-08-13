@TestTargetClass(HostnameVerifier.class)
public class HostnameVerifierTest extends TestCase implements
        CertificatesToPlayWith {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public final void test_verify() {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        try {
            assertFalse(hv.verify("localhost", session));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @AndroidOnly("DefaultHostnameVerifier on RI is weird and cannot be tested this way.")
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public void testVerify() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in;
        X509Certificate x509;
        in = new ByteArrayInputStream(X509_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        mySSLSession session = new mySSLSession(new X509Certificate[] {x509});
        HostnameVerifier verifier = HttpsURLConnection
                .getDefaultHostnameVerifier();
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertFalse(verifier.verify("bar.com", session));
        in = new ByteArrayInputStream(X509_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("\u82b1\u5b50.co.jp", session));
        assertFalse(verifier.verify("a.\u82b1\u5b50.co.jp", session));
        in = new ByteArrayInputStream(X509_FOO_BAR);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertTrue(verifier.verify("bar.com", session));
        assertFalse(verifier.verify("a.bar.com", session));
        in = new ByteArrayInputStream(X509_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        in = new ByteArrayInputStream(X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        in = new ByteArrayInputStream(X509_NO_CNS_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        in = new ByteArrayInputStream(X509_THREE_CNS_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertFalse(verifier.verify("foo.com", session));
        assertFalse(verifier.verify("a.foo.com", session));
        assertFalse(verifier.verify("bar.com", session));
        assertFalse(verifier.verify("a.bar.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.co.jp", session));
        assertFalse(verifier.verify("a.\u82b1\u5b50.co.jp", session));
        in = new ByteArrayInputStream(X509_WILD_FOO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertFalse(verifier.verify("foo.com", session));
        assertTrue(verifier.verify("www.foo.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.foo.com", session));
        assertTrue(verifier.verify("a.b.foo.com", session));
        in = new ByteArrayInputStream(X509_WILD_CO_JP);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertTrue(verifier.verify("*.co.jp", session));
        assertFalse(verifier.verify("foo.co.jp", session));
        assertFalse(verifier.verify("\u82b1\u5b50.co.jp", session));
        in = new ByteArrayInputStream(X509_WILD_FOO_BAR_HANAKO);
        x509 = (X509Certificate) cf.generateCertificate(in);
        session = new mySSLSession(new X509Certificate[] {x509});
        assertFalse(verifier.verify("foo.com", session));
        assertTrue(verifier.verify("www.foo.com", session));
        assertTrue(verifier.verify("\u82b1\u5b50.foo.com", session));
        assertTrue(verifier.verify("a.b.foo.com", session));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "verify",
        args = {String.class, SSLSession.class}
    )
    @AndroidOnly("DefaultHostnameVerifier on RI is weird and cannot be tested this way.")
    @KnownFailure("DefaultHostnameVerifier is broken on Android, fixed in donutburger")
    @SideEffect("the DefaultHostnameVerifier is set in some other tests, therefore we need isolation")
    public void testSubjectAlt() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = new ByteArrayInputStream(X509_MULTIPLE_SUBJECT_ALT);
        X509Certificate x509 = (X509Certificate) cf.generateCertificate(in);
        mySSLSession session = new mySSLSession(new X509Certificate[] {x509});
        HostnameVerifier verifier = HttpsURLConnection
                .getDefaultHostnameVerifier();
        assertEquals(
                "CN=localhost,OU=Unknown,O=Unknown,L=Unknown,ST=Unknown,C=CH",
                x509.getSubjectDN().getName().replace(", ", ","));
        assertTrue(verifier.verify("localhost", session));
        assertTrue(verifier.verify("localhost.localdomain", session));
        assertTrue(verifier.verify("127.0.0.1", session));
        assertFalse(verifier.verify("local.host", session));
        assertFalse(verifier.verify("127.0.0.2", session));
    }
}

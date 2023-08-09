@TestTargetClass(X509TrustManager.class) 
public class X509TrustManagerTest extends TestCase {
    private X509Certificate[] setX509Certificate() {
        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v3()));
            X509Certificate[] xcert = {pemCert};
            return xcert;
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        return null;
    }
    private X509Certificate[] setInvalid() {
        try {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509");
            X509Certificate pemCert = (X509Certificate) certFact
                .generateCertificate(new ByteArrayInputStream(TestUtils
                        .getX509Certificate_v1()));
            X509Certificate[] xcert = {pemCert};
            return xcert;
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
        return null;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkClientTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkClientTrusted_01() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = null;
        try {
            xtm.checkClientTrusted(xcert, "SSL");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        xcert = new X509Certificate[0];
        try {
            xtm.checkClientTrusted(xcert, "SSL");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        xcert = setX509Certificate();
        try {
            xtm.checkClientTrusted(xcert, null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        try {
            xtm.checkClientTrusted(xcert, "");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkClientTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkClientTrusted_02() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = setInvalid();
        try {
            xtm.checkClientTrusted(xcert, "SSL");
            fail("CertificateException wasn't thrown");
        } catch (CertificateException ce) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkClientTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkClientTrusted_03() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = setX509Certificate();
        try {
            xtm.checkClientTrusted(xcert, "SSL");
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkServerTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkServerTrusted_01() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = null;
        try {
            xtm.checkServerTrusted(xcert, "SSL");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        xcert = new X509Certificate[0];
        try {
            xtm.checkServerTrusted(xcert, "SSL");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        xcert = setX509Certificate();
        try {
            xtm.checkServerTrusted(xcert, null);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
        try {
            xtm.checkServerTrusted(xcert, "");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException iae) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");            
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkServerTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkServerTrusted_02() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = setInvalid();
        try {
            xtm.checkServerTrusted(xcert, "SSL");
            fail("CertificateException wasn't thrown");
        } catch (CertificateException ce) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "checkServerTrusted",
        args = {java.security.cert.X509Certificate[].class, java.lang.String.class}
    )
    public void test_checkServerTrusted_03() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        X509Certificate[] xcert = setX509Certificate();
        try {
            xtm.checkServerTrusted(xcert, "SSL");
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAcceptedIssuers",
        args = {}
    )
    public void test_getAcceptedIssuers() {
        X509TrustManagerImpl xtm = new X509TrustManagerImpl();
        try {
            assertNotNull(xtm.getAcceptedIssuers());
        } catch (Exception ex) {
            fail("Unexpected exception " + ex);
        }
    }
}

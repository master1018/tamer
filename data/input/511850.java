@TestTargetClass(HttpsURLConnection.class) 
public class HttpsURLConnectionTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "HttpsURLConnection",
        args = {java.net.URL.class}
    )
    public final void test_Constructor() {
        try {
            MyHttpsURLConnection huc = new MyHttpsURLConnection(new URL("https:
        } catch (Exception e) {
            fail("Unexpected exception: " + e.toString());
        }
        try {
            MyHttpsURLConnection huc = new MyHttpsURLConnection(null);
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getCipherSuite",
        args = {}
    )
    public final void test_getCipherSuite() {
        try {
            URL url = new URL("https:
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getCipherSuite();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https:
            assertEquals("CipherSuite", con.getCipherSuite());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalCertificates",
        args = {}
    )
    public final void test_getLocalCertificates() {
        try {
            URL url = new URL("https:
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getLocalCertificates();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https:
            assertNull(con.getLocalCertificates());
            con = new MyHttpsURLConnection(new URL("https:
            Certificate[] cert = con.getLocalCertificates();
            assertNotNull(cert);
            assertEquals(1, cert.length);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultHostnameVerifier",
        args = {}
    )
    public final void test_getDefaultHostnameVerifier() {
        HostnameVerifier verifyer =
            HttpsURLConnection.getDefaultHostnameVerifier();
        assertNotNull("Default hostname verifyer is null", verifyer);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultSSLSocketFactory",
        args = {}
    )
    public final void test_getDefaultSSLSocketFactory() {
        SSLSocketFactory sf = HttpsURLConnection.getDefaultSSLSocketFactory();
        if (!sf.equals(SSLSocketFactory.getDefault())) {
            fail("incorrect DefaultSSLSocketFactory");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostnameVerifier",
        args = {}
    )
    public final void test_getHostnameVerifier()
        throws Exception {
        HttpsURLConnection con = new MyHttpsURLConnection(
                new URL("https:
        HostnameVerifier verifyer = con.getHostnameVerifier();
        assertNotNull("Hostname verifyer is null", verifyer);
        assertEquals("Incorrect value of hostname verirfyer", 
                HttpsURLConnection.getDefaultHostnameVerifier(), verifyer);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLocalPrincipal",
        args = {}
    )
    public final void test_getLocalPrincipal() {
        try {
            URL url = new URL("https:
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getLocalPrincipal();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        try {
            HttpsURLConnection con = new MyHttpsURLConnection(new URL("https:
            assertNull(con.getLocalPrincipal());
            con = new MyHttpsURLConnection(new URL("https:
            assertNotNull("Local principal is null", con.getLocalPrincipal());
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPeerPrincipal",
        args = {}
    )
    public final void test_getPeerPrincipal() throws Exception {
        try {
            URL url = new URL("https:
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getPeerPrincipal();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        HttpsURLConnection con = new MyHttpsURLConnection(new URL("https:
        try {
            Principal p = con.getPeerPrincipal();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException e) {
        }
        con = new MyHttpsURLConnection(new URL("https:
        try {
            Principal p = con.getPeerPrincipal();
            assertNotNull(p);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getServerCertificates",
        args = {}
    )
    public final void test_getServerCertificates() throws Exception {
        try {
            URL url = new URL("https:
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            try {
                connection.getServerCertificates();
                fail("IllegalStateException wasn't thrown");
            } catch (IllegalStateException ise) {
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e + " for exception case");
        }
        HttpsURLConnection con = new MyHttpsURLConnection(new URL("https:
        try {
            Certificate[] cert = con.getServerCertificates();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException e) {
        }
        con = new MyHttpsURLConnection(new URL("https:
        try {
            Certificate[] cert = con.getServerCertificates();
            assertNotNull(cert);
            assertEquals(1, cert.length);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSSLSocketFactory",
        args = {}
    )
    public final void test_getSSLSocketFactory() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        SSLSocketFactory sf = con.getSSLSocketFactory();
        if (!sf.equals(SSLSocketFactory.getDefault())) {
            fail("incorrect DefaultSSLSocketFactory");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDefaultHostnameVerifier",
        args = {javax.net.ssl.HostnameVerifier.class}
    )
    public final void test_setDefaultHostnameVerifier() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            myHostnameVerifier hnv = new myHostnameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(hnv);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setHostnameVerifier",
        args = {javax.net.ssl.HostnameVerifier.class}
    )
    public final void test_setHostnameVerifier() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        try {
            con.setHostnameVerifier(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            myHostnameVerifier hnv = new myHostnameVerifier();
            con.setHostnameVerifier(hnv);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setDefaultSSLSocketFactory",
        args = {javax.net.ssl.SSLSocketFactory.class}
    )
    public final void test_setDefaultSSLSocketFactory() {
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setSSLSocketFactory",
        args = {javax.net.ssl.SSLSocketFactory.class}
    )
    public final void test_setSSLSocketFactory() {
        HttpsURLConnection con = new MyHttpsURLConnection(null);
        try {
            con.setSSLSocketFactory(null);
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory
                    .getDefault();
            con.setSSLSocketFactory(ssf);
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }
}
class MyHttpsURLConnection extends javax.net.ssl.HttpsURLConnection {
    private String typeDone;
    public MyHttpsURLConnection(URL url) {
        super(url);
    }
    public MyHttpsURLConnection(URL url, String type) {
        super(url);
        typeDone = type;
    }
    public String getCipherSuite() {
        return "CipherSuite";
    }
    public Certificate[] getLocalCertificates() {
        Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance(typeDone);
            byte[] barr = TestUtils.getX509Certificate_v1();
            ByteArrayInputStream bis = new ByteArrayInputStream(barr);
            cert = cf.generateCertificate(bis);
        } catch (CertificateException se) {
            cert = null;
        }
        return cert == null ? null : new Certificate[]{cert};
    }
    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        Certificate cert = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance(typeDone);
            byte[] barr = TestUtils.getX509Certificate_v3();
            ByteArrayInputStream bis = new ByteArrayInputStream(barr);
            cert = cf.generateCertificate(bis);
        } catch (CertificateException se) {
            throw new SSLPeerUnverifiedException("No server's end-entity certificate");
        }
        return cert == null ? null : new Certificate[]{cert};
    }
    public void disconnect() {
    }
    public boolean usingProxy() {
        return false;
    }
    public void connect() {
    }
}
class myHostnameVerifier implements HostnameVerifier {
    myHostnameVerifier() {
    }
    public boolean verify(String hostname, SSLSession session) {
        if (hostname == session.getPeerHost()) {
            return true;
        } else return false;
    }
}

@TestTargetClass(SSLSessionBindingEvent.class) 
public class SSLSessionBindingEventTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "SSLSessionBindingEvent",
        args = {javax.net.ssl.SSLSession.class, java.lang.String.class}
    )
    public final void test_ConstructorLjavax_net_ssl_SSLSessionLjava_lang_String() {
        SSLSession ses = new MySSLSession();
        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
            if (!"test".equals(event.getName())) {
                fail("incorrect name");
            }
            if (!event.getSession().equals(ses)) {
                fail("incorrect session");
            }
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(null, "test");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, null);
        } catch (IllegalArgumentException e) {
          fail("Unexpected IllegalArgumentException: " + e);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        SSLSession ses = new MySSLSession();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
        assertEquals("Incorrect session name", "test", event.getName());
        event = new SSLSessionBindingEvent(ses, null);
        assertEquals("Incorrect session name", null, event.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSession",
        args = {}
    )
    public void test_getSession() {
        SSLSession ses = new MySSLSession();
        SSLSessionBindingEvent event = new SSLSessionBindingEvent(ses, "test");
        assertEquals("Incorrect session", ses, event.getSession());
    }
}
class MySSLSession implements SSLSession {
    public int getApplicationBufferSize() {
        return 0;
    }
    public String getCipherSuite() {
        return "MyTestCipherSuite";
    }
    public long getCreationTime() {
        return 0;
    }
    public byte[] getId() {
        return null;
    }
    public long getLastAccessedTime() {
        return 0;
    }
    public Certificate[] getLocalCertificates() {
        return null;
    }
    public Principal getLocalPrincipal() {
        return null;
    }
    public int getPacketBufferSize() {
        return 0;
    }
    public X509Certificate[] getPeerCertificateChain()
    throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("test exception");
    }
    public Certificate[] getPeerCertificates()
    throws SSLPeerUnverifiedException {
        throw new SSLPeerUnverifiedException("test exception");
    }
    public String getPeerHost() {
        return null;
    }
    public int getPeerPort() {
        return 0;
    }
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return null;
    }
    public String getProtocol() {
        return null;
    }
    public SSLSessionContext getSessionContext() {
        return null;
    }
    public Object getValue(String name) {
        return null;
    }
    public String[] getValueNames() {
        return null;
    }
    public void invalidate() {
    }
    public boolean isValid() {
        return false;
    }
    public void putValue(String name, Object value) {
    }
    public void removeValue(String name) {
    }
}

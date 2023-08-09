@TestTargetClass(SSLSessionBindingListener.class) 
public class SSLSessionBindingListenerTest extends TestCase {
    public class mySSLSessionBindingListener implements SSLSessionBindingListener {
        public boolean boundDone = false;
        public boolean unboundDone = false;
        mySSLSessionBindingListener() {
        }
        public void valueBound(SSLSessionBindingEvent event) {
            if (event != null) boundDone = true;
        }
        public void valueUnbound(SSLSessionBindingEvent event) {
            if (event != null) unboundDone = true;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueBound",
        args = {SSLSessionBindingEvent.class}
    )
    public void test_valueBound() throws UnknownHostException, IOException,
            InterruptedException {
        SSLSocket sock = (SSLSocket) SSLSocketFactory.getDefault()
                .createSocket();
        SSLSession ss = sock.getSession();
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        ss.putValue("test", sbl);
        assertTrue("valueBound was not called.", sbl.boundDone);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "valueUnbound",
        args = {SSLSessionBindingEvent.class}
    )
    public void test_valueUnbound() throws UnknownHostException, IOException {
        SSLSocket sock = (SSLSocket) SSLSocketFactory.getDefault()
                .createSocket();
        SSLSession ss = sock.getSession();
        mySSLSessionBindingListener sbl = new mySSLSessionBindingListener();
        ss.putValue("test", sbl);
        ss.removeValue("test");
        assertTrue("valueUnbound was not called.", sbl.unboundDone);
    }
}

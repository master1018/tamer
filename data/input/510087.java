@TestTargetClass(SecureCacheResponse.class) 
public class SecureCacheResponseTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "SecureCacheResponse",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getCipherSuite",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLocalCertificateChain",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getLocalPrincipal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPeerPrincipal",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getServerCertificateChain",
            args = {}
        )
    })
    public void test_Constructor() throws Exception {
        TestSecureCacheResponse scr = new TestSecureCacheResponse();
        assertNull(scr.getCipherSuite());
        assertNull(scr.getLocalCertificateChain());
        assertNull(scr.getLocalPrincipal());
        assertNull(scr.getPeerPrincipal());
        assertNull(scr.getServerCertificateChain());
        assertNull(scr.getBody());
        assertNull(scr.getHeaders());
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "",
        method = "SecureCacheResponse",
        args = {}
    )
    public void test_additional() throws Exception {
            URL url  = new URL("http:
            ResponseCache.setDefault(new TestResponseCache());
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setUseCaches(true);
            httpCon.connect();
            try {
                Thread.sleep(5000);
            } catch(Exception e) {}
            httpCon.disconnect();
    }
    class TestSecureCacheResponse extends SecureCacheResponse {
        @Override
        public String getCipherSuite() {
            return null;
        }
        @Override
        public List<Certificate> getLocalCertificateChain() {
            return null;
        }
        @Override
        public Principal getLocalPrincipal() {
            return null;
        }
        @Override
        public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
            return null;
        }
        @Override
        public List<Certificate> getServerCertificateChain() throws SSLPeerUnverifiedException {
            return null;
        }
        @Override
        public InputStream getBody() throws IOException {
            return null;
        }
        @Override
        public Map<String, List<String>> getHeaders() throws IOException {
            return null;
        }
    }
    class TestResponseCache extends ResponseCache {
        URI uri1 = null;    
        public TestSecureCacheResponse get(URI uri, String rqstMethod, Map rqstHeaders)
                                                          throws IOException {
          try {
            uri1  = new URI("http:
          } catch (URISyntaxException e) {
          }  
          if (uri.equals(uri)) {
            return new TestSecureCacheResponse();
          }
          return null;
        }
       public CacheRequest put(URI uri, URLConnection conn)
          throws IOException {
          return null;
        }
    }
}

@TestTargetClass(value = ResponseCache.class)
public class ResponseCacheTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_GetDefault() throws Exception {
        assertNull(ResponseCache.getDefault());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "This is a complete subset of tests for setDefault method.",
            method = "setDefault",
            args = {java.net.ResponseCache.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "This is a complete subset of tests for setDefault method.",
            method = "ResponseCache",
            args = {}
        )
    })
    public void test_SetDefaultLjava_net_ResponseCache_Normal()
            throws Exception {
        ResponseCache rc1 = new MockResponseCache();
        ResponseCache rc2 = new MockResponseCache();
        ResponseCache.setDefault(rc1);
        assertSame(ResponseCache.getDefault(), rc1);
        ResponseCache.setDefault(rc2);
        assertSame(ResponseCache.getDefault(), rc2);
        ResponseCache.setDefault(null);
        assertNull(ResponseCache.getDefault());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_GetDefault_Security() {
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err.println("test_setDefaultLjava_net_ResponseCache_NoPermission is not tested");
            return;
        }
        try {
            ResponseCache.getDefault();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(old);
        }
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "This is a complete subset of tests for setDefault method.",
        method = "setDefault",
        args = {java.net.ResponseCache.class}
    )
    public void test_setDefaultLjava_net_ResponseCache_NoPermission() {
        ResponseCache rc = new MockResponseCache();
        SecurityManager old = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSM());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err.println("test_setDefaultLjava_net_ResponseCache_NoPermission is not tested");
            return;
        }
        try {
            ResponseCache.setDefault(rc);
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(old);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "get",
        args = {URI.class, String.class, Map.class}
    )
    public void test_get() throws Exception {
        String uri = "http:
        URL url  = new URL(uri);
        TestResponseCache cache = new TestResponseCache(uri, true);
        ResponseCache.setDefault(cache);
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        httpCon.setUseCaches(true);
        httpCon.connect();
        try {
            Thread.sleep(5000);
        } catch(Exception e) {}
        InputStream is = httpCon.getInputStream();
        byte[] array = new byte [10];
        is.read(array);
        assertEquals(url.toURI(), cache.getWasCalled);
        assertEquals("Cache test", new String(array));
        is.close();
        httpCon.disconnect();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "put",
        args = {URI.class, URLConnection.class}
    )
    @KnownFailure("the call to put is made with a wrong uri."
            + " The RI calls with http:
            + " but android only calls with http:
    public void test_put() throws Exception {
        TestResponseCache cache = new TestResponseCache(
                "http:
        ResponseCache.setDefault(cache);
        int port = Support_PortManager.getNextPort();
        Support_TestWebServer s = new Support_TestWebServer();
        try {
            s.initServer(port, 10000, false);
            Thread.currentThread().sleep(2500);
            URL url  = new URL("http:
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setUseCaches(true);
            httpCon.connect();
            Thread.currentThread().sleep(2500);
            assertEquals(url.toURI(), cache.getWasCalled);
            httpCon.getContent();
            assertEquals(url.toURI(), cache.putWasCalled);
            InputStream is = httpCon.getInputStream();
            byte[] array = new byte[Support_TestWebData.test1.length];
            is.read(array);
            assertTrue(Arrays.equals(Support_TestWebData.tests[0], array));
            is.close();
            httpCon.disconnect();
        } finally {
            s.close();
        }
    }
    class MockResponseCache extends ResponseCache {
        public CacheResponse get(URI arg0, String arg1, Map arg2)
                throws IOException {
            return null;
        }
        public CacheRequest put(URI arg0, URLConnection arg1)
                throws IOException {
            return null;
        }
    }
    class MockSM extends SecurityManager {
        public void checkPermission(Permission permission) {
            if (permission instanceof NetPermission) {
                if ("setResponseCache".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }
            if (permission instanceof NetPermission) {
                if ("getResponseCache".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }
            if (permission instanceof RuntimePermission) {
                if ("setSecurityManager".equals(permission.getName())) {
                    return;
                }
            }
        }
    }
    class TestCacheResponse extends CacheResponse {
        InputStream is = null;
        Map<String, List<String>> headers = null;
        public TestCacheResponse(String filename) {
            String path = getClass().getPackage().getName().replace(".", "/");
            is = getClass().getResourceAsStream("/" + path + "/" + filename);
        }
        @Override
        public InputStream getBody() {
           return is;
        }
        @Override
         public Map getHeaders() {
           return null;
         }
    }
    class TestCacheRequest extends CacheRequest {
        @Override
        public OutputStream getBody() {
            return null;
        }
        @Override
        public void abort() {
        }
    }
    class TestResponseCache extends ResponseCache {
        URI uri1 = null;
        boolean testGet = false;
        public URI getWasCalled = null;
        public URI putWasCalled = null;
        TestResponseCache(String uri, boolean testGet) {
            try {
                uri1  = new URI(uri);            
            } catch (URISyntaxException e) {
            }
            this.testGet = testGet;
        }
        @Override
        public CacheResponse get(URI uri, String rqstMethod, Map rqstHeaders) {
            getWasCalled = uri;
            if (testGet && uri.equals(uri1)) {
                return new TestCacheResponse("file1.cache");
            }
            return null;
        }
        @Override
        public CacheRequest put(URI uri, URLConnection conn) {
            putWasCalled = uri;
            if (!testGet && uri.equals(uri1)) {
                return new TestCacheRequest();
            }
            return null;
        }
    }
}
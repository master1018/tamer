@TestTargetClass(HttpURLConnection.class)
public class HttpURLConnectionTest extends junit.framework.TestCase {
    final String unknownURL = "http:
    URL url;
    HttpURLConnection uc;
    private boolean isGetCalled;
    private boolean isPutCalled;
    private boolean isCacheWriteCalled;
    private boolean isAbortCalled;
    private Map<String, List<String>> mockHeaderMap;
    private InputStream mockIs = new MockInputStream();
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "Verifies only successful response.",
      method = "getResponseCode",
      args = {}
    )
    public void test_getResponseCode() {
        try {
            uc.connect();
            assertEquals("Wrong response", 200, uc.getResponseCode());
        } catch (IOException e) {
            fail("Unexpected exception : " + e.getMessage());
        }
        try {
            URL url = new URL(unknownURL);   
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.getResponseCode();
            fail("IOException was not thrown.");
        } catch(IOException e) {
        }
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "Verifies only successful response message.",
      method = "getResponseMessage",
      args = {}
    )
    public void test_getResponseMessage() {
        try {
            uc.connect();
            assertTrue("Wrong response: " + uc.getResponseMessage(), uc
                    .getResponseMessage().equals("OK"));
        } catch (IOException e) {
            fail("Unexpected exception : " + e.getMessage());
        }
        try {
            URL url = new URL(unknownURL);   
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            conn.getResponseMessage();
            fail("IOException was not thrown.");
        } catch(IOException e) {
        }
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getHeaderFields",
      args = {}
    )
    @BrokenTest("Fails in CTS, passes in CoreTestRunner")
    public void test_getHeaderFields() throws Exception {
        url = new URL("http:
        uc = (HttpURLConnection) url.openConnection();
        try {
            uc.getInputStream();
        } catch (IOException e) {
            fail();
        }
        Map headers = uc.getHeaderFields();
        List list = (List) headers.get("Content-Length");
        if (list == null) {
            list = (List) headers.get("content-length");
        }
        assertNotNull(list);
        String contentLength = (String) list.get(0);
        assertNotNull(contentLength);
        assertTrue(headers.size() > 1);
        try {
            headers.put("hi", "bye");
            fail();
        } catch (UnsupportedOperationException e) {
        }
        try {
            list.set(0, "whatever");
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getRequestProperties",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",                
            method = "setRequestProperty",
            args = {String.class, String.class}
        )
    })
    public void test_getRequestProperties() {
        uc.setRequestProperty("whatever", "you like");
        Map headers = uc.getRequestProperties();
        List newHeader = (List) headers.get("whatever");
        assertNotNull(newHeader);
        assertEquals("you like", newHeader.get(0));
        try {
            headers.put("hi", "bye");
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
          level = TestLevel.PARTIAL_COMPLETE,
          notes = "",
          method = "getRequestProperty",
          args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Exception test verification.",                
            method = "setRequestProperty",
            args = {String.class, String.class}
        )
    })
    public void test_getRequestPropertyLjava_lang_String_BeforeConnected()
            throws MalformedURLException, IOException {
        uc.setRequestProperty("whatever", "you like"); 
        String res = uc.getRequestProperty("whatever"); 
        assertEquals("you like", res); 
        uc.setRequestProperty("", "you like"); 
        res = uc.getRequestProperty(""); 
        assertEquals("you like", res); 
        uc.setRequestProperty("", null); 
        res = uc.getRequestProperty(""); 
        assertEquals(null, res);
        try {
            uc.setRequestProperty(null, "you like"); 
            fail("Should throw NullPointerException"); 
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies IllegalStateException and null as a parameter.",
            method = "setRequestProperty",
            args = {String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "Verifies IllegalStateException and null as a parameter.",                
            method = "getRequestProperty",
            args = {String.class}
        )
    })
    public void test_getRequestPropertyLjava_lang_String_AfterConnected()
            throws IOException {
        uc.connect();
        try {
            uc.setRequestProperty("whatever", "you like"); 
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        try {
            uc.setRequestProperty(null, "you like"); 
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
        String res = uc.getRequestProperty("whatever"); 
        assertEquals(null, res);
        res = uc.getRequestProperty(null);
        assertEquals(null, res);
        try {
            uc.getRequestProperties();
            fail("Should throw IllegalStateException"); 
        } catch (IllegalStateException e) {
        }
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "setFixedLengthStreamingMode",
      args = {int.class}
    )
    public void test_setFixedLengthStreamingModeI() throws Exception {
        try {
            uc.setFixedLengthStreamingMode(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        uc.setFixedLengthStreamingMode(0);
        uc.setFixedLengthStreamingMode(1);
        try {
            uc.setChunkedStreamingMode(1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        uc.connect();
        try {
            uc.setFixedLengthStreamingMode(-1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        try {
            uc.setChunkedStreamingMode(-1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        MockHttpConnection mock = new MockHttpConnection(url);
        assertEquals(-1, mock.getFixedLength());
        mock.setFixedLengthStreamingMode(0);
        assertEquals(0, mock.getFixedLength());
        mock.setFixedLengthStreamingMode(1);
        assertEquals(1, mock.getFixedLength());
        mock.setFixedLengthStreamingMode(0);
        assertEquals(0, mock.getFixedLength());
    }
   @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "setChunkedStreamingMode",
      args = {int.class}
    )
    public void test_setChunkedStreamingModeI() throws Exception {
        uc.setChunkedStreamingMode(0);
        uc.setChunkedStreamingMode(-1);
        uc.setChunkedStreamingMode(-2);
        try {
            uc.setFixedLengthStreamingMode(-1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        try {
            uc.setFixedLengthStreamingMode(1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        uc.connect();
        try {
            uc.setFixedLengthStreamingMode(-1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        try {
            uc.setChunkedStreamingMode(1);
            fail("should throw IllegalStateException");
        } catch (IllegalStateException e) {
        }
        MockHttpConnection mock = new MockHttpConnection(url);
        assertEquals(-1, mock.getChunkLength());
        mock.setChunkedStreamingMode(-1);
        int defaultChunk = mock.getChunkLength();
        assertTrue(defaultChunk > 0);
        mock.setChunkedStreamingMode(0);
        assertEquals(mock.getChunkLength(), defaultChunk);
        mock.setChunkedStreamingMode(1);
        assertEquals(1, mock.getChunkLength());
    }
   @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "setFixedLengthStreamingMode",
      args = {int.class}
   )
   public void test_setFixedLengthStreamingModeI_effect() throws Exception {
        String posted = "just a test";
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url
                .openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setFixedLengthStreamingMode(posted.length() - 1);
        assertNull(conn.getRequestProperty("Content-length"));
        conn.setRequestProperty("Content-length", String.valueOf(posted
                .length()));
        assertEquals(String.valueOf(posted.length()), conn
                .getRequestProperty("Content-length"));
        OutputStream out = conn.getOutputStream();
        try {
            out.write(posted.getBytes());
            fail("should throw IOException");
        } catch (IOException e) {
        }
        try {
            out.close();
            fail("should throw IOException");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "setChunkedStreamingMode",
      args = {int.class}
    )
    public void test_setChunkedStreamingModeI_effect() throws Exception {
        String posted = "just a test";
        int chunkSize = posted.length() / 2;
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url
                .openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setChunkedStreamingMode(chunkSize);
        assertNull(conn.getRequestProperty("Transfer-Encoding"));
        conn.setRequestProperty("Content-length", String.valueOf(posted
                .length() - 1));
        assertEquals(conn.getRequestProperty("Content-length"), String
                .valueOf(posted.length() - 1));
        OutputStream out = conn.getOutputStream();
        out.write(posted.getBytes());
        out.close();
        assertTrue(conn.getResponseCode() > 0);
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL,
      notes = "Simple test.",
      method = "getOutputStream",
      args = {}
    )
    public void test_getOutputStream_afterConnection() throws Exception {
        uc.setDoOutput(true);
        uc.connect();
        assertNotNull(uc.getOutputStream());
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "using GetInputStream() and Connect()",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_Connect_GetInputStream()
            throws Exception {
        ResponseCache rc = new MockNonCachedResponseCache();
        ResponseCache.setDefault(rc);
        uc = (HttpURLConnection) url.openConnection();
        assertFalse(isGetCalled);
        uc.setUseCaches(true);
        uc.setDoInput(true);
        uc.connect();
        assertTrue(isGetCalled);
        assertFalse(isPutCalled);
        InputStream is = uc.getInputStream();
        assertTrue(isPutCalled);
        is.close();
        ((HttpURLConnection) uc).disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "using GetOutputStream() and Connect()",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_Connect_GetOutputStream()
            throws Exception {
        ResponseCache rc = new MockNonCachedResponseCache();
        ResponseCache.setDefault(rc);
        uc.setUseCaches(true);
        URLConnection uc = url.openConnection();
        uc.setDoOutput(true);
        assertFalse(isGetCalled);
        uc.connect();
        assertTrue(isGetCalled);
        assertFalse(isPutCalled);
        OutputStream os = uc.getOutputStream();
        assertFalse(isPutCalled);
        os.close();
        ((HttpURLConnection) uc).disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "real implementation\n in HttpURLConnection using GetOutputStream()",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_GetOutputStream()
            throws Exception {
        ResponseCache rc = new MockNonCachedResponseCache();
        ResponseCache.setDefault(rc);
        uc = (HttpURLConnection) url.openConnection();
        assertFalse(isGetCalled);
        uc.setDoOutput(true);
        uc.setUseCaches(true);
        OutputStream os = uc.getOutputStream();
        assertTrue(isGetCalled);
        assertFalse(isPutCalled);
        os.write(1);
        os.flush();
        os.close();
        ((HttpURLConnection) uc).getResponseCode();
        assertTrue(isGetCalled);
        isGetCalled = false;
        isPutCalled = false;
        ((HttpURLConnection) uc).disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "real implementation in HttpURLConnection using GetInputStream()",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_GetInputStream()
            throws Exception {
        ResponseCache rc = new MockNonCachedResponseCache();
        ResponseCache.setDefault(rc);
        URLConnection uc = url.openConnection();
        assertFalse(isGetCalled);
        uc.setDoOutput(true);
        uc.setUseCaches(true);
        InputStream is = uc.getInputStream();
        assertTrue(isGetCalled);
        assertTrue(isPutCalled);
        ((HttpURLConnection) uc).getResponseCode();
        is.close();
        ((HttpURLConnection) uc).disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "MockResponseCache returns cache null",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_NonCached() throws IOException {
        ResponseCache.setDefault(new MockNonCachedResponseCache());
        uc = (HttpURLConnection) url.openConnection();
        assertTrue(uc.getUseCaches());
        uc.setDoInput(true);
        isGetCalled = false;
        isPutCalled = false;
        InputStream is = uc.getInputStream();
        assertFalse(is instanceof MockInputStream);
        assertTrue(isGetCalled);
        assertTrue(isPutCalled);
        isCacheWriteCalled = false;
        is.read();
        assertTrue(isCacheWriteCalled);
        isCacheWriteCalled = false;
        byte[] buf = new byte[1];
        is.read(buf);
        assertTrue(isCacheWriteCalled);
        isCacheWriteCalled = false;
        buf = new byte[1];
        is.read(buf, 0, 1);
        assertTrue(isCacheWriteCalled);
        isAbortCalled = false;
        is.close();
        assertTrue(isAbortCalled);
        uc.disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "test default, and implementation with MockResponseCache",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_Cached() throws IOException {
        ResponseCache.setDefault(new MockCachedResponseCache());
        URL u = new URL("http:
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        assertTrue(uc.getUseCaches());
        isGetCalled = false;
        isPutCalled = false;
        InputStream is = uc.getInputStream();
        assertEquals(4711, is.read());
        assertTrue(isGetCalled);
        isCacheWriteCalled = false;
        is.read();
        assertFalse(isCacheWriteCalled);
        isCacheWriteCalled = false;
        byte[] buf = new byte[1];
        is.read(buf);
        assertFalse(isCacheWriteCalled);
        isCacheWriteCalled = false;
        buf = new byte[1];
        is.read(buf, 0, 1);
        assertFalse(isCacheWriteCalled);
        isAbortCalled = false;
        is.close();
        assertFalse(isAbortCalled);
        uc.disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "tests header fields written to cache with getHeaderFields.",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_getHeaderFields()
            throws IOException {
        ResponseCache.setDefault(new MockCachedResponseCache());
        URL u = new URL("http:
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        Map<String, List<String>> headerMap = uc.getHeaderFields();
        assertTrue(isGetCalled);
        assertFalse(isPutCalled);
        assertEquals(mockHeaderMap, headerMap);
        assertEquals(4711, uc.getInputStream().read());
        uc.disconnect();
    }
    @TestTargetNew(
      level = TestLevel.PARTIAL_COMPLETE,
      notes = "",
      method = "setUseCaches",
      args = {boolean.class}
    )
    public void test_UseCache_HttpURLConnection_NoCached_GetOutputStream()
            throws Exception {
        ResponseCache.setDefault(new MockNonCachedResponseCache());
        uc = (HttpURLConnection) url.openConnection();
        uc.setChunkedStreamingMode(10);
        uc.setDoOutput(true);
        uc.getOutputStream();
        assertTrue(isGetCalled);
        assertFalse(isPutCalled);
        assertFalse(isAbortCalled);
        uc.disconnect();
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "Negative cases depends on server responds.",
      method = "getErrorStream",
      args = {}
    )
    public void test_getErrorStream() throws Exception {
        uc.connect();
        assertEquals(200, uc.getResponseCode());        
        assertNull(uc.getErrorStream());        
        uc.disconnect();
        assertNull(uc.getErrorStream());
        try {
            URL url = new URL(unknownURL);   
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            assertNull(conn.getErrorStream());
        } catch(IOException e) {
            fail("IOException was thrown.");
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getFollowRedirects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setFollowRedirects",
            args = {boolean.class}
        )
    })
    public void test_followRedirects() {
        assertTrue("The default value of followRedirects is not true",
                HttpURLConnection.getFollowRedirects());
        HttpURLConnection.setFollowRedirects(false);
        assertFalse(HttpURLConnection.getFollowRedirects());
        HttpURLConnection.setFollowRedirects(true);
        assertTrue(HttpURLConnection.getFollowRedirects());
        SecurityManager sm = new SecurityManager() {
            public void checkPermission(Permission perm) {
            }
            public void checkSetFactory() {
                throw new SecurityException();
            }
        };
        SecurityManager oldSm = System.getSecurityManager();
        System.setSecurityManager(sm);
        try {
            HttpURLConnection.setFollowRedirects(false);
            fail("SecurityException should be thrown.");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(oldSm);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getInstanceFollowRedirects",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setInstanceFollowRedirects",
            args = {boolean.class}
        )
    })
    public void test_instanceFollowRedirect() {
        assertTrue(uc.getInstanceFollowRedirects());
        uc.setInstanceFollowRedirects(false);
        assertFalse(uc.getInstanceFollowRedirects());
        uc.setInstanceFollowRedirects(true);
        assertTrue(uc.getInstanceFollowRedirects());
        uc.setFollowRedirects(false);
        assertTrue(uc.getInstanceFollowRedirects());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getRequestMethod",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setRequestMethod",
            args = {java.lang.String.class}
        )
    })
    public void test_requestMethod() throws MalformedURLException, ProtocolException{
        URL url = new URL("http:
        HttpURLConnection con = new MyHttpURLConnection(url);
        assertEquals("The default value of requestMethod is not \"GET\"", "GET",
                con.getRequestMethod());
        String[] methods = { "GET", "DELETE", "HEAD", "OPTIONS", "POST", "PUT",
                "TRACE" };
        for (String method : methods) {
            con.setRequestMethod(method);
            assertEquals("The value of requestMethod is not " + method, method,
                    con.getRequestMethod());
        }
        try {
            con.setRequestMethod("Wrong method");
            fail("Should throw ProtocolException");
        } catch (ProtocolException e) {
        }
        try {
            con.setRequestMethod("get");
            fail("Should throw ProtocolException");
        } catch (ProtocolException e) {
        }        
    }
    private static class MyHttpURLConnection extends HttpURLConnection {
        protected MyHttpURLConnection(URL url) {
            super(url);
        }
        @Override
        public void disconnect() {
        }
        @Override
        public boolean usingProxy() {
            return false;
        }
        @Override
        public void connect() throws IOException {
        }
    }
    @TestTargetNew(
      level = TestLevel.SUFFICIENT,
      notes = "IOException is not verified.",
      method = "getPermission",
      args = {}
    )
    public void test_Permission() throws Exception {
        uc.connect();
        Permission permission = uc.getPermission();
        assertNotNull(permission);
        permission.implies(new SocketPermission("localhost","connect"));
        try {
            URL url = new URL(unknownURL);   
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.getPermission();
        } catch(IOException e) {
            fail("IOException was thrown.");
        }
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "HttpURLConnection",
      args = {java.net.URL.class}
    )
    public void test_Constructor() throws IOException {
        MockHttpConnection conn1 = new MockHttpConnection(url);
        conn1.connect();
        conn1.disconnect();
        MockHttpConnection conn2 = new MockHttpConnection(null);
        conn2.connect();
        conn2.disconnect();
        URL url = new URL("file:
        MockHttpConnection conn3 = new MockHttpConnection(url);
        conn3.connect();
        conn3.disconnect();
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "disconnect",
      args = {}
    )
    public void test_disconnect() {
        try {
            URL url1 = new URL("http:
            HttpURLConnection uc1 = (HttpURLConnection) url1.openConnection();
            uc1.disconnect();
            InputStream is = uc1.getInputStream();
            byte [] array = new byte [10];
            is.read(array);
            assertNotNull(array);
            try {
                uc1.connect();
                uc1.disconnect();
            } catch(IOException e) {
                fail("IOException was thrown.");
            }
        } catch (Exception e) {
            fail("Exception during setup : " + e.getMessage());
        }
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "getHeaderFieldDate",
      args = {java.lang.String.class, long.class}
    )
    public void test_getHeaderFieldDate(String name, long Default) {
        long date = uc.getHeaderFieldDate(uc.getHeaderField(0), 0);
        assertEquals(System.currentTimeMillis(), date);
    }
    @TestTargetNew(
      level = TestLevel.COMPLETE,
      notes = "",
      method = "usingProxy",
      args = {}
    )
    public void test_usingProxy() {
        assertFalse(uc.usingProxy());
    }
    class MockNonCachedResponseCache extends ResponseCache {
        public CacheResponse get(URI arg0, String arg1, Map arg2)
                throws IOException {
            isGetCalled = true;
            return null;
        }
        public CacheRequest put(URI arg0, URLConnection arg1)
                throws IOException {
            isPutCalled = true;
            return new MockCacheRequest();
        }
    }
    class MockCachedResponseCache extends ResponseCache {
        public CacheResponse get(URI arg0, String arg1, Map arg2)
                throws IOException {
            if (null == arg0 || null == arg1 || null == arg2) {
                throw new NullPointerException();
            }
            isGetCalled = true;
            return new MockCacheResponse();
        }
        public CacheRequest put(URI arg0, URLConnection arg1)
                throws IOException {
            if (null == arg0 || null == arg1) {
                throw new NullPointerException();
            }
            isPutCalled = true;
            return new MockCacheRequest();
        }
    }
    class MockCacheRequest extends CacheRequest {
        public OutputStream getBody() throws IOException {
            isCacheWriteCalled = true;
            return new MockOutputStream();
        }
        public void abort() {
            isAbortCalled = true;
        }
    }
    class MockCacheResponse extends CacheResponse {
        public Map<String, List<String>> getHeaders() throws IOException {
            return mockHeaderMap;
        }
        public InputStream getBody() throws IOException {
            return mockIs;
        }
    }
    class MockInputStream extends InputStream {
        public int read() throws IOException {
            return 4711;
        }
        public int read(byte[] arg0, int arg1, int arg2) throws IOException {
            return 1;
        }
        public int read(byte[] arg0) throws IOException {
            return 1;
        }
    }
    class MockOutputStream extends OutputStream {
        public void write(int b) throws IOException {
            isCacheWriteCalled = true;
        }
        public void write(byte[] b, int off, int len) throws IOException {
            isCacheWriteCalled = true;
        }
        public void write(byte[] b) throws IOException {
            isCacheWriteCalled = true;
        }
    }
    class MockHttpConnection extends HttpURLConnection {
        protected MockHttpConnection(URL url) {
            super(url);
        }
        public void disconnect() {
        }
        public boolean usingProxy() {
            return false;
        }
        public void connect() throws IOException {
        }
        public int getChunkLength() {
            return super.chunkLength;
        }
        public int getFixedLength() {
            return super.fixedContentLength;
        }
    }
    protected void setUp() {
        try {
            url = new URL(Support_Configuration.hTTPURLyahoo);
            uc = (HttpURLConnection) url.openConnection();
        } catch (Exception e) {
            fail("Exception during setup : " + e.getMessage());
        }
        mockHeaderMap = new Hashtable<String, List<String>>();
        List<String> valueList = new ArrayList<String>();
        valueList.add("value1");
        mockHeaderMap.put("field1", valueList);
        mockHeaderMap.put("field2", valueList);
        isGetCalled = false;
        isPutCalled = false;
        isCacheWriteCalled = false;
    }
    protected void tearDown() {
        uc.disconnect();
        ResponseCache.setDefault(null);
    }
}

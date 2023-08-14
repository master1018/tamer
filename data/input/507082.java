@TestTargetClass(URLStreamHandler.class) 
public class URLStreamHandlerTest extends TestCase {
    MockURLStreamHandler handler = null;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {URL.class, URL.class}
    )
    public void test_equalsLjava_net_URLLjava_net_URL() {
        try {
            URL url1 = new URL("ftp:
            URL url2 = new URL("http:
            assertFalse(url1.equals(url2));
            URL url3 = new URL("http:
            assertFalse(handler.equals(url1,url2));
            try {
                assertFalse(handler.equals(null, url1));
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getDefaultPort",
        args = {}
    )    
    public void test_getDefaultPort() {
        assertEquals(-1, handler.getDefaultPort());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getHostAddress",
        args = {URL.class}
    )
    public void test_getHostAddress() throws MalformedURLException,
                                        UnknownHostException {
        URL url1 = new URL("ftp:
        assertNull(handler.getHostAddress(url1));
        URL url2 = new URL("http:
        assertNull("testHost", handler.getHostAddress(url2));handler.getHostAddress(url2);
        URL url3 = new URL("http:
        assertEquals(InetAddress.getLocalHost(), handler.getHostAddress(url3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hashCode",
        args = {URL.class}
    )
    public void test_hashCodeLjava_net_URL() {
        try {
            URL url1 = new URL("ftp:
            URL url2 = new URL("http:
            assertTrue(handler.hashCode(url1) != handler.hashCode(url2));
            URL url3 = new URL("http:
            assertFalse(handler.equals(url1,url2));
            try {
                handler.hashCode(null);
                fail("NullPointerException was not thrown.");
            } catch(NullPointerException npe) {
            }
        } catch (MalformedURLException e) {
            fail("MalformedURLException was thrown.");
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "hostsEqual",
        args = {URL.class, URL.class}
    )
    public void test_hostsEqualLjava_net_URLLjava_net_URL() throws 
                                                    MalformedURLException {
        URL url1 = new URL("ftp:
        URL url2 = new URL("http:
        assertTrue(handler.hostsEqual(url1, url2));
        URL url3 = new URL("http:
        assertFalse(handler.hostsEqual(url1, url3));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "openConnection",
        args = { URL.class }
    )
    public void test_openConnectionLjava_net_URL() throws IOException {
        assertNull(handler.openConnection(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "openConnection",
        args = {URL.class, Proxy.class}
    )
    public void test_openConnectionLjava_net_URLLjava_net_Proxy() {
        try {
            handler.openConnection(null, null);
            fail("UnsupportedOperationException was not thrown.");
        } catch(UnsupportedOperationException  uoe) {
        } catch (IOException e) {
            fail("IOException was thrown.");
        }
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                "URLStreamHandlerFactory that can be done at most once.",
        method = "parseURL",
        args = {URL.class, String.class, int.class, int.class}
    )
    public void test_parseURLLjava_net_URLLjava_lang_StringII() 
                                                throws MalformedURLException {
        String str  = "http:
        URL url = new URL("http:
        try {
            handler.parseURL(url, str, 0, str.length());
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "sameFile",
        args = {URL.class, URL.class}
    )    
    public void test_sameFile() throws MalformedURLException {
        URL url1  = new URL("http:
        URL url2  = new URL("http:
        URL url3  = new URL("http:
        URL url4  = new URL("ftp:
        URL url5  = new URL("ftp:
        URL url6  = new URL("http:
        assertTrue("Test case 1", handler.sameFile(url1, url2));
        assertFalse("Test case 2", handler.sameFile(url3, url2));
        assertFalse("Test case 3", handler.sameFile(url3, url4));
        assertFalse("Test case 4", handler.sameFile(url4, url5));
        assertFalse("Test case 5", handler.sameFile(url1, url6));
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                "URLStreamHandlerFactory that can be done at most once.",
        method = "setURL",
        args = {java.net.URL.class, java.lang.String.class, 
                java.lang.String.class, int.class, java.lang.String.class, 
                java.lang.String.class}
    )       
    public void test_setURL1() throws MalformedURLException {
        URL url = new URL("http:
        try {
            handler.setURL(url, "http", "localhost", 80, "foo.c", "ref");
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Completed testing of this method requres set up " +
                 "URLStreamHandlerFactory that can be done at most once.",
        method = "setURL",
        args = {java.net.URL.class, java.lang.String.class, 
                java.lang.String.class, int.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class, 
                java.lang.String.class, java.lang.String.class}
    )       
    public void test_setURL2() throws MalformedURLException {
        URL url = new URL("http:
        try {
            handler.setURL(url, "http", "localhost", 80, "authority", 
                    "user", "foo.c", "query", "ref");
            fail("SecurityException should be thrown.");
        } catch(SecurityException se) {
        }        
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toExternalForm",
        args = {URL.class}
    )
    public void test_toExternalForm() throws MalformedURLException {
        URL [] urls = { new URL("ftp:
                        new URL("http:
                        new URL("http:
        for(URL url:urls) {
            assertEquals("Test case for " + url.toString(),
                    url.toString(), handler.toExternalForm(url));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "URLStreamHandler",
        args = {}
    )
    public void test_Constructor() {
        MockURLStreamHandler msh = new MockURLStreamHandler();
        assertEquals(-1, msh.getDefaultPort());
    }
    public void setUp() {
        handler = new MockURLStreamHandler();
    }
    class MockURLStreamHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL arg0) throws IOException {
            return null;
        }
        public boolean equals(URL u1, URL u2) {
            return super.equals(u1, u2);
        }
        public int getDefaultPort() {
            return super.getDefaultPort();
        }
        public InetAddress getHostAddress(URL u) {
            return super.getHostAddress(u);
        }
        public int hashCode(URL u) {
            return super.hashCode(u);
        }
        public boolean hostsEqual(URL u1, URL u2) {
            return super.hostsEqual(u1, u2);
        }
        public URLConnection openConnection(URL u, Proxy p) throws IOException {
            return super.openConnection(u, p);
        }
        public void parseURL(URL u, String spec, int start, int limit) {
            super.parseURL(u, spec, start, limit);
        }
        public boolean sameFile(URL u1, URL u2) {
            return super.sameFile(u1, u2);
        }
        public void setURL(URL u,
                String protocol,
                String host,
                int port,
                String file,
                String ref) {
            super.setURL(u, protocol, host, port, file, ref);
        }
        public void setURL(URL u,
                String protocol,
                String host,
                int port,
                String authority,
                String userInfo,
                String path,
                String query,
                String ref) {
            super.setURL(u, protocol, host, port, authority, 
                    userInfo, path, query, ref);
        }
        public String toExternalForm(URL u) {
            return super.toExternalForm(u);
        }
    }
}

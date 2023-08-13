@TestTargetClass(ProxySelector.class) 
public class ProxySelectorTest extends TestCase {
    private static final String HTTP_PROXY_HOST = "127.0.0.1";
    private static final int HTTP_PROXY_PORT = 80;
    private static final String HTTPS_PROXY_HOST = "127.0.0.2";
    private static final int HTTPS_PROXY_PORT = 443;
    private static final String FTP_PROXY_HOST = "127.0.0.3";
    private static final int FTP_PROXY_PORT = 80;
    private static final String SOCKS_PROXY_HOST = "127.0.0.4";
    private static final int SOCKS_PROXY_PORT = 1080;
    private static URI httpUri;
    private static URI ftpUri;
    private static URI httpsUri;
    private static URI tcpUri;
    private List proxyList;
    private ProxySelector selector = ProxySelector.getDefault();
    static {
        try {
            httpUri = new URI("http:
            ftpUri = new URI("ftp:
            httpsUri = new URI("https:
            tcpUri = new URI("socket:
        } catch (URISyntaxException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_getDefault() {
        ProxySelector selector1 = ProxySelector.getDefault();
        assertNotNull(selector1);
        ProxySelector selector2 = ProxySelector.getDefault();
        assertSame(selector1, selector2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for getDefault method.",
        method = "getDefault",
        args = {}
    )
    public void test_getDefault_Security() {
        SecurityManager orignalSecurityManager = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSecurityManager());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err.println("test_getDefault_Security is not tested");
            return;
        }
        try {
            ProxySelector.getDefault();
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(orignalSecurityManager);
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "This is a complete subset of tests for setDefault method.",
            method = "setDefault",
            args = {java.net.ProxySelector.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "ProxySelector",
            args = {}
        )
    })
    public void test_setDefaultLjava_net_ProxySelector() {
        ProxySelector originalSelector = ProxySelector.getDefault();
        try {
            ProxySelector newSelector = new MockProxySelector();
            ProxySelector.setDefault(newSelector);
            assertSame(newSelector, ProxySelector.getDefault());
            ProxySelector.setDefault(null);
            assertSame(null, ProxySelector.getDefault());
        } finally {
            ProxySelector.setDefault(originalSelector);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for setDefault method.",
        method = "setDefault",
        args = {java.net.ProxySelector.class}
    )
    public void test_setDefaultLjava_net_ProxySelector_Security() {
        ProxySelector originalSelector = ProxySelector.getDefault();
        SecurityManager orignalSecurityManager = System.getSecurityManager();
        try {
            System.setSecurityManager(new MockSecurityManager());
        } catch (SecurityException e) {
            System.err.println("No setSecurityManager permission.");
            System.err
                    .println("test_setDefaultLjava_net_ProxySelector_Security is not tested");
            return;
        }
        try {
            ProxySelector.setDefault(new MockProxySelector());
            fail("should throw SecurityException");
        } catch (SecurityException e) {
        } finally {
            System.setSecurityManager(orignalSecurityManager);
            ProxySelector.setDefault(originalSelector);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectExact()
            throws URISyntaxException {
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.NO_PROXY);
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);
        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectExact_NullHost()
            throws URISyntaxException {
        httpUri = new URI("http:
        ftpUri = new URI("ftp:
        httpsUri = new URI("https:
        tcpUri = new URI("socket:
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList, Proxy.NO_PROXY);
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, HTTP_PROXY_HOST,
                HTTP_PROXY_PORT);
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, HTTPS_PROXY_HOST,
                HTTPS_PROXY_PORT);
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList, Proxy.Type.HTTP, FTP_PROXY_HOST,
                FTP_PROXY_PORT);
        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList, Proxy.Type.SOCKS, SOCKS_PROXY_HOST,
                SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectExact_DefaultPort()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);
        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectExact_InvalidPort()
            throws URISyntaxException {
        final String INVALID_PORT = "abc";
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", INVALID_PORT);
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", INVALID_PORT);
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", INVALID_PORT);
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksproxyPort", INVALID_PORT);
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTP_PROXY_HOST,HTTP_PROXY_PORT);
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,HTTPS_PROXY_HOST,HTTPS_PROXY_PORT);
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList,Proxy.Type.HTTP,FTP_PROXY_HOST,FTP_PROXY_PORT);
        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectLikeHTTP()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", "");
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectNoHTTP()
            throws URISyntaxException {
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        proxyList = selector.select(httpUri);
        assertProxyEquals(proxyList,Proxy.NO_PROXY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectLikeHTTPS()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("http.proxyHost", "");
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectNoHTTPS()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        proxyList = selector.select(httpsUri);
        assertProxyEquals(proxyList,Proxy.NO_PROXY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectLikeFTP()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("ftp.proxyHost", "");
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList,Proxy.Type.SOCKS,SOCKS_PROXY_HOST,SOCKS_PROXY_PORT);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectNoFTP()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        proxyList = selector.select(ftpUri);
        assertProxyEquals(proxyList,Proxy.NO_PROXY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_SelectNoSOCKS()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        proxyList = selector.select(tcpUri);
        assertProxyEquals(proxyList,Proxy.NO_PROXY);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for connectFailed method.",
        method = "connectFailed",
        args = {java.net.URI.class, java.net.SocketAddress.class, java.io.IOException.class}
    )
    public void test_connectionFailedLjava_net_URILjava_net_SocketAddressLjava_io_IOException()
            throws URISyntaxException {
        System.setProperty("http.proxyHost", HTTP_PROXY_HOST);
        System.setProperty("http.proxyPort", String.valueOf(HTTP_PROXY_PORT));
        System.setProperty("https.proxyHost", HTTPS_PROXY_HOST);
        System.setProperty("https.proxyPort", String.valueOf(HTTPS_PROXY_PORT));
        System.setProperty("ftp.proxyHost", FTP_PROXY_HOST);
        System.setProperty("ftp.proxyPort", String.valueOf(FTP_PROXY_PORT));
        System.setProperty("socksProxyHost", SOCKS_PROXY_HOST);
        System.setProperty("socksProxyPort", String.valueOf(SOCKS_PROXY_PORT));
        List proxyList1 = selector.select(httpUri);
        assertNotNull(proxyList1);
        assertEquals(1, proxyList1.size());
        Proxy proxy1 = (Proxy) proxyList1.get(0);
        selector
                .connectFailed(httpUri, proxy1.address(), new SocketException());
        List proxyList2 = selector.select(httpUri);
        assertNotNull(proxyList2);
        assertEquals(1, proxyList2.size());
        Proxy proxy2 = (Proxy) proxyList2.get(0);
        assertEquals(proxy1, proxy2);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for connectFailed method.",
        method = "connectFailed",
        args = {java.net.URI.class, java.net.SocketAddress.class, java.io.IOException.class}
    )
    public void test_connectionFailedLjava_net_URILjava_net_SocketAddressLjava_io_IOException_IllegalArguement()
            throws URISyntaxException {
        SocketAddress sa = InetSocketAddress.createUnresolved("127.0.0.1", 0);
        try {
            selector.connectFailed(null, sa, new SocketException());
            fail("should throw IllegalArgumentException if any argument is null.");
        } catch (IllegalArgumentException e) {
        }
        try {
            selector.connectFailed(httpUri, null, new SocketException());
            fail("should throw IllegalArgumentException if any argument is null.");
        } catch (IllegalArgumentException e) {
        }
        try {
            selector.connectFailed(httpUri, sa, null);
            fail("should throw IllegalArgumentException if any argument is null.");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "This is a complete subset of tests for select method.",
        method = "select",
        args = {java.net.URI.class}
    )
    public void test_selectLjava_net_URI_IllegalArgument()
            throws URISyntaxException {
        URI[] illegalUris = { new URI("abc"), new URI("http"), null };
        for (int i = 0; i < illegalUris.length; i++) {
            try {
                selector.select(illegalUris[i]);
                fail("should throw IllegalArgumentException");
            } catch (IllegalArgumentException e) {
            }
        }
    }
    private void assertProxyEquals(List selectedProxyList, Proxy proxy) {
        assertNotNull(selectedProxyList);
        assertEquals(1, selectedProxyList.size());
        assertEquals((Proxy) selectedProxyList.get(0), proxy);
    }
    private void assertProxyEquals(List selectedProxyList, Proxy.Type type,
            String host, int port) {
        SocketAddress sa = InetSocketAddress.createUnresolved(host, port);
        Proxy proxy = new Proxy(type, sa);
        assertProxyEquals(selectedProxyList, proxy);
    }
    static class MockProxySelector extends ProxySelector {
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
        }
        public List <Proxy> select(URI uri) {
            return null;
        }
    }
    class MockSecurityManager extends SecurityManager {
        public void checkPermission(Permission permission) {
            if (permission instanceof NetPermission) {
                if ("getProxySelector".equals(permission.getName())) {
                    throw new SecurityException();
                }
            }
            if (permission instanceof NetPermission) {
                if ("setProxySelector".equals(permission.getName())) {
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
    protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
    }
    protected void tearDown() throws Exception {
        TestEnvironment.reset();
        super.tearDown();
    }
}

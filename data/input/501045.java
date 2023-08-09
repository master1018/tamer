@TestTargetClass(android.webkit.CacheManager.CacheResult.class)
public class CacheManager_CacheResultTest
        extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long NETWORK_OPERATION_DELAY = 10000l;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    public CacheManager_CacheResultTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
    }
    @Override
    protected void tearDown() throws Exception {
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getInputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getContentLength",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getETag",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLastModified",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalPath",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocation",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMimeType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOutputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExpires",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHttpStatusCode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getEncoding",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEncoding",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setInputStream",
            args = {InputStream.class}
        )
    })
    public void testCacheResult() throws Exception {
        final long validity = 5 * 50 * 1000; 
        final long age = 30 * 60 * 1000; 
        final long tolerance = 5 * 1000; 
        mWebServer = new CtsTestServer(getActivity());
        final String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebServer.setDocumentAge(age);
        mWebServer.setDocumentValidity(validity);
        mWebView.clearCache(true);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                CacheResult result =
                    CacheManager.getCacheFile(url, null);
                return result == null;
            }
        }.run();
        final long time = System.currentTimeMillis();
        loadUrl(url);
        CacheResult result = CacheManager.getCacheFile(url, null);
        assertNotNull(result);
        assertNotNull(result.getInputStream());
        assertTrue(result.getContentLength() > 0);
        assertNull(result.getETag());
        assertEquals(time - age,
                DateUtils.parseDate(result.getLastModified()).getTime(), tolerance);
        File file = new File(CacheManager.getCacheFileBaseDir().getPath(), result.getLocalPath());
        assertTrue(file.exists());
        assertNull(result.getLocation());
        assertEquals("text/html", result.getMimeType());
        assertNull(result.getOutputStream());
        assertEquals(time + validity, result.getExpires(), tolerance);
        assertEquals(HttpStatus.SC_OK, result.getHttpStatusCode());
        assertNotNull(result.getEncoding());
        result.setEncoding("iso-8859-1");
        assertEquals("iso-8859-1", result.getEncoding());
        result.setInputStream(null);
        assertNull(result.getInputStream());
    }
    private void loadUrl(String url){
        mWebView.loadUrl(url);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
        assertEquals(100, mWebView.getProgress());
    }
}

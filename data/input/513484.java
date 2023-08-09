@TestTargetClass(android.webkit.CacheManager.class)
public class CacheManagerTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long NETWORK_OPERATION_DELAY = 10000l;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    public CacheManagerTest() {
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
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCacheFileBaseDir",
        args = {}
    )
    public void testGetCacheFileBaseDir() {
        assertTrue(CacheManager.getCacheFileBaseDir().exists());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "startCacheTransaction",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "endCacheTransaction",
            args = {}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "the javadoc of these two methods doesn't exist.")
    public void testCacheTransaction() {
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCacheFile",
            args = {String.class, Map.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "saveCacheFile",
            args = {String.class, CacheResult.class}
        )
    })
    public void testCacheFile() throws Exception {
        mWebServer = new CtsTestServer(getActivity());
        final String url = mWebServer.getAssetUrl(TestHtmlConstants.EMBEDDED_IMG_URL);
        mWebView.clearCache(true);
        new DelayedCheck(NETWORK_OPERATION_DELAY) {
            @Override
            protected boolean check() {
                CacheResult result =
                    CacheManager.getCacheFile(url, null);
                return result == null;
            }
        }.run();
        loadUrl(url);
        CacheResult result = CacheManager.getCacheFile(url, null);
        assertNotNull(result);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "cacheDisabled",
        args = {}
    )
    public void testCacheDisabled() {
        assertFalse(CacheManager.cacheDisabled());
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

@TestTargetClass(android.webkit.WebHistoryItem.class)
public class WebHistoryItemTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private CtsTestServer mWebServer;
    public WebHistoryItemTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebServer = new CtsTestServer(getActivity());
    }
    @Override
    protected void tearDown() throws Exception {
        mWebServer.shutdown();
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFavicon",
            args = {}
        )
    })
    public void testWebHistoryItem() {
        final WebView view = getActivity().getWebView();
        WebBackForwardList list = view.copyBackForwardList();
        assertEquals(0, list.getSize());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(view, url);
        list = view.copyBackForwardList();
        assertEquals(1, list.getSize());
        WebHistoryItem item = list.getCurrentItem();
        assertNotNull(item);
        int firstId = item.getId();
        assertEquals(url, item.getUrl());
        assertNull(item.getOriginalUrl());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, item.getTitle());
        Bitmap icon = view.getFavicon();
        assertEquals(icon, item.getFavicon());
        url = mWebServer.getAssetUrl(TestHtmlConstants.BR_TAG_URL);
        assertLoadUrlSuccessfully(view, url);
        list = view.copyBackForwardList();
        assertEquals(2, list.getSize());
        item = list.getCurrentItem();
        assertNotNull(item);
        assertEquals(TestHtmlConstants.BR_TAG_TITLE, item.getTitle());
        int secondId = item.getId();
        assertTrue(firstId != secondId);
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginalUrl",
            args = {}
    )
    @ToBeFixed(explanation = "History item does not have the original URL set after a redirect.")
    @BrokenTest(value = "Bug 2121787: Test times out on the host side. Not 100% reproducible.")
    public void testRedirect() throws InterruptedException {
        final WebView view = getActivity().getWebView();
        view.setWebViewClient(new WebViewClient());
        WebBackForwardList list = view.copyBackForwardList();
        assertEquals(0, list.getSize());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        String redirect = mWebServer.getRedirectingAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(view, redirect);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                WebBackForwardList list = view.copyBackForwardList();
                return list.getSize() >= 1;
            }
        }.run();
        list = view.copyBackForwardList();
        assertEquals(1, list.getSize());
        WebHistoryItem item = list.getCurrentItem();
        assertNotNull(item);
        assertEquals(url, item.getUrl());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, item.getTitle());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "clone() is protected and WebHistoryItem cannot be subclassed",
        method = "clone",
        args = {}
    )
    public void testClone() {
    }
    private void assertLoadUrlSuccessfully(final WebView view, String url) {
        view.loadUrl(url);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return view.getProgress() == 100;
            }
        }.run();
    }
}

@TestTargetClass(android.webkit.WebIconDatabase.class)
public class WebIconDatabaseTest extends
                 ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long ICON_FETCH_TIMEOUT = 15000;
    private static final String DATA_FOLDER = "/webkittest/";
    private String mFilePath;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    public WebIconDatabaseTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WebViewStubActivity activity = (WebViewStubActivity) getActivity();
        mFilePath = activity.getFilesDir().toString() + DATA_FOLDER;
        clearDatabasePath();
        mWebView = activity.getWebView();
        mWebView.clearCache(true);
    }
    @Override
    protected void tearDown() throws Exception {
        clearDatabasePath();
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "close",
            args = {}
        )
    })
    @BrokenTest(value="intermittently fails bug 2250024")
    public void testOpen() {
        final WebIconDatabase webIconDatabase = WebIconDatabase.getInstance();
        final File path = new File(mFilePath);
        assertNull(path.listFiles());
        webIconDatabase.open(mFilePath);
        new DelayedCheck(10000) {
            @Override
            protected boolean check() {
                return path.listFiles() != null;
            }
        }.run();
        assertTrue(path.listFiles().length > 0);
        webIconDatabase.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    @UiThreadTest
    public void testGetInstance() {
        WebIconDatabase webIconDatabase1 = WebIconDatabase.getInstance();
        WebIconDatabase webIconDatabase2 = WebIconDatabase.getInstance();
        assertSame(webIconDatabase1, webIconDatabase2);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "retainIconForPageUrl",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestIconForPageUrl",
            args = {String.class, WebIconDatabase.IconListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "releaseIconForPageUrl",
            args = {String.class}
        )
    })
    @BrokenTest(value="intermittently fails bug 2250024")
    public void testRetainIconForPageUrl() throws Exception {
        final WebIconDatabase webIconDatabase = WebIconDatabase.getInstance();
        webIconDatabase.open(mFilePath);
        mWebServer = new CtsTestServer(getActivity());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        MyIconListener listener = new MyIconListener();
        webIconDatabase.retainIconForPageUrl(url);
        webIconDatabase.requestIconForPageUrl(url, listener);
        listener.waitForIcon(ICON_FETCH_TIMEOUT);
        assertTrue(listener.hasReceivedStatus());
        assertNotNull(listener.getIcon());
        webIconDatabase.releaseIconForPageUrl(url);
        listener = new MyIconListener();
        webIconDatabase.requestIconForPageUrl(url, listener);
        listener.waitForIcon(ICON_FETCH_TIMEOUT);
        assertTrue(listener.hasReceivedStatus());
        assertNotNull(listener.getIcon());
        webIconDatabase.close();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeAllIcons",
        args = {}
    )
    @BrokenTest(value="intermittently fails bug 2250024")
    public void testRemoveAllIcons() throws Exception {
        final WebIconDatabase webIconDatabase = WebIconDatabase.getInstance();
        webIconDatabase.open(mFilePath);
        mWebServer = new CtsTestServer(getActivity());
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        MyIconListener listener = new MyIconListener();
        webIconDatabase.retainIconForPageUrl(url);
        webIconDatabase.requestIconForPageUrl(url, listener);
        listener.waitForIcon(ICON_FETCH_TIMEOUT);
        assertTrue(listener.hasReceivedStatus());
        assertNotNull(listener.getIcon());
        webIconDatabase.removeAllIcons();
        listener = new MyIconListener();
        webIconDatabase.requestIconForPageUrl(url, listener);
        listener.waitForIcon(ICON_FETCH_TIMEOUT);
        assertFalse(listener.hasReceivedStatus());
        assertNull(listener.getIcon());
        webIconDatabase.close();
    }
    private static class MyIconListener implements WebIconDatabase.IconListener {
        private Bitmap mIcon;
        private String mUrl;
        private boolean mHasReceivedIcon = false;
        public synchronized void onReceivedIcon(String url, Bitmap icon) {
            mHasReceivedIcon = true;
            mIcon = icon;
            mUrl = url;
            notifyAll();
        }
        public synchronized void waitForIcon(long timeout) throws InterruptedException {
            if (!mHasReceivedIcon) {
                wait(timeout);
            }
        }
        public boolean hasReceivedStatus() {
            return mHasReceivedIcon;
        }
        public Bitmap getIcon() {
            return mIcon;
        }
        public String getUrl() {
            return mUrl;
        }
    }
    private void clearDatabasePath() throws InterruptedException {
        File path = new File(mFilePath);
        if (path.exists()) {
            Thread.sleep(1000);
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    assertTrue(files[i].delete());
                }
            }
            path.delete();
        }
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

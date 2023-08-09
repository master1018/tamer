@TestTargetClass(android.webkit.WebView.class)
public class WebViewTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final int INITIAL_PROGRESS = 100;
    private static long TEST_TIMEOUT = 20000L;
    private static long TIME_FOR_LAYOUT = 1000L;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    public WebViewTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
        File f = getActivity().getFileStreamPath("snapshot");
        if (f.exists()) {
            f.delete();
        }
    }
    @Override
    protected void tearDown() throws Exception {
        mWebView.clearHistory();
        mWebView.clearCache(true);
        if (mWebServer != null) {
            mWebServer.shutdown();
        }
        super.tearDown();
    }
    private void startWebServer(boolean secure) throws Exception {
        assertNull(mWebServer);
        mWebServer = new CtsTestServer(getActivity(), secure);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class, AttributeSet.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "WebView",
            args = {Context.class, AttributeSet.class, int.class}
        )
    })
    public void testConstructor() {
        new WebView(getActivity());
        new WebView(getActivity(), null);
        new WebView(getActivity(), null, 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "findAddress",
        args = {String.class}
    )
    public void testFindAddress() {
        assertEquals("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFORNIA 92926",
                WebView.findAddress("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFORNIA 92926"));
        assertEquals("455 LARKSPUR DR CALIFORNIA SPRINGS CA 92926",
                WebView.findAddress("455 LARKSPUR DR CALIFORNIA SPRINGS CA 92926"));
        assertNull(WebView.findAddress("455 LARKSPUR DRIVE CALIFORNIA SPRINGS CALIFONIA 92926"));
        assertEquals("455 LARKSPUR DR CALIFORNIA SPRINGS CA",
                WebView.findAddress("455 LARKSPUR DR CALIFORNIA SPRINGS CA"));
        assertNull(WebView.findAddress("CALIFORNIA SPRINGS CA"));
        assertNull(WebView.findAddress("455 LARKSPUR DR"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getZoomControls",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSettings",
            args = {}
        )
    })
    @SuppressWarnings("deprecation")
    @UiThreadTest
    public void testGetZoomControls() {
         WebSettings settings = mWebView.getSettings();
         assertTrue(settings.supportZoom());
         View zoomControls = mWebView.getZoomControls();
         assertNotNull(zoomControls);
         settings.setSupportZoom(false);
         assertFalse(settings.supportZoom());
         assertNull(mWebView.getZoomControls());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "invokeZoomPicker",
        args = {},
        notes = "Cannot test the effect of this method"
    )
    public void testInvokeZoomPicker() throws Exception {
        WebSettings settings = mWebView.getSettings();
        assertTrue(settings.supportZoom());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        mWebView.invokeZoomPicker();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "zoomIn",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "zoomOut",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getScale",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSettings",
            args = {}
        )
    })
    @UiThreadTest
    public void testZoom() {
        WebSettings settings = mWebView.getSettings();
        settings.setSupportZoom(false);
        assertFalse(settings.supportZoom());
        float currScale = mWebView.getScale();
        float previousScale = currScale;
        assertTrue(mWebView.zoomIn());
        currScale = mWebView.getScale();
        assertTrue(currScale > previousScale);
        assertTrue(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale < previousScale);
        settings.setSupportZoom(true);
        assertTrue(settings.supportZoom());
        currScale = mWebView.getScale();
        assertTrue(mWebView.zoomIn());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale > previousScale);
        while (currScale > previousScale) {
            mWebView.zoomIn();
            previousScale = currScale;
            currScale = mWebView.getScale();
        }
        assertFalse(mWebView.zoomIn());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertEquals(currScale, previousScale);
        assertTrue(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertTrue(currScale < previousScale);
        while (currScale < previousScale) {
            mWebView.zoomOut();
            previousScale = currScale;
            currScale = mWebView.getScale();
        }
        assertFalse(mWebView.zoomOut());
        previousScale = currScale;
        currScale = mWebView.getScale();
        assertEquals(currScale, previousScale);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setScrollBarStyle",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayHorizontalScrollbar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayVerticalScrollbar",
            args = {}
        )
    })
    @UiThreadTest
    public void testSetScrollBarStyle() {
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());
        mWebView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalScrollbarOverlay",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalScrollbarOverlay",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayHorizontalScrollbar",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "overlayVerticalScrollbar",
            args = {}
        )
    })
    public void testScrollBarOverlay() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;" +
                "width:" + dimension + "px;margin:0px auto;\">Test scroll bar overlay.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebView.overlayHorizontalScrollbar());
        assertFalse(mWebView.overlayVerticalScrollbar());
        int startX = mWebView.getScrollX();
        int startY = mWebView.getScrollY();
        final int bigVelocity = 10000;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(bigVelocity, bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();
        int overlayOffsetX = mWebView.getScrollX() - startX;
        int insetOffsetY = mWebView.getScrollY() - startY;
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(-bigVelocity, -bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();
        mWebView.setHorizontalScrollbarOverlay(false);
        mWebView.setVerticalScrollbarOverlay(true);
        assertFalse(mWebView.overlayHorizontalScrollbar());
        assertTrue(mWebView.overlayVerticalScrollbar());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(bigVelocity, bigVelocity);
            }
        });
        getInstrumentation().waitForIdleSync();
        int insetOffsetX = mWebView.getScrollX() - startX;
        int overlayOffsetY = mWebView.getScrollY() - startY;
        assertTrue(overlayOffsetY > insetOffsetY);
        assertTrue(overlayOffsetX > insetOffsetX);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadUrl",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginalUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getProgress",
            args = {}
        )
    })
    public void testLoadUrl() throws Exception {
        assertNull(mWebView.getUrl());
        assertNull(mWebView.getOriginalUrl());
        assertEquals(INITIAL_PROGRESS, mWebView.getProgress());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(100, mWebView.getProgress());
        assertEquals(url, mWebView.getUrl());
        assertEquals(url, mWebView.getOriginalUrl());
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, mWebView.getTitle());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getUrl",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getOriginalUrl",
            args = {}
        )
    })
    public void testGetOriginalUrl() throws Exception {
        assertNull(mWebView.getUrl());
        assertNull(mWebView.getOriginalUrl());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        String redirect = mWebServer.getRedirectingAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(redirect);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(url, mWebView.getUrl());
        assertEquals(redirect, mWebView.getOriginalUrl());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "stopLoading",
        args = {}
    )
    public void testStopLoading() throws Exception {
        assertNull(mWebView.getUrl());
        assertEquals(INITIAL_PROGRESS, mWebView.getProgress());
        startWebServer(false);
        String url = mWebServer.getDelayedAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        mWebView.stopLoading();
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return 100 == mWebView.getProgress();
            }
        }.run();
        assertNull(mWebView.getUrl());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoBack",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoForward",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "canGoBackOrForward",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goBack",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goForward",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "goBackOrForward",
            args = {int.class}
        )
    })
    public void testGoBackAndForward() throws Exception {
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(false, 1);
        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);
        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(false, 1);
        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);
        assertGoBackOrForwardBySteps(true, -1);
        assertGoBackOrForwardBySteps(false, 1);
        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);
        mWebView.goBack();
        delayedCheckWebBackForwardList(url2, 1, 3);
        assertGoBackOrForwardBySteps(true, -1);
        assertGoBackOrForwardBySteps(true, 1);
        mWebView.goForward();
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);
        mWebView.goBackOrForward(-2);
        delayedCheckWebBackForwardList(url1, 0, 3);
        assertGoBackOrForwardBySteps(false, -1);
        assertGoBackOrForwardBySteps(true, 2);
        mWebView.goBackOrForward(2);
        delayedCheckWebBackForwardList(url3, 2, 3);
        assertGoBackOrForwardBySteps(true, -2);
        assertGoBackOrForwardBySteps(false, 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addJavascriptInterface",
        args = {Object.class, String.class}
    )
    public void testAddJavascriptInterface() throws Exception {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        final DummyJavaScriptInterface obj = new DummyJavaScriptInterface();
        mWebView.addJavascriptInterface(obj, "dummy");
        assertFalse(obj.hasChangedTitle());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.ADD_JAVA_SCRIPT_INTERFACE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return obj.hasChangedTitle();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setBackgroundColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reload",
            args = {}
        )
    })
    public void testCapturePicture() throws Exception {
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.BLANK_PAGE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        Picture p = mWebView.capturePicture();
        Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b));
        assertBitmapFillWithColor(b, Color.WHITE);
        mWebView.setBackgroundColor(Color.CYAN);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        p.draw(new Canvas(b));
        assertBitmapFillWithColor(b, Color.WHITE);
        p = mWebView.capturePicture();
        p.draw(new Canvas(b));
        assertBitmapFillWithColor(b, Color.CYAN);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setPictureListener",
        args = {PictureListener.class}
    )
    public void testSetPictureListener() throws Exception {
        final MyPictureListener listener = new MyPictureListener();
        mWebView.setPictureListener(listener);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return listener.callCount > 0;
            }
        }.run();
        assertEquals(mWebView, listener.webView);
        assertNotNull(listener.picture);
        final int oldCallCount = listener.callCount;
        url = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        new DelayedCheck(TEST_TIMEOUT) {
            protected boolean check() {
                return listener.callCount > oldCallCount;
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "savePicture",
            args = {Bundle.class, File.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "Cannot test whether picture has been restored correctly.",
            method = "restorePicture",
            args = {Bundle.class, File.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "reload",
            args = {}
        )
    })
    public void testSaveAndRestorePicture() throws Throwable {
        mWebView.setBackgroundColor(Color.CYAN);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.BLANK_PAGE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        final Bundle bundle = new Bundle();
        final File f = getActivity().getFileStreamPath("snapshot");
        try {
            assertTrue(bundle.isEmpty());
            assertEquals(0, f.length());
            assertTrue(mWebView.savePicture(bundle, f));
            assertTrue(f.length() > 0);
            assertFalse(bundle.isEmpty());
            Picture p = Picture.createFromStream(new FileInputStream(f));
            Bitmap b = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
            p.draw(new Canvas(b));
            assertBitmapFillWithColor(b, Color.CYAN);
            mWebView.setBackgroundColor(Color.WHITE);
            mWebView.reload();
            waitForLoadComplete(mWebView, TEST_TIMEOUT);
            b = Bitmap.createBitmap(mWebView.getWidth(), mWebView.getHeight(), Config.ARGB_8888);
            mWebView.draw(new Canvas(b));
            assertBitmapFillWithColor(b, Color.WHITE);
            runTestOnUiThread(new Runnable() {
                public void run() {
                    assertTrue(mWebView.restorePicture(bundle, f));
                }
            });
            getInstrumentation().waitForIdleSync();
        } finally {
            if (f.exists()) {
                f.delete();
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHttpAuthUsernamePassword",
            args = {String.class, String.class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHttpAuthUsernamePassword",
            args = {String.class, String.class}
        )
    })
    public void testAccessHttpAuthUsernamePassword() {
        try {
            WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();
            String host = "http:
            String realm = "testrealm";
            String userName = "user";
            String password = "password";
            String[] result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNull(result);
            mWebView.setHttpAuthUsernamePassword(host, realm, userName, password);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(password, result[1]);
            String newPassword = "newpassword";
            mWebView.setHttpAuthUsernamePassword(host, realm, userName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(newPassword, result[1]);
            String newUserName = "newuser";
            mWebView.setHttpAuthUsernamePassword(host, realm, newUserName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(newUserName, result[0]);
            assertEquals(newPassword, result[1]);
            mWebView.setHttpAuthUsernamePassword(host, realm, null, password);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertNull(result[0]);
            assertEquals(password, result[1]);
            mWebView.setHttpAuthUsernamePassword(host, realm, userName, null);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(userName, result[0]);
            assertEquals(null, result[1]);
            mWebView.setHttpAuthUsernamePassword(host, realm, null, null);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertNull(result[0]);
            assertNull(result[1]);
            mWebView.setHttpAuthUsernamePassword(host, realm, newUserName, newPassword);
            result = mWebView.getHttpAuthUsernamePassword(host, realm);
            assertNotNull(result);
            assertEquals(newUserName, result[0]);
            assertEquals(newPassword, result[1]);
        } finally {
            WebViewDatabase.getInstance(getActivity()).clearHttpAuthUsernamePassword();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "savePassword",
        args = {String.class, String.class, String.class}
    )
    public void testSavePassword() {
        WebViewDatabase db = WebViewDatabase.getInstance(getActivity());
        try {
            db.clearUsernamePassword();
            String host = "http:
            String userName = "user";
            String password = "password";
            assertFalse(db.hasUsernamePassword());
            mWebView.savePassword(host, userName, password);
            assertTrue(db.hasUsernamePassword());
        } finally {
            db.clearUsernamePassword();
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadData",
            args = {String.class, String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTitle",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "capturePicture",
            args = {}
        )
    })
    public void testLoadData() throws Exception {
        assertNull(mWebView.getTitle());
        mWebView.loadData("<html><head><title>Hello,World!</title></head><body></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("Hello,World!", mWebView.getTitle());
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        AssetManager assets = getActivity().getAssets();
        Bitmap b1 = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.SMALL_IMG_URL));
        b1 = b1.copy(Config.ARGB_8888, true);
        Picture p = mWebView.capturePicture();
        Bitmap b2 = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b2));
        assertTrue(checkBitmapInsideAnother(b1, b2));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "loadDataWithBaseURL",
            args = {String.class, String.class, String.class, String.class, String.class}
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
        )
    })
    public void testLoadDataWithBaseUrl() throws Exception {
        assertNull(mWebView.getTitle());
        assertNull(mWebView.getUrl());
        String imgUrl = TestHtmlConstants.SMALL_IMG_URL; 
        startWebServer(false);
        String baseUrl = mWebServer.getAssetUrl("foo.html");
        String failUrl = "random";
        mWebView.loadDataWithBaseURL(baseUrl,
                "<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8", failUrl);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebServer.getLastRequestUrl().endsWith(imgUrl));
        assertEquals(failUrl, mWebView.getUrl());
        imgUrl = TestHtmlConstants.LARGE_IMG_URL;
        mWebView.loadDataWithBaseURL(baseUrl,
                "<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8", null);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebServer.getLastRequestUrl().endsWith(imgUrl));
        assertEquals("about:blank", mWebView.getUrl());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "findAll",
        args = {String.class},
        notes = "Cannot check highlighting"
    )
    @UiThreadTest
    public void testFindAll() throws InterruptedException {
        String p = "<p>Find all instances of find on the page and highlight them.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(2, mWebView.findAll("find"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findNext",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findAll",
            args = {String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearMatches",
            args = {}
        )
    })
    public void testFindNext() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;\">" +
                "Find all instances of a word on the page and highlight them.</p>";
        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.findAll("all");
            }
        });
        getInstrumentation().waitForIdleSync();
        int previousScrollY = mWebView.getScrollY();
        findNextOnUiThread(true);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() > previousScrollY);
        previousScrollY = mWebView.getScrollY();
        findNextOnUiThread(true);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() < previousScrollY);
        previousScrollY = mWebView.getScrollY();
        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() > previousScrollY);
        previousScrollY = mWebView.getScrollY();
        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() < previousScrollY);
        previousScrollY = mWebView.getScrollY();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.clearMatches();
            }
        });
        getInstrumentation().waitForIdleSync();
        findNextOnUiThread(false);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() == previousScrollY);
        findNextOnUiThread(true);
        delayedCheckStopScrolling();
        assertTrue(mWebView.getScrollY() == previousScrollY);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "documentHasImages",
        args = {android.os.Message.class}
    )
    public void testDocumentHasImages() throws Exception {
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        final DocumentHasImageCheckHandler handler =
            new DocumentHasImageCheckHandler(mWebView.getHandler().getLooper());
        Message response = new Message();
        response.setTarget(handler);
        assertFalse(handler.hasCalledHandleMessage());
        mWebView.documentHasImages(response);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(1, handler.getMsgArg1());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pageDown",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pageUp",
            args = {boolean.class}
        )
    })
    public void testPageScroll() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;\">" +
                "Scroll by half the size of the page.</p>";
        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(pageDownOnUiThread(false));
        while (pageDownOnUiThread(false)) {
        }
        assertFalse(pageDownOnUiThread(false));
        int bottomScrollY = mWebView.getScrollY();
        assertTrue(pageUpOnUiThread(false));
        while (pageUpOnUiThread(false)) {
        }
        assertFalse(pageUpOnUiThread(false));
        int topScrollY = mWebView.getScrollY();
        assertTrue(pageDownOnUiThread(true));
        assertEquals(bottomScrollY, mWebView.getScrollY());
        assertTrue(pageUpOnUiThread(true));
        assertEquals(topScrollY, mWebView.getScrollY());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getContentHeight",
        args = {}
    )
    public void testGetContentHeight() throws InterruptedException {
        mWebView.loadData("<html><body></body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(mWebView.getHeight(), mWebView.getContentHeight() * mWebView.getScale(), 2f);
        final int pageHeight = 600;
        String p = "<p style=\"height:" + pageHeight + "px;margin:0px auto;\">Get the height of "
                + "HTML content.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertTrue(mWebView.getContentHeight() > pageHeight);
        int extraSpace = mWebView.getContentHeight() - pageHeight;
        mWebView.loadData("<html><body>" + p + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(pageHeight + pageHeight + extraSpace, mWebView.getContentHeight());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "Cannot test whether the view is cleared.",
        method = "clearView",
        args = {}
    )
    public void testClearView() throws Throwable {
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.SMALL_IMG_URL);
        mWebView.loadData("<html><body><img src=\"" + imgUrl + "\"/></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        AssetManager assets = getActivity().getAssets();
        Bitmap b1 = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.SMALL_IMG_URL));
        b1 = b1.copy(Config.ARGB_8888, true);
        Picture p = mWebView.capturePicture();
        Bitmap b2 = Bitmap.createBitmap(p.getWidth(), p.getHeight(), Config.ARGB_8888);
        p.draw(new Canvas(b2));
        assertTrue(checkBitmapInsideAnother(b1, b2));
        mWebView.clearView();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.invalidate();
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "clearCache",
        args = {boolean.class}
    )
    public void testClearCache() throws Exception {
        final File cacheFileBaseDir = CacheManager.getCacheFileBaseDir();
        mWebView.clearCache(true);
        assertEquals(0, cacheFileBaseDir.list().length);
        startWebServer(false);
        mWebView.loadUrl(mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL));
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        int cacheFileCount = cacheFileBaseDir.list().length;
        assertTrue(cacheFileCount > 0);
        mWebView.clearCache(false);
        assertEquals(cacheFileCount, cacheFileBaseDir.list().length);
        mWebView.clearCache(true);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return cacheFileBaseDir.list().length == 0;
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "enablePlatformNotifications",
            args = {},
            notes = "Cannot simulate data state or proxy changes"
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "disablePlatformNotifications",
            args = {},
            notes = "Cannot simulate data state or proxy changes"
        )
    })
    public void testPlatformNotifications() {
        WebView.enablePlatformNotifications();
        WebView.disablePlatformNotifications();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "getPluginList",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "refreshPlugins",
            args = {boolean.class}
        )
    })
    public void testAccessPluginList() {
        assertNotNull(WebView.getPluginList());
        mWebView.refreshPlugins(false);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "destroy",
        args = {}
    )
    public void testDestroy() {
        WebView localWebView = new WebView(getActivity());
        localWebView.destroy();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "flingScroll",
        args = {int.class, int.class}
    )
    public void testFlingScroll() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;" +
                "width:" + dimension + "px\">Test fling scroll.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        int previousScrollX = mWebView.getScrollX();
        int previousScrollY = mWebView.getScrollY();
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.flingScroll(100, 100);
            }
        });
        int timeSlice = 500;
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() > previousScrollX);
        assertTrue(mWebView.getScrollY() > previousScrollY);
        previousScrollY = mWebView.getScrollY();
        previousScrollX = mWebView.getScrollX();
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() >= previousScrollX);
        assertTrue(mWebView.getScrollY() >= previousScrollY);
        previousScrollY = mWebView.getScrollY();
        previousScrollX = mWebView.getScrollX();
        Thread.sleep(timeSlice);
        assertTrue(mWebView.getScrollX() >= previousScrollX);
        assertTrue(mWebView.getScrollY() >= previousScrollY);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestFocusNodeHref",
        args = {android.os.Message.class}
    )
    public void testRequestFocusNodeHref() throws InterruptedException {
        String links = "<DL><p><DT><A HREF=\"" + TestHtmlConstants.HTML_URL1
                + "\">HTML_URL1</A><DT><A HREF=\"" + TestHtmlConstants.HTML_URL2
                + "\">HTML_URL2</A></DL><p>";
        mWebView.loadData("<html><body>" + links + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        final HrefCheckHandler handler = new HrefCheckHandler(mWebView.getHandler().getLooper());
        Message hrefMsg = new Message();
        hrefMsg.setTarget(handler);
        handler.reset();
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mWebView.requestFocusNodeHref(hrefMsg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(TestHtmlConstants.HTML_URL1, handler.getResultUrl());
        handler.reset();
        hrefMsg = new Message();
        hrefMsg.setTarget(handler);
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        mWebView.requestFocusNodeHref(hrefMsg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(TestHtmlConstants.HTML_URL2, handler.getResultUrl());
        mWebView.requestFocusNodeHref(null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestImageRef",
        args = {android.os.Message.class}
    )
    public void testRequestImageRef() throws Exception {
        AssetManager assets = getActivity().getAssets();
        Bitmap bitmap = BitmapFactory.decodeStream(assets.open(TestHtmlConstants.LARGE_IMG_URL));
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        startWebServer(false);
        String imgUrl = mWebServer.getAssetUrl(TestHtmlConstants.LARGE_IMG_URL);
        mWebView.loadData("<html><title>Title</title><body><img src=\"" + imgUrl
                + "\"/></body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        final HrefCheckHandler handler = new HrefCheckHandler(mWebView.getHandler().getLooper());
        Message msg = new Message();
        msg.setTarget(handler);
        handler.reset();
        int[] location = new int[2];
        mWebView.getLocationOnScreen(location);
        getInstrumentation().sendPointerSync(
                MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN,
                        location[0] + imgWidth / 2,
                        location[1] + imgHeight / 2, 0));
        mWebView.requestImageRef(msg);
        new DelayedCheck() {
            @Override
            protected boolean check() {
                return handler.hasCalledHandleMessage();
            }
        }.run();
        assertEquals(imgUrl, handler.mResultUrl);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "debugDump",
        args = {}
    )
    public void testDebugDump() {
        mWebView.debugDump();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearFormData",
        args = {}
    )
    @BrokenTest(value = "Causes the process to crash some time after test completion.")
    public void testClearFormData() throws Throwable {
        String form = "<form><input type=\"text\" name=\"testClearFormData\"></form>";
        mWebView.loadData("<html><body>" + form + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        moveFocusDown();
        getInstrumentation().sendStringSync("test");
        sendKeys(KeyEvent.KEYCODE_ENTER);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        moveFocusDown();
        View input = mWebView.findFocus();
        assertTrue(input instanceof AutoCompleteTextView);
        getInstrumentation().sendStringSync("te");
        assertTrue(((AutoCompleteTextView) input).isPopupShowing());
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        moveFocusDown();
        mWebView.clearFormData();
        input = mWebView.findFocus();
        assertTrue(input instanceof AutoCompleteTextView);
        getInstrumentation().sendStringSync("te");
        assertFalse(((AutoCompleteTextView) input).isPopupShowing());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getHitTestResult",
        args = {}
    )
    public void testGetHitTestResult() throws Throwable {
        String anchor = "<p><a href=\"" + TestHtmlConstants.EXT_WEB_URL1
                + "\">normal anchor</a></p>";
        String blankAnchor = "<p><a href=\"\">blank anchor</a></p>";
        String form = "<p><form><input type=\"text\" name=\"Test\"><br>"
                + "<input type=\"submit\" value=\"Submit\"></form></p>";
        String phoneNo = "3106984000";
        String tel = "<p><a href=\"tel:" + phoneNo + "\">Phone</a></p>";
        String email = "test@gmail.com";
        String mailto = "<p><a href=\"mailto:" + email + "\">Email</a></p>";
        String location = "shanghai";
        String geo = "<p><a href=\"geo:0,0?q=" + location + "\">Location</a></p>";
        mWebView.loadDataWithBaseURL("fake:
                + tel + mailto + geo + "</body></html>", "text/html", "UTF-8", null);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        moveFocusDown();
        HitTestResult result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals(TestHtmlConstants.EXT_WEB_URL1, result.getExtra());
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals("fake:
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.EDIT_TEXT_TYPE, result.getType());
        assertNull(result.getExtra());
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.UNKNOWN_TYPE, result.getType());
        assertNull(result.getExtra());
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.PHONE_TYPE, result.getType());
        assertEquals(phoneNo, result.getExtra());
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.EMAIL_TYPE, result.getType());
        assertEquals(email, result.getExtra());
        moveFocusDown();
        result = mWebView.getHitTestResult();
        assertEquals(HitTestResult.GEO_TYPE, result.getType());
        assertEquals(location, result.getExtra());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setInitialScale",
        args = {int.class}
    )
    public void testSetInitialScale() throws InterruptedException {
        String p = "<p style=\"height:1000px;width:1000px\">Test setInitialScale.</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        final float defaultScale = getInstrumentation().getTargetContext().getResources().
            getDisplayMetrics().density;
        assertEquals(defaultScale, mWebView.getScale(), 0f);
        mWebView.setInitialScale(0);
        mWebView.loadData("<html><body>" + p + "2" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(defaultScale, mWebView.getScale(), 0f);
        mWebView.setInitialScale(50);
        mWebView.loadData("<html><body>" + p + "3" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(0.5f, mWebView.getScale(), .02f);
        mWebView.setInitialScale(0);
        mWebView.loadData("<html><body>" + p + "4" + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals(defaultScale, mWebView.getScale(), 0f);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        notes = "No API to trigger receiving an icon. Favicon not loaded automatically.",
        method = "getFavicon",
        args = {}
    )
    @ToBeFixed(explanation = "Favicon is not loaded automatically.")
    public void testGetFavicon() throws Exception {
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.TEST_FAVICON_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        mWebView.getFavicon();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearHistory",
        args = {}
    )
    public void testClearHistory() throws Exception {
        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);
        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);
        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);
        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);
        mWebView.clearHistory();
        delayedCheckWebBackForwardList(url3, 0, 1);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "saveState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "restoreState",
            args = {Bundle.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "copyBackForwardList",
            args = {}
        )
    })
    @ToBeFixed(explanation="Web history items do not get inflated after restore.")
    public void testSaveAndRestoreState() throws Throwable {
        assertNull(mWebView.saveState(new Bundle()));
        startWebServer(false);
        String url1 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL1);
        String url2 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL2);
        String url3 = mWebServer.getAssetUrl(TestHtmlConstants.HTML_URL3);
        assertLoadUrlSuccessfully(mWebView, url1);
        delayedCheckWebBackForwardList(url1, 0, 1);
        assertLoadUrlSuccessfully(mWebView, url2);
        delayedCheckWebBackForwardList(url2, 1, 2);
        assertLoadUrlSuccessfully(mWebView, url3);
        delayedCheckWebBackForwardList(url3, 2, 3);
        Bundle bundle = new Bundle();
        WebBackForwardList saveList = mWebView.saveState(bundle);
        assertNotNull(saveList);
        assertEquals(3, saveList.getSize());
        assertEquals(2, saveList.getCurrentIndex());
        assertEquals(url1, saveList.getItemAtIndex(0).getUrl());
        assertEquals(url2, saveList.getItemAtIndex(1).getUrl());
        assertEquals(url3, saveList.getItemAtIndex(2).getUrl());
        final WebView newWebView = new WebView(getActivity());
        WebBackForwardList copyListBeforeRestore = newWebView.copyBackForwardList();
        assertNotNull(copyListBeforeRestore);
        assertEquals(0, copyListBeforeRestore.getSize());
        final WebBackForwardList restoreList = newWebView.restoreState(bundle);
        assertNotNull(restoreList);
        assertEquals(3, restoreList.getSize());
        assertEquals(2, saveList.getCurrentIndex());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setWebViewClient",
        args = {WebViewClient.class}
    )
    public void testSetWebViewClient() throws Throwable {
        final MockWebViewClient webViewClient = new MockWebViewClient();
        mWebView.setWebViewClient(webViewClient);
        assertFalse(webViewClient.onScaleChangedCalled());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.zoomIn();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(webViewClient.onScaleChangedCalled());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCertificate",
            args = {SslCertificate.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCertificate",
            args = {}
        )
    })
    public void testAccessCertificate() throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView = new MockWebView(getActivity());
                getActivity().setContentView(mWebView);
            }
        });
        getInstrumentation().waitForIdleSync();
        final MockWebView mockWebView = (MockWebView) mWebView;
        mockWebView.setWebViewClient(new MockWebViewClient());
        mockWebView.reset();
        startWebServer(true);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mockWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return mockWebView.hasCalledSetCertificate();
            }
        }.run();
        SslCertificate cert = mockWebView.getCertificate();
        assertNotNull(cert);
        assertEquals("Android", cert.getIssuedTo().getUName());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "WebViewClient.onReceivedSslError() is hidden, cannot store SSL preferences.",
        method = "clearSslPreferences",
        args = {}
    )
    public void testClearSslPreferences() {
        mWebView.clearSslPreferences();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "pauseTimers",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "resumeTimers",
            args = {}
        )
    })
    @ToBeFixed(explanation = "WebView.pauseTimers() does not pause javascript timers")
    @BrokenTest(value = "Frequently crashes the process some time after test completion.")
    public void testPauseTimers() throws Exception {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.TEST_TIMER_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        int counter = Integer.parseInt(mWebView.getTitle());
        Thread.sleep(2000);
        assertTrue(Integer.parseInt(mWebView.getTitle()) > counter);
        mWebView.pauseTimers();
        Thread.sleep(2000); 
        counter = Integer.parseInt(mWebView.getTitle());
        Thread.sleep(2000);
        mWebView.resumeTimers();
        Thread.sleep(2000);
        assertTrue(Integer.parseInt(mWebView.getTitle()) > counter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "requestChildRectangleOnScreen",
        args = {View.class, Rect.class, boolean.class}
    )
    public void testRequestChildRectangleOnScreen() throws Throwable {
        DisplayMetrics metrics = mWebView.getContext().getResources().getDisplayMetrics();
        final int dimension = 2 * Math.max(metrics.widthPixels, metrics.heightPixels);
        String p = "<p style=\"height:" + dimension + "px;width:" + dimension + "px\">&nbsp;</p>";
        mWebView.loadData("<html><body>" + p + "</body></html>", "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        getInstrumentation().waitForIdleSync();
        runTestOnUiThread(new Runnable() {
            public void run() {
                int origX = mWebView.getScrollX();
                int origY = mWebView.getScrollY();
                int half = dimension / 2;
                Rect rect = new Rect(half, half, half + 1, half + 1);
                assertTrue(mWebView.requestChildRectangleOnScreen(mWebView, rect, true));
                assertTrue(mWebView.getScrollX() > origX);
                assertTrue(mWebView.getScrollY() > origY);
            }
        });
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "performLongClick",
        args = {}
    )
    public void testPerformLongClick() throws InterruptedException {
        String form = "<p><form><input type=\"text\" name=\"Test\"><br>"
                + "<input type=\"submit\" value=\"Submit\"></form></p>";
        mWebView.loadDataWithBaseURL("fake:
                + "</body></html>", "text/html", "UTF-8", null);
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        mWebView.performLongClick();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDownloadListener",
            args = {DownloadListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "requestFocus",
            args = {int.class, Rect.class}
        )
    })
    @ToBeFixed(explanation="Mime type and content length passed to listener are incorrect.")
    public void testSetDownloadListener() throws Throwable {
        final String mimeType = "application/octet-stream";
        final int length = 100;
        final MyDownloadListener listener = new MyDownloadListener();
        startWebServer(false);
        String url = mWebServer.getBinaryUrl(mimeType, length);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setDownloadListener(listener);
        mWebView.loadData("<html><body><a href=\"" + url + "\">link</a></body></html>",
                "text/html", "UTF-8");
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        runTestOnUiThread(new Runnable() {
            public void run() {
                assertTrue(mWebView.requestFocus(View.FOCUS_DOWN, null));
            }
        });
        getInstrumentation().waitForIdleSync();
        getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return listener.called;
            }
        }.run();
        assertEquals(url, listener.url);
        assertTrue(listener.contentDisposition.contains("test.bin"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setLayoutParams",
        args = {android.view.ViewGroup.LayoutParams.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for setLayoutParams() is incomplete.")
    @UiThreadTest
    public void testSetLayoutParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(600, 800);
        mWebView.setLayoutParams(params);
        assertSame(params, mWebView.getLayoutParams());
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        notes = "No documentation",
        method = "setMapTrackballToArrowKeys",
        args = {boolean.class}
    )
    public void testSetMapTrackballToArrowKeys() {
        mWebView.setMapTrackballToArrowKeys(true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setNetworkAvailable",
        args = {boolean.class}
    )
    public void testSetNetworkAvailable() throws Exception {
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.NETWORK_STATE_URL);
        assertLoadUrlSuccessfully(mWebView, url);
        assertEquals("ONLINE", mWebView.getTitle());
        mWebView.setNetworkAvailable(false);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("OFFLINE", mWebView.getTitle());
        mWebView.setNetworkAvailable(true);
        mWebView.reload();
        waitForLoadComplete(mWebView, TEST_TIMEOUT);
        assertEquals("ONLINE", mWebView.getTitle());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setWebChromeClient",
        args = {WebChromeClient.class}
    )
    public void testSetWebChromeClient() throws Throwable {
        final MockWebChromeClient webChromeClient = new MockWebChromeClient();
        mWebView.setWebChromeClient(webChromeClient);
        assertFalse(webChromeClient.onProgressChangedCalled());
        startWebServer(false);
        String url = mWebServer.getAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        mWebView.loadUrl(url);
        new DelayedCheck(TEST_TIMEOUT) {
            @Override
            protected boolean check() {
                return webChromeClient.onProgressChangedCalled();
            }
        }.run();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "dispatchKeyEvent",
            args = {KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onAttachedToWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDetachedFromWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onChildViewAdded",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onChildViewRemoved",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDraw",
            args = {Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onFocusChanged",
            args = {boolean.class, int.class, Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onGlobalFocusChanged",
            args = {View.class, View.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onMeasure",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onScrollChanged",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onSizeChanged",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTouchEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onTrackballEvent",
            args = {MotionEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onWindowFocusChanged",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeScroll",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeHorizontalScrollRange",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            method = "computeVerticalScrollRange",
            args = {}
        )
    })
    public void testInternals() {
    }
    private static class MockWebView extends WebView {
        private boolean mHasCalledSetCertificate;
        public MockWebView(Context context) {
            super(context);
        }
        @Override
        public void setCertificate(SslCertificate certificate) {
            super.setCertificate(certificate);
            mHasCalledSetCertificate = true;
        }
        public void reset() {
            mHasCalledSetCertificate = false;
        }
        public boolean hasCalledSetCertificate() {
            return mHasCalledSetCertificate;
        }
    }
    private static class MockWebViewClient extends WebViewClient {
        private boolean mOnScaleChangedCalled = false;
        public boolean onScaleChangedCalled() {
            return mOnScaleChangedCalled;
        }
        @Override
        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            super.onScaleChanged(view, oldScale, newScale);
            mOnScaleChangedCalled = true;
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }
    }
    private static class MockWebChromeClient extends WebChromeClient {
        private boolean mOnProgressChanged = false;
        public boolean onProgressChangedCalled() {
            return mOnProgressChanged;
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mOnProgressChanged = true;
        }
    }
    private static class HrefCheckHandler extends Handler {
        private boolean mHadRecieved;
        private String mResultUrl;
        public HrefCheckHandler(Looper looper) {
            super(looper);
        }
        public boolean hasCalledHandleMessage() {
            return mHadRecieved;
        }
        public String getResultUrl() {
            return mResultUrl;
        }
        public void reset(){
            mResultUrl = null;
            mHadRecieved = false;
        }
        @Override
        public void handleMessage(Message msg) {
            mHadRecieved = true;
            mResultUrl = msg.getData().getString("url");
        }
    }
    private static class DocumentHasImageCheckHandler extends Handler {
        private boolean mReceived;
        private int mMsgArg1;
        public DocumentHasImageCheckHandler(Looper looper) {
            super(looper);
        }
        public boolean hasCalledHandleMessage() {
            return mReceived;
        }
        public int getMsgArg1() {
            return mMsgArg1;
        }
        public void reset(){
            mMsgArg1 = -1;
            mReceived = false;
        }
        @Override
        public void handleMessage(Message msg) {
            mReceived = true;
            mMsgArg1 = msg.arg1;
        }
    };
    private void findNextOnUiThread(final boolean forward) throws Throwable {
        runTestOnUiThread(new Runnable() {
            public void run() {
                mWebView.findNext(forward);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    private void moveFocusDown() throws Throwable {
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(500);
    }
    private boolean pageDownOnUiThread(final boolean bottom) throws Throwable {
        PageDownRunner runner = new PageDownRunner(bottom);
        runTestOnUiThread(runner);
        getInstrumentation().waitForIdleSync();
        return runner.mResult;
    }
    private class PageDownRunner implements Runnable {
        private boolean mResult, mBottom;
        public PageDownRunner(boolean bottom) {
            mBottom = bottom;
        }
        public void run() {
            mResult = mWebView.pageDown(mBottom);
        }
    }
    private boolean pageUpOnUiThread(final boolean top) throws Throwable {
        PageUpRunner runner = new PageUpRunner(top);
        runTestOnUiThread(runner);
        getInstrumentation().waitForIdleSync();
        return runner.mResult;
    }
    private class PageUpRunner implements Runnable {
        private boolean mResult, mTop;
        public PageUpRunner(boolean top) {
            this.mTop = top;
        }
        public void run() {
            mResult = mWebView.pageUp(mTop);
        }
    }
    private void delayedCheckStopScrolling() {
        new DelayedCheck() {
            private int scrollY = mWebView.getScrollY();
            @Override
            protected boolean check() {
                if (scrollY == mWebView.getScrollY()){
                    return true;
                } else {
                    scrollY = mWebView.getScrollY();
                    return false;
                }
            }
        }.run();
    }
    private void delayedCheckWebBackForwardList(final String currUrl, final int currIndex,
            final int size) {
        new DelayedCheck() {
            @Override
            protected boolean check() {
                WebBackForwardList list = mWebView.copyBackForwardList();
                return checkWebBackForwardList(list, currUrl, currIndex, size);
            }
        }.run();
    }
    private boolean checkWebBackForwardList(WebBackForwardList list, String currUrl,
            int currIndex, int size) {
        return (list != null)
                && (list.getSize() == size)
                && (list.getCurrentIndex() == currIndex)
                && list.getItemAtIndex(currIndex).getUrl().equals(currUrl);
    }
    private void assertGoBackOrForwardBySteps(boolean expected, int steps) {
        if (steps == 0)
            return;
        int start = steps > 0 ? 1 : steps;
        int end = steps > 0 ? steps : -1;
        for (int i = start; i <= end; i++) {
            assertEquals(expected, mWebView.canGoBackOrForward(i));
            if (i == 1) {
                assertEquals(expected, mWebView.canGoForward());
            } else if (i == -1) {
                assertEquals(expected, mWebView.canGoBack());
            }
        }
    }
    private void assertBitmapFillWithColor(Bitmap bitmap, int color) {
        for (int i = 0; i < bitmap.getWidth(); i ++)
            for (int j = 0; j < bitmap.getHeight(); j ++) {
                assertEquals(color, bitmap.getPixel(i, j));
            }
    }
    private boolean checkBitmapInsideAnother(Bitmap b1, Bitmap b2) {
        int w = b1.getWidth();
        int h = b1.getHeight();
        for (int i = 0; i < (b2.getWidth()-w+1); i++) {
            for (int j = 0; j < (b2.getHeight()-h+1); j++) {
                if (checkBitmapInsideAnother(b1, b2, i, j))
                    return true;
            }
        }
        return false;
    }
    private boolean comparePixel(int p1, int p2, int maxError) {
        int err;
        err = Math.abs(((p1&0xff000000)>>>24) - ((p2&0xff000000)>>>24));
        if (err > maxError)
            return false;
        err = Math.abs(((p1&0x00ff0000)>>>16) - ((p2&0x00ff0000)>>>16));
        if (err > maxError)
            return false;
        err = Math.abs(((p1&0x0000ff00)>>>8) - ((p2&0x0000ff00)>>>8));
        if (err > maxError)
            return false;
        err = Math.abs(((p1&0x000000ff)>>>0) - ((p2&0x000000ff)>>>0));
        if (err > maxError)
            return false;
        return true;
    }
    private boolean checkBitmapInsideAnother(Bitmap b1, Bitmap b2, int x, int y) {
        for (int i = 0; i < b1.getWidth(); i++)
            for (int j = 0; j < b1.getHeight(); j++) {
                if (!comparePixel(b1.getPixel(i, j), b2.getPixel(x + i, y + j), 10)) {
                    return false;
                }
            }
        return true;
    }
    private void assertLoadUrlSuccessfully(WebView webView, String url)
            throws InterruptedException {
        webView.loadUrl(url);
        waitForLoadComplete(webView, TEST_TIMEOUT);
    }
    private void waitForLoadComplete(final WebView webView, long timeout)
            throws InterruptedException {
        new DelayedCheck(timeout) {
            @Override
            protected boolean check() {
                return webView.getProgress() == 100;
            }
        }.run();
        Thread.sleep(TIME_FOR_LAYOUT);
    }
    private final class DummyJavaScriptInterface {
        private boolean mTitleChanged;
        private boolean hasChangedTitle() {
            return mTitleChanged;
        }
        public void onLoad(String oldTitle) {
            mWebView.getHandler().post(new Runnable() {
                public void run() {
                    mWebView.loadUrl("javascript:changeTitle(\"new title\")");
                    mTitleChanged = true;
                }
            });
        }
    }
    private final class MyDownloadListener implements DownloadListener {
        public String url;
        public String mimeType;
        public long contentLength;
        public String contentDisposition;
        public boolean called;
        public void onDownloadStart(String url, String userAgent, String contentDisposition,
                String mimetype, long contentLength) {
            this.called = true;
            this.url = url;
            this.mimeType = mimetype;
            this.contentLength = contentLength;
            this.contentDisposition = contentDisposition;
        }
    }
    private static class MyPictureListener implements PictureListener {
        public int callCount;
        public WebView webView;
        public Picture picture;
        public void onNewPicture(WebView view, Picture picture) {
            this.callCount += 1;
            this.webView = view;
            this.picture = picture;
        }
    }
}

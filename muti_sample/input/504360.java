@TestTargetClass(android.webkit.HttpAuthHandler.class)
public class HttpAuthHandlerTest extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static final long TIMEOUT = 10000;
    private WebView mWebView;
    private CtsTestServer mWebServer;
    public HttpAuthHandlerTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mWebView = getActivity().getWebView();
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
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "proceed",
            args = {String.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "useHttpAuthUsernamePassword() always returns true",
            method = "useHttpAuthUsernamePassword",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "This method is for internal use by the handler",
            method = "handleMessage",
            args = {android.os.Message.class}
        )
    })
    public void testProceed() throws Exception {
        mWebServer = new CtsTestServer(getActivity());
        String url = mWebServer.getAuthAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        MyWebViewClient client = new MyWebViewClient(true, "FakeUser", "FakePass");
        mWebView.setWebViewClient(client);
        assertLoadUrlSuccessfully(url);
        assertEquals(CtsTestServer.AUTH_REALM, client.realm);
        assertEquals(CtsTestServer.getReasonString(HttpStatus.SC_FORBIDDEN), mWebView.getTitle());
        assertTrue(client.useHttpAuthUsernamePassword);
        client = new MyWebViewClient(true, null, null);
        mWebView.setWebViewClient(client);
        assertLoadUrlSuccessfully(url);
        assertEquals(CtsTestServer.AUTH_REALM, client.realm);
        assertEquals(
                CtsTestServer.getReasonString(HttpStatus.SC_UNAUTHORIZED), mWebView.getTitle());
        assertTrue(client.useHttpAuthUsernamePassword);
        client = new MyWebViewClient(true, CtsTestServer.AUTH_USER, CtsTestServer.AUTH_PASS);
        mWebView.setWebViewClient(client);
        assertLoadUrlSuccessfully(url);
        assertEquals(CtsTestServer.AUTH_REALM, client.realm);
        assertEquals(TestHtmlConstants.HELLO_WORLD_TITLE, mWebView.getTitle());
        assertTrue(client.useHttpAuthUsernamePassword);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "cancel",
        args = {}
    )
    public void testCancel() throws Exception {
        mWebServer = new CtsTestServer(getActivity());
        String url = mWebServer.getAuthAssetUrl(TestHtmlConstants.HELLO_WORLD_URL);
        MyWebViewClient client = new MyWebViewClient(false, null, null);
        mWebView.setWebViewClient(client);
        assertLoadUrlSuccessfully(url);
        assertEquals(CtsTestServer.AUTH_REALM, client.realm);
        assertEquals(
                CtsTestServer.getReasonString(HttpStatus.SC_UNAUTHORIZED), mWebView.getTitle());
    }
    private void assertLoadUrlSuccessfully(String url) throws InterruptedException {
        mWebView.loadUrl(url);
        new DelayedCheck(TIMEOUT) {
            @Override
            protected boolean check() {
                return mWebView.getProgress() == 100;
            }
        }.run();
    }
    private static class MyWebViewClient extends WebViewClient {
        String realm;
        boolean useHttpAuthUsernamePassword;
        private boolean mProceed;
        private String mUser;
        private String mPassword;
        MyWebViewClient(boolean proceed, String user, String password) {
            mProceed = proceed;
            mUser = user;
            mPassword = password;
        }
        public void onReceivedHttpAuthRequest(WebView view,
                HttpAuthHandler handler, String host, String realm) {
            this.realm = realm;
            this.useHttpAuthUsernamePassword = handler.useHttpAuthUsernamePassword();
            if (mProceed) {
                handler.proceed(mUser, mPassword);
            } else {
                handler.cancel();
            }
        }
    }
}

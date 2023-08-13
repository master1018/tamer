public class JNIBindingsTestApp extends ActivityInstrumentationTestCase2<BrowserActivity> {
    private final static String TAG = "JNIBindingsTest";
    private static final int MSG_WEBKIT_DATA_READY = 101;
    private BrowserActivity mActivity = null;
    private Instrumentation mInst = null;
    private boolean mTestDone = false;
    private String mWebKitResult;
    private String mExpectedWebKitResult = "Running JNI Bindings test...\n" +
            "testPrimitiveTypes passed!\n" +
            "testObjectTypes passed!\n" +
            "testArray passed!\n" +
            "testObjectArray passed!\n" +
            "testObjectMembers passed!\n" +
            "testJSPrimitivesToStringsInJava passed!\n" +
            "testJavaReturnTypes passed!\n" +
            "getIfaceProperties passed!\n" +
            "testParameterTypeMismatch passed!\n";
    private class GetWebKitDataThread extends Thread {
        private JNIBindingsTestApp mTestApp;
        private WebView mWebView;
        private Handler mHandler;
        GetWebKitDataThread(JNIBindingsTestApp testApp, WebView webView) {
            mTestApp = testApp;
            mWebView = webView;
        }
        public void run() {
            Looper.prepare();
            mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_WEBKIT_DATA_READY: {
                            mTestApp.setWebKitResult((String)msg.obj);
                            Looper.myLooper().quit();
                        }
                        default: super.handleMessage(msg); break;
                    }
                }
            };
            mWebView.documentAsText(mHandler.obtainMessage(MSG_WEBKIT_DATA_READY));
            Looper.loop();
        }
    }
    public synchronized void setWebKitResult(String result) {
       mWebKitResult = result;
       notify();
    }
    public JNIBindingsTestApp() {
        super(BrowserActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInst = getInstrumentation();
        mInst.waitForIdleSync();
    }
    void setUpBrowser() {
        Tab tab = mActivity.getTabControl().getCurrentTab();
        WebView webView = tab.getWebView();
        webView.addJavascriptInterface(new JNIBindingsTest(this), "JNIBindingsTest");
        webView.setWebChromeClient(new TestWebChromeClient(webView.getWebChromeClient()) {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                    JsResult result) {
                String logMsg = String.format("JS Alert '%s' received from %s", message, url);
                Log.w(TAG, logMsg);
                result.confirm();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message,
                    JsResult result) {
                String logMsg = String.format("JS Confirmation '%s' received from %s",
                        message, url);
                Log.w(TAG, logMsg);
                result.confirm();
                return true;
            }
            @Override
            public boolean onJsPrompt(WebView view, String url, String message,
                    String defaultValue, JsPromptResult result) {
                String logMsg = String.format("JS Prompt '%s' received from %s; " +
                        "Giving default value '%s'", message, url, defaultValue);
                Log.w(TAG, logMsg);
                result.confirm(defaultValue);
                return true;
            }
        });
        webView.setWebViewClient(new TestWebViewClient(webView.getWebViewClient()) {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                String message = String.format("Error '%s' (%d) loading url: %s",
                        description, errorCode, failingUrl);
                Log.w(TAG, message);
            }
            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler,
                    SslError error) {
                Log.w(TAG, "SSL error: " + error);
                handler.proceed();
            }
        });
    }
    public synchronized void testComplete() {
        mTestDone = true;
        notify();
    }
    public void testJNIBindings() {
        setUpBrowser();
        Tab tab = mActivity.getTabControl().getCurrentTab();
        WebView webView = tab.getWebView();
        webView.loadUrl("file:
        synchronized(this) {
            while(!mTestDone) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        GetWebKitDataThread getWKData = new GetWebKitDataThread(this, webView);
        mWebKitResult = null;
        getWKData.start();
        synchronized(this) {
            while(mWebKitResult == null) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        Log.v(TAG, "WebKit result:");
        Log.v(TAG, mWebKitResult);
        assertEquals("Bindings test failed! See logcat for more details!", mExpectedWebKitResult,
                mWebKitResult);
    }
}

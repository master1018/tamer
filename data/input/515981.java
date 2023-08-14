@TestTargetClass(HitTestResult.class)
public class WebView_HitTestResultTest
        extends ActivityInstrumentationTestCase2<WebViewStubActivity> {
    private static long TEST_TIMEOUT = 5000L;
    private static long TIME_FOR_LAYOUT = 1000L;
    public WebView_HitTestResultTest() {
        super("com.android.cts.stub", WebViewStubActivity.class);
    }
    private void waitForLoading(final WebView webView, long timeout) throws InterruptedException {
        new DelayedCheck(timeout) {
            @Override
            protected boolean check() {
                return webView.getProgress() == 100;
            }
        }.run();
        Thread.sleep(TIME_FOR_LAYOUT);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getType",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExtra",
            args = {}
        )
    })
    public void testHitTestResult() throws InterruptedException {
        WebView webView = getActivity().getWebView();
        String anchor = "<p><a href=\"" + TestHtmlConstants.EXT_WEB_URL1
                + "\">normal anchor</a></p>";
        webView.loadDataWithBaseURL("fake:
                + "</body></html>", "text/html", "UTF-8", null);
        waitForLoading(webView, TEST_TIMEOUT);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        Thread.sleep(500);
        HitTestResult result = webView.getHitTestResult();
        assertEquals(HitTestResult.SRC_ANCHOR_TYPE, result.getType());
        assertEquals(TestHtmlConstants.EXT_WEB_URL1, result.getExtra());
    }
}

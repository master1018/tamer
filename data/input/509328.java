@TestTargetClass(WebViewTransport.class)
public class WebView_WebViewTransportTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setWebView",
            args = {WebView.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWebView",
            args = {}
        )
    })
    public void testAccessWebView() {
        WebView webView = new WebView(mContext);
        WebViewTransport transport = webView.new WebViewTransport();
        assertNull(transport.getWebView());
        transport.setWebView(webView);
        assertSame(webView, transport.getWebView());
    }
}

@TestTargetClass(android.webkit.SslErrorHandler.class)
public class SslErrorHandlerTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "cancel",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "handleMessage",
            args = {Message.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "WebViewClient.onReceivedSslError() is hidden. Cannot test.",
            method = "proceed",
            args = {}
        )
    })
    public void testSslErrorHandler() {
    }
}

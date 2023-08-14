public class WebChromeClient {
    public void onProgressChanged(WebView view, int newProgress) {}
    public void onReceivedTitle(WebView view, String title) {}
    public void onReceivedIcon(WebView view, Bitmap icon) {}
    public void onReceivedTouchIconUrl(WebView view, String url,
            boolean precomposed) {}
    public interface CustomViewCallback {
        public void onCustomViewHidden();
    }
    public void onShowCustomView(View view, CustomViewCallback callback) {};
    public void onHideCustomView() {}
    public boolean onCreateWindow(WebView view, boolean dialog,
            boolean userGesture, Message resultMsg) {
        return false;
    }
    public void onRequestFocus(WebView view) {}
    public void onCloseWindow(WebView window) {}
    public boolean onJsAlert(WebView view, String url, String message,
            JsResult result) {
        return false;
    }
    public boolean onJsConfirm(WebView view, String url, String message,
            JsResult result) {
        return false;
    }
    public boolean onJsPrompt(WebView view, String url, String message,
            String defaultValue, JsPromptResult result) {
        return false;
    }
    public boolean onJsBeforeUnload(WebView view, String url, String message,
            JsResult result) {
        return false;
    }
    public void onExceededDatabaseQuota(String url, String databaseIdentifier,
        long currentQuota, long estimatedSize, long totalUsedQuota,
        WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(currentQuota);
    }
    public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
            WebStorage.QuotaUpdater quotaUpdater) {
        quotaUpdater.updateQuota(0);
    }
    public void onGeolocationPermissionsShowPrompt(String origin,
            GeolocationPermissions.Callback callback) {}
    public void onGeolocationPermissionsHidePrompt() {}
    public boolean onJsTimeout() {
        return true;
    }
    @Deprecated
    public void onConsoleMessage(String message, int lineNumber, String sourceID) { }
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(),
                consoleMessage.sourceId());
        return false;
    }
    public Bitmap getDefaultVideoPoster() {
        return null;
    }
    public View getVideoLoadingProgressView() {
        return null;
    }
    public void getVisitedHistory(ValueCallback<String[]> callback) {
    }
    public void openFileChooser(ValueCallback<Uri> uploadFile) {
        uploadFile.onReceiveValue(null);
    }
}

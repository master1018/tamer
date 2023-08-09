abstract class TestWebChromeClient extends WebChromeClient {
    private WebChromeClient mWrappedClient;
    protected TestWebChromeClient(WebChromeClient wrappedClient) {
        mWrappedClient = wrappedClient;
    }
    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        mWrappedClient.onProgressChanged(view, newProgress);
    }
    @Override
    public void onReceivedTitle(WebView view, String title) {
        mWrappedClient.onReceivedTitle(view, title);
    }
    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        mWrappedClient.onReceivedIcon(view, icon);
    }
    @Override
    public void onReceivedTouchIconUrl(WebView view, String url,
            boolean precomposed) {
        mWrappedClient.onReceivedTouchIconUrl(view, url, precomposed);
    }
    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        mWrappedClient.onShowCustomView(view, callback);
    }
    @Override
    public void onHideCustomView() {
        mWrappedClient.onHideCustomView();
    }
    @Override
    public boolean onCreateWindow(WebView view, boolean dialog,
            boolean userGesture, Message resultMsg) {
        return mWrappedClient.onCreateWindow(view, dialog, userGesture, resultMsg);
    }
    @Override
    public void onRequestFocus(WebView view) {
        mWrappedClient.onRequestFocus(view);
    }
    @Override
    public void onCloseWindow(WebView window) {
        mWrappedClient.onCloseWindow(window);
    }
    @Override
    public boolean onJsAlert(WebView view, String url, String message,
            JsResult result) {
        return mWrappedClient.onJsAlert(view, url, message, result);
    }
    @Override
    public boolean onJsConfirm(WebView view, String url, String message,
            JsResult result) {
        return mWrappedClient.onJsConfirm(view, url, message, result);
    }
    @Override
    public boolean onJsPrompt(WebView view, String url, String message,
            String defaultValue, JsPromptResult result) {
        return mWrappedClient.onJsPrompt(view, url, message, defaultValue, result);
    }
    @Override
    public boolean onJsBeforeUnload(WebView view, String url, String message,
            JsResult result) {
        return mWrappedClient.onJsBeforeUnload(view, url, message, result);
    }
    @Override
    public void onExceededDatabaseQuota(String url, String databaseIdentifier,
            long currentQuota, long estimatedSize, long totalUsedQuota,
            WebStorage.QuotaUpdater quotaUpdater) {
        mWrappedClient.onExceededDatabaseQuota(url, databaseIdentifier, currentQuota,
                estimatedSize, totalUsedQuota, quotaUpdater);
    }
    @Override
    public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota,
            WebStorage.QuotaUpdater quotaUpdater) {
        mWrappedClient.onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota, quotaUpdater);
    }
    @Override
    public void onGeolocationPermissionsShowPrompt(String origin,
            GeolocationPermissions.Callback callback) {
        mWrappedClient.onGeolocationPermissionsShowPrompt(origin, callback);
    }
    @Override
    public void onGeolocationPermissionsHidePrompt() {
        mWrappedClient.onGeolocationPermissionsHidePrompt();
    }
    @Override
    public boolean onJsTimeout() {
        return mWrappedClient.onJsTimeout();
    }
    @Override
    @Deprecated
    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
        mWrappedClient.onConsoleMessage(message, lineNumber, sourceID);
    }
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        return mWrappedClient.onConsoleMessage(consoleMessage);
    }
    @Override
    public Bitmap getDefaultVideoPoster() {
        return mWrappedClient.getDefaultVideoPoster();
    }
    @Override
    public View getVideoLoadingProgressView() {
        return mWrappedClient.getVideoLoadingProgressView();
    }
    @Override
    public void getVisitedHistory(ValueCallback<String[]> callback) {
        mWrappedClient.getVisitedHistory(callback);
    }
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadFile) {
        mWrappedClient.openFileChooser(uploadFile);
    }
}

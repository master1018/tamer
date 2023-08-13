abstract class TestWebViewClient extends WebViewClient {
  private WebViewClient mWrappedClient;
  protected TestWebViewClient(WebViewClient wrappedClient) {
    mWrappedClient = wrappedClient;
  }
  @Override
  public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return mWrappedClient.shouldOverrideUrlLoading(view, url);
  }
  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    mWrappedClient.onPageStarted(view, url, favicon);
  }
  @Override
  public void onPageFinished(WebView view, String url) {
    mWrappedClient.onPageFinished(view, url);
  }
  @Override
  public void onLoadResource(WebView view, String url) {
    mWrappedClient.onLoadResource(view, url);
  }
  @Deprecated
  @Override
  public void onTooManyRedirects(WebView view, Message cancelMsg,
          Message continueMsg) {
      mWrappedClient.onTooManyRedirects(view, cancelMsg, continueMsg);
  }
  @Override
  public void onReceivedError(WebView view, int errorCode,
          String description, String failingUrl) {
    mWrappedClient.onReceivedError(view, errorCode, description, failingUrl);
  }
  @Override
  public void onFormResubmission(WebView view, Message dontResend,
          Message resend) {
    mWrappedClient.onFormResubmission(view, dontResend, resend);
  }
  @Override
  public void doUpdateVisitedHistory(WebView view, String url,
          boolean isReload) {
    mWrappedClient.doUpdateVisitedHistory(view, url, isReload);
  }
  @Override
  public void onReceivedSslError(WebView view, SslErrorHandler handler,
          SslError error) {
      mWrappedClient.onReceivedSslError(view, handler, error);
  }
  @Override
  public void onReceivedHttpAuthRequest(WebView view,
          HttpAuthHandler handler, String host, String realm) {
      mWrappedClient.onReceivedHttpAuthRequest(view, handler, host, realm);
  }
  @Override
  public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
      return mWrappedClient.shouldOverrideKeyEvent(view, event);
  }
  @Override
  public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
    mWrappedClient.onUnhandledKeyEvent(view, event);
  }
  @Override
  public void onScaleChanged(WebView view, float oldScale, float newScale) {
    mWrappedClient.onScaleChanged(view, oldScale, newScale);
  }
}

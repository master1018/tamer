class PluginFullScreenHolder extends Dialog {
    private final WebView mWebView;
    private final int mNpp;
    private View mContentView;
    PluginFullScreenHolder(WebView webView, int npp) {
        super(webView.getContext(), android.R.style.Theme_NoTitleBar_Fullscreen);
        mWebView = webView;
        mNpp = npp;
    }
    @Override
    public void setContentView(View contentView) {
        contentView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        if (contentView instanceof SurfaceView) {
            final SurfaceView sView = (SurfaceView) contentView;
            if (sView.isFixedSize()) {
                sView.getHolder().setSizeFromLayout();
            }
        }
        super.setContentView(contentView);
        mContentView = contentView;
    }
    @Override
    public void onBackPressed() {
        mWebView.mPrivateHandler.obtainMessage(WebView.HIDE_FULLSCREEN)
                .sendToTarget();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isSystem()) {
            return super.onKeyDown(keyCode, event);
        }
        mWebView.onKeyDown(keyCode, event);
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.isSystem()) {
            return super.onKeyUp(keyCode, event);
        }
        mWebView.onKeyUp(keyCode, event);
        return true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        mWebView.onTrackballEvent(event);
        return true;
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mContentView != null && mContentView.getParent() != null) {
            ViewGroup vg = (ViewGroup) mContentView.getParent();
            vg.removeView(mContentView);
        }
        mWebView.getWebViewCore().sendMessage(
                WebViewCore.EventHub.HIDE_FULLSCREEN, mNpp, 0);
    }
}

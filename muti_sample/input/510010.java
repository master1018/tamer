public class SettingsSafetyLegalActivity extends AlertActivity 
        implements DialogInterface.OnCancelListener, DialogInterface.OnClickListener {
    private static final String PROPERTY_LSAFETYLEGAL_URL = "ro.url.safetylegal";
    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String userSafetylegalUrl = SystemProperties.get(PROPERTY_LSAFETYLEGAL_URL);
        final Configuration configuration = getResources().getConfiguration();
        final String language = configuration.locale.getLanguage();
        final String country = configuration.locale.getCountry();
        String loc = String.format("locale=%s-%s", language, country);
        userSafetylegalUrl = String.format("%s&%s", userSafetylegalUrl, loc);
        mWebView = new WebView(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        if (savedInstanceState == null) {
            mWebView.loadUrl(userSafetylegalUrl);
        } else {
            mWebView.restoreState(savedInstanceState);
        }
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mAlert.setTitle(getString(R.string.settings_safetylegal_activity_title));
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                showErrorAndFinish(failingUrl);
            }
        });
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.settings_safetylegal_activity_loading);
        p.mView = mWebView;
        p.mForceInverseBackground = true;
        setupAlert();
    }
    private void showErrorAndFinish(String url) {
        new AlertDialog.Builder(this)
                .setMessage(getResources()
                        .getString(R.string.settings_safetylegal_activity_unreachable, url))
                .setTitle(R.string.settings_safetylegal_activity_title)
                .setPositiveButton(android.R.string.ok, this)
                .setOnCancelListener(this)
                .setCancelable(true)
                .show();
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK 
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }
    public void onClick(DialogInterface dialog, int whichButton) {
        finish();
    }
    public void onCancel(DialogInterface dialog) {
        finish();
    }
    @Override
    public void onSaveInstanceState(Bundle icicle) {
        mWebView.saveState(icicle);
        super.onSaveInstanceState(icicle);
    }
}

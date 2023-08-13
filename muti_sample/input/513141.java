public class HTMLViewerActivity extends Activity {
    private WebView mWebView;
    static final int MAXFILESIZE = 8096;
    static final String LOGTAG = "HTMLViewerActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieSyncManager.createInstance(this);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        mWebView = new WebView(this);
        setContentView(mWebView);
        mWebView.setWebChromeClient( new WebChrome() );
        WebSettings s = mWebView.getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        s.setUseWideViewPort(true);
        s.setSavePassword(false);
        s.setSaveFormData(false);
        s.setBlockNetworkLoads(true);
        s.setJavaScriptEnabled(false);
        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            Intent intent = getIntent();
            if (intent.getData() != null) {
                Uri uri = intent.getData();
                String contentUri = "file".equals(uri.getScheme())
                        ? FileContentProvider.BASE_URI + uri.getEncodedPath()
                        : uri.toString();
                String intentType = intent.getType();
                if (intentType != null) {
                    contentUri += "?" + intentType;
                }
                mWebView.loadUrl(contentUri);
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync(); 
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mWebView.saveState(outState);
    }
    @Override
    protected void onStop() {
        super.onStop();
        CookieSyncManager.getInstance().stopSync(); 
        mWebView.stopLoading();       
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }
    class WebChrome extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            HTMLViewerActivity.this.setTitle(title);
        }
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            getWindow().setFeatureInt(
                    Window.FEATURE_PROGRESS, newProgress*100);
            if (newProgress == 100) {
                CookieSyncManager.getInstance().sync();
            }
        }
    }
}

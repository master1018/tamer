public class CookieSyncManagerStubActivity extends Activity {
    private WebView mWebView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CookieSyncManager.createInstance(this);
        mWebView = new WebView(this);
        setContentView(mWebView);
    }
    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }
    @Override
    protected void onStop() {
        super.onStop();
        CookieSyncManager.getInstance().stopSync();
    }
    public WebView getWebView(){
        return mWebView;
    }
}

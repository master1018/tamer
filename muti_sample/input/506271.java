public class UninstallAppProgress extends Activity implements OnClickListener {
    private final String TAG="UninstallAppProgress";
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo;
    private TextView mStatusTextView;
    private Button mOkButton;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private volatile int mResultCode = -1;
    private final int UNINSTALL_COMPLETE = 1;
    public final static int SUCCEEDED=1;
    public final static int FAILED=0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UNINSTALL_COMPLETE:
                    mResultCode = msg.arg1;
                    if (msg.arg1 == SUCCEEDED) {
                        mStatusTextView.setText(R.string.uninstall_done);
                    } else {
                        mStatusTextView.setText(R.string.uninstall_failed);
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mOkPanel.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra(PackageUtil.INTENT_ATTR_APPLICATION_INFO);
        initView();
    }
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(boolean succeeded) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = succeeded?SUCCEEDED:FAILED;
            mHandler.sendMessage(msg);
        }
    }
    void setResultAndFinish(int retCode) {
        setResult(retCode);
        finish();
    }
    public void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.uninstall_progress);
        PackageUtil.initSnippetForInstalledApp(this, mAppInfo, R.id.app_snippet);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.uninstalling);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        mOkPanel = (View)findViewById(R.id.ok_panel);
        mOkButton = (Button)findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(this);
        mOkPanel.setVisibility(View.INVISIBLE);
        PackageDeleteObserver observer = new PackageDeleteObserver();
        getPackageManager().deletePackage(mAppInfo.packageName, observer, 0);
    }
    public void onClick(View v) {
        if(v == mOkButton) {
            Log.i(TAG, "Finished uninstalling pkg: " + mAppInfo.packageName);
            setResultAndFinish(mResultCode);
        }
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mResultCode == -1) {
                return true;
            } else {
                setResult(mResultCode);
            }
        }
        return super.dispatchKeyEvent(ev);
    }
}

public class IccPanel extends Dialog {
    protected static final String TAG = PhoneApp.LOG_TAG;
    private StatusBarManager mStatusBarManager;
    public IccPanel(Context context) {
        super(context, R.style.IccPanel);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window winP = getWindow();
        winP.setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        winP.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        winP.setGravity(Gravity.CENTER);
        PhoneApp app = PhoneApp.getInstance();
        mStatusBarManager = (StatusBarManager) app.getSystemService(Context.STATUS_BAR_SERVICE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        mStatusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
    }
    @Override
    public void onStop() {
        super.onStop();
        mStatusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

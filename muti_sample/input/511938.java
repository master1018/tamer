public class WindowStubActivity extends Activity {
    private static boolean mIsOnCreateOptionsMenuCalled;
    private static boolean mIsOnOptionsMenuClosedCalled;
    private static boolean mIsOnKeyDownCalled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.windowstub_layout);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Quit").setAlphabeticShortcut('q');
        menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Action").setAlphabeticShortcut('a');
        mIsOnCreateOptionsMenuCalled = true;
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        mIsOnOptionsMenuClosedCalled = true;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mIsOnKeyDownCalled = true;
        return super.onKeyDown(keyCode, event);
    }
    public boolean isOnCreateOptionsMenuCalled() {
        return mIsOnCreateOptionsMenuCalled;
    }
    public boolean isOnOptionsMenuClosedCalled() {
        return mIsOnOptionsMenuClosedCalled;
    }
    public boolean isOnKeyDownCalled() {
        return mIsOnKeyDownCalled;
    }
    public void setFlagFalse() {
        mIsOnCreateOptionsMenuCalled = false;
        mIsOnOptionsMenuClosedCalled = false;
    }
}

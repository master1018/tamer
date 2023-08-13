public class AppStubActivity extends Activity {
    private Dialog mDialog;
    public boolean mOnPrepareDialog;
    public boolean mOnOptionsMenuClosedCalled;
    public boolean mOnPrepareOptionsMenuCalled;
    public boolean mOnOptionsItemSelectedCalled;
    public boolean mOnCreateOptionsMenu;
    public boolean mIndterminate = false;
    public boolean mIndterminatevisibility = false;
    public boolean mSecPro = false;
    public boolean mOnContextItemSelectedCalled;
    public boolean mOnCreateContextMenu;
    public boolean mApplyResourceCalled;
    public boolean mCreateContextMenuCalled;
    public boolean mRequestWinFeatureRet = false;
    public AppStubActivity() {
    }
    public void finalize() {
        try {
            super.finalize();
        } catch (Throwable exception) {
            System.err.print("exception!");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mRequestWinFeatureRet = requestWindowFeature(1);
        setContentView(R.layout.app_activity);
    }
    public Dialog getDialogById(int id) {
        return mDialog;
    }
    @Override
    public Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);
        mDialog = new Dialog(this);
        return mDialog;
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        mOnPrepareDialog = true;
    }
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        mOnOptionsMenuClosedCalled = true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mOnPrepareOptionsMenuCalled = true;
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOnCreateOptionsMenu = true;
        if(menu != null)
            menu.add(0, 0, 0, "Fake Item");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mOnOptionsItemSelectedCalled = true;
        return super.onOptionsItemSelected(item);
    }
    public boolean setProBarIndeterminate(boolean indeterminate){
        mIndterminate = indeterminate;
        super.setProgressBarIndeterminate(indeterminate);
        return mIndterminate;
    }
    public boolean setProBarIndeterminateVisibility(boolean visible){
        mIndterminatevisibility = visible;
        super.setProgressBarIndeterminateVisibility(visible);
        return mIndterminatevisibility;
    }
    public boolean setSecPro(int secPro){
        mSecPro = true;
        super.setSecondaryProgress(secPro);
        return mSecPro;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        mOnContextItemSelectedCalled = true;
        return super.onContextItemSelected(item);
    }
    @Override
    public void onApplyThemeResource( Resources.Theme theme,
                                      int resid,
                                      boolean first){
        super.onApplyThemeResource(theme,resid,first);
        mApplyResourceCalled = true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu,v,menuInfo);
        mCreateContextMenuCalled = true;
    }
}

public class LauncherActivityStub extends LauncherActivity {
    public boolean isOnCreateCalled = false;
    public boolean isOnListItemClick = false;
    private Intent mSuperIntent;
    public Intent getSuperIntent() {
        return mSuperIntent;
    }
    @Override
    protected Intent getTargetIntent() {
        mSuperIntent = super.getTargetIntent();
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory(Intent.CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return targetIntent;
    }
    @Override
    protected Intent intentForPosition(int position) {
        return super.intentForPosition(position);
    }
    public LauncherActivityStub() {
        super();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isOnCreateCalled = true;
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        isOnListItemClick = true;
    }
}

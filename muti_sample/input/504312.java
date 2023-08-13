public class MockTabActivity extends TabActivity {
    private static final String TAB1 = "tab1";
    private static final String TAB2 = "tab2";
    private static final String TAB3 = "tab3";
    public boolean isOnChildTitleChangedCalled;
    public boolean isOnPostCreateCalled;
    public boolean isOnSaveInstanceStateCalled;
    public boolean isOnContentChangedCalled;
    public static boolean isOnRestoreInstanceStateCalled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TabHost tabHost = getTabHost();
        tabHost.addTab(tabHost.newTabSpec(TAB1).setIndicator(TAB1)
                .setContent(new Intent(this, ChildTabActivity.class)));
        tabHost.addTab(tabHost.newTabSpec(TAB2).setIndicator(TAB2)
                .setContent(new Intent(this, MockActivity.class)));
        tabHost.addTab(tabHost.newTabSpec(TAB3).setIndicator(TAB3).setContent(
                new Intent(this, AppStubActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
    @Override
    protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
        super.onChildTitleChanged(childActivity, title);
        isOnChildTitleChangedCalled = true;
    }
    @Override
    protected void onPostCreate(Bundle icicle) {
        super.onPostCreate(icicle);
        isOnPostCreateCalled = true;
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        isOnRestoreInstanceStateCalled = true;
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        isOnSaveInstanceStateCalled = true;
    }
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        isOnContentChangedCalled = true;
    }
}

@TestTargetClass(TabSpec.class)
public class TabHost_TabSpecTest extends ActivityInstrumentationTestCase2<TabHostStubActivity> {
    private static final String TAG_TAB2 = "tab 2";
    private TabHost mTabHost;
    private TabHostStubActivity mActivity;
    public TabHost_TabSpecTest() {
        super("com.android.cts.stub", TabHostStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mTabHost = mActivity.getTabHost();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIndicator",
        args = {java.lang.CharSequence.class}
    )
    @UiThreadTest
    public void testSetIndicator1() {
        TabSpec tabSpec = mTabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2).setContent(new MockTabContentFactoryText());
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(1);
        View currentTabView = mTabHost.getCurrentTabView();
        int idTitle = com.android.internal.R.id.title;
        TextView tvTitle = (TextView) currentTabView.findViewById(idTitle);
        assertEquals(TAG_TAB2, tvTitle.getText().toString());
        tabSpec = mTabHost.newTabSpec("tab 3");
        tabSpec.setIndicator((CharSequence)null).setContent(new MockTabContentFactoryList());
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(2);
        currentTabView = mTabHost.getCurrentTabView();
        tvTitle = (TextView) currentTabView.findViewById(idTitle);
        assertEquals("", tvTitle.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setIndicator",
        args = {java.lang.CharSequence.class, android.graphics.drawable.Drawable.class}
    )
    @UiThreadTest
    public void testSetIndicator2() {
        TabSpec tabSpec = mTabHost.newTabSpec(TAG_TAB2);
        Drawable d = new ColorDrawable(Color.GRAY);
        tabSpec.setIndicator(TAG_TAB2, d);
        tabSpec.setContent(new MockTabContentFactoryText());
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(1);
        View currentTabView = mTabHost.getCurrentTabView();
        int idTitle = com.android.internal.R.id.title;
        int idIcon = com.android.internal.R.id.icon;
        TextView tvTitle = (TextView) currentTabView.findViewById(idTitle);
        ImageView ivIcon = ((ImageView) currentTabView.findViewById(idIcon));
        assertEquals(TAG_TAB2, tvTitle.getText().toString());
        assertSame(d, ivIcon.getDrawable());
        tabSpec = mTabHost.newTabSpec("tab 3");
        tabSpec.setIndicator(null, d);
        tabSpec.setContent(new MockTabContentFactoryList());
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(2);
        currentTabView = mTabHost.getCurrentTabView();
        tvTitle = (TextView) currentTabView.findViewById(idTitle);
        ivIcon = ((ImageView) currentTabView.findViewById(idIcon));
        assertEquals("", tvTitle.getText().toString());
        assertSame(d, ivIcon.getDrawable());
        tabSpec = mTabHost.newTabSpec("tab 4");
        tabSpec.setIndicator(null, null);
        tabSpec.setContent(new MockTabContentFactoryList());
        mTabHost.addTab(tabSpec);
        mTabHost.setCurrentTab(3);
        currentTabView = mTabHost.getCurrentTabView();
        tvTitle = (TextView) currentTabView.findViewById(idTitle);
        ivIcon = ((ImageView) currentTabView.findViewById(idIcon));
        assertEquals("", tvTitle.getText().toString());
        assertNull(ivIcon.getDrawable());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setContent",
        args = {int.class}
    )
    @UiThreadTest
    public void testSetContent1() {
        TabSpec tabSpec2 = mTabHost.newTabSpec("tab spec 2");
        tabSpec2.setIndicator("tab 2");
        tabSpec2.setContent(com.android.cts.stub.R.id.tabhost_textview);
        mTabHost.addTab(tabSpec2);
        mTabHost.setCurrentTab(1);
        TextView currentView = (TextView) mTabHost.getCurrentView();
        assertEquals(mActivity.getResources().getString(R.string.hello_world),
                currentView.getText().toString());
        TabSpec tabSpec3 = mTabHost.newTabSpec("tab spec 3");
        tabSpec3.setIndicator("tab 3");
        tabSpec3.setContent(com.android.cts.stub.R.id.tabhost_listview);
        mTabHost.addTab(tabSpec3);
        mTabHost.setCurrentTab(2);
        assertTrue(mTabHost.getCurrentView() instanceof ListView);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setContent",
        args = {android.widget.TabHost.TabContentFactory.class}
    )
    @UiThreadTest
    public void testSetContent2() {
        TabSpec tabSpec2 = mTabHost.newTabSpec("tab spec 2");
        tabSpec2.setIndicator("tab 2");
        tabSpec2.setContent(new MockTabContentFactoryText());
        mTabHost.addTab(tabSpec2);
        mTabHost.setCurrentTab(1);
        TextView currentView = (TextView) mTabHost.getCurrentView();
        assertEquals("tab spec 2", currentView.getText().toString());
        TabSpec tabSpec3 = mTabHost.newTabSpec("tab spec 3");
        tabSpec3.setIndicator("tab 3");
        tabSpec3.setContent(new MockTabContentFactoryList());
        mTabHost.addTab(tabSpec3);
        mTabHost.setCurrentTab(2);
        assertTrue(mTabHost.getCurrentView() instanceof ListView);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        method = "setContent",
        args = {android.content.Intent.class}
    )
    public void testSetContent3() {
        Uri uri = Uri.parse("ctstest:
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                TabSpec tabSpec = mTabHost.newTabSpec("tab spec");
                tabSpec.setIndicator("tab");
                tabSpec.setContent(intent);
                mTabHost.addTab(tabSpec);
                mTabHost.setCurrentTab(1);
            }
        });
        Instrumentation instrumentation = getInstrumentation();
        ActivityMonitor am = instrumentation.addMonitor(MockURLSpanTestActivity.class.getName(),
                null, false);
        Activity newActivity = am.waitForActivityWithTimeout(5000);
        assertNotNull(newActivity);
        newActivity.finish();
    }
    private class MockTabContentFactoryText implements TabHost.TabContentFactory {
        public View createTabContent(String tag) {
            final TextView tv = new TextView(mActivity);
            tv.setText(tag);
            return tv;
        }
    }
    private class MockTabContentFactoryList implements TabHost.TabContentFactory {
        public View createTabContent(String tag) {
            final ListView lv = new ListView(mActivity);
            return lv;
        }
    }
}

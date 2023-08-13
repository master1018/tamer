@TestTargetClass(TabHost.class)
public class TabHostTest extends ActivityInstrumentationTestCase2<TabHostStubActivity> {
    private static final String TAG_TAB1 = "tab 1";
    private static final String TAG_TAB2 = "tab 2";
    private static final int TAB_HOST_ID = android.R.id.tabhost;
    private TabHostStubActivity mActivity;
    public TabHostTest() {
        super("com.android.cts.stub", TabHostStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabHost",
            args = {android.content.Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "TabHost",
            args = {android.content.Context.class, android.util.AttributeSet.class}
        )
    })
    public void testConstructor() {
        new TabHost(mActivity);
        new TabHost(mActivity, null);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "newTabSpec",
        args = {java.lang.String.class}
    )
    public void testNewTabSpec() {
        TabHost tabHost = new TabHost(mActivity);
        assertNotNull(tabHost.newTabSpec(TAG_TAB2));
        assertNotNull(tabHost.newTabSpec(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test setup()",
        method = "setup",
        args = {}
    )
    public void testSetup1() throws Throwable {
        final Activity activity = launchActivity("com.android.cts.stub", StubActivity.class, null);
        runTestOnUiThread(new Runnable() {
            public void run() {
                activity.setContentView(R.layout.tabhost_layout);
                TabHost tabHost = (TabHost) activity.findViewById(TAB_HOST_ID);
                assertNull(tabHost.getTabWidget());
                assertNull(tabHost.getTabContentView());
                tabHost.setup();
                assertNotNull(tabHost.getTabWidget());
                assertNotNull(tabHost.getTabContentView());
                TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB1);
                tabSpec.setIndicator(TAG_TAB1);
                tabSpec.setContent(new MyTabContentFactoryList());
                tabHost.addTab(tabSpec);
                tabHost.setCurrentTab(0);
            }
        });
        getInstrumentation().waitForIdleSync();
        activity.finish();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setup",
        args = {android.app.LocalActivityManager.class}
    )
    public void testSetup2() throws Throwable {
        final ActivityGroup activity = launchActivity("com.android.cts.stub",
                ActivityGroup.class, null);
        runTestOnUiThread(new Runnable() {
            public void run() {
                activity.setContentView(R.layout.tabhost_layout);
                TabHost tabHost = (TabHost) activity.findViewById(TAB_HOST_ID);
                assertNull(tabHost.getTabWidget());
                assertNull(tabHost.getTabContentView());
                tabHost.setup(activity.getLocalActivityManager());
                assertNotNull(tabHost.getTabWidget());
                assertNotNull(tabHost.getTabContentView());
                TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB1);
                tabSpec.setIndicator(TAG_TAB1);
                Intent intent = new Intent(Intent.ACTION_VIEW, null,
                        mActivity, StubActivity.class);
                tabSpec.setContent(intent);
                tabHost.addTab(tabSpec);
                tabHost.setCurrentTab(0);
            }
        });
        getInstrumentation().waitForIdleSync();
        activity.finish();
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onTouchModeChanged",
        args = {boolean.class}
    )
    public void testOnTouchModeChanged() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addTab",
        args = {android.widget.TabHost.TabSpec.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "the javadoc for addTab() is incomplete." +
            "1. not clear what is supposed to happen if tabSpec is null." +
            "2. no description about the IllegalArgumentException thrown from this method.")
    @UiThreadTest
    public void testAddTab() {
        TabHost tabHost = mActivity.getTabHost();
        assertEquals(1, tabHost.getTabWidget().getChildCount());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryList());
        tabHost.addTab(tabSpec);
        assertEquals(2, tabHost.getTabWidget().getChildCount());
        tabHost.setCurrentTab(1);
        assertTrue(tabHost.getCurrentView() instanceof ListView);
        assertEquals(TAG_TAB2, tabHost.getCurrentTabTag());
        try {
            tabHost.addTab(tabHost.newTabSpec("tab 3"));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            tabHost.addTab(tabHost.newTabSpec("tab 3").setIndicator("tab 3"));
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            tabHost.addTab(null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.NOT_FEASIBLE,
        method = "clearAllTabs",
        args = {}
    )
    @ToBeFixed(explanation = "clearAllTabs() cannot be called on a TabHost that's currently being"
            + " displayed. After doing so, NPE is thrown in the UI thread and the process dies.")
    @UiThreadTest
    public void testClearAllTabs() {
        TabHost tabHost = mActivity.getTabHost();
        MyTabContentFactoryText tcf = new MyTabContentFactoryText();
        tabHost.addTab(tabHost.newTabSpec(TAG_TAB1).setIndicator(TAG_TAB1).setContent(tcf));
        tabHost.addTab(tabHost.newTabSpec(TAG_TAB2).setIndicator(TAG_TAB2).setContent(tcf));
        assertEquals(3, tabHost.getTabWidget().getChildCount());
        assertEquals(3, tabHost.getTabContentView().getChildCount());
        assertEquals(0, tabHost.getCurrentTab());
        assertNotNull(tabHost.getCurrentView());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTabWidget",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testGetTabWidget() {
        TabHost tabHost = mActivity.getTabHost();
        assertEquals(android.R.id.tabs, tabHost.getTabWidget().getId());
        WidgetTestUtils.assertScaledPixels(1, tabHost.getTabWidget().getPaddingLeft(),
                getActivity());
        WidgetTestUtils.assertScaledPixels(1, tabHost.getTabWidget().getPaddingRight(),
                getActivity());
        WidgetTestUtils.assertScaledPixels(4, tabHost.getTabWidget().getPaddingTop(),
                getActivity());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getCurrentTab",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setCurrentTab",
            args = {int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    @UiThreadTest
    public void testAccessCurrentTab() {
        TabHost tabHost = mActivity.getTabHost();
        assertEquals(0, tabHost.getCurrentTab());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryText());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);
        assertEquals(1, tabHost.getCurrentTab());
        tabHost.setCurrentTab(0);
        assertEquals(0, tabHost.getCurrentTab());
        tabHost.setCurrentTab(tabHost.getTabWidget().getChildCount() + 1);
        assertEquals(0, tabHost.getCurrentTab());
        tabHost.setCurrentTab(-1);
        assertEquals(0, tabHost.getCurrentTab());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCurrentTabView",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    @UiThreadTest
    public void testGetCurrentTabView() {
        TabHost tabHost = mActivity.getTabHost();
        assertSame(tabHost.getTabWidget().getChildAt(0), tabHost.getCurrentTabView());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryText());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);
        assertSame(tabHost.getTabWidget().getChildAt(1), tabHost.getCurrentTabView());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getCurrentView",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    @UiThreadTest
    public void testGetCurrentView() {
        TabHost tabHost = mActivity.getTabHost();
        TextView textView = (TextView) tabHost.getCurrentView();
        assertEquals(TabHostStubActivity.INITIAL_VIEW_TEXT, textView.getText().toString());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryList());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);
        assertTrue(tabHost.getCurrentView() instanceof ListView);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setCurrentTabByTag",
        args = {java.lang.String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    @UiThreadTest
    public void testSetCurrentTabByTag() {
        TabHost tabHost = mActivity.getTabHost();
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryText());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTabByTag(TAG_TAB2);
        assertEquals(1, tabHost.getCurrentTab());
        tabHost.setCurrentTabByTag(TabHostStubActivity.INITIAL_TAB_TAG);
        assertEquals(0, tabHost.getCurrentTab());
        tabHost.setCurrentTabByTag(null);
        assertEquals(0, tabHost.getCurrentTab());
        tabHost.setCurrentTabByTag("unknown tag");
        assertEquals(0, tabHost.getCurrentTab());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTabContentView",
        args = {}
    )
    @UiThreadTest
    public void testGetTabContentView() {
        TabHost tabHost = mActivity.getTabHost();
        assertEquals(3, tabHost.getTabContentView().getChildCount());
        TextView child0 = (TextView) tabHost.getTabContentView().getChildAt(0);
        assertEquals(mActivity.getResources().getString(R.string.hello_world),
                child0.getText().toString());
        assertTrue(tabHost.getTabContentView().getChildAt(1) instanceof ListView);
        TextView child2 = (TextView) tabHost.getTabContentView().getChildAt(2);
        tabHost.setCurrentTab(0);
        assertEquals(TabHostStubActivity.INITIAL_VIEW_TEXT, child2.getText().toString());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryList());
        tabHost.addTab(tabSpec);
        assertEquals(3, tabHost.getTabContentView().getChildCount());
        tabHost.setCurrentTab(1);
        assertEquals(4, tabHost.getTabContentView().getChildCount());
        child0 = (TextView) tabHost.getTabContentView().getChildAt(0);
        assertEquals(mActivity.getResources().getString(R.string.hello_world),
                child0.getText().toString());
        assertTrue(tabHost.getTabContentView().getChildAt(1) instanceof ListView);
        child2 = (TextView) tabHost.getTabContentView().getChildAt(2);
        tabHost.setCurrentTab(0);
        assertEquals(TabHostStubActivity.INITIAL_VIEW_TEXT, child2.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "dispatchKeyEvent",
        args = {android.view.KeyEvent.class}
    )
    @UiThreadTest
    public void testDispatchKeyEvent() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "dispatchWindowFocusChanged",
        args = {boolean.class}
    )
    @UiThreadTest
    public void testDispatchWindowFocusChanged() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setOnTabChangedListener",
        args = {android.widget.TabHost.OnTabChangeListener.class}
    )
    @UiThreadTest
    public void testSetOnTabChangedListener() {
        TabHost tabHost = mActivity.getTabHost();
        MockOnTabChangeListener listener = new MockOnTabChangeListener();
        tabHost.setOnTabChangedListener(listener);
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryList());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);
        assertTrue(listener.hasCalledOnTabChanged());
        listener.reset();
        tabHost.setCurrentTab(0);
        assertTrue(listener.hasCalledOnTabChanged());
        listener.reset();
        tabHost.setCurrentTab(0);
        assertFalse(listener.hasCalledOnTabChanged());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getCurrentTabTag()",
        method = "getCurrentTabTag",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    @UiThreadTest
    public void testGetCurrentTabTag() {
        TabHost tabHost = mActivity.getTabHost();
        assertEquals(TabHostStubActivity.INITIAL_TAB_TAG, tabHost.getCurrentTabTag());
        TabSpec tabSpec = tabHost.newTabSpec(TAG_TAB2);
        tabSpec.setIndicator(TAG_TAB2);
        tabSpec.setContent(new MyTabContentFactoryList());
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);
        assertEquals(TAG_TAB2, tabHost.getCurrentTabTag());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onDetachedFromWindow",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            method = "onAttachedToWindow",
            args = {}
        )
    })
    @UiThreadTest
    public void testOnAttachedToAndDetachedFromWindow() {
    }
    private class MyTabContentFactoryText implements TabHost.TabContentFactory {
        public View createTabContent(String tag) {
            final TextView tv = new TextView(mActivity);
            tv.setText(tag);
            return tv;
        }
    }
    private class MyTabContentFactoryList implements TabHost.TabContentFactory {
        public View createTabContent(String tag) {
            final ListView lv = new ListView(mActivity);
            return lv;
        }
    }
    private class MockOnTabChangeListener implements OnTabChangeListener {
        private boolean mCalledOnTabChanged = false;
        boolean hasCalledOnTabChanged() {
            return mCalledOnTabChanged;
        }
        void reset() {
            mCalledOnTabChanged = false;
        }
        public void onTabChanged(String tabId) {
            mCalledOnTabChanged = true;
        }
    }
}

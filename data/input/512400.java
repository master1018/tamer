public class ListFocusableTest extends ActivityInstrumentationTestCase<ListTopGravity> {
    private ListTopGravity mActivity;
    private ListView mListView;
    public ListFocusableTest() {
        super("com.android.frameworks.coretests", ListTopGravity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mListView = getActivity().getListView();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertNotNull(mListView);
        assertEquals(0, mListView.getSelectedItemPosition());
    }
    @MediumTest
    public void testAdapterFull() {
        setFullAdapter();
        assertTrue(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterEmpty() {
        setEmptyAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterNull() {
        setNullAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterFullSetFocusable() {
        assertTrue(mListView.isFocusable());
        setFocusable();
        assertTrue(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterFullSetNonFocusable() {
        assertTrue(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterEmptySetFocusable() {
        setEmptyAdapter();
        assertFalse(mListView.isFocusable());
        setFocusable();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterEmptySetNonFocusable() {
        setEmptyAdapter();
        assertFalse(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterNullSetFocusable() {
        setNullAdapter();
        assertFalse(mListView.isFocusable());
        setFocusable();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testAdapterNullSetNonFocusable() {
        setNullAdapter();
        assertFalse(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testFocusableSetAdapterFull() {
        assertTrue(mListView.isFocusable());
        setFullAdapter();
        assertTrue(mListView.isFocusable());
    }
    @MediumTest
    public void testNonFocusableSetAdapterFull() {
        assertTrue(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
        setFullAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testFocusableSetAdapterEmpty() {
        assertTrue(mListView.isFocusable());
        setEmptyAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testNonFocusableSetAdapterEmpty() {
        assertTrue(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
        setEmptyAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testFocusableSetAdapterNull() {
        assertTrue(mListView.isFocusable());
        setNullAdapter();
        assertFalse(mListView.isFocusable());
    }
    @MediumTest
    public void testNonFocusableSetAdapterNull() {
        assertTrue(mListView.isFocusable());
        setNonFocusable();
        assertFalse(mListView.isFocusable());
        setNullAdapter();
        assertFalse(mListView.isFocusable());
    }
    private ListAdapter createFullAdapter() {
        return new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1,
                new String[] { "Android", "Robot" });
    }
    private ListAdapter createEmptyAdapter() {
        return new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1,
                new String[] { });
    }
    private void setFullAdapter() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setAdapter(createFullAdapter());
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    private void setEmptyAdapter() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setAdapter(createEmptyAdapter());
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    private void setNullAdapter() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setAdapter(null);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    private void setFocusable() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setFocusable(true);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
    private void setNonFocusable() {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mListView.setFocusable(false);
            }
        });
        getInstrumentation().waitForIdleSync();
    }
}

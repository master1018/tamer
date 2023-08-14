public abstract class TestListActivity extends ListActivity {
    public static final String PERFORMANCE_TESTS = "android.test.performance";
    static final int MODE_GROUP = Menu.FIRST;
    String mSuite;
    String[] mTests;
    private int mMode = TestRunner.REGRESSION;
    private MenuItem mRegressionItem;
    private MenuItem mPerformanceItem;
    private MenuItem mProfilingItem;
    private final Comparator<String> sComparator = new Comparator<String>() {
        public final int compare(String a, String b) {
            String s1 = makeCompareName(a);
            String s2 = makeCompareName(b);
            return s1.compareToIgnoreCase(s2);
        }
    };
    public TestListActivity() {
        super();
    }
    public abstract String getTestSuite();
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent = getIntent();
        mMode = intent.getIntExtra(TestListActivity.PERFORMANCE_TESTS, mMode);
        if (intent.getAction().equals(Intent.ACTION_MAIN)) {
            mSuite = getTestSuite();
        } else if (intent.getAction().equals(Intent.ACTION_RUN)) {
            Intent ntent = new Intent(Intent.ACTION_RUN,
                    intent.getData() != null
                            ? intent.getData()
                            : Uri.parse(getTestSuite()));
            ntent.setClassName("com.android.testharness",
                    "com.android.testharness.RunTest");
            ntent.putExtras(intent);
            ntent.putExtra("package", getPackageName());
            startActivity(ntent);
            finish();
            return;
        } else if (intent.getAction().equals(Intent.ACTION_VIEW)) {
            mSuite = intent.getData() != null ? intent.getData().toString()
                    : null;
        }
        String[] children = TestRunner.getChildren(this, mSuite);
        Arrays.sort(children, sComparator);
        int len = children.length;
        mTests = new String[len];
        System.arraycopy(children, 0, mTests, 0, len);
        setTitle(TestRunner.getTitle(mSuite));
        MatrixCursor cursor = new MatrixCursor(new String[] { "name", "_id" });
        addTestRows(cursor);
        CursorAdapter adapter = new SimpleCursorAdapter(
                this,
                com.android.internal.R.layout.simple_list_item_1,
                cursor,
                new String[] {"name"},
                new int[] {com.android.internal.R.id.text1});
        setListAdapter(adapter);
    }
    private void addTestRows(MatrixCursor cursor) {
        int id = 0;
        cursor.newRow().add("Run All").add(id++);       
        for (String test : mTests) {
            String title = TestRunner.getTitle(test);
            String prefix = TestRunner.isTestSuite(this, test)
                    ? "Browse " : "Run ";
            cursor.newRow().add(prefix + title).add(id++);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mRegressionItem = menu.add(MODE_GROUP, -1, 0, "Regression Mode");
        mPerformanceItem = menu.add(MODE_GROUP, -1, 0, "Performance Mode");
        mProfilingItem = menu.add(MODE_GROUP, -1, 0, "Profiling Mode");
        menu.setGroupCheckable(MODE_GROUP, true, true);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mRegressionItem) {
            mMode = TestRunner.REGRESSION;
        } else if (item == mPerformanceItem) {
            mMode = TestRunner.PERFORMANCE;
        } else if (item == mProfilingItem) {
            mMode = TestRunner.PROFILING;
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        switch (mMode) {
        case TestRunner.REGRESSION:
            mRegressionItem.setChecked(true);
            break;
        case TestRunner.PERFORMANCE:
            mPerformanceItem.setChecked(true);
            break;
        case TestRunner.PROFILING:
            mProfilingItem.setChecked(true);
            break;
        }
        return true;
    }
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent();
        if (position == 0) {
            if (false) {
                intent.setClassName("com.android.testharness",
                        "com.android.testharness.RunAll");
                intent.putExtra("tests", new String[]{mSuite});
            } else {
                intent.setClassName("com.android.testharness",
                        "com.android.testharness.RunTest");
                intent.setAction(Intent.ACTION_RUN);
                intent.setData(Uri.parse(mSuite));
            }
        } else {
            String test = mTests[position - 1];
            if (TestRunner.isTestSuite(this, test)) {
                intent.setClassName(getPackageName(), this.getClass().getName());
                intent.setAction(Intent.ACTION_VIEW);
            } else {
                intent.setClassName("com.android.testharness",
                        "com.android.testharness.RunTest");
            }
            intent.setData(Uri.parse(test));
        }
        intent.putExtra(PERFORMANCE_TESTS, mMode);
        intent.putExtra("package", getPackageName());
        startActivity(intent);
    }
    private String makeCompareName(String s) {
        int index = s.lastIndexOf('.');
        if (index == -1) {
            return s;
        }
        return s.substring(index + 1);
    }
}

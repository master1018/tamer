public abstract class TestBrowserActivity extends ListActivity
        implements android.test.TestBrowserView, AdapterView.OnItemClickListener,
        TestSuiteProvider {
    private TestBrowserController mTestBrowserController;
    public static final String BUNDLE_EXTRA_PACKAGE = "package";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getListView().setOnItemClickListener(this);
        mTestBrowserController = ServiceLocator.getTestBrowserController();
        mTestBrowserController.setTargetPackageName(getPackageName());
        mTestBrowserController.registerView(this);
        mTestBrowserController.setTargetBrowserActivityClassName(this.getClass().getName());
        String[] apkPaths = {getPackageCodePath()};
        ClassPathPackageInfoSource.setApkPaths(apkPaths);
    }
    @Override
    protected void onStart() {
        super.onStart();
        TestSuite testSuite = getTestSuiteToBrowse();
        mTestBrowserController.setTestSuite(testSuite);
        String name = testSuite.getName();
        if (name != null) {
            setTitle(name.substring(name.lastIndexOf(".") + 1));
        }
    }
    @SuppressWarnings("unchecked")
    private TestSuite getTestSuiteToBrowse() {
        Intent intent = getIntent();
        if (Intent.ACTION_RUN.equals(intent.getAction())) {
            String testClassName = intent.getData().toString();
            try {
                Class<Test> testClass = (Class<Test>) getClassLoader().loadClass(testClassName);
                return TestCaseUtil.createTestSuite(testClass);
            } catch (ClassNotFoundException e) {
                Log.e("TestBrowserActivity", "ClassNotFoundException for " + testClassName, e);
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                Log.e("TestBrowserActivity", "IllegalAccessException for " + testClassName, e);
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                Log.e("TestBrowserActivity", "InstantiationException for " + testClassName, e);
                throw new RuntimeException(e);
            }
        } else {
            return getTopTestSuite();
        }
    }
    public TestSuite getTestSuite() {
        return getTopTestSuite();
    }
    public abstract TestSuite getTopTestSuite();
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        Intent intent = mTestBrowserController.getIntentForTestAt(position);
        intent.putExtra(BUNDLE_EXTRA_PACKAGE, getPackageName());
        startActivity(intent);
    }
    public void setTestNames(List<String> testNames) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                R.layout.test_list_item, testNames);
        setListAdapter(arrayAdapter);
    }
}

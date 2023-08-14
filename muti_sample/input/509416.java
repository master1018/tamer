public class ApiDemosTest extends ReferenceAppTestCase<ApiDemos> {
    public ApiDemosTest() {
        super("com.example.android.apis", ApiDemos.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final ApiDemos a = getActivity();
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                a.setSelection(0);
            }
        });
    }
    public void testdPadNav() {
        final ApiDemos a = getActivity();
        assert(a.getSelectedItemPosition() == 0);
        sendKeys(KeyEvent.KEYCODE_DPAD_UP);
        assert(a.getSelectedItemPosition() == 0);
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assert(a.getSelectedItemPosition() == 1);
        sendKeys(KeyEvent.KEYCODE_DPAD_LEFT);
        assert(a.getSelectedItemPosition() == 1);
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        assert(a.getSelectedItemPosition() == 1);
    }
    public void testNumberOfItemsInListView() {
        final ApiDemos a = getActivity();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_SAMPLE_CODE);
        PackageManager pm = a.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(mainIntent, 0);
        int numberOfActivities = list.size();
        for (int x = 0; x < numberOfActivities; ++x) {
            sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
            assert(a.getSelectedItemPosition() == x + 1);
        }
        sendKeys(KeyEvent.KEYCODE_DPAD_DOWN);
        assert(a.getSelectedItemPosition() == numberOfActivities);
        takeSnapshot("snap1");
    }
}

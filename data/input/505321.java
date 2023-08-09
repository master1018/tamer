public class AppWidgetManagerPermissionTest extends AndroidTestCase {
    private AppWidgetManager mAppWidgetManager = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAppWidgetManager = AppWidgetManager.getInstance(getContext());
        assertNotNull(mAppWidgetManager);
    }
    @SmallTest
    public void testBindAppWidget() {
        try {
            mAppWidgetManager.bindAppWidgetId(1, new ComponentName(mContext, "foo"));
            fail("Was able to call bindAppWidgetId");
        } catch (SecurityException e) {
        }
    }
}

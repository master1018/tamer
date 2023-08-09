public class PluralResourcesTest extends AndroidTestCase {
    public static boolean DEBUG = false;
    private static final String TAG = "PluralResourcesTest";
    private Resources mResources;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = mContext.getResources();
    }
    private Resources resourcesForLanguage(final String lang) {
        final Configuration config = new Configuration();
        config.updateFrom(mResources.getConfiguration());
        config.locale = new Locale(lang);
        return new Resources(mResources.getAssets(), mResources.getDisplayMetrics(), config);
    }
    @SmallTest
    public void testPlurals() {
        CharSequence cs;
        final Resources res = resourcesForLanguage("en");
        cs = res.getQuantityText(R.plurals.plurals_test, 0);
        if (DEBUG) {
            Log.d(TAG, "english 0 cs=" + cs);
        }
        Assert.assertEquals(cs.toString(), "Some dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 1);
        if (DEBUG) {
            Log.d(TAG, "english 1 cs=" + cs);
        }
        Assert.assertEquals(cs.toString(), "A dog");
        cs = res.getQuantityText(R.plurals.plurals_test, 2);
        Assert.assertEquals(cs.toString(), "Some dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 5);
        Assert.assertEquals(cs.toString(), "Some dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 500);
        Assert.assertEquals(cs.toString(), "Some dogs");
    }
    @SmallTest
    public void testCzech() {
        CharSequence cs;
        final Resources res = resourcesForLanguage("cs");
        cs = res.getQuantityText(R.plurals.plurals_test, 0);
        if (DEBUG) {
            Log.d(TAG, "czech 0 cs=" + cs);
        }
        Assert.assertEquals(cs.toString(), "Some Czech dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 1);
        if (DEBUG) {
            Log.d(TAG, "czech 1 cs=" + cs);
        }
        Assert.assertEquals(cs.toString(), "A Czech dog");
        cs = res.getQuantityText(R.plurals.plurals_test, 2);
        if (DEBUG) {
            Log.d(TAG, "czech 2 cs=" + cs);
        }
        Assert.assertEquals(cs.toString(), "Few Czech dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 5);
        Assert.assertEquals(cs.toString(), "Some Czech dogs");
        cs = res.getQuantityText(R.plurals.plurals_test, 500);
        Assert.assertEquals(cs.toString(), "Some Czech dogs");
    }
}

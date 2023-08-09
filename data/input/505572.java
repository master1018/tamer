public class BigCacheTest extends ActivityInstrumentationTestCase<BigCache> {
    private View mTiny;
    private View mLarge;
    public BigCacheTest() {
        super("com.android.frameworks.coretests", BigCache.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final BigCache activity = getActivity();
        mTiny = activity.findViewById(R.id.a);
        mLarge = activity.findViewById(R.id.b);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mTiny);
        assertNotNull(mLarge);
    }
    @MediumTest
    public void testDrawingCacheBelowMaximumSize() throws Exception {
        final int max = ViewConfiguration.get(getActivity()).getScaledMaximumDrawingCacheSize();
        assertTrue(mTiny.getWidth() * mTiny.getHeight() * 2 < max);
        assertNotNull(createCacheForView(mTiny));
    }
    public void testDrawingCacheAboveMaximumSize() throws Exception {
        final int max = ViewConfiguration.get(getActivity()).getScaledMaximumDrawingCacheSize();
        assertTrue(mLarge.getWidth() * mLarge.getHeight() * 2 > max);
        assertNull(createCacheForView(mLarge));
    }
    private Bitmap createCacheForView(final View view) {
        final Bitmap[] cache = new Bitmap[1];
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                view.setDrawingCacheEnabled(true);
                view.invalidate();
                view.buildDrawingCache();
                cache[0] = view.getDrawingCache();
            }
        });
        getInstrumentation().waitForIdleSync();
        return cache[0];
    }
}

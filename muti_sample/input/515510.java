public class InflateTest extends AndroidTestCase implements PerformanceTestCase {
    private LayoutInflater mInflater;
    private Resources mResources;
    private View mView;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mInflater = LayoutInflater.from(mContext);
        mResources = mContext.getResources();
    }
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        return 0;
    }
    public boolean isPerformanceOnly() {
        return false;
    }
    public void inflateTest(int resourceId) {
        mView = mInflater.inflate(resourceId, null);
        mResources.flushLayoutCache();
    }
    public void inflateCachedTest(int resourceId) {
        mInflater.inflate(resourceId, null);
        mInflater.inflate(resourceId, null);
    }
    @SmallTest
    public void testLayout1() throws Exception {
        inflateTest(R.layout.layout_one);
    }
    @SmallTest
    public void testLayout2() throws Exception {
        inflateTest(R.layout.layout_two);
    }
    @SmallTest
    public void testLayout3() throws Exception {
        inflateTest(R.layout.layout_three);
    }
    @SmallTest
    public void testLayout4() throws Exception {
        inflateTest(R.layout.layout_four);
    }
    @SmallTest
    public void testLayout5() throws Exception {
        inflateTest(R.layout.layout_five);
    }
    @SmallTest
    public void testLayout6() throws Exception {
        inflateTest(R.layout.layout_six);
    }
    @SmallTest
    public void testCachedLayout1() throws Exception {
        inflateCachedTest(R.layout.layout_one);
    }
    @SmallTest
    public void testCachedLayout2() throws Exception {
        inflateCachedTest(R.layout.layout_two);
    }
    @SmallTest
    public void testCachedLayout3() throws Exception {
        inflateCachedTest(R.layout.layout_three);
    }
    @SmallTest
    public void testCachedLayout4() throws Exception {
        inflateCachedTest(R.layout.layout_four);
    }
    @SmallTest
    public void testCachedLayout5() throws Exception {
        inflateCachedTest(R.layout.layout_five);
    }
    @SmallTest
    public void testCachedLayout6() throws Exception {
        inflateCachedTest(R.layout.layout_six);
    }
    public static class ViewOne extends View {
        public ViewOne(Context context) {
            super(context);
        }
        public ViewOne(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
    }
}

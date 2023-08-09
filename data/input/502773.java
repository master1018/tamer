public abstract class IconLoaderTest extends AndroidTestCase {
    protected IconLoader mLoader;
    @Override
    protected void setUp() throws Exception {
        mLoader = create();
    }
    protected abstract IconLoader create() throws Exception;
    public void testGetIcon() {
        assertNull(mLoader.getIcon(null));
        assertNull(mLoader.getIcon(""));
        assertNull(mLoader.getIcon("0"));
        assertNotNull(mLoader.getIcon(String.valueOf(android.R.drawable.star_on)));
    }
}

public class TextLayoutTest extends TestCase {
    protected String mString;
    protected TextPaint mPaint;
    protected void setUp() throws Exception {
        super.setUp();
        mString = "The quick brown fox";
        mPaint = new TextPaint();
    }
    @SmallTest
    public void testStaticLayout() throws Exception {
        Layout l = new StaticLayout(mString, mPaint, 200,
                Layout.Alignment.ALIGN_NORMAL, 1, 0,
                true);
    }
    @SmallTest
    public void testDynamicLayoutTest() throws Exception {
        Layout l = new DynamicLayout(mString, mPaint, 200,
                Layout.Alignment.ALIGN_NORMAL, 1, 0,
                true);
    }
}

@TestTargetClass(NinePatchDrawable.class)
public class NinePatchDrawableTest extends InstrumentationTestCase {
    private static final int MIN_CHUNK_SIZE = 32;
    private NinePatchDrawable mNinePatchDrawable;
    private Resources mResources;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mResources = getInstrumentation().getTargetContext().getResources();
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_0);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NinePatchDrawable",
            args = {Bitmap.class, byte[].class, Rect.class, String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "NinePatchDrawable",
            args = {android.graphics.NinePatch.class}
        )
    })
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "NinePatchDrawable#NinePatchDrawable(Bitmap, byte[], Rect, String) "
            + "when param bitmap, chunk, padding or srcName is null")
    @SuppressWarnings("deprecation")
    public void testConstructors() {
        byte[] chunk = new byte[MIN_CHUNK_SIZE];
        chunk[MIN_CHUNK_SIZE - 1] = 1;
        Rect r = new Rect();
        Bitmap bmp = BitmapFactory.decodeResource(mResources, R.drawable.ninepatch_0);
        String name = mResources.getResourceName(R.drawable.ninepatch_0);
        new NinePatchDrawable(bmp, chunk, r, name);
        try {
            new NinePatchDrawable(null, chunk, r, name);
            fail("The constructor should check whether the bitmap is null.");
        } catch (NullPointerException e) {
        }
        try {
            mNinePatchDrawable = new NinePatchDrawable(bmp, chunk, null, name);
            fail("The constructor should not accept null padding.");
        } catch (NullPointerException e) {
        }
        try {
            new NinePatchDrawable(bmp, chunk, r, null);
        } catch (NullPointerException e) {
            fail("The constructor should accept null srcname.");
        }
        try {
            new NinePatchDrawable(new NinePatch(bmp, chunk, name));
        } catch (NullPointerException e) {
        }
        mNinePatchDrawable = new NinePatchDrawable(null);
        chunk = new byte[MIN_CHUNK_SIZE - 1];
        chunk[MIN_CHUNK_SIZE - 2] = 1;
        try {
            new NinePatchDrawable(bmp, chunk, r, name);
            fail("The constructor should check whether the chunk is illegal.");
        } catch (RuntimeException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "NinePatchDrawable#draw(Canvas) when param canvas is null")
    public void testDraw() {
        Bitmap bmp = Bitmap.createBitmap(9, 9, Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        int ocean = Color.rgb(0, 0xFF, 0x80);
        mNinePatchDrawable.setBounds(0, 0, 9, 9);
        mNinePatchDrawable.draw(c);
        assertColorFillRect(bmp, 0, 0, 4, 4, Color.RED);
        assertColorFillRect(bmp, 5, 0, 4, 4, Color.BLUE);
        assertColorFillRect(bmp, 0, 5, 4, 4, ocean);
        assertColorFillRect(bmp, 5, 5, 4, 4, Color.YELLOW);
        assertColorFillRect(bmp, 4, 0, 1, 9, Color.WHITE);
        assertColorFillRect(bmp, 0, 4, 9, 1, Color.WHITE);
        bmp.eraseColor(0xff000000);
        mNinePatchDrawable.setBounds(0, 0, 3, 3);
        mNinePatchDrawable.draw(c);
        assertColorFillRect(bmp, 0, 0, 1, 1, Color.RED);
        assertColorFillRect(bmp, 2, 0, 1, 1, Color.BLUE);
        assertColorFillRect(bmp, 0, 2, 1, 1, ocean);
        assertColorFillRect(bmp, 2, 2, 1, 1, Color.YELLOW);
        assertColorFillRect(bmp, 1, 0, 1, 3, Color.WHITE);
        assertColorFillRect(bmp, 0, 1, 3, 1, Color.WHITE);
        try {
            mNinePatchDrawable.draw(null);
            fail("The method should check whether the canvas is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getChangingConfigurations",
        args = {}
    )
    public void testGetChangingConfigurations() {
        ConstantState constantState = mNinePatchDrawable.getConstantState();
        assertEquals(0, constantState.getChangingConfigurations());
        assertEquals(0, mNinePatchDrawable.getChangingConfigurations());
        mNinePatchDrawable.setChangingConfigurations(0xff);
        assertEquals(0xff, mNinePatchDrawable.getChangingConfigurations());
        assertEquals(0, constantState.getChangingConfigurations());
        constantState = mNinePatchDrawable.getConstantState();
        assertEquals(0xff,  constantState.getChangingConfigurations());
        mNinePatchDrawable.setChangingConfigurations(0xff00);
        assertEquals(0xff,  constantState.getChangingConfigurations());
        assertEquals(0xffff,  mNinePatchDrawable.getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPadding",
        args = {android.graphics.Rect.class}
    )
    @ToBeFixed(bug = "1417734", explanation = "should add @throws clause into javadoc of "
            + "NinePatchDrawable#getPadding(Rect) when param padding is null or "
            + "the insternal padding field is not set ")
    public void testGetPadding() {
        Rect r = new Rect();
        NinePatchDrawable npd = (NinePatchDrawable) mResources.getDrawable(R.drawable.ninepatch_0);
        assertTrue(npd.getPadding(r));
        assertEquals(0, r.left);
        assertEquals(0, r.top);
        assertTrue(r.right > 0);
        assertTrue(r.bottom > 0);
        npd = (NinePatchDrawable) mResources.getDrawable(R.drawable.ninepatch_1);
        assertTrue(npd.getPadding(r));
        assertTrue(r.left > 0);
        assertTrue(r.top > 0);
        assertTrue(r.right > 0);
        assertTrue(r.bottom > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAlpha",
        args = {int.class}
    )
    public void testSetAlpha() {
        assertEquals(0xff, mNinePatchDrawable.getPaint().getAlpha());
        mNinePatchDrawable.setAlpha(0);
        assertEquals(0, mNinePatchDrawable.getPaint().getAlpha());
        mNinePatchDrawable.setAlpha(-1);
        assertEquals(0xff, mNinePatchDrawable.getPaint().getAlpha());
        mNinePatchDrawable.setAlpha(0xfffe);
        assertEquals(0xfe, mNinePatchDrawable.getPaint().getAlpha());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    public void testSetColorFilter() {
        assertNull(mNinePatchDrawable.getPaint().getColorFilter());
        MockColorFilter cf = new MockColorFilter();
        mNinePatchDrawable.setColorFilter(cf);
        assertSame(cf, mNinePatchDrawable.getPaint().getColorFilter());
        mNinePatchDrawable.setColorFilter(null);
        assertNull(mNinePatchDrawable.getPaint().getColorFilter());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDither",
        args = {boolean.class}
    )
    public void testSetDither() {
        assertTrue(mNinePatchDrawable.getPaint().isDither());
        mNinePatchDrawable.setDither(false);
        assertFalse(mNinePatchDrawable.getPaint().isDither());
        mNinePatchDrawable.setDither(true);
        assertTrue(mNinePatchDrawable.getPaint().isDither());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getPaint",
        args = {}
    )
    public void testGetPaint() {
        Paint paint = mNinePatchDrawable.getPaint();
        assertNotNull(paint);
        assertSame(paint, mNinePatchDrawable.getPaint());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntrinsicWidth",
        args = {}
    )
    public void testGetIntrinsicWidth() {
        Bitmap bmp = getBitmapUnscaled(R.drawable.ninepatch_0);
        assertEquals(bmp.getWidth(), mNinePatchDrawable.getIntrinsicWidth());
        assertEquals(5, mNinePatchDrawable.getIntrinsicWidth());
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        bmp = getBitmapUnscaled(R.drawable.ninepatch_1);
        assertEquals(bmp.getWidth(), mNinePatchDrawable.getIntrinsicWidth());
        assertEquals(9, mNinePatchDrawable.getIntrinsicWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMinimumWidth",
        args = {}
    )
    public void testGetMinimumWidth() {
        Bitmap bmp = getBitmapUnscaled(R.drawable.ninepatch_0);
        assertEquals(bmp.getWidth(), mNinePatchDrawable.getMinimumWidth());
        assertEquals(5, mNinePatchDrawable.getMinimumWidth());
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        bmp = getBitmapUnscaled(R.drawable.ninepatch_1);
        assertEquals(bmp.getWidth(), mNinePatchDrawable.getMinimumWidth());
        assertEquals(9, mNinePatchDrawable.getMinimumWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIntrinsicHeight",
        args = {}
    )
    public void testGetIntrinsicHeight() {
        Bitmap bmp = getBitmapUnscaled(R.drawable.ninepatch_0);
        assertEquals(bmp.getHeight(), mNinePatchDrawable.getIntrinsicHeight());
        assertEquals(5, mNinePatchDrawable.getIntrinsicHeight());
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        bmp = getBitmapUnscaled(R.drawable.ninepatch_1);
        assertEquals(bmp.getHeight(), mNinePatchDrawable.getIntrinsicHeight());
        assertEquals(9, mNinePatchDrawable.getIntrinsicHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getMinimumHeight",
        args = {}
    )
    public void testGetMinimumHeight() {
        Bitmap bmp = getBitmapUnscaled(R.drawable.ninepatch_0);
        assertEquals(bmp.getHeight(), mNinePatchDrawable.getMinimumHeight());
        assertEquals(5, mNinePatchDrawable.getMinimumHeight());
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        bmp = getBitmapUnscaled(R.drawable.ninepatch_1);
        assertEquals(bmp.getHeight(), mNinePatchDrawable.getMinimumHeight());
        assertEquals(9, mNinePatchDrawable.getMinimumHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOpacity",
        args = {}
    )
    public void testGetOpacity() {
        assertEquals(PixelFormat.OPAQUE, mNinePatchDrawable.getOpacity());
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        assertEquals(PixelFormat.TRANSLUCENT, mNinePatchDrawable.getOpacity());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getTransparentRegion",
        args = {}
    )
    public void testGetTransparentRegion() {
        Region r = mNinePatchDrawable.getTransparentRegion();
        assertNull(r);
        mNinePatchDrawable.setBounds(0, 0, 7, 7);
        r = mNinePatchDrawable.getTransparentRegion();
        assertNull(r);
        mNinePatchDrawable = getNinePatchDrawable(R.drawable.ninepatch_1);
        r = mNinePatchDrawable.getTransparentRegion();
        assertNull(r);
        mNinePatchDrawable.setBounds(1, 1, 7, 7);
        r = mNinePatchDrawable.getTransparentRegion();
        assertNotNull(r);
        assertEquals(new Rect(1, 1, 7, 7), r.getBounds());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getConstantState",
        args = {}
    )
    public void testGetConstantState() {
        assertNotNull(mNinePatchDrawable.getConstantState());
        ConstantState constantState = mNinePatchDrawable.getConstantState();
        mNinePatchDrawable.setChangingConfigurations(0xff);
        assertEquals(0, constantState.getChangingConfigurations());
        constantState = mNinePatchDrawable.getConstantState();
        assertEquals(0xff, constantState.getChangingConfigurations());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "inflate",
        args = {Resources.class, XmlPullParser.class, AttributeSet.class}
    )
    public void testInflate() throws XmlPullParserException, IOException {
        final int width = 80;
        final int height = 120;
        final int[] COLOR = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(COLOR, width, height, Bitmap.Config.RGB_565);
        NinePatchDrawable ninePatchDrawable =
            new NinePatchDrawable(mResources, bitmap, new byte[1000], null, "TESTNAME");
        assertEquals(height, ninePatchDrawable.getIntrinsicHeight());
        assertEquals(width, ninePatchDrawable.getIntrinsicWidth());
        XmlResourceParser parser = mResources.getXml(R.drawable.ninepatchdrawable);
        int type;
        while ((type = parser.next()) != XmlPullParser.END_DOCUMENT
                && type != XmlPullParser.START_TAG) {
        }
        AttributeSet attrs = Xml.asAttributeSet(parser);
        ninePatchDrawable.inflate(mResources, parser, attrs);
        assertTrue(ninePatchDrawable.getPaint().isDither());
        assertTrue(height != ninePatchDrawable.getIntrinsicHeight());
        assertTrue(width != ninePatchDrawable.getIntrinsicWidth());
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "mutate",
        args = {}
    )
    @ToBeFixed(bug = "", explanation = "mutate() always throw NullPointerException.")
    public void testMutate() {
        NinePatchDrawable d1 =
            (NinePatchDrawable) mResources.getDrawable(R.drawable.ninepatchdrawable);
        NinePatchDrawable d2 =
            (NinePatchDrawable) mResources.getDrawable(R.drawable.ninepatchdrawable);
        NinePatchDrawable d3 =
            (NinePatchDrawable) mResources.getDrawable(R.drawable.ninepatchdrawable);
        d1.setDither(false);
        assertFalse(d1.getPaint().isDither());
        assertTrue(d2.getPaint().isDither());
        assertTrue(d3.getPaint().isDither());
        d1.mutate();
    }
    private void assertColorFillRect(Bitmap bmp, int x, int y, int w, int h, int color) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                assertEquals(color, bmp.getPixel(i, j));
            }
        }
    }
    private NinePatchDrawable getNinePatchDrawable(int resId) {
        Bitmap bitmap = getBitmapUnscaled(resId);
        NinePatch np = new NinePatch(bitmap, bitmap.getNinePatchChunk(), null);
        return new NinePatchDrawable(mResources, np);
    }
    private Bitmap getBitmapUnscaled(int resId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inDensity = opts.inTargetDensity = mResources.getDisplayMetrics().densityDpi;
        Bitmap bitmap = BitmapFactory.decodeResource(mResources, resId, opts);
        return bitmap;
    }
    private class MockColorFilter extends ColorFilter {
    }
}

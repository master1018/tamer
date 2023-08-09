@TestTargetClass(ImageSpan.class)
public class ImageSpanTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.Bitmap.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.Bitmap.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.graphics.drawable.Drawable.class, java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, android.net.Uri.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, android.net.Uri.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test constructor(s) of ImageSpan.",
            method = "ImageSpan",
            args = {android.content.Context.class, int.class, int.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "some constructors miss javadoc")
    public void testConstructor() {
        int width = 80;
        int height = 120;
        int[] color = new int[width * height];
        Bitmap b = Bitmap.createBitmap(color, width, height, Bitmap.Config.RGB_565);
        new ImageSpan(b);
        new ImageSpan(b, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(b, DynamicDrawableSpan.ALIGN_BASELINE);
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        new ImageSpan(d);
        new ImageSpan(d, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(d, DynamicDrawableSpan.ALIGN_BASELINE);
        new ImageSpan(d, "cts test.");
        new ImageSpan(d, "cts test.", DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(d, "cts test.", DynamicDrawableSpan.ALIGN_BASELINE);
        new ImageSpan(mContext, Uri.parse("content:
        new ImageSpan(mContext, Uri.parse("content:
                DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(mContext, Uri.parse("content:
                DynamicDrawableSpan.ALIGN_BASELINE);
        new ImageSpan(mContext, R.drawable.pass);
        new ImageSpan(mContext, R.drawable.pass, DynamicDrawableSpan.ALIGN_BOTTOM);
        new ImageSpan(mContext, R.drawable.pass, DynamicDrawableSpan.ALIGN_BASELINE);
        new ImageSpan((Bitmap) null);
        new ImageSpan((Drawable) null);
        new ImageSpan((Drawable) null, (String) null);
        new ImageSpan((Context) null, -1);
        new ImageSpan((Bitmap) null, -1);
        new ImageSpan((Drawable) null, -1);
        new ImageSpan((Drawable) null, (String) null, -1);
        new ImageSpan((Context) null, -1, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getSource().",
        method = "getSource",
        args = {}
    )
    public void testGetSource() {
        Drawable d = mContext.getResources().getDrawable(R.drawable.pass);
        ImageSpan imageSpan = new ImageSpan(d);
        assertNull(imageSpan.getSource());
        String source = "cts test.";
        imageSpan = new ImageSpan(d, source);
        assertEquals(source, imageSpan.getSource());
        source = "content:
        imageSpan = new ImageSpan(mContext, Uri.parse(source));
        assertEquals(source, imageSpan.getSource());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test getDrawable().",
        method = "getDrawable",
        args = {}
    )
    public void testGetDrawable() {
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.pass);
        ImageSpan imageSpan = new ImageSpan(drawable);
        assertSame(drawable, imageSpan.getDrawable());
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        imageSpan = new ImageSpan(mContext, R.drawable.pass);
        BitmapDrawable resultDrawable = (BitmapDrawable) imageSpan.getDrawable();
        WidgetTestUtils.assertEquals(bitmapDrawable.getBitmap(), resultDrawable.getBitmap());
        imageSpan = new ImageSpan(mContext, Uri.parse("unknown uri."));
        assertNull(imageSpan.getDrawable());
        imageSpan = new ImageSpan((Context) null, -1);
        assertNull(imageSpan.getDrawable());
    }
}

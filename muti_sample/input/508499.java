@TestTargetClass(android.graphics.drawable.PictureDrawable.class)
public class PictureDrawableTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "PictureDrawable",
        args = {android.graphics.Picture.class}
    )
    public void testConstructor() {
        assertNull((new PictureDrawable(null)).getPicture());
        assertNotNull((new PictureDrawable(new Picture())).getPicture());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "draw",
        args = {android.graphics.Canvas.class}
    )
    public void testDraw() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        Picture picture = new Picture();
        Canvas recodingCanvas = picture.beginRecording(100, 200);
        recodingCanvas.drawARGB(255, 0xa, 0xc, 0xb);
        picture.endRecording();
        pictureDrawable.setPicture(picture);
        pictureDrawable.setBounds(0, 0, 100, 200);
        Bitmap destBitmap = Bitmap.createBitmap(100, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(destBitmap);
        assertEquals(100, canvas.getClipBounds().width());
        assertEquals(200, canvas.getClipBounds().height());
        canvas.drawARGB(255, 0x0f, 0x0b, 0x0c);
        assertEquals(0xff0f0b0c, destBitmap.getPixel(10, 10));
        pictureDrawable.draw(canvas);
        assertEquals(0xff0a0c0b, destBitmap.getPixel(10, 10));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntrinsicWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getIntrinsicHeight",
            args = {}
        )
    })
    public void testGetIntrinsicSize() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        assertEquals(-1, pictureDrawable.getIntrinsicWidth());
        assertEquals(-1, pictureDrawable.getIntrinsicHeight());
        Picture picture = new Picture();
        picture.beginRecording(99, 101);
        pictureDrawable.setPicture(picture);
        assertEquals(99, pictureDrawable.getIntrinsicWidth());
        assertEquals(101, pictureDrawable.getIntrinsicHeight());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getOpacity",
        args = {}
    )
    public void testGetOpacity() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        assertEquals(PixelFormat.TRANSLUCENT, pictureDrawable.getOpacity());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setAlpha",
        args = {int.class}
    )
    public void testSetAlpha() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        pictureDrawable.setAlpha(0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setColorFilter",
        args = {android.graphics.ColorFilter.class}
    )
    public void testSetColorFilter() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        ColorFilter colorFilter = new ColorFilter();
        pictureDrawable.setColorFilter(colorFilter);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDither",
        args = {boolean.class}
    )
    public void testSetDither() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        pictureDrawable.setDither(true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setFilterBitmap",
        args = {boolean.class}
    )
    public void testSetFilterBitmap() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        pictureDrawable.setFilterBitmap(true);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPicture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPicture",
            args = {android.graphics.Picture.class}
        )
    })
    public void testAccessPicture() {
        PictureDrawable pictureDrawable = new PictureDrawable(null);
        assertNull(pictureDrawable.getPicture());
        Picture picture = new Picture();
        pictureDrawable.setPicture(picture);
        assertNotNull(pictureDrawable.getPicture());
        assertEquals(picture, pictureDrawable.getPicture());
        pictureDrawable.setPicture(null);
        assertNull(pictureDrawable.getPicture());
    }
}

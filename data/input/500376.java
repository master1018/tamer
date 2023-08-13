@TestTargetClass(Picture.class)
public class PictureTest extends TestCase {
    private static final int TEST_WIDTH = 4; 
    private static final int TEST_HEIGHT = 3; 
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Picture",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "Picture",
            args = {android.graphics.Picture.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "beginRecording",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "endRecording",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "writeToStream",
            args = {java.io.OutputStream.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "createFromStream",
            args = {java.io.InputStream.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {android.graphics.Canvas.class}
        )
    })
    public void testPicture() throws Exception {
        Picture picture = new Picture();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Canvas canvas = picture.beginRecording(TEST_WIDTH, TEST_HEIGHT);
        assertNotNull(canvas);
        drawPicture(canvas);
        picture.endRecording();
        Bitmap bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        picture.draw(canvas);
        checkSize(picture);
        checkBitmap(bitmap);
        picture.writeToStream(bout);
        picture = Picture.createFromStream(new ByteArrayInputStream(bout.toByteArray()));
        bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        picture.draw(canvas);
        checkSize(picture);
        checkBitmap(bitmap);
        Picture pic = new Picture(picture);
        bitmap = Bitmap.createBitmap(TEST_WIDTH, TEST_HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        pic.draw(canvas);
        checkSize(pic);
        checkBitmap(bitmap);
    }
    private void checkSize(Picture picture) {
        assertEquals(TEST_WIDTH, picture.getWidth());
        assertEquals(TEST_HEIGHT, picture.getHeight());
    }
    private void drawPicture(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Style.FILL);
        canvas.drawRect(0, 0, TEST_WIDTH, TEST_HEIGHT, paint);
        paint.setColor(Color.RED);
        canvas.drawLine(0, 0, TEST_WIDTH, 0, paint);
        paint.setColor(Color.BLUE);
        canvas.drawPoint(0, 0, paint);
    }
    private void checkBitmap(Bitmap bitmap) {
        assertEquals(Color.BLUE, bitmap.getPixel(0, 0));
        for (int x = 1; x < TEST_WIDTH; x++) {
            assertEquals(Color.RED, bitmap.getPixel(x, 0));
        }
        for (int y = 1; y < TEST_HEIGHT; y++) {
            for (int x = 0; x < TEST_WIDTH; x++) {
                assertEquals(Color.GREEN, bitmap.getPixel(x, y));
            }
        }
    }
}

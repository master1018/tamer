@TestTargetClass(Bitmap.CompressFormat.class)
public class Bitmap_CompressFormatTest extends AndroidTestCase{
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(CompressFormat.JPEG, CompressFormat.valueOf("JPEG"));
        assertEquals(CompressFormat.PNG, CompressFormat.valueOf("PNG"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "compress",
            args = {android.graphics.Bitmap.CompressFormat.class, int.class,
                    java.io.OutputStream.class}
        )
    })
    public void testValues(){
        CompressFormat[] comFormat = CompressFormat.values();
        assertEquals(2, comFormat.length);
        assertEquals(CompressFormat.JPEG, comFormat[0]);
        assertEquals(CompressFormat.PNG, comFormat[1]);
        Bitmap b = Bitmap.createBitmap(10, 24, Config.ARGB_8888);
        assertTrue(b.compress(CompressFormat.JPEG, 24, new ByteArrayOutputStream()));
        assertTrue(b.compress(CompressFormat.PNG, 24, new ByteArrayOutputStream()));
    }
}

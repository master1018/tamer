@TestTargetClass(Bitmap.Config.class)
public class Bitmap_ConfigTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(Config.ALPHA_8, Config.valueOf("ALPHA_8"));
        assertEquals(Config.RGB_565, Config.valueOf("RGB_565"));
        assertEquals(Config.ARGB_4444, Config.valueOf("ARGB_4444"));
        assertEquals(Config.ARGB_8888, Config.valueOf("ARGB_8888"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues(){
        Config[] config = Config.values();
        assertEquals(4, config.length);
        assertEquals(Config.ALPHA_8, config[0]);
        assertEquals(Config.RGB_565, config[1]);
        assertEquals(Config.ARGB_4444, config[2]);
        assertEquals(Config.ARGB_8888, config[3]);
        assertNotNull(Bitmap.createBitmap(10, 24, Config.ALPHA_8));
        assertNotNull(Bitmap.createBitmap(10, 24, Config.ARGB_4444));
        assertNotNull(Bitmap.createBitmap(10, 24, Config.ARGB_8888));
        assertNotNull(Bitmap.createBitmap(10, 24, Config.RGB_565));
    }
}

@TestTargetClass(android.graphics.Shader.class)
public class ShaderTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Shader",
        args = {}
    )
    public void testConstructor() {
        new Shader();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLocalMatrix",
            args = {android.graphics.Matrix.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocalMatrix",
            args = {android.graphics.Matrix.class}
        )
    })
    public void testAccessLocalMatrix() {
        int width = 80;
        int height = 120;
        int[] color = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(color, width, height, Bitmap.Config.RGB_565);
        Shader shader = new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        Matrix m = new Matrix();
        shader.setLocalMatrix(m);
        assertFalse(shader.getLocalMatrix(m));
        shader.setLocalMatrix(null);
        assertFalse(shader.getLocalMatrix(m));
    }
}

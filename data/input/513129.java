@TestTargetClass(Camera.Size.class)
@LargeTest
public class Camera_SizeTest extends TestCase {
    private final int HEIGHT1 = 320;
    private final int WIDTH1 = 240;
    private final int HEIGHT2 = 480;
    private final int WIDTH2 = 320;
    private final int HEIGHT3 = 640;
    private final int WIDTH3 = 480;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Camera.Size",
        args = {int.class, int.class}
    )
    public void testConstructor() {
        Camera camera = Camera.open();
        Parameters parameters = camera.getParameters();
        checkSize(parameters, WIDTH1, HEIGHT1);
        checkSize(parameters, WIDTH2, HEIGHT2);
        checkSize(parameters, WIDTH3, HEIGHT3);
    }
    private void checkSize(Parameters parameters, int width, int height) {
        parameters.setPictureSize(width, height);
        assertEquals(width, parameters.getPictureSize().width);
        assertEquals(height, parameters.getPictureSize().height);
    }
}

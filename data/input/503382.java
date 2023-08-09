public class CameraPermissionTest extends AndroidTestCase {
    private static String PATH_PREFIX = Environment.getExternalStorageDirectory().toString();
    private static String CAMERA_IMAGE_PATH = PATH_PREFIX + "this-should-not-exist.jpg";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    class ShutterCallback implements Camera.ShutterCallback {
        public void onShutter() { }
    }
    class RawPictureCallback implements Camera.PictureCallback {
        public void onPictureTaken(byte [] rawData, Camera camera) { }
    }
    class JpegPictureCallback implements Camera.PictureCallback {
        public void onPictureTaken(byte [] jpegData, Camera camera) {
            if (jpegData == null) {
                return;
            }
            try {
                FileOutputStream s = new FileOutputStream(CAMERA_IMAGE_PATH);
                s.write(jpegData);
                s.flush();
            }
            catch (SecurityException e) {
            } catch (Exception e) {
            }
            fail("Successfully captured an image of " + jpegData.length +
                 " bytes, and saved it to " + CAMERA_IMAGE_PATH);
        }
    }
    @MediumTest
    public void testCamera() {
        try {
            (Camera.open()).takePicture(new ShutterCallback(),
                                        new RawPictureCallback(),
                                        new JpegPictureCallback());
            fail("Was able to take a picture with the camera with no permission");
        }
        catch (SecurityException e) {
        } catch (RuntimeException e) {
        }
    }
}

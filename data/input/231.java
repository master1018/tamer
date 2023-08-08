public class StreetviewUserPhotoOptionsTest extends JsBeanTestCase {
    public void testFields() {
        loadApi(new Runnable() {
            public void run() {
                StreetviewUserPhotosOptions opts = StreetviewUserPhotosOptions.newInstance();
                JsArrayString array = ArrayHelper.toJsArrayString("picasa", "flickr");
                opts.setPhotoRepositories(array).setPhotoRepositories(array);
                assertSame(array, getJso(opts, "photoRepositories"));
            }
        });
    }
}

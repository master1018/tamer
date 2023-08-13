public class BitmapTexture extends Texture {
    final Bitmap mBitmap;
    BitmapTexture(Bitmap bitmap) {
        mBitmap = bitmap;
    }
    @Override
    protected Bitmap load(RenderView view) {
        return mBitmap;
    }
}

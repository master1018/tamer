public class BitmapShader extends Shader {
    private Bitmap mBitmap;
    public BitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) {
        mBitmap = bitmap;
        native_instance = nativeCreate(bitmap.ni(),
                                       tileX.nativeInt, tileY.nativeInt);
    }
    private static native int nativeCreate(int native_bitmap,
                                           int shaderTileModeX,
                                           int shaderTileModeY);    
}

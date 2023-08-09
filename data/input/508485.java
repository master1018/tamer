public class Picture {
    private Canvas mRecordingCanvas;
    private final int mNativePicture;
    private static final int WORKING_STREAM_STORAGE = 16 * 1024;
    public Picture() {
        this(nativeConstructor(0));
    }
    public Picture(Picture src) {
        this(nativeConstructor(src != null ? src.mNativePicture : 0));
    }
    public Canvas beginRecording(int width, int height) {
        int ni = nativeBeginRecording(mNativePicture, width, height);
        mRecordingCanvas = new RecordingCanvas(this, ni);
        return mRecordingCanvas;
    }
    public void endRecording() {
        if (mRecordingCanvas != null) {
            mRecordingCanvas = null;
            nativeEndRecording(mNativePicture);
        }
    }
    public native int getWidth();
    public native int getHeight();
    public void draw(Canvas canvas) {
        if (mRecordingCanvas != null) {
            endRecording();
        }
        nativeDraw(canvas.mNativeCanvas, mNativePicture);
    }
    public static Picture createFromStream(InputStream stream) {
        return new Picture(
            nativeCreateFromStream(stream, new byte[WORKING_STREAM_STORAGE]));
    }
    public void writeToStream(OutputStream stream) {
        if (stream == null) {
            throw new NullPointerException();
        }
        if (!nativeWriteToStream(mNativePicture, stream,
                             new byte[WORKING_STREAM_STORAGE])) {
            throw new RuntimeException();
        }
    }
    protected void finalize() throws Throwable {
        nativeDestructor(mNativePicture);
    }
     final int ni() {
        return mNativePicture;
    }
    private Picture(int nativePicture) {
        if (nativePicture == 0) {
            throw new RuntimeException();
        }
        mNativePicture = nativePicture;
    }
    private static native int nativeConstructor(int nativeSrcOr0);
    private static native int nativeCreateFromStream(InputStream stream,
                                                byte[] storage);
    private static native int nativeBeginRecording(int nativeCanvas,
                                                    int w, int h);
    private static native void nativeEndRecording(int nativeCanvas);
    private static native void nativeDraw(int nativeCanvas, int nativePicture);
    private static native boolean nativeWriteToStream(int nativePicture,
                                           OutputStream stream, byte[] storage);
    private static native void nativeDestructor(int nativePicture);
    private static class RecordingCanvas extends Canvas {
        private final Picture mPicture;
        public RecordingCanvas(Picture pict, int nativeCanvas) {
            super(nativeCanvas);
            mPicture = pict;
        }
        @Override
        public void setBitmap(Bitmap bitmap) {
            throw new RuntimeException(
                                "Cannot call setBitmap on a picture canvas");
        }
        @Override
        public void drawPicture(Picture picture) {
            if (mPicture == picture) {
                throw new RuntimeException(
                            "Cannot draw a picture into its recording canvas");
            }
            super.drawPicture(picture);
        }
    }
}

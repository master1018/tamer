public class FaceDetector {
    public class Face {
        public static final float CONFIDENCE_THRESHOLD = 0.4f;
        public static final int EULER_X = 0;
        public static final int EULER_Y = 1;
        public static final int EULER_Z = 2;
        public float confidence() {
            return mConfidence;
        }
        public void getMidPoint(PointF point) {
            point.set(mMidPointX, mMidPointY);
        }
        public float eyesDistance() {
            return mEyesDist;
        }
        public float pose(int euler) {
            if (euler == EULER_X)
                return mPoseEulerX;
            else if (euler == EULER_Y)
                return mPoseEulerY;
            else if (euler == EULER_Z)
                return mPoseEulerZ;
           throw new IllegalArgumentException();
        }
        private Face() {
        }
        private float   mConfidence;
        private float   mMidPointX;
        private float   mMidPointY;
        private float   mEyesDist;
        private float   mPoseEulerX;
        private float   mPoseEulerY;
        private float   mPoseEulerZ;
    }
    public FaceDetector(int width, int height, int maxFaces)
    {
        if (!sInitialized) {
            return;
        }
        fft_initialize(width, height, maxFaces);
        mWidth = width;
        mHeight = height;
        mMaxFaces = maxFaces;
        mBWBuffer = new byte[width * height];
    }
    public int findFaces(Bitmap bitmap, Face[] faces)
    {
        if (!sInitialized) {
            return 0;
        }
        if (bitmap.getWidth() != mWidth || bitmap.getHeight() != mHeight) {
            throw new IllegalArgumentException(
                    "bitmap size doesn't match initialization");
        }
        if (faces.length < mMaxFaces) {
            throw new IllegalArgumentException(
                    "faces[] smaller than maxFaces");
        }
        int numFaces = fft_detect(bitmap);
        if (numFaces >= mMaxFaces)
            numFaces = mMaxFaces;
        for (int i=0 ; i<numFaces ; i++) {
            if (faces[i] == null)
                faces[i] = new Face();
            fft_get_face(faces[i], i);
        }
        return numFaces;
    }
    @Override
    protected void finalize() throws Throwable {
        fft_destroy();
    }
    private static boolean sInitialized;
    native private static void nativeClassInit();
    static {
        sInitialized = false;
        try {
            System.loadLibrary("FFTEm");
            nativeClassInit();
            sInitialized = true;
        } catch (UnsatisfiedLinkError e) {
            Log.d("FFTEm", "face detection library not found!");
        }
    }
    native private int  fft_initialize(int width, int height, int maxFaces);
    native private int  fft_detect(Bitmap bitmap);
    native private void fft_get_face(Face face, int i);
    native private void fft_destroy();
    private int     mFD;
    private int     mSDK;
    private int     mDCR;
    private int     mWidth;
    private int     mHeight;
    private int     mMaxFaces;    
    private byte    mBWBuffer[];
}

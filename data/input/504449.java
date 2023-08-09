@TestTargetClass(YuvImage.class)
public class YuvImageTest extends AndroidTestCase {
    private static final int CSHIFT = 16;
    private static final int CYR = 19595;
    private static final int CYG = 38470;
    private static final int CYB = 7471;
    private static final int CUR = -11059;
    private static final int CUG = -21709;
    private static final int CUB = 32768;
    private static final int CVR = 32768;
    private static final int CVG = -27439;
    private static final int CVB = -5329;
    private static String TAG = "YuvImageTest";
    private int[] mFormats = { ImageFormat.NV21, ImageFormat.YUY2 };
    private static final int WIDTH = 256;
    private static final int HEIGHT = 128;
    private Bitmap[] mTestBitmaps = new Bitmap[1];
    private int[] mRectWidths = { 128, 124, 123 };
    private int[] mRectHeights = { 64, 60, 59 };
    private Rect[] mRects = { new Rect(0, 0, 0 + mRectWidths[0],  0 + mRectHeights[0]),
            new Rect(10, 10, 10 + mRectWidths[0], 10 + mRectHeights[0]),
            new Rect(0, 0, 0 + mRectWidths[1], 0 + mRectHeights[1]),
            new Rect(11, 11, 11 + mRectWidths[1], 11 + mRectHeights[1]) };
    private Rect[] mRectsShifted = { mRects[0], mRects[1] };
    private Rect mRectOddSides = new Rect(10, 10, 10 + mRectWidths[2],
            10 + mRectHeights[2]);
    private int[] mPaddings = { 0, 32 };
    private int mMseMargin = 3 * (15 * 15);
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "YuvImage",
        args = {byte[].class, int.class, int.class, int.class, int[].class}
    )
    public void testYuvImage(){
        int width = 100;
        int height = 100;
        byte[] yuv = new byte[width * height * 2];
        YuvImage image;
        for (int i = 0; i < mFormats.length; ++i) {
            try {
                image = new YuvImage(yuv, mFormats[i], width, height, null);
            } catch (Exception e) {
                Log.e(TAG, "unexpected exception", e);
                fail("unexpected exception");
            }
        }
        for (int i = 0; i < mFormats.length; ++i) {
            int[] expected = null;
            int[] actual = null;
            if (mFormats[i] == ImageFormat.NV21) {
                expected = new int[] {width, width};
            } else if (mFormats[i] == ImageFormat.YUY2) {
                expected = new int[] {width * 2};
            }
            try {
                image = new YuvImage(yuv, mFormats[i], width, height, null);
                actual = image.getStrides();
                assertTrue("default strides not calculated correctly",
                        Arrays.equals(expected, actual));
            } catch (Exception e){
                Log.e(TAG, "unexpected exception", e);
                fail("unexpected exception");
            }
        }
        int format = mFormats[0];
        try {
            image = new YuvImage(yuv, format, -1, height, null);
            fail("not catching illegal width");
        } catch(IllegalArgumentException e) {
        }
        try {
            image = new YuvImage(yuv, format, width, -1, null);
            fail("not catching illegal height");
        } catch(IllegalArgumentException e) {
        }
        try {
            image = new YuvImage(null, format, width, height, null);
            fail("not catching null yuv data");
        } catch(IllegalArgumentException e) {
        }
   }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "compressToJpeg",
        args = {android.graphics.Rect.class, int.class, java.io.OutputStream.class}
    )
    public void testCompressYuvToJpeg() {
        generateTestBitmaps(WIDTH, HEIGHT);
        checkParameters();
        for (int i = 0; i < mFormats.length; ++i) {
            for (int j = 0; j < mTestBitmaps.length; ++j) {
                for (int k = 0; k < mPaddings.length; ++k) {
                    YuvImage image = generateYuvImage(mFormats[i],
                        mTestBitmaps[j], mPaddings[k]);
                    for (int l = 0; l < mRects.length; ++l) {
                        compressRects(mTestBitmaps[j], image,
                                mRects[l], mRects[l]);
                    }
                    compressRects(mTestBitmaps[j], image, mRectsShifted[0],
                            mRectsShifted[1]);
                    compressOddRect(mTestBitmaps[j], image, mRectOddSides);
                }
            }
        }
    }
    private void generateTestBitmaps(int width, int height) {
        Bitmap dst = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(dst);
        Resources res = getContext().getResources();
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.testimage);
        c.drawBitmap(src, null, new Rect(0, 0, WIDTH, HEIGHT), null);
        mTestBitmaps[0] = dst;
    }
    private YuvImage generateYuvImage(int format, Bitmap bitmap, int paddings) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int stride = width + paddings;;
        YuvImage image = null;
        int[] argb = new int [stride * height];
        bitmap.getPixels(argb, 0, stride, 0, 0, width, height);
        byte[] yuv = convertArgbsToYuvs(argb, stride, height, format);
        int[] strides = null;
        if (format == ImageFormat.NV21) {
            strides = new int[] {stride, stride};
        } else if (format == ImageFormat.YUY2) {
           strides = new int[] {stride * 2};
        }
        image = new YuvImage(yuv, format, width, height, strides);
        return image;
    }
    private void compressRects(Bitmap testBitmap, YuvImage image,
            Rect rect1, Rect rect2) {
        Bitmap expected = null;
        Bitmap actual = null;
        boolean sameRect = rect1.equals(rect2) ? true : false;
        expected = Bitmap.createBitmap(testBitmap, rect1.left, rect1.top,
                rect1.width(), rect1.height());
        actual = compressDecompress(image, rect2);
        compareBitmaps(expected, actual, mMseMargin, sameRect);
    }
    private void compressOddRect(Bitmap testBitmap, YuvImage image,
            Rect rect) {
        Bitmap expected = null;
        Bitmap actual = null;
        actual = compressDecompress(image, rect);
        Rect newRect = rect;
        expected = Bitmap.createBitmap(testBitmap, newRect.left, newRect.top,
              newRect.width(), newRect.height());
        compareBitmaps(expected, actual, mMseMargin, true);
    }
    private Bitmap compressDecompress(YuvImage image, Rect rect) {
        Bitmap result = null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            boolean rt = image.compressToJpeg(rect, 90, stream);
            assertTrue("fail in compression", rt);
            byte[] jpegData = stream.toByteArray();
            result = BitmapFactory.decodeByteArray(jpegData, 0,
                    jpegData.length);
        } catch(Exception e){
            Log.e(TAG, "unexpected exception", e);
            fail("unexpected exception");
        }
        return result;
    }
    private byte[] convertArgbsToYuvs(int[] argb, int width, int height,
            int format) {
        byte[] yuv = new byte[width * height *
                ImageFormat.getBitsPerPixel(format)];
        if (format == ImageFormat.NV21) {
            int vuStart = width * height;
            byte[] yuvColor = new byte[3];
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    int idx = row * width + col;
                    argb2yuv(argb[idx], yuvColor);
                    yuv[idx] = yuvColor[0];
                    if ((row & 1) == 0 && (col & 1) == 0) {
                        int offset = row / 2 * width + col / 2 * 2;
                        yuv[vuStart + offset] = yuvColor[2];
                        yuv[vuStart + offset + 1] = yuvColor[1];
                    }
                }
            }
        } else if (format == ImageFormat.YUY2) {
            byte[] yuvColor0 = new byte[3];
            byte[] yuvColor1 = new byte[3];
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; col += 2) {
                    int idx = row * width + col;
                    argb2yuv(argb[idx], yuvColor0);
                    argb2yuv(argb[idx + 1], yuvColor1);
                    int offset = idx / 2 * 4;
                    yuv[offset] = yuvColor0[0];
                    yuv[offset + 1] = yuvColor0[1];
                    yuv[offset + 2] = yuvColor1[0];
                    yuv[offset + 3] = yuvColor0[2];
                }
            }
        }
        return yuv;
    }
    private void compareBitmaps(Bitmap expected, Bitmap actual,
            int mseMargin, boolean lessThanMargin) {
        assertEquals("mismatching widths", expected.getWidth(),
                actual.getWidth());
        assertEquals("mismatching heights", expected.getHeight(),
                actual.getHeight());
        double mse = 0;
        int width = expected.getWidth();
        int height = expected.getHeight();
        int[] expColors = new int [width * height];
        expected.getPixels(expColors, 0, width, 0, 0, width, height);
        int[] actualColors = new int [width * height];
        actual.getPixels(actualColors, 0, width, 0, 0, width, height);
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                int idx = row * width + col;
                mse += distance(expColors[idx], actualColors[idx]);
            }
        }
        mse /= width * height;
        Log.i(TAG, "MSE: " + mse);
        if (lessThanMargin) {
            assertTrue("MSE too large for normal case: " + mse,
                    mse <= mseMargin);
        } else {
            assertFalse("MSE too small for abnormal case: " + mse,
                    mse <= mseMargin);
        }
    }
    private double distance(int exp, int actual) {
        int r = Color.red(actual) - Color.red(exp);
        int g = Color.green(actual) - Color.green(exp);
        int b = Color.blue(actual) - Color.blue(exp);
        return r * r + g * g + b * b;
    }
    private void argb2yuv(int argb, byte[] yuv) {
        int r = Color.red(argb);
        int g = Color.green(argb);
        int b = Color.blue(argb);
        yuv[0] = (byte) ((CYR * r + CYG * g + CYB * b) >> CSHIFT);
        yuv[1] = (byte) (((CUR * r + CUG * g + CUB * b) >> CSHIFT) + 128);
        yuv[2] = (byte) (((CVR * r + CVG * g + CVB * b) >> CSHIFT) + 128);
    }
    private void checkParameters() {
        int format = ImageFormat.NV21;
        int[] argb = new int[WIDTH * HEIGHT];
        mTestBitmaps[0].getPixels(argb, 0, WIDTH, 0, 0, WIDTH, HEIGHT);
        byte[] yuv = convertArgbsToYuvs(argb, WIDTH, HEIGHT, format);
        YuvImage image = new YuvImage(yuv, format, WIDTH, HEIGHT, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try{
            Rect rect = new Rect(0, 0, WIDTH, HEIGHT);
            image.compressToJpeg(rect, 101, stream);
            fail("not catching illegal compression quality");
        } catch(IllegalArgumentException e){
        }
        try{
            Rect rect = new Rect(0, 0, WIDTH, HEIGHT);
            image.compressToJpeg(rect, -1, stream);
            fail("not catching illegal compression quality");
        } catch(IllegalArgumentException e){
        }
        try {
            Rect rect = new Rect(0, 0, WIDTH, HEIGHT);
            image.compressToJpeg(rect, 80, null);
            fail("not catching null stream");
        } catch(IllegalArgumentException e){
        }
        try {
            Rect rect = new Rect(10, 0, WIDTH, HEIGHT + 5);
            image.compressToJpeg(rect, 80, stream);
            fail("not catching illegal rectangular region");
        } catch(IllegalArgumentException e){
        }
    }
}

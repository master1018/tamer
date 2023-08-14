public class BitmapManagerUnitTests extends AndroidTestCase {
    IImageList mImageList;
    IImage mImage;
    BitmapManager mBitmapManager;
    Context mContext;
    private class DecodeThread extends Thread {
        Bitmap bitmap;
        public DecodeThread() {
        }
        @Override
        public void run() {
            bitmap = mImage.thumbBitmap(IImage.ROTATE_AS_NEEDED);
        }
        public Bitmap getBitmap() {
            return bitmap;
        }
    }
    @Override
    public void setUp() {
        mContext = getContext();
        mBitmapManager = BitmapManager.instance();
        mImageList = ImageManager.makeImageList(
                mContext.getContentResolver(),
                ImageManager.DataLocation.ALL,
                ImageManager.INCLUDE_IMAGES,
                ImageManager.SORT_DESCENDING,
                null);
        mImage = mImageList.getImageAt(0);
    }
    public void testSingleton() {
        BitmapManager manager = BitmapManager.instance();
        assertNotNull(manager);
        assertNotNull(mBitmapManager);
        assertSame(manager, mBitmapManager);
    }
    public void testCanThreadDecoding() {
        Thread t = new DecodeThread();
        assertTrue(mBitmapManager.canThreadDecoding(t));
        mBitmapManager.cancelThreadDecoding(t, mContext.getContentResolver());
        assertFalse(mBitmapManager.canThreadDecoding(t));
        mBitmapManager.allowThreadDecoding(t);
        assertTrue(mBitmapManager.canThreadDecoding(t));
    }
    public void testDefaultAllowDecoding() {
        DecodeThread t = new DecodeThread();
        try {
            t.start();
            t.join();
        } catch (InterruptedException ex) {
        } finally {
            assertNotNull(t.getBitmap());
        }
    }
    public void testCancelDecoding() {
        DecodeThread t = new DecodeThread();
        mBitmapManager.cancelThreadDecoding(t, mContext.getContentResolver());
        try {
            t.start();
            t.join();
        } catch (InterruptedException ex) {
        } finally {
            assertNull(t.getBitmap());
        }
    }
    public void testAllowDecoding() {
        DecodeThread t = new DecodeThread();
        mBitmapManager.cancelThreadDecoding(t, mContext.getContentResolver());
        mBitmapManager.allowThreadDecoding(t);
        try {
            t.start();
            t.join();
        } catch (InterruptedException ex) {
        } finally {
            assertNotNull(t.getBitmap());
        }
    }
    public void testThreadDecoding() {
        DecodeThread t1 = new DecodeThread();
        DecodeThread t2 = new DecodeThread();
        mBitmapManager.allowThreadDecoding(t1);
        mBitmapManager.cancelThreadDecoding(t2, mContext.getContentResolver());
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ex) {
        } finally {
            assertTrue(mBitmapManager.canThreadDecoding(t1));
            assertNotNull(t1.getBitmap());
            assertFalse(mBitmapManager.canThreadDecoding(t2));
            assertNull(t2.getBitmap());
        }
    }
    @Override
    public String toString() {
        return "BitmapManagerUnitTest";
    }
}

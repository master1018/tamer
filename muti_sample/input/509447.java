@TestTargetClass(BitmapFactory.class)
public class BitmapFactoryTest extends InstrumentationTestCase {
    private Resources mRes;
    private BitmapFactory.Options mOpt1;
    private BitmapFactory.Options mOpt2;
    private static final int START_HEIGHT = 31;
    private static final int START_WIDTH = 31;
    private int mDefaultDensity;
    private int mTargetDensity;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mRes = getInstrumentation().getTargetContext().getResources();
        mDefaultDensity = DisplayMetrics.DENSITY_DEFAULT;
        mTargetDensity = mRes.getDisplayMetrics().densityDpi;
        mOpt1 = new BitmapFactory.Options();
        mOpt1.inScaled = false;
        mOpt2 = new BitmapFactory.Options();
        mOpt2.inScaled = false;
        mOpt2.inJustDecodeBounds = true;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "BitmapFactory",
        args = {}
    )
    public void testConstructor() {
        new BitmapFactory();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeResource",
        args = {android.content.res.Resources.class, int.class,
                android.graphics.BitmapFactory.Options.class}
    )
    public void testDecodeResource1() {
        Bitmap b = BitmapFactory.decodeResource(mRes, R.drawable.start,
                mOpt1);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
        assertNull(BitmapFactory.decodeResource(mRes, R.drawable.start, mOpt2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeResource",
        args = {android.content.res.Resources.class, int.class}
    )
    public void testDecodeResource2() {
        Bitmap b = BitmapFactory.decodeResource(mRes, R.drawable.start);
        assertNotNull(b);
        assertEquals(START_HEIGHT * mTargetDensity / mDefaultDensity, b.getHeight(), 1.1);
        assertEquals(START_WIDTH * mTargetDensity / mDefaultDensity, b.getWidth(), 1.1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeByteArray",
        args = {byte[].class, int.class, int.class, android.graphics.BitmapFactory.Options.class}
    )
    public void testDecodeByteArray1() {
        byte[] array = obtainArray();
        Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length, mOpt1);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
        assertNull(BitmapFactory.decodeByteArray(array, 0, array.length, mOpt2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeByteArray",
        args = {byte[].class, int.class, int.class}
    )
    public void testDecodeByteArray2() {
        byte[] array = obtainArray();
        Bitmap b = BitmapFactory.decodeByteArray(array, 0, array.length);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeStream",
        args = {java.io.InputStream.class, android.graphics.Rect.class,
                android.graphics.BitmapFactory.Options.class}
    )
    public void testDecodeStream1() {
        InputStream is = obtainInputStream();
        Rect r = new Rect(1, 1, 1, 1);
        Bitmap b = BitmapFactory.decodeStream(is, r, mOpt1);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
        assertNull(BitmapFactory.decodeStream(is, r, mOpt2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeStream",
        args = {java.io.InputStream.class}
    )
    public void testDecodeStream2() {
        InputStream is = obtainInputStream();
        Bitmap b = BitmapFactory.decodeStream(is);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeFileDescriptor",
        args = {java.io.FileDescriptor.class, android.graphics.Rect.class,
                android.graphics.BitmapFactory.Options.class}
    )
    public void testDecodeFileDescriptor1() throws IOException {
        FileDescriptor input = obtainDescriptor(obtainPath());
        Rect r = new Rect(1, 1, 1, 1);
        Bitmap b = BitmapFactory.decodeFileDescriptor(input, r, mOpt1);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
        assertNull(BitmapFactory.decodeFileDescriptor(input, r, mOpt2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeFileDescriptor",
        args = {java.io.FileDescriptor.class}
    )
    public void testDecodeFileDescriptor2() throws IOException {
        FileDescriptor input = obtainDescriptor(obtainPath());
        Bitmap b = BitmapFactory.decodeFileDescriptor(input);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeFile",
        args = {java.lang.String.class, android.graphics.BitmapFactory.Options.class}
    )
    public void testDecodeFile1() throws IOException {
        Bitmap b = BitmapFactory.decodeFile(obtainPath(), mOpt1);
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
        assertNull(BitmapFactory.decodeFile(obtainPath(), mOpt2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "decodeFile",
        args = {java.lang.String.class}
    )
    public void testDecodeFile2() throws IOException {
        Bitmap b = BitmapFactory.decodeFile(obtainPath());
        assertNotNull(b);
        assertEquals(START_HEIGHT, b.getHeight());
        assertEquals(START_WIDTH, b.getWidth());
    }
    private byte[] obtainArray() {
        ByteArrayOutputStream stm = new ByteArrayOutputStream();
        Options opt = new BitmapFactory.Options();
        opt.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(mRes, R.drawable.start, opt);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stm);
        return(stm.toByteArray());
    }
    private InputStream obtainInputStream() {
        return mRes.openRawResource(R.drawable.start);
    }
    private FileDescriptor obtainDescriptor(String path) throws IOException {
      File file = new File(path);
      return(ParcelFileDescriptor.open(file,
              ParcelFileDescriptor.MODE_READ_ONLY).getFileDescriptor());
    }
    private String obtainPath() throws IOException {
        File dir = getInstrumentation().getTargetContext().getFilesDir();
        dir.mkdirs();
        File file = new File(dir, "test.jpg");
        if (!file.createNewFile()) {
            if (!file.exists()) {
                fail("Failed to create new File!");
            }
        }
        InputStream is = obtainInputStream();
        FileOutputStream fOutput = new FileOutputStream(file);
        byte[] dataBuffer = new byte[1024];
        int readLength = 0;
        while ((readLength = is.read(dataBuffer)) != -1) {
            fOutput.write(dataBuffer, 0, readLength);
        }
        is.close();
        fOutput.close();
        return (file.getPath());
    }
}

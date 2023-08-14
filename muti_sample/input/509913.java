public class GestureTest extends TestCase {
    private Gesture mGesture;
    private LineGestureStrokeHelper mLineHelper;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mGesture = new Gesture();
        mLineHelper = new LineGestureStrokeHelper();
    }
    public void testGetStrokes() {
        assertEquals(0, mGesture.getStrokes().size());
        GestureStroke line = mLineHelper.createLineGesture();
        mGesture.addStroke(line);
        assertEquals(1, mGesture.getStrokes().size());
        assertEquals(line, mGesture.getStrokes().get(0));
    }
    public void testGetStrokesCount() {
        assertEquals(0, mGesture.getStrokesCount());
        GestureStroke line = mLineHelper.createLineGesture();
        mGesture.addStroke(line);
        assertEquals(1, mGesture.getStrokesCount());
    }
    public void testGetBoundingBox() {
        mGesture.addStroke(mLineHelper.createLineGesture());
        mLineHelper.assertLineBoundingBox(mGesture.getBoundingBox());
    }
    public void testToPath() {
        mGesture.addStroke(mLineHelper.createLineGesture());
        mLineHelper.assertLineBoundingBox(mGesture.toPath());
    }
    public void testToPathPath() {
        mGesture.addStroke(mLineHelper.createLineGesture());
        mLineHelper.assertLineBoundingBox(mGesture.toPath(null));
        Path myPath = new Path();
        Path generatedPath = mGesture.toPath(myPath);
        assertTrue(myPath == generatedPath);
        mLineHelper.assertLineBoundingBox(generatedPath);
    }
    public void testReadWriteToParcel() {
        Parcel parcel = Parcel.obtain();
        mGesture.addStroke(mLineHelper.createLineGesture());
        mGesture.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        Gesture readGesture = Gesture.CREATOR.createFromParcel(parcel);
        new GestureComparator().assertGesturesEquals(mGesture, readGesture);
    }
}

public abstract class GestureStorageTester extends TestCase {
    private GestureStorageAccessor mFixture;
    protected Gesture mLineGesture;
    protected Gesture mAnotherGesture;
    protected static final String TEST_GESTURE_NAME ="cts-test-gesture";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFixture = createFixture();
        GestureStroke stroke = new LineGestureStrokeHelper().createLineGesture();
        mLineGesture = new Gesture();
        mLineGesture.addStroke(stroke);
        mAnotherGesture = new Gesture();
        mAnotherGesture.addStroke(stroke);
    }
    protected abstract GestureStorageAccessor createFixture();
    public void testGetGestureEntries() {
        assertEquals(0, mFixture.getGestureEntries().size());
        mFixture.addGesture(TEST_GESTURE_NAME, mLineGesture);
        assertEquals(1, mFixture.getGestureEntries().size());
        assertTrue(mFixture.getGestureEntries().contains(TEST_GESTURE_NAME));
    }
    public void testRecognize() {
        mFixture.addGesture(TEST_GESTURE_NAME, mLineGesture);
        Gesture newLineGesture = new Gesture();
        GestureStroke stroke = new LineGestureStrokeHelper().createLineGesture();
        newLineGesture.addStroke(stroke);
        ArrayList<Prediction> predictions = mFixture.recognize(newLineGesture);
        assertEquals(1, predictions.size());
        assertEquals(TEST_GESTURE_NAME, predictions.get(0).name);
    }
    public void testRemoveGesture() {
        mFixture.addGesture(TEST_GESTURE_NAME, mLineGesture);
        mFixture.addGesture(TEST_GESTURE_NAME, mAnotherGesture);
        mFixture.removeGesture(TEST_GESTURE_NAME, mAnotherGesture);
        assertFalse(mFixture.getGestures(TEST_GESTURE_NAME).contains(mAnotherGesture));
        mFixture.removeGesture(TEST_GESTURE_NAME, mLineGesture);
        assertFalse(mFixture.getGestureEntries().contains(TEST_GESTURE_NAME));
    }
    public void testRemoveEntry() {
        mFixture.addGesture(TEST_GESTURE_NAME, mLineGesture);
        mFixture.addGesture(TEST_GESTURE_NAME, mAnotherGesture);
        mFixture.removeEntry(TEST_GESTURE_NAME);
        assertFalse(mFixture.getGestureEntries().contains(TEST_GESTURE_NAME));
        assertNull(mFixture.getGestures(TEST_GESTURE_NAME));
    }
    public void testGetGestures() {
        assertNull(mFixture.getGestures(TEST_GESTURE_NAME));
        mFixture.addGesture(TEST_GESTURE_NAME, mLineGesture);
        assertTrue(mFixture.getGestures(TEST_GESTURE_NAME).contains(mLineGesture));
    }
}

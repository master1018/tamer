@TestTargetClass(GestureStore.class)
public class GestureStoreTest extends GestureStorageTester {
    private GestureStore mGestureStore = null;
    private static class GestureStoreFacade implements GestureStorageAccessor {
        private GestureStore mGestureStore;
        public GestureStoreFacade(GestureStore gestureStore) {
            mGestureStore = gestureStore;
        }
        public void addGesture(String entryName, Gesture gesture) {
            mGestureStore.addGesture(entryName, gesture);
        }
        public Set<String> getGestureEntries() {
            return mGestureStore.getGestureEntries();
        }
        public ArrayList<Gesture> getGestures(String entryName) {
            return mGestureStore.getGestures(entryName);
        }
        public int getOrientationStyle() {
            return mGestureStore.getOrientationStyle();
        }
        public int getSequenceType() {
            return mGestureStore.getSequenceType();
        }
        public ArrayList<Prediction> recognize(Gesture gesture) {
            return mGestureStore.recognize(gesture);
        }
        public void removeEntry(String entryName) {
            mGestureStore.removeEntry(entryName);
        }
        public void removeGesture(String entryName, Gesture gesture) {
            mGestureStore.removeGesture(entryName, gesture);
        }
        public void setOrientationStyle(int style) {
            mGestureStore.setOrientationStyle(style);
        }
        public void setSequenceType(int type) {
            mGestureStore.setSequenceType(type);
        }
    }
    @Override
    protected GestureStorageAccessor createFixture() {
        if (mGestureStore == null) {
            mGestureStore = new GestureStore();
        }
        return new GestureStoreFacade(mGestureStore);
    }
    public void testHasChanged_add() {
        assertFalse(mGestureStore.hasChanged());
        mGestureStore.addGesture(TEST_GESTURE_NAME, mLineGesture);
        assertTrue(mGestureStore.hasChanged());
    }
    public void testHasChanged_removeGesture() throws IOException {
        mGestureStore.addGesture(TEST_GESTURE_NAME, mLineGesture);
        mGestureStore.save(new ByteArrayOutputStream());
        assertFalse(mGestureStore.hasChanged());
        mGestureStore.removeGesture(TEST_GESTURE_NAME, mLineGesture);
        assertTrue(mGestureStore.hasChanged());
    }
    public void testHasChanged_removeEntry() throws IOException {
        mGestureStore.addGesture(TEST_GESTURE_NAME, mLineGesture);
        mGestureStore.save(new ByteArrayOutputStream());
        assertFalse(mGestureStore.hasChanged());
        mGestureStore.removeEntry(TEST_GESTURE_NAME);
        assertTrue(mGestureStore.hasChanged());
    }
    public void testSaveLoadOutputStream() throws IOException {
        ByteArrayOutputStream outStream = null;
        ByteArrayInputStream inStream = null;
        try {
            mGestureStore.addGesture(TEST_GESTURE_NAME, mLineGesture);
            outStream = new ByteArrayOutputStream();
            mGestureStore.save(outStream);
            inStream = new ByteArrayInputStream(outStream.toByteArray());
            GestureStore loadStore = new GestureStore();
            loadStore.load(inStream);
            assertEquals(mGestureStore.getOrientationStyle(), loadStore.getOrientationStyle());
            assertEquals(mGestureStore.getSequenceType(), loadStore.getSequenceType());
            assertEquals(mGestureStore.getGestureEntries(), loadStore.getGestureEntries());
            Gesture loadedGesture = loadStore.getGestures(TEST_GESTURE_NAME).get(0);
            new GestureComparator().assertGesturesEquals(mLineGesture, loadedGesture);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }
}

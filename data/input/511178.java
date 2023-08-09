@TestTargetClass(GestureLibrary.class)
public class GestureLibraryTest extends GestureStorageTester {
    private GestureLibrary mLibrary = null;
    private static class GestureLibraryFacade implements GestureStorageAccessor {
        private GestureLibrary mGestureLibrary;
        public GestureLibraryFacade(GestureLibrary gestureLibrary) {
            mGestureLibrary = gestureLibrary;
        }
        public void addGesture(String entryName, Gesture gesture) {
            mGestureLibrary.addGesture(entryName, gesture);
        }
        public Set<String> getGestureEntries() {
            return mGestureLibrary.getGestureEntries();
        }
        public ArrayList<Gesture> getGestures(String entryName) {
            return mGestureLibrary.getGestures(entryName);
        }
        public int getOrientationStyle() {
            return mGestureLibrary.getOrientationStyle();
        }
        public int getSequenceType() {
            return mGestureLibrary.getSequenceType();
        }
        public ArrayList<Prediction> recognize(Gesture gesture) {
            return mGestureLibrary.recognize(gesture);
        }
        public void removeEntry(String entryName) {
            mGestureLibrary.removeEntry(entryName);
        }
        public void removeGesture(String entryName, Gesture gesture) {
            mGestureLibrary.removeGesture(entryName, gesture);
        }
        public void setOrientationStyle(int style) {
            mGestureLibrary.setOrientationStyle(style);
        }
        public void setSequenceType(int type) {
            mGestureLibrary.setSequenceType(type);
        }
    }
    private static class GestureLibraryStub extends GestureLibrary {
        @Override
        public boolean load() {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean save() {
            throw new UnsupportedOperationException();
        }
    }
    @Override
    protected GestureStorageAccessor createFixture() {
        if (mLibrary == null) {
            mLibrary = new GestureLibraryStub();
        }
        return new GestureLibraryFacade(mLibrary);
    }
    public void testIsReadOnly() {
        assertFalse(mLibrary.isReadOnly());
    }
}

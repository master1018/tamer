public abstract class GestureLibrary {
    protected final GestureStore mStore;
    protected GestureLibrary() {
        mStore = new GestureStore();
    }
    public abstract boolean save();
    public abstract boolean load();
    public boolean isReadOnly() {
        return false;
    }
    public Learner getLearner() {
        return mStore.getLearner();
    }
    public void setOrientationStyle(int style) {
        mStore.setOrientationStyle(style);
    }
    public int getOrientationStyle() {
        return mStore.getOrientationStyle();
    }
    public void setSequenceType(int type) {
        mStore.setSequenceType(type);
    }
    public int getSequenceType() {
        return mStore.getSequenceType();
    }
    public Set<String> getGestureEntries() {
        return mStore.getGestureEntries();
    }
    public ArrayList<Prediction> recognize(Gesture gesture) {
        return mStore.recognize(gesture);
    }
    public void addGesture(String entryName, Gesture gesture) {
        mStore.addGesture(entryName, gesture);
    }
    public void removeGesture(String entryName, Gesture gesture) {
        mStore.removeGesture(entryName, gesture);
    }
    public void removeEntry(String entryName) {
        mStore.removeEntry(entryName);
    }
    public ArrayList<Gesture> getGestures(String entryName) {
        return mStore.getGestures(entryName);
    }
}

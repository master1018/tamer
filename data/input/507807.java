public class StorageListener extends StorageEventListener {
    private static final boolean localLOGV = true;
    public static final String TAG="StorageListener";
    String oldState;
    String newState;
    String path;
    private boolean doneFlag = false;
    @Override
    public void onStorageStateChanged(String path, String oldState, String newState) {
        if (localLOGV) Log.i(TAG, "Storage state changed from " + oldState + " to " + newState);
        synchronized (this) {
            this.oldState = oldState;
            this.newState = newState;
            this.path = path;
            doneFlag = true;
            notifyAll();
        }
    }
    public boolean isDone() {
        return doneFlag;
    }
}

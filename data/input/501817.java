public abstract class Resource {
    private boolean mTouched = true;
    public abstract FolderConfiguration getConfiguration();
    public final void touch() {
       mTouched = true; 
    }
    public final boolean isTouched() {
        return mTouched;
    }
    public final void resetTouch() {
        mTouched = false;
    }
}

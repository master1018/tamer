public abstract class BaseContactEditorView extends LinearLayout {
    protected LayoutInflater mInflater;
    protected PhotoEditorView mPhoto;
    protected boolean mHasPhotoEditor = false;
    public BaseContactEditorView(Context context) {
        super(context);
    }
    public BaseContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setPhotoBitmap(Bitmap bitmap) {
        mPhoto.setPhotoBitmap(bitmap);
    }
    public boolean hasPhotoEditor() {
        return mHasPhotoEditor;
    }
    public boolean hasSetPhoto() {
        return mPhoto.hasSetPhoto();
    }
    public PhotoEditorView getPhotoEditor() {
        return mPhoto;
    }
    public abstract long getRawContactId();
    public abstract void setState(EntityDelta state, ContactsSource source, ViewIdGenerator vig);
    public abstract void setNameEditorListener(EditorListener listener);
}

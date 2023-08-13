public class PhotoEditorView extends ImageView implements Editor, OnClickListener {
    private static final String TAG = "PhotoEditorView";
    private ValuesDelta mEntry;
    private EditorListener mListener;
    private boolean mHasSetPhoto = false;
    private boolean mReadOnly;
    public PhotoEditorView(Context context) {
        super(context);
    }
    public PhotoEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.setOnClickListener(this);
    }
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onRequest(EditorListener.REQUEST_PICK_PHOTO);
        }
    }
    public void onFieldChanged(String column, String value) {
        throw new UnsupportedOperationException("Photos don't support direct field changes");
    }
    public void setValues(DataKind kind, ValuesDelta values, EntityDelta state, boolean readOnly,
            ViewIdGenerator vig) {
        mEntry = values;
        mReadOnly = readOnly;
        setId(vig.getId(state, kind, values, 0));
        if (values != null) {
            final byte[] photoBytes = values.getAsByteArray(Photo.PHOTO);
            if (photoBytes != null) {
                final Bitmap photo = BitmapFactory.decodeByteArray(photoBytes, 0,
                        photoBytes.length);
                setScaleType(ImageView.ScaleType.CENTER_CROP);
                setImageBitmap(photo);
                setEnabled(true);
                mHasSetPhoto = true;
                mEntry.setFromTemplate(false);
            } else {
                resetDefault();
            }
        } else {
            resetDefault();
        }
    }
    public boolean hasSetPhoto() {
        return mHasSetPhoto;
    }
    public void setPhotoBitmap(Bitmap photo) {
        if (photo == null) {
            mEntry.put(Photo.PHOTO, (byte[])null);
            resetDefault();
            return;
        }
        final int size = photo.getWidth() * photo.getHeight() * 4;
        final ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            photo.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            mEntry.put(Photo.PHOTO, out.toByteArray());
            setImageBitmap(photo);
            setEnabled(true);
            mHasSetPhoto = true;
            mEntry.setFromTemplate(false);
            mEntry.put(Photo.IS_SUPER_PRIMARY, 1);
        } catch (IOException e) {
            Log.w(TAG, "Unable to serialize photo: " + e.toString());
        }
    }
    public void setSuperPrimary(boolean superPrimary) {
        mEntry.put(Photo.IS_SUPER_PRIMARY, superPrimary ? 1 : 0);
    }
    protected void resetDefault() {
        setScaleType(ImageView.ScaleType.CENTER);
        if (mReadOnly) {
            setImageResource(R.drawable.ic_contact_picture);
            setEnabled(false);
        } else {
            setImageResource(R.drawable.ic_menu_add_picture);
            setEnabled(true);
        }
        mHasSetPhoto = false;
        mEntry.setFromTemplate(true);
    }
    public void setEditorListener(EditorListener listener) {
        mListener = listener;
    }
}

public class ContactEditorView extends BaseContactEditorView implements OnClickListener {
    private TextView mReadOnly;
    private TextView mReadOnlyName;
    private View mPhotoStub;
    private GenericEditorView mName;
    private boolean mIsSourceReadOnly;
    private ViewGroup mGeneral;
    private ViewGroup mSecondary;
    private boolean mSecondaryVisible;
    private TextView mSecondaryHeader;
    private Drawable mSecondaryOpen;
    private Drawable mSecondaryClosed;
    private View mHeaderColorBar;
    private View mSideBar;
    private ImageView mHeaderIcon;
    private TextView mHeaderAccountType;
    private TextView mHeaderAccountName;
    private long mRawContactId = -1;
    public ContactEditorView(Context context) {
        super(context);
    }
    public ContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mPhoto = (PhotoEditorView)findViewById(R.id.edit_photo);
        mPhotoStub = findViewById(R.id.stub_photo);
        final int photoSize = getResources().getDimensionPixelSize(R.dimen.edit_photo_size);
        mReadOnly = (TextView)findViewById(R.id.edit_read_only);
        mName = (GenericEditorView)findViewById(R.id.edit_name);
        mName.setMinimumHeight(photoSize);
        mName.setDeletable(false);
        mReadOnlyName = (TextView) findViewById(R.id.read_only_name);
        mGeneral = (ViewGroup)findViewById(R.id.sect_general);
        mSecondary = (ViewGroup)findViewById(R.id.sect_secondary);
        mHeaderColorBar = findViewById(R.id.header_color_bar);
        mSideBar = findViewById(R.id.color_bar);
        mHeaderIcon = (ImageView) findViewById(R.id.header_icon);
        mHeaderAccountType = (TextView) findViewById(R.id.header_account_type);
        mHeaderAccountName = (TextView) findViewById(R.id.header_account_name);
        mSecondaryHeader = (TextView)findViewById(R.id.head_secondary);
        mSecondaryHeader.setOnClickListener(this);
        final Resources res = getResources();
        mSecondaryOpen = res.getDrawable(com.android.internal.R.drawable.expander_ic_maximized);
        mSecondaryClosed = res.getDrawable(com.android.internal.R.drawable.expander_ic_minimized);
        this.setSecondaryVisible(false);
    }
    public void onClick(View v) {
        final boolean makeVisible = mSecondary.getVisibility() != View.VISIBLE;
        this.setSecondaryVisible(makeVisible);
    }
    private void setSecondaryVisible(boolean makeVisible) {
        mSecondaryVisible = makeVisible;
        if (!mIsSourceReadOnly && mSecondary.getChildCount() > 0) {
            mSecondaryHeader.setVisibility(View.VISIBLE);
            mSecondaryHeader.setCompoundDrawablesWithIntrinsicBounds(
                    makeVisible ? mSecondaryOpen : mSecondaryClosed, null, null, null);
            mSecondary.setVisibility(makeVisible ? View.VISIBLE : View.GONE);
        } else {
            mSecondaryHeader.setVisibility(View.GONE);
            mSecondary.setVisibility(View.GONE);
        }
    }
    @Override
    public void setState(EntityDelta state, ContactsSource source, ViewIdGenerator vig) {
        mGeneral.removeAllViews();
        mSecondary.removeAllViews();
        if (state == null || source == null) return;
        setId(vig.getId(state, null, null, ViewIdGenerator.NO_VIEW_INDEX));
        mIsSourceReadOnly = source.readOnly;
        EntityModifier.ensureKindExists(state, source, StructuredName.CONTENT_ITEM_TYPE);
        ValuesDelta values = state.getValues();
        String accountName = values.getAsString(RawContacts.ACCOUNT_NAME);
        CharSequence accountType = source.getDisplayLabel(mContext);
        if (TextUtils.isEmpty(accountType)) {
            accountType = mContext.getString(R.string.account_phone);
        }
        if (!TextUtils.isEmpty(accountName)) {
            mHeaderAccountName.setText(
                    mContext.getString(R.string.from_account_format, accountName));
        }
        mHeaderAccountType.setText(mContext.getString(R.string.account_type_format, accountType));
        mHeaderIcon.setImageDrawable(source.getDisplayIcon(mContext));
        mRawContactId = values.getAsLong(RawContacts._ID);
        EntityModifier.ensureKindExists(state, source, Photo.CONTENT_ITEM_TYPE);
        mHasPhotoEditor = (source.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null);
        mPhoto.setVisibility(mHasPhotoEditor ? View.VISIBLE : View.GONE);
        mPhoto.setEnabled(!mIsSourceReadOnly);
        mName.setEnabled(!mIsSourceReadOnly);
        if (mIsSourceReadOnly) {
            mGeneral.setVisibility(View.GONE);
            mName.setVisibility(View.GONE);
            mReadOnly.setVisibility(View.VISIBLE);
            mReadOnly.setText(mContext.getString(R.string.contact_read_only, accountType));
            mReadOnlyName.setVisibility(View.VISIBLE);
        } else {
            mGeneral.setVisibility(View.VISIBLE);
            mName.setVisibility(View.VISIBLE);
            mReadOnly.setVisibility(View.GONE);
            mReadOnlyName.setVisibility(View.GONE);
        }
        boolean anySecondaryFieldFilled = false;
        for (DataKind kind : source.getSortedDataKinds()) {
            if (!kind.editable) continue;
            final String mimeType = kind.mimeType;
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                if (!mIsSourceReadOnly) {
                    mName.setValues(kind, primary, state, mIsSourceReadOnly, vig);
                } else {
                    String displayName = primary.getAsString(StructuredName.DISPLAY_NAME);
                    mReadOnlyName.setText(displayName);
                }
            } else if (Photo.CONTENT_ITEM_TYPE.equals(mimeType)) {
                final ValuesDelta primary = state.getPrimaryEntry(mimeType);
                mPhoto.setValues(kind, primary, state, mIsSourceReadOnly, vig);
                if (mIsSourceReadOnly && !mPhoto.hasSetPhoto()) {
                    mPhotoStub.setVisibility(View.GONE);
                } else {
                    mPhotoStub.setVisibility(View.VISIBLE);
                }
            } else if (!mIsSourceReadOnly) {
                if (kind.fieldList == null) continue;
                final ViewGroup parent = kind.secondary ? mSecondary : mGeneral;
                final KindSectionView section = (KindSectionView)mInflater.inflate(
                        R.layout.item_kind_section, parent, false);
                section.setState(kind, state, mIsSourceReadOnly, vig);
                if (kind.secondary && section.isAnyEditorFilledOut()) {
                    anySecondaryFieldFilled = true;
                }
                parent.addView(section);
            }
        }
        setSecondaryVisible(anySecondaryFieldFilled);
    }
    @Override
    public void setNameEditorListener(EditorListener listener) {
        mName.setEditorListener(listener);
    }
    @Override
    public long getRawContactId() {
        return mRawContactId;
    }
    private static class SavedState extends BaseSavedState {
        public boolean mSecondaryVisible;
        SavedState(Parcelable superState) {
            super(superState);
        }
        private SavedState(Parcel in) {
            super(in);
            mSecondaryVisible = (in.readInt() == 0 ? false : true);
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mSecondaryVisible ? 1 : 0);
        }
        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.mSecondaryVisible = mSecondaryVisible;
        return ss;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setSecondaryVisible(ss.mSecondaryVisible);
    }
}

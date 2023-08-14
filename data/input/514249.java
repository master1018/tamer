class ReadOnlyContactEditorView extends BaseContactEditorView {
    private View mPhotoStub;
    private TextView mName;
    private TextView mReadOnlyWarning;
    private ViewGroup mGeneral;
    private View mHeaderColorBar;
    private View mSideBar;
    private ImageView mHeaderIcon;
    private TextView mHeaderAccountType;
    private TextView mHeaderAccountName;
    private long mRawContactId = -1;
    public ReadOnlyContactEditorView(Context context) {
        super(context);
    }
    public ReadOnlyContactEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflater = (LayoutInflater)getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mPhoto = (PhotoEditorView)findViewById(R.id.edit_photo);
        mPhotoStub = findViewById(R.id.stub_photo);
        mName = (TextView) findViewById(R.id.read_only_name);
        mReadOnlyWarning = (TextView) findViewById(R.id.read_only_warning);
        mGeneral = (ViewGroup)findViewById(R.id.sect_general);
        mHeaderColorBar = findViewById(R.id.header_color_bar);
        mSideBar = findViewById(R.id.color_bar);
        mHeaderIcon = (ImageView) findViewById(R.id.header_icon);
        mHeaderAccountType = (TextView) findViewById(R.id.header_account_type);
        mHeaderAccountName = (TextView) findViewById(R.id.header_account_name);
    }
    @Override
    public void setState(EntityDelta state, ContactsSource source, ViewIdGenerator vig) {
        mGeneral.removeAllViews();
        if (state == null || source == null) return;
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
        ValuesDelta primary;
        DataKind kind = source.getKindForMimetype(Photo.CONTENT_ITEM_TYPE);
        if (kind != null) {
            EntityModifier.ensureKindExists(state, source, Photo.CONTENT_ITEM_TYPE);
            mHasPhotoEditor = (source.getKindForMimetype(Photo.CONTENT_ITEM_TYPE) != null);
            primary = state.getPrimaryEntry(Photo.CONTENT_ITEM_TYPE);
            mPhoto.setValues(kind, primary, state, source.readOnly, vig);
            if (!mHasPhotoEditor || !mPhoto.hasSetPhoto()) {
                mPhotoStub.setVisibility(View.GONE);
            } else {
                mPhotoStub.setVisibility(View.VISIBLE);
            }
        } else {
            mPhotoStub.setVisibility(View.VISIBLE);
        }
        primary = state.getPrimaryEntry(StructuredName.CONTENT_ITEM_TYPE);
        mName.setText(primary.getAsString(StructuredName.DISPLAY_NAME));
        mReadOnlyWarning.setText(mContext.getString(R.string.contact_read_only, accountType));
        ArrayList<ValuesDelta> phones = state.getMimeEntries(Phone.CONTENT_ITEM_TYPE);
        if (phones != null) {
            for (ValuesDelta phone : phones) {
                View field = mInflater.inflate(
                        R.layout.item_read_only_field, mGeneral, false);
                TextView v;
                v = (TextView) field.findViewById(R.id.label);
                v.setText(mContext.getText(R.string.phoneLabelsGroup));
                v = (TextView) field.findViewById(R.id.data);
                v.setText(PhoneNumberUtils.formatNumber(phone.getAsString(Phone.NUMBER)));
                mGeneral.addView(field);
            }
        }
        ArrayList<ValuesDelta> emails = state.getMimeEntries(Email.CONTENT_ITEM_TYPE);
        if (emails != null) {
            for (ValuesDelta email : emails) {
                View field = mInflater.inflate(
                        R.layout.item_read_only_field, mGeneral, false);
                TextView v;
                v = (TextView) field.findViewById(R.id.label);
                v.setText(mContext.getText(R.string.emailLabelsGroup));
                v = (TextView) field.findViewById(R.id.data);
                v.setText(email.getAsString(Email.DATA));
                mGeneral.addView(field);
            }
        }
        if (mGeneral.getChildCount() > 0) {
            mGeneral.setVisibility(View.VISIBLE);
        } else {
            mGeneral.setVisibility(View.GONE);
        }
    }
    @Override
    public void setNameEditorListener(EditorListener listener) {
    }
    @Override
    public long getRawContactId() {
        return mRawContactId;
    }
}

public class EditPhoneNumberPreference extends EditTextPreference {
    private static final int CM_CONFIRM = 0;
    private static final int CM_ACTIVATION = 1;
    private int mConfirmationMode;
    private static final String VALUE_SEPARATOR = ":";
    private static final String VALUE_OFF = "0";
    private static final String VALUE_ON = "1";
    private ImageButton mContactPickButton;
    private View.OnFocusChangeListener mDialogFocusChangeListener;
    private OnDialogClosedListener mDialogOnClosedListener;
    private GetDefaultNumberListener mGetDefaultNumberListener;
    private Activity mParentActivity;
    private Intent mContactListIntent;
    private int mPrefId;
    private CharSequence mEnableText;
    private CharSequence mDisableText;
    private CharSequence mChangeNumberText;
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private int mButtonClicked;
    private String mPhoneNumber;
    private boolean mChecked;
    interface OnDialogClosedListener {
        void onDialogClosed(EditPhoneNumberPreference preference, int buttonClicked);
    }
    interface GetDefaultNumberListener {
        String onGetDefaultNumber(EditPhoneNumberPreference preference);
    }
    public EditPhoneNumberPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.pref_dialog_editphonenumber);
        mContactListIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mContactListIntent.setType(Phone.CONTENT_ITEM_TYPE);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.EditPhoneNumberPreference, 0, R.style.EditPhoneNumberPreference);
        mEnableText = a.getString(R.styleable.EditPhoneNumberPreference_enableButtonText);
        mDisableText = a.getString(R.styleable.EditPhoneNumberPreference_disableButtonText);
        mChangeNumberText = a.getString(R.styleable.EditPhoneNumberPreference_changeNumButtonText);
        mConfirmationMode = a.getInt(R.styleable.EditPhoneNumberPreference_confirmMode, 0);
        a.recycle();
        a = context.obtainStyledAttributes(attrs, android.R.styleable.CheckBoxPreference, 0, 0);
        mSummaryOn = a.getString(android.R.styleable.CheckBoxPreference_summaryOn);
        mSummaryOff = a.getString(android.R.styleable.CheckBoxPreference_summaryOff);
        a.recycle();
    }
    public EditPhoneNumberPreference(Context context) {
        this(context, null);
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        if (summaryView != null) {
            CharSequence sum;
            int vis;
            if (mConfirmationMode == CM_ACTIVATION) {
                if (mChecked) {
                    sum = (mSummaryOn == null) ? getSummary() : mSummaryOn;
                } else {
                    sum = (mSummaryOff == null) ? getSummary() : mSummaryOff;
                }
            } else {
                sum = getSummary();
            }
            if (sum != null) {
                summaryView.setText(sum);
                vis = View.VISIBLE;
            } else {
                vis = View.GONE;
            }
            if (vis != summaryView.getVisibility()) {
                summaryView.setVisibility(vis);
            }
        }
    }
    @Override
    protected void onBindDialogView(View view) {
        mButtonClicked = DialogInterface.BUTTON2;
        super.onBindDialogView(view);
        EditText editText = getEditText();
        mContactPickButton = (ImageButton) view.findViewById(R.id.select_contact);
        if (editText != null) {
            if (mGetDefaultNumberListener != null) {
                String defaultNumber = mGetDefaultNumberListener.onGetDefaultNumber(this);
                if (defaultNumber != null) {
                    mPhoneNumber = defaultNumber;
                }
            }
            editText.setText(mPhoneNumber);
            editText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
            editText.setKeyListener(DialerKeyListener.getInstance());
            editText.setOnFocusChangeListener(mDialogFocusChangeListener);
        }
        if (mContactPickButton != null) {
            mContactPickButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (mParentActivity != null) {
                        mParentActivity.startActivityForResult(mContactListIntent, mPrefId);
                    }
                }
            });
        }
    }
    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        ViewGroup container = (ViewGroup) dialogView
                .findViewById(R.id.edit_container);
        if (container != null) {
            container.addView(editText, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (mConfirmationMode == CM_ACTIVATION) {
            if (mChecked) {
                builder.setPositiveButton(mChangeNumberText, this);
                builder.setNeutralButton(mDisableText, this);
            } else {
                builder.setPositiveButton(null, null);
                builder.setNeutralButton(mEnableText, this);
            }
        }
        builder.setIcon(R.drawable.ic_dialog_call);
    }
    public void setDialogOnFocusChangeListener(View.OnFocusChangeListener l) {
        mDialogFocusChangeListener = l;
    }
    public void setDialogOnClosedListener(OnDialogClosedListener l) {
        mDialogOnClosedListener = l;
    }
    public void setParentActivity(Activity parent, int identifier) {
        mParentActivity = parent;
        mPrefId = identifier;
        mGetDefaultNumberListener = null;
    }
    public void setParentActivity(Activity parent, int identifier, GetDefaultNumberListener l) {
        mParentActivity = parent;
        mPrefId = identifier;
        mGetDefaultNumberListener = l;
    }
    public void onPickActivityResult(String pickedValue) {
        EditText editText = getEditText();
        if (editText != null) {
            editText.setText(pickedValue);
        }
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if ((mConfirmationMode == CM_ACTIVATION) && (which == DialogInterface.BUTTON3)) {
            setToggled(!isToggled());
        }
        mButtonClicked = which;
        super.onClick(dialog, which);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if ((mButtonClicked == DialogInterface.BUTTON1) ||
                (mButtonClicked == DialogInterface.BUTTON3)){
            setPhoneNumber(getEditText().getText().toString());
            super.onDialogClosed(positiveResult);
            setText(getStringValue());
        } else {
            super.onDialogClosed(positiveResult);
        }
        if (mDialogOnClosedListener != null) {
            mDialogOnClosedListener.onDialogClosed(this, mButtonClicked);
        }
    }
    public boolean isToggled() {
        return mChecked;
    }
    public EditPhoneNumberPreference setToggled(boolean checked) {
        mChecked = checked;
        setText(getStringValue());
        notifyChanged();
        return this;
    }
    public String getPhoneNumber() {
        return PhoneNumberUtils.stripSeparators(mPhoneNumber);
    }
    protected String getRawPhoneNumber() {
        return mPhoneNumber;
    }
    public EditPhoneNumberPreference setPhoneNumber(String number) {
        mPhoneNumber = number;
        setText(getStringValue());
        notifyChanged();
        return this;
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValueFromString(restoreValue ? getPersistedString(getStringValue())
                : (String) defaultValue);
    }
    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = false;
        if ((mConfirmationMode == CM_ACTIVATION) && (mEncodedText != null)) {
            String[] inValues = mEncodedText.split(":", 2);
            shouldDisable = inValues[0].equals(VALUE_ON);
        } else {
            shouldDisable = (TextUtils.isEmpty(mPhoneNumber) && (mConfirmationMode == CM_CONFIRM));
        }
        return shouldDisable;
    }
    private String mEncodedText = null;
    @Override
    protected boolean persistString(String value) {
        mEncodedText = value;
        return super.persistString(value);
    }
    public EditPhoneNumberPreference setSummaryOn(CharSequence summary) {
        mSummaryOn = summary;
        if (isToggled()) {
            notifyChanged();
        }
        return this;
    }
    public EditPhoneNumberPreference setSummaryOn(int summaryResId) {
        return setSummaryOn(getContext().getString(summaryResId));
    }
    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }
    public EditPhoneNumberPreference setSummaryOff(CharSequence summary) {
        mSummaryOff = summary;
        if (!isToggled()) {
            notifyChanged();
        }
        return this;
    }
    public EditPhoneNumberPreference setSummaryOff(int summaryResId) {
        return setSummaryOff(getContext().getString(summaryResId));
    }
    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }
    protected void setValueFromString(String value) {
        String[] inValues = value.split(":", 2);
        setToggled(inValues[0].equals(VALUE_ON));
        setPhoneNumber(inValues[1]);
    }
    protected String getStringValue() {
        return ((isToggled() ? VALUE_ON : VALUE_OFF) + VALUE_SEPARATOR + getPhoneNumber());
    }
    public void showPhoneNumberDialog() {
        showDialog(null);
    }
}

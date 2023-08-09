public class CheckBoxPreference extends Preference {
    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    private boolean mChecked;
    private boolean mSendAccessibilityEventViewClickedType;
    private AccessibilityManager mAccessibilityManager;
    private boolean mDisableDependentsState;
    public CheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                com.android.internal.R.styleable.CheckBoxPreference, defStyle, 0);
        mSummaryOn = a.getString(com.android.internal.R.styleable.CheckBoxPreference_summaryOn);
        mSummaryOff = a.getString(com.android.internal.R.styleable.CheckBoxPreference_summaryOff);
        mDisableDependentsState = a.getBoolean(
                com.android.internal.R.styleable.CheckBoxPreference_disableDependentsState, false);
        a.recycle();
        mAccessibilityManager =
            (AccessibilityManager) getContext().getSystemService(Service.ACCESSIBILITY_SERVICE);
    }
    public CheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.checkBoxPreferenceStyle);
    }
    public CheckBoxPreference(Context context) {
        this(context, null);
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkboxView = view.findViewById(com.android.internal.R.id.checkbox);
        if (checkboxView != null && checkboxView instanceof Checkable) {
            ((Checkable) checkboxView).setChecked(mChecked);
            if (mSendAccessibilityEventViewClickedType &&
                    mAccessibilityManager.isEnabled() &&
                    checkboxView.isEnabled()) {
                mSendAccessibilityEventViewClickedType = false;
                int eventType = AccessibilityEvent.TYPE_VIEW_CLICKED;
                checkboxView.sendAccessibilityEventUnchecked(AccessibilityEvent.obtain(eventType));
            }
        }
        TextView summaryView = (TextView) view.findViewById(com.android.internal.R.id.summary);
        if (summaryView != null) {
            boolean useDefaultSummary = true;
            if (mChecked && mSummaryOn != null) {
                summaryView.setText(mSummaryOn);
                useDefaultSummary = false;
            } else if (!mChecked && mSummaryOff != null) {
                summaryView.setText(mSummaryOff);
                useDefaultSummary = false;
            }
            if (useDefaultSummary) {
                final CharSequence summary = getSummary();
                if (summary != null) {
                    summaryView.setText(summary);
                    useDefaultSummary = false;
                }
            }
            int newVisibility = View.GONE;
            if (!useDefaultSummary) {
                newVisibility = View.VISIBLE;
            }
            if (newVisibility != summaryView.getVisibility()) {
                summaryView.setVisibility(newVisibility);
            }
        }
    }
    @Override
    protected void onClick() {
        super.onClick();
        boolean newValue = !isChecked();
        mSendAccessibilityEventViewClickedType = true;
        if (!callChangeListener(newValue)) {
            return;
        }
        setChecked(newValue);
    }
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            persistBoolean(checked);
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }
    public boolean isChecked() {
        return mChecked;
    }
    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = mDisableDependentsState ? mChecked : !mChecked;
        return shouldDisable || super.shouldDisableDependents();
    }
    public void setSummaryOn(CharSequence summary) {
        mSummaryOn = summary;
        if (isChecked()) {
            notifyChanged();
        }
    }
    public void setSummaryOn(int summaryResId) {
        setSummaryOn(getContext().getString(summaryResId));
    }
    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }
    public void setSummaryOff(CharSequence summary) {
        mSummaryOff = summary;
        if (!isChecked()) {
            notifyChanged();
        }
    }
    public void setSummaryOff(int summaryResId) {
        setSummaryOff(getContext().getString(summaryResId));
    }
    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }
    public boolean getDisableDependentsState() {
        return mDisableDependentsState;
    }
    public void setDisableDependentsState(boolean disableDependentsState) {
        mDisableDependentsState = disableDependentsState;
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(restoreValue ? getPersistedBoolean(mChecked)
                : (Boolean) defaultValue);
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.checked = isChecked();
        return myState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.checked);
    }
    private static class SavedState extends BaseSavedState {
        boolean checked;
        public SavedState(Parcel source) {
            super(source);
            checked = source.readInt() == 1;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(checked ? 1 : 0);
        }
        public SavedState(Parcelable superState) {
            super(superState);
        }
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}

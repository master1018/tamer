public class YesNoPreference extends DialogPreference {
    private boolean mWasPositiveResult;
    public YesNoPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public YesNoPreference(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.yesNoPreferenceStyle);
    }
    public YesNoPreference(Context context) {
        this(context, null);
    }
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (callChangeListener(positiveResult)) {
            setValue(positiveResult);
        }
    }
    public void setValue(boolean value) {
        mWasPositiveResult = value;
        persistBoolean(value);
        notifyDependencyChange(!value);
    }
    public boolean getValue() {
        return mWasPositiveResult;
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedBoolean(mWasPositiveResult) :
            (Boolean) defaultValue);
    }
    @Override
    public boolean shouldDisableDependents() {
        return !mWasPositiveResult || super.shouldDisableDependents();
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.wasPositiveResult = getValue();
        return myState;
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setValue(myState.wasPositiveResult);
    }
    private static class SavedState extends BaseSavedState {
        boolean wasPositiveResult;
        public SavedState(Parcel source) {
            super(source);
            wasPositiveResult = source.readInt() == 1;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(wasPositiveResult ? 1 : 0);
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

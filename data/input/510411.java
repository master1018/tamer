public class MyPreference extends Preference {
    private int mClickCounter;
    public MyPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_widget_mypreference);        
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        final TextView myTextView = (TextView) view.findViewById(R.id.mypreference_widget);
        if (myTextView != null) {
            myTextView.setText(String.valueOf(mClickCounter));
        }
    }
    @Override
    protected void onClick() {
        int newValue = mClickCounter + 1;
        if (!callChangeListener(newValue)) {
            return;
        }
        mClickCounter = newValue;
        persistInt(mClickCounter);
        notifyChanged();
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInteger(index, 0);
    }
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            mClickCounter = getPersistedInt(mClickCounter);
        } else {
            int value = (Integer) defaultValue;
            mClickCounter = value;
            persistInt(value);
        }
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        final SavedState myState = new SavedState(superState);
        myState.clickCounter = mClickCounter;
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
        mClickCounter = myState.clickCounter;
        notifyChanged();
    }
    private static class SavedState extends BaseSavedState {
        int clickCounter;
        public SavedState(Parcel source) {
            super(source);
            clickCounter = source.readInt();
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(clickCounter);
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

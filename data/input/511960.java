public abstract class AbsSavedState implements Parcelable {
    public static final AbsSavedState EMPTY_STATE = new AbsSavedState() {};
    private final Parcelable mSuperState;
    private AbsSavedState() {
        mSuperState = null;
    }
    protected AbsSavedState(Parcelable superState) {
        if (superState == null) {
            throw new IllegalArgumentException("superState must not be null");
        }
        mSuperState = superState != EMPTY_STATE ? superState : null;
    }
    protected AbsSavedState(Parcel source) {
        Parcelable superState = (Parcelable) source.readParcelable(null);
        mSuperState = superState != null ? superState : EMPTY_STATE;
    }
    final public Parcelable getSuperState() {
        return mSuperState;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
         dest.writeParcelable(mSuperState, flags);
    }
    public static final Parcelable.Creator<AbsSavedState> CREATOR 
        = new Parcelable.Creator<AbsSavedState>() {
        public AbsSavedState createFromParcel(Parcel in) {
            Parcelable superState = (Parcelable) in.readParcelable(null);
            if (superState != null) {
                throw new IllegalStateException("superState must be null");
            }
            return EMPTY_STATE;
        }
        public AbsSavedState[] newArray(int size) {
            return new AbsSavedState[size];
        }
    };
}

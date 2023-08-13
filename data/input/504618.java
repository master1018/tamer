public final class InputBindResult implements Parcelable {
    static final String TAG = "InputBindResult";
    public final IInputMethodSession method;
    public final String id;
    public final int sequence;
    public InputBindResult(IInputMethodSession _method, String _id, int _sequence) {
        method = _method;
        id = _id;
        sequence = _sequence;
    }
    InputBindResult(Parcel source) {
        method = IInputMethodSession.Stub.asInterface(source.readStrongBinder());
        id = source.readString();
        sequence = source.readInt();
    }
    @Override
    public String toString() {
        return "InputBindResult{" + method + " " + id
                + " #" + sequence + "}";
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongInterface(method);
        dest.writeString(id);
        dest.writeInt(sequence);
    }
    public static final Parcelable.Creator<InputBindResult> CREATOR = new Parcelable.Creator<InputBindResult>() {
        public InputBindResult createFromParcel(Parcel source) {
            return new InputBindResult(source);
        }
        public InputBindResult[] newArray(int size) {
            return new InputBindResult[size];
        }
    };
    public int describeContents() {
        return 0;
    }
}

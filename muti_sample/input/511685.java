public final class InputBinding implements Parcelable {
    static final String TAG = "InputBinding";
    final InputConnection mConnection;
    final IBinder mConnectionToken;
    final int mUid;
    final int mPid;
    public InputBinding(InputConnection conn, IBinder connToken,
            int uid, int pid) {
        mConnection = conn;
        mConnectionToken = connToken;
        mUid = uid;
        mPid = pid;
    }
    public InputBinding(InputConnection conn, InputBinding binding) {
        mConnection = conn;
        mConnectionToken = binding.getConnectionToken();
        mUid = binding.getUid();
        mPid = binding.getPid();
    }
    InputBinding(Parcel source) {
        mConnection = null;
        mConnectionToken = source.readStrongBinder();
        mUid = source.readInt();
        mPid = source.readInt();
    }
    public InputConnection getConnection() {
        return mConnection;
    }
    public IBinder getConnectionToken() {
        return mConnectionToken;
    }
    public int getUid() {
        return mUid;
    }
    public int getPid() {
        return mPid;
    }
    @Override
    public String toString() {
        return "InputBinding{" + mConnectionToken
                + " / uid " + mUid + " / pid " + mPid + "}";
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mConnectionToken);
        dest.writeInt(mUid);
        dest.writeInt(mPid);
    }
    public static final Parcelable.Creator<InputBinding> CREATOR = new Parcelable.Creator<InputBinding>() {
        public InputBinding createFromParcel(Parcel source) {
            return new InputBinding(source);
        }
        public InputBinding[] newArray(int size) {
            return new InputBinding[size];
        }
    };
    public int describeContents() {
        return 0;
    }
}

public class NetworkInfo implements Parcelable {
    public enum State {
        UNKNOWN,
        AVAILABLE,
        CURRENT,
        FORBIDDEN;
    }
    String operatorAlphaLong;
    String operatorAlphaShort;
    String operatorNumeric;
    State state = State.UNKNOWN;
    public String
    getOperatorAlphaLong() {
        return operatorAlphaLong;
    }
    public String
    getOperatorAlphaShort() {
        return operatorAlphaShort;
    }
    public String
    getOperatorNumeric() {
        return operatorNumeric;
    }
    public State
    getState() {
        return state;
    }
    NetworkInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                State state) {
        this.operatorAlphaLong = operatorAlphaLong;
        this.operatorAlphaShort = operatorAlphaShort;
        this.operatorNumeric = operatorNumeric;
        this.state = state;
    }
    public NetworkInfo(String operatorAlphaLong,
                String operatorAlphaShort,
                String operatorNumeric,
                String stateString) {
        this (operatorAlphaLong, operatorAlphaShort,
                operatorNumeric, rilStateToState(stateString));
    }
    private static State rilStateToState(String s) {
        if (s.equals("unknown")) {
            return State.UNKNOWN;
        } else if (s.equals("available")) {
            return State.AVAILABLE;
        } else if (s.equals("current")) {
            return State.CURRENT;
        } else if (s.equals("forbidden")) {
            return State.FORBIDDEN;
        } else {
            throw new RuntimeException(
                "RIL impl error: Invalid network state '" + s + "'");
        }
    }
    public String toString() {
        return "NetworkInfo " + operatorAlphaLong
                + "/" + operatorAlphaShort
                + "/" + operatorNumeric
                + "/" + state;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(operatorAlphaLong);
        dest.writeString(operatorAlphaShort);
        dest.writeString(operatorNumeric);
        dest.writeSerializable(state);
    }
    public static final Creator<NetworkInfo> CREATOR =
        new Creator<NetworkInfo>() {
            public NetworkInfo createFromParcel(Parcel in) {
                NetworkInfo netInfo = new NetworkInfo(
                        in.readString(), 
                        in.readString(), 
                        in.readString(), 
                        (State) in.readSerializable()); 
                return netInfo;
            }
            public NetworkInfo[] newArray(int size) {
                return new NetworkInfo[size];
            }
        };
}

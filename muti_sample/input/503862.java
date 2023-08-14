public class ImErrorInfo implements Parcelable, Serializable {
    public static final int NO_ERROR = 0;
    public static final int ILLEGAL_CONTACT_LIST_MANAGER_STATE = -100;
    public static final int CONTACT_LIST_EXISTS = -101;
    public static final int CONTACT_LIST_NOT_FOUND = -102;
    public static final int INVALID_HOST_NAME = -200;
    public static final int UNKNOWN_SERVER = -201;
    public static final int CANT_CONNECT_TO_SERVER = -202;
    public static final int INVALID_USERNAME = -203;
    public static final int INVALID_SESSION_CONTEXT = -204;
    public static final int UNKNOWN_LOGIN_ERROR = -300;
    public static final int NOT_LOGGED_IN = 301;
    public static final int UNSUPPORTED_CIR_CHANNEL = -400;
    public static final int ILLEGAL_CONTACT_ADDRESS = -500;
    public static final int CONTACT_EXISTS_IN_LIST =  -501;
    public static final int CANT_ADD_BLOCKED_CONTACT = -600;
    public static final int PARSER_ERROR = -700;
    public static final int SERIALIZER_ERROR = -750;
    public static final int NETWORK_ERROR = -800;
    public static final int ILLEGAL_SERVER_RESPONSE = -900;
    public static final int UNKNOWN_ERROR = -1000;
    private int mCode;
    private String mDescription;
    public ImErrorInfo(int code, String description) {
        mCode = code;
        mDescription = description;
    }
    public ImErrorInfo(Parcel source) {
        mCode = source.readInt();
        mDescription = source.readString();
    }
    public int getCode() {
        return mCode;
    }
    public String getDescription() {
        return mDescription;
    }
    @Override
    public String toString() {
        return mCode + " - " + mDescription;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCode);
        dest.writeString(mDescription);
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<ImErrorInfo> CREATOR = new Parcelable.Creator<ImErrorInfo>() {
        public ImErrorInfo createFromParcel(Parcel source) {
            return new ImErrorInfo(source);
        }
        public ImErrorInfo[] newArray(int size) {
            return new ImErrorInfo[size];
        }
    };
}

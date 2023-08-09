public final class Presence implements Parcelable {
    public static final int OFFLINE = 0;
    public static final int DO_NOT_DISTURB = 1;
    public static final int AWAY = 2;
    public static final int IDLE = 3;
    public static final int AVAILABLE = 4;
    public static final int CLIENT_TYPE_DEFAULT = 0;
    public static final int CLIENT_TYPE_MOBILE = 1;
    private int mStatus;
    private String mStatusText;
    private byte[] mAvatarData;
    private String mAvatarType;
    private int mClientType;
    private Map<String, String> mExtendedInfo;
    public Presence() {
        this(Presence.OFFLINE, null, null, null, CLIENT_TYPE_DEFAULT, null);
    }
    public Presence(int status, String statusText, byte[] avatarData,
            String avatarType, int clientType) {
        this(status, statusText, avatarData, avatarType, clientType, null);
    }
    public Presence(int status, String statusText, byte[] avatarData,
            String avatarType, int clientType, Map<String, String> extendedInfo) {
        setStatus(status);
        mStatusText = statusText;
        setAvatar(avatarData, avatarType);
        mClientType = clientType;
        mExtendedInfo = extendedInfo;
    }
    public Presence(Presence p) {
        this(p.mStatus, p.mStatusText, p.mAvatarData, p.mAvatarType,
                p.mClientType, p.mExtendedInfo);
    }
    public Presence(Parcel source) {
        mStatus = source.readInt();
        mStatusText = source.readString();
        mAvatarData = source.createByteArray();
        mAvatarType = source.readString();
        mClientType = source.readInt();
        mExtendedInfo = source.readHashMap(null);
    }
    public byte[] getAvatarData() {
        if(mAvatarData == null){
            return null;
        } else {
            byte[] data = new byte[mAvatarData.length];
            System.arraycopy(mAvatarData, 0, data, 0, mAvatarData.length);
            return data;
        }
    }
    public String getAvatarType() {
        return mAvatarType;
    }
    public int getClientType() {
        return mClientType;
    }
    public Map<String, String> getExtendedInfo() {
        return mExtendedInfo == null ? null : Collections.unmodifiableMap(mExtendedInfo);
    }
    public boolean isOnline() {
        return mStatus != OFFLINE;
    }
    public int getStatus() {
        return mStatus;
    }
    public void setStatus(int status) {
        if (status < OFFLINE || status > AVAILABLE ) {
            throw new IllegalArgumentException("invalid presence status value");
        }
        mStatus = status;
    }
    public String getStatusText() {
        return mStatusText;
    }
    public void setAvatar(byte[] data, String type) {
        if(data != null) {
            mAvatarData = new byte[data.length];
            System.arraycopy(data, 0, mAvatarData, 0, data.length);
        } else {
            mAvatarData = null;
        }
        mAvatarType = type;
    }
    public void setStatusText(String statusText) {
        mStatusText = statusText;
    }
    public void setClientType(int clientType) {
        mClientType = clientType;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mStatus);
        dest.writeString(mStatusText);
        dest.writeByteArray(mAvatarData);
        dest.writeString(mAvatarType);
        dest.writeInt(mClientType);
        dest.writeMap(mExtendedInfo);
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<Presence> CREATOR = new Parcelable.Creator<Presence>() {
        public Presence createFromParcel(Parcel source) {
            return new Presence(source);
        }
        public Presence[] newArray(int size) {
            return new Presence[size];
        }
    };
}

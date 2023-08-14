public class NeighboringCellInfo implements Parcelable
{
    static final public int UNKNOWN_RSSI = 99;
    static final public int UNKNOWN_CID = -1;
    private int mRssi;
    private int mCid;
    private int mLac;
    private int mPsc;
    private int mNetworkType;
    @Deprecated
    public NeighboringCellInfo() {
        mRssi = UNKNOWN_RSSI;
        mLac = UNKNOWN_CID;
        mCid = UNKNOWN_CID;
        mPsc = UNKNOWN_CID;
        mNetworkType = NETWORK_TYPE_UNKNOWN;
    }
    @Deprecated
    public NeighboringCellInfo(int rssi, int cid) {
        mRssi = rssi;
        mCid = cid;
    }
    public NeighboringCellInfo(int rssi, String location, int radioType) {
        mRssi = rssi;
        mNetworkType = NETWORK_TYPE_UNKNOWN;
        mPsc = UNKNOWN_CID;
        mLac = UNKNOWN_CID;
        mCid = UNKNOWN_CID;
        int l = location.length();
        if (l > 8) return;
        if (l < 8) {
            for (int i = 0; i < (8-l); i++) {
                location = "0" + location;
            }
        }
        try {
            switch (radioType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
                mNetworkType = radioType;
                mLac = Integer.valueOf(location.substring(0, 4), 16);
                mCid = Integer.valueOf(location.substring(4), 16);
                break;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
                mNetworkType = radioType;
                mPsc = Integer.valueOf(location, 16);
                break;
            }
        } catch (NumberFormatException e) {
            mPsc = UNKNOWN_CID;
            mLac = UNKNOWN_CID;
            mCid = UNKNOWN_CID;
            mNetworkType = NETWORK_TYPE_UNKNOWN;
        }
    }
    public NeighboringCellInfo(Parcel in) {
        mRssi = in.readInt();
        mLac = in.readInt();
        mCid = in.readInt();
        mPsc = in.readInt();
        mNetworkType = in.readInt();
    }
    public int getRssi() {
        return mRssi;
    }
    public int getLac() {
        return mLac;
    }
    public int getCid() {
        return mCid;
    }
    public int getPsc() {
        return mPsc;
    }
    public int getNetworkType() {
        return mNetworkType;
    }
    @Deprecated
    public void setCid(int cid) {
        mCid = cid;
    }
    @Deprecated
    public void setRssi(int rssi) {
        mRssi = rssi;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (mPsc != UNKNOWN_CID) {
            sb.append(Integer.toHexString(mPsc))
                    .append("@").append(((mRssi == UNKNOWN_RSSI)? "-" : mRssi));
        } else if(mLac != UNKNOWN_CID && mCid != UNKNOWN_CID) {
            sb.append(Integer.toHexString(mLac))
                    .append(Integer.toHexString(mCid))
                    .append("@").append(((mRssi == UNKNOWN_RSSI)? "-" : mRssi));
        }
        sb.append("]");
        return sb.toString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mRssi);
        dest.writeInt(mLac);
        dest.writeInt(mCid);
        dest.writeInt(mPsc);
        dest.writeInt(mNetworkType);
    }
    public static final Parcelable.Creator<NeighboringCellInfo> CREATOR
    = new Parcelable.Creator<NeighboringCellInfo>() {
        public NeighboringCellInfo createFromParcel(Parcel in) {
            return new NeighboringCellInfo(in);
        }
        public NeighboringCellInfo[] newArray(int size) {
            return new NeighboringCellInfo[size];
        }
    };
}
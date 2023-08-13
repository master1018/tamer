public class Invitation implements Parcelable {
    private String mId;
    private Address mGroupAddress;
    private Address mSender;
    private String mReason;
    public Invitation(String id, Address groupAddress, Address sender,
            String resean) {
        mId = id;
        mGroupAddress = groupAddress;
        mSender = sender;
        mReason = resean;
    }
    public Invitation(Parcel source) {
        mId = source.readString();
        mGroupAddress = AddressParcelHelper.readFromParcel(source);
        mSender = AddressParcelHelper.readFromParcel(source);
        mReason = source.readString();
    }
    public String getInviteID() {
        return mId;
    }
    public Address getGroupAddress() {
        return mGroupAddress;
    }
    public Address getSender() {
        return mSender;
    }
    public String getReason() {
        return mReason;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        AddressParcelHelper.writeToParcel(dest, mGroupAddress);
        AddressParcelHelper.writeToParcel(dest, mSender);
        dest.writeString(mReason);
    }
    public int describeContents() {
        return 0;
    }
    public static final Parcelable.Creator<Invitation> CREATOR = new Parcelable.Creator<Invitation>() {
        public Invitation createFromParcel(Parcel source) {
            return new Invitation(source);
        }
        public Invitation[] newArray(int size) {
            return new Invitation[size];
        }
    };
}

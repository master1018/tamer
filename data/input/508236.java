public class Contact extends ImEntity implements Parcelable{
    private Address mAddress;
    private String mName;
    private Presence mPresence;
    public Contact(Address address, String name) {
        mAddress = address;
        mName = name;
        mPresence = new Presence();
    }
    public Contact(Parcel source) {
        mAddress = AddressParcelHelper.readFromParcel(source);
        mName = source.readString();
        mPresence = new Presence(source);
    }
    public Address getAddress() {
        return mAddress;
    }
    public String getName() {
        return mName;
    }
    public Presence getPresence() {
        return mPresence;
    }
    public boolean equals(Object other) {
        return other instanceof Contact
            && mAddress.equals(((Contact)other).mAddress);
    }
    public int hashCode() {
        return mAddress.hashCode();
    }
    public void setPresence(Presence presence) {
        mPresence = presence;
    }
    public void writeToParcel(Parcel dest, int flags) {
        AddressParcelHelper.writeToParcel(dest, mAddress);
        dest.writeString(mName);
        mPresence.writeToParcel(dest, 0);
    }
    public int describeContents() {
        return 0;
    }
    public final static Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}

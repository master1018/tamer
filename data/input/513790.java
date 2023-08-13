public class SmsRawData implements Parcelable {
    byte[] data;
    public static final Parcelable.Creator<SmsRawData> CREATOR
            = new Parcelable.Creator<SmsRawData> (){
        public SmsRawData createFromParcel(Parcel source) {
            int size;
            size = source.readInt();
            byte[] data = new byte[size];
            source.readByteArray(data);
            return new SmsRawData(data);
        }
        public SmsRawData[] newArray(int size) {
            return new SmsRawData[size];
        }
    };
    public SmsRawData(byte[] data) {
        this.data = data;
    }
    public byte[] getBytes() {
        return data;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(data.length);
        dest.writeByteArray(data);
    }
}

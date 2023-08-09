public class ScanResult implements Parcelable {
    public String SSID;
    public String BSSID;
    public String capabilities;
    public int level;
    public int frequency;
    public ScanResult(String SSID, String BSSID, String caps, int level, int frequency) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.capabilities = caps;
        this.level = level;
        this.frequency = frequency;
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String none = "<none>";
        sb.append("SSID: ").
            append(SSID == null ? none : SSID).
            append(", BSSID: ").
            append(BSSID == null ? none : BSSID).
            append(", capabilities: ").
            append(capabilities == null ? none : capabilities).
            append(", level: ").
            append(level).
            append(", frequency: ").
            append(frequency);
        return sb.toString();
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(BSSID);
        dest.writeString(capabilities);
        dest.writeInt(level);
        dest.writeInt(frequency);
    }
    public static final Creator<ScanResult> CREATOR =
        new Creator<ScanResult>() {
            public ScanResult createFromParcel(Parcel in) {
                return new ScanResult(
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.readInt(),
                    in.readInt()
                );
            }
            public ScanResult[] newArray(int size) {
                return new ScanResult[size];
            }
        };
}

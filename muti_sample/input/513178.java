public class InterfaceConfiguration implements Parcelable {
    public String hwAddr;
    public int ipAddr;
    public int netmask;
    public String interfaceFlags;
    public InterfaceConfiguration() {
        super();
    }
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append("ipddress "); putAddress(str, ipAddr);
        str.append(" netmask "); putAddress(str, netmask);
        str.append(" flags ").append(interfaceFlags);
        str.append(" hwaddr ").append(hwAddr);
        return str.toString();
    }
    private static void putAddress(StringBuffer buf, int addr) {
        buf.append((addr >> 24) & 0xff).append('.').
            append((addr >> 16) & 0xff).append('.').
            append((addr >> 8) & 0xff).append('.').
            append(addr & 0xff);
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hwAddr);
        dest.writeInt(ipAddr);
        dest.writeInt(netmask);
        dest.writeString(interfaceFlags);
    }
    public static final Creator<InterfaceConfiguration> CREATOR =
        new Creator<InterfaceConfiguration>() {
            public InterfaceConfiguration createFromParcel(Parcel in) {
                InterfaceConfiguration info = new InterfaceConfiguration();
                info.hwAddr = in.readString();
                info.ipAddr = in.readInt();
                info.netmask = in.readInt();
                info.interfaceFlags = in.readString();
                return info;
            }
            public InterfaceConfiguration[] newArray(int size) {
                return new InterfaceConfiguration[size];
            }
        };
}

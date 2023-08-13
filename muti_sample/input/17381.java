public class CK_VERSION {
    public byte major;  
    public byte minor;  
    public CK_VERSION(int major, int minor) {
        this.major = (byte)major;
        this.minor = (byte)minor;
    }
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(major & 0xff);
        buffer.append('.');
        int m = minor & 0xff;
        if (m < 10) {
            buffer.append('0');
        }
        buffer.append(m);
        return buffer.toString();
    }
}

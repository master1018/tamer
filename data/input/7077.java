public class BinaryRefAddr extends RefAddr {
    private byte[] buf = null;
    public BinaryRefAddr(String addrType, byte[] src) {
        this(addrType, src, 0, src.length);
    }
    public BinaryRefAddr(String addrType, byte[] src, int offset, int count) {
        super(addrType);
        buf = new byte[count];
        System.arraycopy(src, offset, buf, 0, count);
    }
    public Object getContent() {
        return buf;
    }
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof BinaryRefAddr)) {
            BinaryRefAddr target = (BinaryRefAddr)obj;
            if (addrType.compareTo(target.addrType) == 0) {
                if (buf == null && target.buf == null)
                    return true;
                if (buf == null || target.buf == null ||
                    buf.length != target.buf.length)
                    return false;
                for (int i = 0; i < buf.length; i++)
                    if (buf[i] != target.buf[i])
                        return false;
                return true;
            }
        }
        return false;
    }
    public int hashCode() {
        int hash = addrType.hashCode();
        for (int i = 0; i < buf.length; i++) {
            hash += buf[i];     
        }
        return hash;
    }
    public String toString(){
        StringBuffer str = new StringBuffer("Address Type: " + addrType + "\n");
        str.append("AddressContents: ");
        for (int i = 0; i<buf.length && i < 32; i++) {
            str.append(Integer.toHexString(buf[i]) +" ");
        }
        if (buf.length >= 32)
            str.append(" ...\n");
        return (str.toString());
    }
    private static final long serialVersionUID = -3415254970957330361L;
}

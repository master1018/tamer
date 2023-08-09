class Inet4Address extends InetAddress {
    final static int INADDRSZ = 4;
    private static final long serialVersionUID = 3286316764910316507L;
    static {
        init();
    }
    Inet4Address() {
        super();
        hostName = null;
        address = 0;
        family = IPv4;
    }
    Inet4Address(String hostName, byte addr[]) {
        this.hostName = hostName;
        this.family = IPv4;
        if (addr != null) {
            if (addr.length == INADDRSZ) {
                address  = addr[3] & 0xFF;
                address |= ((addr[2] << 8) & 0xFF00);
                address |= ((addr[1] << 16) & 0xFF0000);
                address |= ((addr[0] << 24) & 0xFF000000);
            }
        }
    }
    Inet4Address(String hostName, int address) {
        this.hostName = hostName;
        this.family = IPv4;
        this.address = address;
    }
    private Object writeReplace() throws ObjectStreamException {
        InetAddress inet = new InetAddress();
        inet.hostName = this.hostName;
        inet.address = this.address;
        inet.family = 2;
        return inet;
    }
    public boolean isMulticastAddress() {
        return ((address & 0xf0000000) == 0xe0000000);
    }
    public boolean isAnyLocalAddress() {
        return address == 0;
    }
    private static final int loopback = 2130706433; 
    public boolean isLoopbackAddress() {
        byte[] byteAddr = getAddress();
        return byteAddr[0] == 127;
    }
    public boolean isLinkLocalAddress() {
        return (((address >>> 24) & 0xFF) == 169)
            && (((address >>> 16) & 0xFF) == 254);
    }
    public boolean isSiteLocalAddress() {
        return (((address >>> 24) & 0xFF) == 10)
            || ((((address >>> 24) & 0xFF) == 172)
                && (((address >>> 16) & 0xF0) == 16))
            || ((((address >>> 24) & 0xFF) == 192)
                && (((address >>> 16) & 0xFF) == 168));
    }
    public boolean isMCGlobal() {
        byte[] byteAddr = getAddress();
        return ((byteAddr[0] & 0xff) >= 224 && (byteAddr[0] & 0xff) <= 238 ) &&
            !((byteAddr[0] & 0xff) == 224 && byteAddr[1] == 0 &&
              byteAddr[2] == 0);
    }
    public boolean isMCNodeLocal() {
        return false;
    }
    public boolean isMCLinkLocal() {
        return (((address >>> 24) & 0xFF) == 224)
            && (((address >>> 16) & 0xFF) == 0)
            && (((address >>> 8) & 0xFF) == 0);
    }
    public boolean isMCSiteLocal() {
        return (((address >>> 24) & 0xFF) == 239)
            && (((address >>> 16) & 0xFF) == 255);
    }
    public boolean isMCOrgLocal() {
        return (((address >>> 24) & 0xFF) == 239)
            && (((address >>> 16) & 0xFF) >= 192)
            && (((address >>> 16) & 0xFF) <= 195);
    }
    public byte[] getAddress() {
        byte[] addr = new byte[INADDRSZ];
        addr[0] = (byte) ((address >>> 24) & 0xFF);
        addr[1] = (byte) ((address >>> 16) & 0xFF);
        addr[2] = (byte) ((address >>> 8) & 0xFF);
        addr[3] = (byte) (address & 0xFF);
        return addr;
    }
    public String getHostAddress() {
        return numericToTextFormat(getAddress());
    }
    public int hashCode() {
        return address;
    }
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof Inet4Address) &&
            (((InetAddress)obj).address == address);
    }
    static String numericToTextFormat(byte[] src)
    {
        return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
    }
    private static native void init();
}

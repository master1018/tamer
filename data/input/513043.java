public final class Inet4Address extends InetAddress {
    private static final long serialVersionUID = 3286316764910316507L;
    final static InetAddress ANY = new Inet4Address(new byte[] { 0, 0, 0, 0 });
    final static InetAddress LOOPBACK = new Inet4Address(
            new byte[] { 127, 0, 0, 1 }, "localhost"); 
    Inet4Address(byte[] address) {
        family = AF_INET;
        ipaddress = address;
    }
    Inet4Address(byte[] address, String name) {
        family = AF_INET;
        ipaddress = address;
        hostName = name;
    }
    @Override
    public boolean isMulticastAddress() {
        return (ipaddress[0] & 0xF0) == 0xE0;
    }
    @Override
    public boolean isAnyLocalAddress() {
        for (int i = 0; i < ipaddress.length; i++) {
            if (ipaddress[i] != 0) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean isLoopbackAddress() {
        return (ipaddress[0] & 255) == 127;
    }
    @Override
    public boolean isLinkLocalAddress() {
        return (((ipaddress[0] & 255) == 169) && ((ipaddress[1] & 255) == 254));
    }
    @Override
    public boolean isSiteLocalAddress() {
        return ((ipaddress[0] & 255) == 10) || ((ipaddress[0] & 255) == 172)
                && (((ipaddress[1] & 255) > 15) && (ipaddress[1] & 255) < 32)
                || ((ipaddress[0] & 255) == 192)
                && ((ipaddress[1] & 255) == 168);
    }
    @Override
    public boolean isMCGlobal() {
        if (!isMulticastAddress()) {
            return false;
        }
        int address = InetAddress.bytesToInt(ipaddress, 0);
        if (address >>> 8 < 0xE00001) {
            return false;
        }
        if (address >>> 24 > 0xEE) {
            return false;
        }
        return true;
    }
    @Override
    public boolean isMCNodeLocal() {
        return false;
    }
    @Override
    public boolean isMCLinkLocal() {
        return InetAddress.bytesToInt(ipaddress, 0) >>> 8 == 0xE00000;
    }
    @Override
    public boolean isMCSiteLocal() {
        return (InetAddress.bytesToInt(ipaddress, 0) >>> 16) == 0xEFFF;
    }
    @Override
    public boolean isMCOrgLocal() {
        int prefix = InetAddress.bytesToInt(ipaddress, 0) >>> 16;
        return prefix >= 0xEFC0 && prefix <= 0xEFC3;
    }
    private Object writeReplace() throws ObjectStreamException {
        return new Inet4Address(ipaddress, hostName);
    }
}

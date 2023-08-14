public class GetLoopbackAddress
{
    static InetAddress IPv4Loopback;
    static InetAddress IPv6Loopback;
    static {
        try {
            IPv4Loopback = InetAddress.getByAddress(
                new byte[] {0x7F,0x00,0x00,0x01});
            IPv6Loopback = InetAddress.getByAddress(
                new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01});
        } catch (UnknownHostException e) {
        }
    }
    public static void main(String[] args) {
        InetAddress addr = InetAddress.getLoopbackAddress();
        if (!(addr.equals(IPv4Loopback) || addr.equals(IPv6Loopback)))
            throw new RuntimeException("Failed: getLoopbackAddress" +
                 " not returning a valid loopback address");
        InetAddress addr2 = InetAddress.getLoopbackAddress();
        if (addr != addr2)
            throw new RuntimeException("Failed: getLoopbackAddress" +
                " should return a reference to the same InetAddress loopback instance.");
    }
}

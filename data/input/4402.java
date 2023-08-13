public class IPv6Numeric {
    public static void main(String[] args) {
        try {
            InetAddress addr = InetAddress.getByName(":");
        } catch (java.net.UnknownHostException uhe) {
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
}

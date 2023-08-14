public class IsReachable {
    public static void main(String[] args) {
        try {
            InetAddress addr = InetAddress.getByName("localhost");
            if (!addr.isReachable(10000))
                throw new RuntimeException("Localhost should always be reachable");
            NetworkInterface inf = NetworkInterface.getByInetAddress(addr);
            if (inf != null) {
                if (!addr.isReachable(inf, 20, 10000))
                throw new RuntimeException("Localhost should always be reachable");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected exception:" + e);
        }
    }
}

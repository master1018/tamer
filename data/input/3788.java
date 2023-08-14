public class TestDefaults {
    public static void main(String args[]) throws Exception {
        MulticastSocket mc = new MulticastSocket();
        int ttl = mc.getTimeToLive();
        InetAddress ia = mc.getInterface();
        boolean mode = mc.getLoopbackMode();
        System.out.println("Default multicast settings:");
        System.out.println("      ttl: " + ttl);
        System.out.println("interface: " + ia);
        System.out.println(" loopback: " + mode);
        if (ttl != 1) {
            throw new Exception("Default ttl != 1  -- test failed!!!");
        }
    }
}

public class SetLoopbackMode {
    static final boolean FAILED = true;
    static final boolean PASSED = false;
    static boolean test(MulticastSocket mc, InetAddress grp) throws IOException {
        boolean disabled = mc.getLoopbackMode();
        if (disabled) {
            System.out.println("Loopback mode is disabled.");
        } else {
            System.out.println("Loopback mode is enabled.");
        }
        byte b[] = "hello".getBytes();
        DatagramPacket p = new DatagramPacket(b, b.length, grp,
                                mc.getLocalPort());
        mc.send(p);
        boolean gotPacket = false;
        mc.setSoTimeout(1000);
        try {
            b = new byte[16];
            p = new DatagramPacket(b, b.length);
            mc.receive(p);
            gotPacket = true;
            for (;;) {
                mc.receive(p);
            }
        } catch (SocketTimeoutException x) {
        }
        if (gotPacket && disabled) {
            System.out.println("Packet received (unexpected as loopback is disabled)");
            return FAILED;
        }
        if (!gotPacket && !disabled) {
            System.out.println
                ("Packet not received (packet excepted as loopback is enabled)");
            return FAILED;
        }
        if (gotPacket && !disabled) {
            System.out.println("Packet received - correct.");
        } else {
            System.out.println("Packet not received - correct.");
        }
        return PASSED;
    }
    public static void main (String args[]) throws Exception {
        int failures = 0;
        MulticastSocket mc = new MulticastSocket();
        InetAddress grp = InetAddress.getByName("224.80.80.80");
        InetAddress lb = InetAddress.getByName("::1");
        if (NetworkInterface.getByInetAddress(lb) != null) {
            grp = InetAddress.getByName("ff01::1");
        }
        System.out.println("\nTest will use multicast group: " + grp);
        mc.joinGroup(grp);
        System.out.println("\n******************\n");
        mc.setLoopbackMode(true);
        if (test(mc, grp) == FAILED) failures++;
        System.out.println("\n******************\n");
        mc.setLoopbackMode(false);
        if (test(mc, grp) == FAILED) failures++;
        System.out.println("\n******************\n");
        if (failures > 0) {
            throw new RuntimeException("Test failed");
        }
    }
}

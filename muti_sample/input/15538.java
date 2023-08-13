public class TrafficClass {
    static final int IPTOS_RELIABILITY = 0x4;
    static int failures = 0;
    static void testDatagramSocket(DatagramSocket s) {
        try {
            s.setTrafficClass( IPTOS_RELIABILITY );
            int tc = s.getTrafficClass();
        } catch (Exception e) {
            failures++;
            System.err.println("testDatagramSocket failed: " + e);
        }
    }
    static void testSocket(Socket s) {
        try {
            s.setTrafficClass(IPTOS_RELIABILITY);
            int tc = s.getTrafficClass();
        } catch (Exception e) {
            failures++;
            System.err.println("testSocket failed: " + e);
        }
    }
    public static void main(String args[]) throws Exception {
        DatagramSocket ds = new DatagramSocket();
        testDatagramSocket(ds);
        DatagramChannel dc = DatagramChannel.open();
        testDatagramSocket(dc.socket());
        Socket s = new Socket();
        testSocket(s);
        SocketChannel sc = SocketChannel.open();
        testSocket(sc.socket());
        if (failures > 0) {
            throw new Exception(failures + " sub-test(s) failed - " +
                "see log for details.");
        }
    }
}

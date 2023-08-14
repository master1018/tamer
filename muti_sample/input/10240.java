public class BindFailTest {
    public static void main(String args[]) throws Exception {
        DatagramSocket s = new DatagramSocket();
        int port = s.getLocalPort();
        for (int i=0; i<32000; i++) {
            try {
                DatagramSocket s2 = new DatagramSocket(port);
            } catch (BindException e) {
            }
        }
    }
}

public class Send12k {
    static final int SEND_SIZE = 16 * 1024;
    public static void main(String args[]) throws Exception {
        DatagramSocket s1 = new DatagramSocket();
        DatagramSocket s2 = new DatagramSocket();
        byte b1[] = new byte[ SEND_SIZE ];
        DatagramPacket p1 = new DatagramPacket(b1, 0, b1.length,
                                               InetAddress.getLocalHost(),
                                               s2.getLocalPort());
        boolean sendOkay = true;
        try {
            s1.send(p1);
        } catch (SocketException e) {
            sendOkay = false;
        }
        if (sendOkay) {
            byte b2[] = new byte[ SEND_SIZE * 2];
            DatagramPacket p2 = new DatagramPacket( b2, SEND_SIZE * 2 );
            s2.setSoTimeout(2000);
            try {
                s2.receive(p1);
            } catch (InterruptedIOException ioe) {
                throw new Exception("Datagram not received within timeout");
            }
            if (p1.getLength() != SEND_SIZE) {
                throw new Exception("Received datagram incorrect size");
            }
        }
    }
}

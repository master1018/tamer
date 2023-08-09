public class DatagramTimeout {
    public static void main(String[] args) throws Exception {
        boolean success = false;
        DatagramSocket sock = new DatagramSocket();
        try {
            DatagramPacket p;
            byte[] buffer = new byte[50];
            p = new DatagramPacket(buffer, buffer.length);
            sock.setSoTimeout(2);
            sock.receive(p);
        } catch (SocketTimeoutException e) {
            success = true;
        } finally {
            sock.close();
        }
        if (!success)
            throw new RuntimeException("Socket timeout failure.");
    }
}

public class ReportSocketClosed {
    public static void main(String[] args) throws Exception {
        DatagramSocket soc;
        InetAddress sin = null;
        byte[]  array = {21,22,23};
        try {
            soc = new DatagramSocket(4001);
            sin = InetAddress.getLocalHost();
            soc.close();
        } catch (Exception e) {
            throw new Exception("Got unexpected exception" + e);
        }
        try {
            soc.receive(new DatagramPacket(array, array.length));
        } catch (Exception e2) {
            if (e2 instanceof SocketException)
                return;
            else
                throw e2;
        }
    }
}

public class ADatagramSocket {
    public static void main(String[] args) throws IOException {
        System.err.println("setting DatagramSocketImplFactory...");
        try {
          DatagramSocket.setDatagramSocketImplFactory(new java.net.MyDatagramSocketImplFactory());
        } catch (Exception ex) {
          throw new RuntimeException("Setting DatagramSocketImplFactory failed!");
        }
        new QuoteServerThread().start();
        DatagramSocket socket = new DatagramSocket();
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getLocalHost();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String received = new String(packet.getData(), 0);
        System.err.println("Success!! Server current time is: " + received);
        socket.close();
    }
}
class QuoteServerThread extends Thread {
    protected DatagramSocket socket = null;
    public QuoteServerThread() throws IOException {
        this("QuoteServerThread");
    }
    public QuoteServerThread(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(4445);
    }
    public void run() {
      try {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        String dString = null;
        dString = new Date().toString();
        buf = dString.getBytes();
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
      } catch (IOException e) {
        e.printStackTrace();
      }
      socket.close();
    }
}

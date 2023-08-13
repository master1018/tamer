public class PortUnreachable implements Runnable {
    DatagramSocket clientSock;
    int serverPort;
    int clientPort;
    public void run() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            Thread.currentThread().sleep(2000);
            byte b[] = "A late msg".getBytes();
            DatagramPacket packet = new DatagramPacket(b, b.length, addr,
                                                       serverPort);
            clientSock.send(packet);
            Thread.currentThread().sleep(5000);
            DatagramSocket sock = new DatagramSocket(serverPort);
            b = "Grettings from the server".getBytes();
            packet = new DatagramPacket(b, b.length, addr, clientPort);
            sock.send(packet);
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    PortUnreachable() throws Exception {
        clientSock = new DatagramSocket();
        clientPort = clientSock.getLocalPort();
        DatagramSocket sock2 = new DatagramSocket();
        serverPort = sock2.getLocalPort();
        sock2.close();
        InetAddress addr = InetAddress.getLocalHost();
        byte b[] = "Hello me".getBytes();
        DatagramPacket packet = new DatagramPacket(b, b.length, addr,
                                                   serverPort);
        for (int i=0; i<100; i++)
            clientSock.send(packet);
        Thread thr = new Thread(this);
        thr.start();
        clientSock.setSoTimeout(10000);
        clientSock.receive(packet);
        clientSock.close();
    }
    public static void main(String[] args) throws Exception {
        new PortUnreachable();
    }
}

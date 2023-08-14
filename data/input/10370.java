public class TestClose {
    public static void main(String[] args) throws Exception {
        ServerSocket ss;
        Socket s;
        InetAddress ad1, ad2;
        int port1, port2, serverport;
        ss = new ServerSocket(0);
        serverport = ss.getLocalPort();
        s = new Socket("localhost", serverport);
        s.close();
        ss.close();
        ad1 = ss.getInetAddress();
        if (ad1 == null)
            throw new RuntimeException("ServerSocket.getInetAddress() returned null");
        port1 = ss.getLocalPort();
        if (port1 != serverport)
            throw new RuntimeException("ServerSocket.getLocalPort() returned the wrong value");
        ad2 = s.getInetAddress();
        if (ad2 == null)
            throw new RuntimeException("Socket.getInetAddress() returned null");
        port2 = s.getPort();
        if (port2 != serverport)
            throw new RuntimeException("Socket.getPort() returned wrong value");
        ad2 = s.getLocalAddress();
        if (ad2 == null)
            throw new RuntimeException("Socket.getLocalAddress() returned null");
        port2 = s.getLocalPort();
        if (port2 == -1)
            throw new RuntimeException("Socket.getLocalPort returned -1");
    }
}

public class TestAfterClose
{
    static int failCount;
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket(0, 0, null);
            Socket socket = new Socket("localhost", ss.getLocalPort());
            ss.accept();
            ss.close();
            test(socket);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        if (failCount > 0)
            throw new RuntimeException("Failed: failcount = " + failCount);
    }
    static void test(Socket socket) throws IOException {
        int socketPort = socket.getPort();
        InetAddress socketInetAddress = socket.getInetAddress();
        SocketAddress socketRemoteSocketAddress = socket.getRemoteSocketAddress();
        int socketLocalPort = socket.getLocalPort();
        socket.close();
        if (socketPort != socket.getPort()) {
            System.out.println("Socket.getPort failed");
            failCount++;
        }
        if (!socket.getInetAddress().equals(socketInetAddress)) {
            System.out.println("Socket.getInetAddress failed");
            failCount++;
        }
        if (!socket.getRemoteSocketAddress().equals(socketRemoteSocketAddress)) {
            System.out.println("Socket.getRemoteSocketAddresss failed");
            failCount++;
        }
        if (socketLocalPort != socket.getLocalPort()) {
            System.out.println("Socket.getLocalPort failed");
            failCount++;
        }
        InetAddress anyAddr = null;
        try {
            anyAddr = InetAddress.getByAddress("",new byte[] {0,0,0,0});
        } catch (UnknownHostException uhe) {
        }
        if (anyAddr != null && !socket.getLocalAddress().equals(anyAddr)) {
            System.out.println("Socket.getLocalAddress failed");
            failCount++;
        }
        InetSocketAddress addr = new InetSocketAddress(socket.getLocalPort());
        if (!socket.getLocalSocketAddress().equals(addr)) {
            System.out.println("Socket.getLocalSocketAddress failed");
            failCount++;
        }
        if (!socket.isConnected()) {
            System.out.println("Socket.isConnected failed");
            failCount++;
        }
        if (!socket.isBound()) {
            System.out.println("Socket.isBound failed");
            failCount++;
        }
    }
}

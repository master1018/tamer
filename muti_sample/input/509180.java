public class UnixSocketTest extends TestCase {
    public void test_getInputStream() throws IOException {
        final ServerSocket pingServer = new ServerSocket(0);
        Socket pingClient = new Socket();
        try {
            pingClient.connect(new InetSocketAddress(
                    InetAddress.getLocalHost(), pingServer.getLocalPort()));
            Socket worker = pingServer.accept();
            pingServer.close();
            OutputStream clientOut = pingClient.getOutputStream();
            clientOut.write(new byte[256]);
            InputStream in = worker.getInputStream();
            in.read();
            OutputStream out = worker.getOutputStream();
            out.write(new byte[42]);
            worker.close();
            InputStream clientIn = pingClient.getInputStream();
            clientIn.read(new byte[42]);
            try {
                clientIn.read();
                fail("Should throw SocketException");
            } catch (SocketException e) {
            }
            clientIn.close();
            try {
                clientIn.read();
                fail("Should throw SocketException");
            } catch (SocketException e) {
            }
            try {
                clientIn.read(new byte[5]);
                fail("Should throw SocketException");
            } catch (SocketException e) {
            }
        } finally {
            pingClient.close();
            pingServer.close();
        }
    }
    public void test_connectLjava_net_SocketAddressI() throws Exception {
        Socket theSocket = new Socket();
        try {
            theSocket.connect(new InetSocketAddress(InetAddress.getLocalHost(),
                    1), 200);
            fail("No interrupted exception when connecting to address nobody listening on with short timeout 200");
        } catch (ConnectException e) {
        }
        theSocket.close();
    }
    public void test_getOutputStream() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        Socket socket = new Socket("127.0.0.1", port, false);
        OutputStream o = socket.getOutputStream();
        try {
            o.write(1);
        } catch (SocketException e) {
        } finally {
            socket.close();
        }
    }
}

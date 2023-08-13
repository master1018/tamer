public class SocketClosedException {
    static void doServerSide() throws Exception {
        try {
            Socket socket = serverSocket.accept();
            OutputStream os = socket.getOutputStream();
            os.write(85);
            os.flush();
            socket.close();
        } finally {
            serverSocket.close();
        }
    }
    static void doClientSide(int port) throws Exception {
        Socket socket = new Socket("localhost", port);
        InputStream is = socket.getInputStream();
        is.read();
        socket.close();
        is.read();
    }
    static ServerSocket serverSocket;
    static Exception serverException = null;
    public static void main(String[] args) throws Exception {
        serverSocket = new ServerSocket(0);
        startServer();
        try {
            doClientSide(serverSocket.getLocalPort());
        } catch (SocketException e) {
            if (!e.getMessage().equalsIgnoreCase("Socket closed")) {
                throw new Exception("Received a wrong exception message: " +
                                        e.getMessage());
            }
            System.out.println("PASSED: received the right exception message: "
                                        + e.getMessage());
        }
        if (serverException != null) {
            throw serverException;
        }
    }
    static void startServer() {
        (new Thread() {
            public void run() {
                try {
                    doServerSide();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

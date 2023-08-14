public class ByteServer {
    public static final String LOCALHOST = "localhost";
    private int bytecount;
    private Socket  socket;
    private ServerSocket  serversocket;
    private Thread serverthread;
    volatile Exception savedException;
    public ByteServer(int bytecount) throws Exception{
        this.bytecount = bytecount;
        serversocket = new ServerSocket(0);
    }
    public int port() {
        return serversocket.getLocalPort();
    }
    public void start() {
        serverthread = new Thread() {
            public void run() {
                try {
                    socket = serversocket.accept();
                    socket.getOutputStream().write(new byte[bytecount]);
                    socket.getOutputStream().flush();
                } catch (Exception e) {
                    System.err.println("Exception in ByteServer: " + e);
                    System.exit(1);
                }
            }
        };
        serverthread.start();
    }
    public void exit() throws Exception {
        serverthread.join();
        socket.close();
        serversocket.close();
    }
}

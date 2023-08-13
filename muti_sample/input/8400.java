public class TemporarySelector {
    static volatile boolean done = false;
    public static void main(String[] args) throws Exception {
        Runnable r = new Runnable() {
            public void run() {
                while (!done) {
                    System.gc();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }
                }
            }
        };
        try {
            ServerSocketChannel ssc =  ServerSocketChannel.open();
            final ServerSocket ss =  ssc.socket();
            ss.bind(new InetSocketAddress(0));
            int localPort = ss.getLocalPort();
            System.out.println("Connecting to server socket");
            System.out.flush();
            SocketChannel channel = SocketChannel.open(new InetSocketAddress("localhost", localPort));
            System.out.println("Connected to server socket");
            System.out.flush();
            Thread t = new Thread(r);
            t.start();
            byte[] buffer = new byte[500];
            System.out.println("Reading from socket input stream");
            System.out.flush();
            Socket socket = channel.socket();
            socket.setSoTimeout(10000);  
            try {
                socket.getInputStream().read(buffer);
            } catch (java.net.SocketTimeoutException ste) {
            }
        } finally {
            done = true;
        }
    }
}

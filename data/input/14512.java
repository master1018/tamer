public class RetryUponTimeout implements Runnable {
    public void run(){
        Socket socket = null;
        try {
            for (int i = 0; i < 2; i++) {
                socket = server.accept();
                InputStream is = socket.getInputStream ();
                MessageHeader header = new MessageHeader (is);
                count++;
            }
        } catch (Exception ex) {
        } finally {
            try {
                socket.close();
                server.close();
            } catch (IOException ioex) {
            }
        }
    }
    static ServerSocket server;
    static int count = 0;
    public static void main(String[] args) throws Exception {
        try {
            server = new ServerSocket (0);
            int port = server.getLocalPort ();
            new Thread(new RetryUponTimeout()).start ();
            URL url = new URL("http:
            java.net.URLConnection uc = url.openConnection();
            uc.setReadTimeout(1000);
            uc.getInputStream();
        } catch (SocketTimeoutException stex) {
            server.close();
        }
        if (count > 1) {
            throw new RuntimeException("Server received "+count+" requests instead of one.");
        }
    }
}

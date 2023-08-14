public class RST implements Runnable {
    Socket client;
    public void run() {
        try {
            client.setSoLinger(true, 0);        
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    RST() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        client = new Socket("localhost", ss.getLocalPort());
        Socket server = ss.accept();
        Thread thr = new Thread(this);
        thr.start();
        SocketException exc = null;
        try {
            InputStream in = server.getInputStream();
            int n = in.read();
        } catch (SocketException se) {
            exc = se;
        }
        server.close();
        ss.close();
        if (exc == null) {
            throw new Exception("Expected SocketException not thrown");
        }
        if (exc.getMessage().toLowerCase().indexOf("reset") == -1) {
            throw new Exception("SocketException thrown but not expected \"connection reset\"");
        }
    }
    public static void main(String args[]) throws Exception {
        new RST();
    }
}

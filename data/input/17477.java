public class SetOption {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Socket s1 = new Socket("localhost", ss.getLocalPort());
        Socket s2 = ss.accept();
        s1.close();
        boolean exc_thrown = false;
        try {
            s1.setSoTimeout(1000);
        } catch (SocketException e) {
            exc_thrown = true;
        }
        if (!exc_thrown) {
            throw new Exception("SocketException not thrown on closed socket");
        }
    }
}

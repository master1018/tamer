public class Application {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        System.out.println(port);
        System.out.flush();
        Socket s = ss.accept();
        s.close();
        ss.close();
    }
}

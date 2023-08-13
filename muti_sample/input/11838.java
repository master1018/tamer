public class ShutdownBoth {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Socket s1 = new Socket(ss.getInetAddress(), ss.getLocalPort());
        Socket s2 = ss.accept();
        try {
            s1.shutdownInput();
            s1.shutdownOutput();            
        } finally {
            s1.close();
            s2.close();
            ss.close();
        }
    }
}

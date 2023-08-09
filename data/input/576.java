public class CloseAvailable implements Runnable {
    static ServerSocket ss;
    static InetAddress addr;
    static int port;
    public static void main(String[] args) throws Exception {
        boolean error = true;
        addr = InetAddress.getLocalHost();
        ss = new ServerSocket(0);
        port = ss.getLocalPort();
        Thread t = new Thread(new CloseAvailable());
        t.start();
        Socket  soc = ss.accept();
        ss.close();
        DataInputStream is = new DataInputStream(soc.getInputStream());
        is.close();
        try {
            is.available();
        }
        catch (IOException ex) {
            error = false;
        }
        if (error)
            throw new RuntimeException("Available() can be called after stream closed.");
    }
    public void run() {
        try {
            Socket s = new Socket(addr, port);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

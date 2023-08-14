public class GetLocalAddress implements Runnable {
    static ServerSocket ss;
    static InetAddress addr;
    static int port;
    public static void main(String args[]) throws Exception {
        boolean      error = true;
        int          linger = 65546;
        int          value = 0;
        addr = InetAddress.getLocalHost();
        ss = new ServerSocket(0);
        port = ss.getLocalPort();
        Thread t = new Thread(new GetLocalAddress());
        t.start();
        Socket soc = ss.accept();
        if(addr.equals(soc.getLocalAddress())) {
           error = false;
           }
        if (error)
            throw new RuntimeException("Socket.GetLocalAddress failed.");
        soc.close();
    }
    public void run() {
        try {
            Socket s = new Socket(addr, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

public class SoTimeout implements Runnable {
    static ServerSocket serverSocket;
    static long timeWritten;
    static InetAddress addr;
    static int port;
    public static void main(String[] args) throws Exception {
        addr = InetAddress.getLocalHost();
        serverSocket = new ServerSocket(0);
        port = serverSocket.getLocalPort();
        byte[] b = new byte[12];
        Thread t = new Thread(new SoTimeout());
        t.start();
        Socket s = serverSocket.accept();
        serverSocket.close();
        s.setSoTimeout(5000);
        s.getInputStream().read(b, 0, b.length);
        s.close();
        long waited = System.currentTimeMillis() - timeWritten;
        if (waited > 2000) {
            throw new Exception("shouldn't take " + waited + " to complete");
        }
    }
    public void run() {
        try {
            byte[] b = new byte[12];
            Socket s = new Socket(addr, port);
            Thread.yield();
            timeWritten = System.currentTimeMillis();
            s.getOutputStream().write(b, 0, 12);
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

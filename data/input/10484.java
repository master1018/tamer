public class LingerTest {
    static class Sender implements Runnable {
        Socket s;
        public Sender(Socket s) {
            this.s = s;
        }
        public void run() {
            System.out.println ("Sender starts");
            try {
                s.getOutputStream().write(new byte[128*1024]);
            }
            catch (IOException ioe) {
            }
            System.out.println ("Sender ends");
        }
    }
    static class Closer implements Runnable {
        Socket s;
        public Closer(Socket s) {
            this.s = s;
        }
        public void run() {
            System.out.println ("Closer starts");
            try {
                s.close();
            }
            catch (IOException ioe) {
            }
            System.out.println ("Closer ends");
        }
    }
    static class Another implements Runnable {
        int port;
        long delay;
        boolean connected = false;
        public Another(int port, long delay) {
            this.port = port;
            this.delay = delay;
        }
        public void run() {
            System.out.println ("Another starts");
            try {
                Thread.sleep(delay);
                Socket s = new Socket("localhost", port);
                synchronized (this) {
                    connected = true;
                }
                s.close();
            }
            catch (Exception ioe) {
                ioe.printStackTrace();
            }
            System.out.println ("Another ends");
        }
        public synchronized boolean connected() {
            return connected;
        }
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Socket s1 = new Socket("localhost", ss.getLocalPort());
        Socket s2 = ss.accept();
            s1.setSendBufferSize(128*1024);
        s1.setSoLinger(true, 30);
        s2.setReceiveBufferSize(1*1024);
            Thread thr = new Thread(new Sender(s1));
        thr.start();
            Another another = new Another(ss.getLocalPort(), 5000);
        thr = new Thread(another);
        thr.start();
            Thread.sleep(1000);
            (new Thread(new Closer(s1))).start();
            Thread.sleep(10000);
        ss.close();
            if (!another.connected()) {
            throw new RuntimeException("Another thread is blocked");
        }
        System.out.println ("Main ends");
    }
}

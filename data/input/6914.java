public class ThreadStop {
    static class Server implements Runnable {
        ServerSocket ss;
        Server() throws IOException {
            ss = new ServerSocket(0);
        }
        public int localPort() {
            return ss.getLocalPort();
        }
        public void run() {
            try {
                Socket s = ss.accept();
            } catch (IOException ioe) {
            } catch (ThreadDeath x) {
            } finally {
                try {
                    ss.close();
                } catch (IOException x) { }
            }
        }
    }
    public static void main(String args[]) throws Exception {
        Server svr = new Server();
        Thread thr = new Thread(svr);
        thr.start();
        Thread.currentThread().sleep(2000);
        thr.stop();
        Thread.currentThread().sleep(2000);
        try {
            Socket s = new Socket("localhost", svr.localPort());
        } catch (IOException ioe) { }
    }
}

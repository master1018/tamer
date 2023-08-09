public class TimeoutTest {
    class Server extends Thread {
        ServerSocket server;
        Server (ServerSocket server) {
            super ();
            this.server = server;
        }
        public void run () {
            try {
                Socket s = server.accept ();
                while (!finished ()) {
                    Thread.sleep (1000);
                }
                s.close();
            } catch (Exception e) {
            }
        }
        boolean isFinished = false;
        synchronized boolean finished () {
            return (isFinished);
        }
        synchronized void done () {
            isFinished = true;
        }
    }
    public static void main(String[] args) throws Exception {
        TimeoutTest t = new TimeoutTest ();
        t.test ();
    }
    public void test() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Server s = new Server (ss);
        try{
            URL url = new URL ("http:
            URLConnection urlc = url.openConnection ();
            InputStream is = urlc.getInputStream ();
            throw new RuntimeException("Should have received timeout");
        } catch (SocketTimeoutException e) {
            return;
        } finally {
            s.done();
            ss.close();
        }
    }
}

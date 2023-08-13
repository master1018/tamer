public class InheritTimeout {
    class Reaper extends Thread {
        Socket s;
        int timeout;
        Reaper(Socket s, int timeout) {
            this.s = s;
            this.timeout = timeout;
        }
        public void run() {
            try {
                Thread.currentThread().sleep(timeout);
                s.close();
            } catch (Exception e) {
            }
        }
    }
   InheritTimeout() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        ss.setSoTimeout(1000);
        InetAddress ia = InetAddress.getLocalHost();
        InetSocketAddress isa =
            new InetSocketAddress(ia, ss.getLocalPort());
        Socket s1 = new Socket();
        s1.connect(isa);
        Socket s2 = ss.accept();
        Reaper r = new Reaper(s2, 5000);
        r.start();
        boolean readTimedOut = false;
        try {
            s2.getInputStream().read();
        } catch (SocketTimeoutException te) {
            readTimedOut = true;
        } catch (SocketException e) {
            if (!s2.isClosed()) {
                throw e;
            }
        }
        s1.close();
        ss.close();
        if (readTimedOut) {
            throw new Exception("Unexpected SocketTimeoutException throw!");
        }
   }
   public static void main(String args[]) throws Exception {
        new InheritTimeout();
   }
}

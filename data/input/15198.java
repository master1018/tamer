public class Socket_getInputStream_read extends AsyncCloseTest implements Runnable {
    Socket s;
    int timeout = 0;
    public Socket_getInputStream_read() {
    }
    public Socket_getInputStream_read(int timeout) {
        this.timeout = timeout;
    }
    public String description() {
        String s = "Socket.getInputStream().read()";
        if (timeout > 0) {
            s += " (with timeout)";
        }
        return s;
    }
    public void run() {
        InputStream in;
        try {
            in = s.getInputStream();
            if (timeout > 0) {
                s.setSoTimeout(timeout);
            }
        } catch (Exception e) {
            failed(e.getMessage());
            return;
        }
        try {
            int n = in.read();
            failed("getInptuStream().read() returned unexpectly!!");
        } catch (SocketException se) {
            closed();
        } catch (Exception e) {
            failed(e.getMessage());
        }
    }
    public boolean go() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        InetAddress lh = InetAddress.getLocalHost();
        s = new Socket();
        s.connect( new InetSocketAddress(lh, ss.getLocalPort()) );
        Socket s2 = ss.accept();
        Thread thr = new Thread(this);
        thr.start();
        Thread.currentThread().sleep(1000);
        s.close();
        Thread.currentThread().sleep(1000);
        if (isClosed()) {
            return true;
        } else {
            failed("getInputStream().read() wasn't preempted");
            return false;
        }
    }
}

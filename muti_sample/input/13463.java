public class Socket_getOutputStream_write extends AsyncCloseTest implements Runnable {
    Socket s;
    public String description() {
        return "Socket.getOutputStream().write()";
    }
    public void run() {
        try {
            OutputStream out = s.getOutputStream();
            for (;;) {
                byte b[] = new byte[8192];
                out.write(b);
            }
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
        Thread.currentThread().sleep(2000);
        s.close();
        Thread.currentThread().sleep(2000);
        if (isClosed()) {
            return true;
        } else {
            failed("getOutputStream().write() wasn't preempted");
            return false;
        }
    }
}

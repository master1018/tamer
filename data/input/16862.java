public class ServerSocket_accept extends AsyncCloseTest implements Runnable {
    ServerSocket ss;
    int timeout = 0;
    public ServerSocket_accept() {
    }
    public ServerSocket_accept(int timeout) {
        this.timeout = timeout;
    }
    public String description() {
        String s = "ServerSocket.accept()";
        if (timeout > 0) {
            s += " (with timeout)";
        }
        return s;
    }
    public void run() {
        try {
            Socket s = ss.accept();
        } catch (SocketException se) {
            closed();
        } catch (Exception e) {
            failed(e.getMessage());
        }
    }
    public boolean go() throws Exception {
        ss = new ServerSocket(0);
        Thread thr = new Thread(this);
        thr.start();
        Thread.currentThread().sleep(1000);
        ss.close();
        Thread.currentThread().sleep(1000);
        if (isClosed()) {
            return true;
        } else {
            failed("ServerSocket.accept() wasn't preempted");
            return false;
        }
    }
}

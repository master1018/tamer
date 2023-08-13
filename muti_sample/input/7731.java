public class DatagramSocket_receive extends AsyncCloseTest implements Runnable {
    DatagramSocket s;
    int timeout = 0;
    public DatagramSocket_receive() {
    }
    public DatagramSocket_receive(int timeout) {
        this.timeout = timeout;
    }
    public String description() {
        String s = "DatagramSocket.receive";
        if (timeout > 0) {
            s += " (timeout specified)";
        }
        return s;
    }
    public void run() {
        DatagramPacket p;
        try {
            byte b[] = new byte[1024];
            p  = new DatagramPacket(b, b.length);
            if (timeout > 0) {
                s.setSoTimeout(timeout);
            }
        } catch (Exception e) {
            failed(e.getMessage());
            return;
        }
        try {
            s.receive(p);
        } catch (SocketException se) {
            closed();
        } catch (Exception e) {
            failed(e.getMessage());
        }
    }
    public boolean go() throws Exception {
        s = new DatagramSocket();
        Thread thr = new Thread(this);
        thr.start();
        Thread.currentThread().sleep(1000);
        s.close();
        Thread.currentThread().sleep(1000);
        if (isClosed()) {
            return true;
        } else {
            failed("DatagramSocket.receive wasn't preempted");
            return false;
        }
    }
}

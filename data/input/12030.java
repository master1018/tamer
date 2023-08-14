public class NotifyHandshakeTestHeyYou extends Thread
        implements HandshakeCompletedListener {
    public AccessControlContext acc;
    public SSLSession ssls;
    SSLSocket socket;
    public boolean set;
    public NotifyHandshakeTestHeyYou(SSLSocket socket) {
        this.socket = socket;
        socket.addHandshakeCompletedListener(this);
        acc = AccessController.getContext();
        com.NotifyHandshakeTest.trigger();
    }
    public void handshakeCompleted(HandshakeCompletedEvent event) {
        set = true;
        ssls = event.getSession();
        com.NotifyHandshakeTest.trigger();
    }
    public void run() {
        try {
            System.out.println("Going to sleep for 1000 seconds...");
            Thread.sleep(100000);
        } catch (InterruptedException e) {
        }
    }
}

public class DelegatedTask implements Runnable {
    private final HandshakeProtocol handshaker;
    private final PrivilegedExceptionAction<Void> action;
    private final AccessControlContext  context;
    public DelegatedTask(PrivilegedExceptionAction<Void> action, HandshakeProtocol handshaker, AccessControlContext  context) {
        this.action = action;
        this.handshaker = handshaker;
        this.context = context;
    }
    public void run() {
        synchronized (handshaker) {
            try {
                AccessController.doPrivileged(action, context);
            } catch (PrivilegedActionException e) {
                handshaker.delegatedTaskErr = e.getException();
            } catch (RuntimeException e) {
                handshaker.delegatedTaskErr = e;
            }
        }
    }
}

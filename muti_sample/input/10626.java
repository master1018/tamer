public class OrangeImpl extends UnicastRemoteObject implements Orange {
    private static final Logger logger = Logger.getLogger("reliability.orange");
    private final String name;
    public OrangeImpl(String name) throws RemoteException {
        this.name = name;
    }
    public int[] recurse(OrangeEcho echo, int[] message, int level)
        throws RemoteException
    {
        String threadName = Thread.currentThread().getName();
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".recurse(message["
            + message.length + "], " + level + "): BEGIN");
        int[] response;
        if (level > 0) {
            response = echo.recurse(this, message, level);
        } else {
            for (int i = 0; i < message.length; i++) {
                message[i] = ~message[i];
            }
            response = message;
        }
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".recurse(message["
            + message.length + "], " + level + "): END");
        return response;
    }
    public String toString() {
        return name;
    }
}

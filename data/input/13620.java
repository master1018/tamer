public class OrangeEchoImpl extends UnicastRemoteObject implements OrangeEcho {
    private static final Logger logger =
        Logger.getLogger("reliability.orangeecho");
    private final String name;
    public OrangeEchoImpl(String name) throws RemoteException {
        this.name = name;
    }
    public int[] recurse(Orange orange, int[] message, int level)
        throws RemoteException
    {
        String threadName = Thread.currentThread().getName();
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".recurse(message["
            + message.length + "], " + level + "): BEGIN");
        int[] response = orange.recurse(this, message, level - 1);
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".recurse(message["
            + message.length + "], " + level + "): END");
        return response;
    }
    public String toString() {
        return name;
    }
}

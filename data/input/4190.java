public class AppleImpl extends UnicastRemoteObject implements Apple {
    private static final Logger logger = Logger.getLogger("reliability.apple");
    private final String name;
    public AppleImpl(String name) throws RemoteException {
        this.name = name;
    }
    public void notify(AppleEvent[] events) {
        String threadName = Thread.currentThread().getName();
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".notify: BEGIN");
        for (int i = 0; i < events.length; i++) {
            logger.log(Level.FINEST,
                threadName + ": " + toString() + ".notify(): events["
                + i + "] = " + events[i].toString());
        }
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".notify(): END");
    }
    public Orange newOrange(String name) throws RemoteException {
        String threadName = Thread.currentThread().getName();
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".newOrange(" + name + "): BEGIN");
        Orange orange = new OrangeImpl(name);
        logger.log(Level.FINEST,
            threadName + ": " + toString() + ".newOrange(" + name + "): END");
        return orange;
    }
    public String toString() {
        return name;
    }
}

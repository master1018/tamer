public class ApplicationServer implements Runnable {
    private static final Logger logger = Logger.getLogger("reliability.orange");
    private static final int LOOKUP_ATTEMPTS = 5;
    private static final int DEFAULT_NUMAPPLES = 10;
    private static final String DEFAULT_REGISTRYHOST = "localhost";
    private final int numApples;
    private final String registryHost;
    private final Apple[] apples;
    private AppleUser user;
    ApplicationServer() {
        this(DEFAULT_NUMAPPLES, DEFAULT_REGISTRYHOST);
    }
    ApplicationServer(int numApples, String registryHost) {
        this.numApples = numApples;
        this.registryHost = registryHost;
        apples = new Apple[numApples];
    }
    public void run() {
        try {
            int i = 0;
            Exception exc = null;
            for (i = 0; i < LOOKUP_ATTEMPTS; i++) {
                try {
                    Registry registry = LocateRegistry.getRegistry(
                        registryHost, 2006);
                    user = (AppleUser) registry.lookup("AppleUser");
                    user.startTest();
                    break; 
                } catch (Exception e) {
                    exc = e;
                    Thread.sleep(10000); 
                }
            }
            if (user == null) {
                logger.log(Level.SEVERE, "Failed to lookup AppleUser:", exc);
                return;
            }
            try {
                for (i = 0; i < numApples; i++) {
                    apples[i] = new AppleImpl("AppleImpl #" + (i + 1));
                }
            } catch (RemoteException e) {
                logger.log(Level.SEVERE,
                    "Failed to create AppleImpl #" + (i + 1) + ":", e);
                user.reportException(e);
                return;
            }
            try {
                for (i = 0; i < numApples; i++) {
                    user.useApple(apples[i]);
                }
            } catch (RemoteException e) {
                logger.log(Level.SEVERE,
                    "Failed to register callbacks for " + apples[i] + ":", e);
                user.reportException(e);
                return;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected exception:", e);
        }
    }
    private static void usage() {
        System.err.println("Usage: ApplicationServer [-numApples <numApples>]");
        System.err.println("                         [-registryHost <host>]");
        System.err.println("  numApples  The number of apples (threads) to use.");
        System.err.println("             The default is 10 apples.");
        System.err.println("  host       The host running rmiregistry " +
                                         "which contains AppleUser.");
        System.err.println("             The default is \"localhost\".");
        System.err.println();
    }
    public static void main(String[] args) {
        int num = DEFAULT_NUMAPPLES;
        String host = DEFAULT_REGISTRYHOST;
        try {
            for (int i = 0; i < args.length ; i++ ) {
                String arg = args[i];
                if (arg.equals("-numApples")) {
                    i++;
                    num = Integer.parseInt(args[i]);
                } else if (arg.equals("-registryHost")) {
                    i++;
                    host = args[i];
                } else {
                    usage();
                }
            }
        } catch (Throwable t) {
            usage();
            throw new RuntimeException("TEST FAILED: Bad argument");
        }
        Thread server = new Thread(new ApplicationServer(num,host));
        server.start();
    }
}

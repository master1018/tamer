public class AppleUserImpl extends UnicastRemoteObject implements AppleUser {
    private static final Logger logger =
        Logger.getLogger("reliability.appleuser");
    private static int threadNum = 0;
    private static long testDuration = 0;
    private static int maxLevel = 7;
    private static Exception status = null;
    private static boolean finished = false;
    private static boolean startTestNotified = false;
    private static final Random random = new Random();
    private static final Object lock = new Object();
    public AppleUserImpl() throws RemoteException {
    }
    public synchronized void startTest() throws RemoteException {
        startTestNotified = true;
        this.notifyAll();
    }
    public void reportException(Exception status) throws RemoteException {
        synchronized (lock) {
            this.status = status;
            lock.notifyAll();
        }
    }
    public synchronized void useApple(Apple apple) throws RemoteException {
        String threadName = Thread.currentThread().getName();
        logger.log(Level.FINEST,
            threadName + ": AppleUserImpl.useApple(): BEGIN");
        AppleUserThread t =
            new AppleUserThread("AppleUserThread-" + (++threadNum), apple);
        t.start();
        logger.log(Level.FINEST,
            threadName + ": AppleUserImpl.useApple(): END");
    }
    class AppleUserThread extends Thread {
        final Apple apple;
        public AppleUserThread(String name, Apple apple) {
            super(name);
            this.apple = apple;
        }
        public void run() {
            int orangeNum = 0;
            long stopTime = System.currentTimeMillis() + testDuration;
            Logger logger = Logger.getLogger("reliability.appleuserthread");
            try {
                do { 
                    int numEvents = Math.abs(random.nextInt() % 5);
                    AppleEvent[] events = new AppleEvent[numEvents];
                    for (int i = 0; i < events.length; i++) {
                        events[i] = new AppleEvent(orangeNum % 3);
                    }
                    apple.notify(events);
                    Orange orange = apple.newOrange(
                        "Orange(" + getName() + ")-" + (++orangeNum));
                    int msgLength = 1000 + Math.abs(random.nextInt() % 3000);
                    int[] message = new int[msgLength];
                    for (int i = 0; i < message.length; i++) {
                        message[i] = random.nextInt();
                    }
                    OrangeEchoImpl echo = new OrangeEchoImpl(
                        "OrangeEcho(" + getName() + ")-" + orangeNum);
                    int[] response = orange.recurse(echo, message,
                        2 + Math.abs(random.nextInt() % (maxLevel + 1)));
                    if (response.length != message.length) {
                        throw new RuntimeException(
                            "ERROR: CORRUPTED RESPONSE: " +
                            "wrong length of returned array " + "(should be " +
                            message.length + ", is " + response.length + ")");
                    }
                    for (int i = 0; i < message.length; i++) {
                        if (~message[i] != response[i]) {
                            throw new RuntimeException(
                                "ERROR: CORRUPTED RESPONSE: " +
                                "at element " + i + "/" + message.length +
                                " of returned array (should be " +
                                Integer.toHexString(~message[i]) + ", is " +
                                Integer.toHexString(response[i]) + ")");
                        }
                    }
                    try {
                        Thread.sleep(Math.abs(random.nextInt() % 10) * 1000);
                    } catch (InterruptedException e) {
                    }
                } while (System.currentTimeMillis() < stopTime);
            } catch (Exception e) {
                status = e;
            }
            finished = true;
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    private static void usage() {
        System.err.println("Usage: AppleUserImpl [-hours <hours> | " +
                                                 "-seconds <seconds>]");
        System.err.println("                     [-maxLevel <maxLevel>]");
        System.err.println("                     [-othervm]");
        System.err.println("                     [-exit]");
        System.err.println("  hours    The number of hours to run the juicer.");
        System.err.println("           The default is 0 hours.");
        System.err.println("  seconds  The number of seconds to run the juicer.");
        System.err.println("           The default is 0 seconds.");
        System.err.println("  maxLevel The maximum number of levels to ");
        System.err.println("           recurse on each call.");
        System.err.println("           The default is 7 levels.");
        System.err.println("  othervm  If present, the VM will wait for the");
        System.err.println("           ApplicationServer to start in");
        System.err.println("           another process.");
        System.err.println("           The default is to run everything in");
        System.err.println("           a single VM.");
        System.err.println("  exit     If present, the VM will call");
        System.err.println("           System.exit() when main() finishes.");
        System.err.println("           The default is to not call");
        System.err.println("           System.exit().");
        System.err.println();
    }
    public static void main(String[] args) {
        String durationString = null;
        boolean othervm = false;
        boolean exit = false;
        try {
            for (int i = 0; i < args.length ; i++ ) {
                String arg = args[i];
                if (arg.equals("-hours")) {
                    if (durationString != null) {
                        usage();
                    }
                    i++;
                    int hours = Integer.parseInt(args[i]);
                    durationString = hours + " hours";
                    testDuration = hours * 60 * 60 * 1000;
                } else if (arg.equals("-seconds")) {
                    if (durationString != null) {
                        usage();
                    }
                    i++;
                    long seconds = Long.parseLong(args[i]);
                    durationString = seconds + " seconds";
                    testDuration = seconds * 1000;
                } else if (arg.equals("-maxLevel")) {
                    i++;
                    maxLevel = Integer.parseInt(args[i]);
                } else if (arg.equals("-othervm")) {
                    othervm = true;
                } else if (arg.equals("-exit")) {
                    exit = true;
                } else {
                    usage();
                }
            }
            if (durationString == null) {
                durationString = testDuration + " milliseconds";
            }
        } catch (Throwable t) {
            usage();
            throw new RuntimeException("TEST FAILED: Bad argument");
        }
        AppleUserImpl user = null;
        long startTime = 0;
        Thread server = null;
        int exitValue = 0;
        try {
            user = new AppleUserImpl();
            synchronized (user) {
                LocateRegistry.createRegistry(2006);
                LocateRegistry.getRegistry(2006).rebind("AppleUser",user);
                if (othervm) {
                    logger.log(Level.INFO, "Application server must be " +
                        "started in separate process");
                } else {
                    Class app = Class.forName("ApplicationServer");
                    server = new Thread((Runnable) app.newInstance());
                    logger.log(Level.INFO, "Starting application server " +
                        "in same process");
                    server.start();
                }
                logger.log(Level.INFO, "Waiting for application server " +
                    "process to start");
                while (!startTestNotified) {
                   user.wait();
                }
            }
            startTime = System.currentTimeMillis();
            logger.log(Level.INFO, "Test starting");
            logger.log(Level.INFO, "Waiting " + durationString + " for " +
                "test to complete or exception to be thrown");
            synchronized (lock) {
                while (status == null && !finished) {
                    lock.wait();
                }
            }
            if (status != null) {
                throw new RuntimeException("TEST FAILED: "
                    + "juicer server reported an exception", status);
            } else {
                logger.log(Level.INFO, "TEST PASSED");
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "TEST FAILED");
            exitValue = 1;
            if (exit) {
                e.printStackTrace();
            }
            throw new RuntimeException("TEST FAILED: "
                    + "unexpected exception", e);
        } finally {
            long actualDuration = System.currentTimeMillis() - startTime;
            logger.log(Level.INFO, "Test finished");
            try {
                UnicastRemoteObject.unexportObject(user, true);
            } catch (NoSuchObjectException ignore) {
            }
            logger.log(Level.INFO, "Test duration was " +
                (actualDuration/1000) + " seconds " +
                "(" + (actualDuration/3600000) + " hours)");
            System.gc(); System.gc();
            if (exit) {
                System.exit(exitValue);
            }
        }
    }
}

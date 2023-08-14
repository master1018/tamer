public class ActivationLibrary {
    private static final int SAFE_WAIT_TIME;
    static {
        int slopFactor = 1;
        try {
            slopFactor = Integer.valueOf(
                TestLibrary.getExtraProperty("jcov.sleep.multiplier","1"));
        } catch (NumberFormatException ignore) {}
        SAFE_WAIT_TIME = 60000 * slopFactor;
    }
    private static final String SYSTEM_NAME =
        ActivationSystem.class.getName();
    private static void mesg(Object mesg) {
        System.err.println("ACTIVATION_LIBRARY: " + mesg.toString());
    }
    public static void deactivate(Remote remote,
                                  ActivationID id) {
        for (int i = 0; i < 5; i ++) {
            try {
                if (Activatable.inactive(id) == true) {
                    mesg("inactive successful");
                    return;
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                continue;
            } catch (Exception e) {
                try {
                    Activatable.unexportObject(remote, true);
                } catch (NoSuchObjectException ex) {
                }
                return;
            }
        }
        mesg("unable to inactivate after several attempts");
        mesg("unexporting object forcibly instead");
        try {
            Activatable.unexportObject(remote, true);
        } catch (NoSuchObjectException e) {
        }
    }
    public static boolean rmidRunning(int port) {
        int allowedNotReady = 10;
        int connectionRefusedExceptions = 0;
        for (int i = 0; i < 15 ; i++) {
            try {
                Thread.sleep(500);
                LocateRegistry.getRegistry(port).lookup(SYSTEM_NAME);
                return true;
            } catch (java.rmi.ConnectException e) {
                if ((connectionRefusedExceptions ++) >= allowedNotReady) {
                    return false;
                }
            } catch (NotBoundException e) {
                return false;
            } catch (Exception e) {
                mesg("caught an exception trying to" +
                     " start rmid, last exception was: " +
                     e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }
    private static boolean
        containsString(String[] strings, String contained)
    {
        if (strings == null) {
            if (contained == null) {
                return true;
            }
            return false;
        }
        for (int i = 0 ; i < strings.length ; i ++ ) {
            if ((strings[i] != null) &&
                (strings[i].indexOf(contained) >= 0))
            {
                return true;
            }
        }
        return false;
    }
    public static void rmidCleanup(RMID rmid) {
        rmidCleanup(rmid, TestLibrary.RMID_PORT);
    }
    public static void rmidCleanup(RMID rmid, int port) {
        if (rmid != null) {
            if (!ActivationLibrary.safeDestroy(rmid, port, SAFE_WAIT_TIME)) {
                TestLibrary.bomb("rmid not destroyed in: " +
                                 SAFE_WAIT_TIME +
                                 " milliseconds");
            }
        }
        RMID.removeLog();
    }
    private static boolean safeDestroy(RMID rmid, int port, long timeAllowed) {
        DestroyThread destroyThread = new DestroyThread(rmid, port);
        destroyThread.start();
        try {
            destroyThread.join(timeAllowed);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        return destroyThread.shutdownSucceeded();
    }
    private static class DestroyThread extends Thread {
        private final RMID rmid;
        private final int port;
        private boolean succeeded = false;
        DestroyThread(RMID rmid, int port) {
            this.rmid = rmid;
            this.port = port;
            this.setDaemon(true);
        }
        public void run() {
            if (ActivationLibrary.rmidRunning(port)) {
                rmid.destroy();
                synchronized (this) {
                    succeeded = true;
                }
                mesg("finished destroying rmid");
            } else {
                mesg("tried to shutdown when rmid was not running");
            }
        }
        public synchronized boolean shutdownSucceeded() {
            return succeeded;
        }
    }
}

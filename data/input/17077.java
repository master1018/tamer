public class ShutdownGracefully
    extends Activatable implements Runnable, RegisteringActivatable
{
    private static RegisteringActivatable registering = null;
    private final static long SHUTDOWN_TIMEOUT = 400 * 1000;
    public static void main(String args[]) {
        RMID rmid = null;
        System.err.println("\nRegression test for bug/rfe 4183169\n");
        try {
            TestLibrary.suggestSecurityManager(
                "java.rmi.RMISecurityManager");
            RMID.removeLog();
            rmid = RMID.createRMID();
            rmid.addOptions(new String[] {
                "-Djava.security.manager=TestSecurityManager",
                "-Dsun.rmi.activation.snapshotInterval=1"});
            rmid.start();
            Properties p = new Properties();
            p.put("java.security.policy",
                  TestParams.defaultGroupPolicy);
            p.put("java.security.manager",
                  "java.lang.SecurityManager");
            System.err.println("activation group will be created " +
                               "in a new VM");
            ActivationGroupDesc groupDesc =
                new ActivationGroupDesc(p, null);
            ActivationSystem system = ActivationGroup.getSystem();
            ActivationGroupID groupID = system.registerGroup(groupDesc);
            System.err.println("registering activatable");
            ActivationDesc desc = new ActivationDesc
                (groupID, "ShutdownGracefully", null, null);
            registering = (RegisteringActivatable)
                Activatable.register(desc);
            System.err.println("activate and deactivate object " +
                               "via method call");
            registering.shutdown();
            p.put("dummyname", "dummyvalue");
            groupDesc = new ActivationGroupDesc(p, null);
            ActivationGroupID secondGroupID =
                system.registerGroup(groupDesc);
            desc = new ActivationDesc(secondGroupID,
                "ShutdownGracefully", null, null);
            try {
                registering = (RegisteringActivatable)
                    Activatable.register(desc);
                System.err.println("second activate and deactivate " +
                                   "object via method call");
            } catch (ActivationException e) {
                System.err.println("received exception from registration " +
                                   "call that should have failed...");
            }
        } catch (Exception e) {
            TestLibrary.bomb("\nfailure: unexpected exception ", e);
        } finally {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
            registering = null;
            Process rmidProcess = rmid.getVM();
            if (rmidProcess != null) {
                try {
                    Runnable waitThread =
                        new ShutdownDetectThread(rmidProcess);
                    synchronized (waitThread) {
                        (new Thread(waitThread)).start();
                        waitThread.wait(SHUTDOWN_TIMEOUT);
                        System.err.println("rmid has shutdown");
                        if (!rmidDone) {
                            rmidProcess.destroy();
                            TestLibrary.bomb("rmid did not shutdown " +
                                             "gracefully in time");
                        }
                    }
                } catch (Exception e) {
                    TestLibrary.bomb("exception waiting for rmid " +
                                     "to shut down");
                }
            }
        }
        System.err.println
            ("\nsuccess: ShutdownGracefully test passed ");
    }
    private static boolean rmidDone = false;
    private static class ShutdownDetectThread implements Runnable {
        private Process rmidProcess = null;
        ShutdownDetectThread(Process rmidProcess) {
            this.rmidProcess = rmidProcess;
        }
        public void run() {
            System.err.println("waiting for rmid to shutdown");
            try {
                rmidProcess.waitFor();
            } catch (InterruptedException e) {
            }
            synchronized (this) {
                this.notify();
                rmidDone = true;
            }
            RMID.removeLog();
        }
    }
    public ShutdownGracefully
        (ActivationID id, MarshalledObject mo) throws RemoteException
    {
        super(id, 0);
    }
    public void shutdown() throws Exception {
        (new Thread(this, "ShutdownGracefully")).start();
    }
    public void run() {
        try {
            Thread.sleep(50 * 1000);
        } catch (InterruptedException e) {
        }
        ActivationLibrary.deactivate(this, getID());
    }
}

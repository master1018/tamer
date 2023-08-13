public class DeadCachedConnection {
    static public final int regport = 17340;
    static public void main(String[] argv)
        throws Exception {
        System.err.println ("Starting registry on port " + regport);
        DeadCachedConnection.makeRegistry(regport);
        Registry reg = null;
        System.err.println ("Locating just-started registry...");
        try {
            reg = LocateRegistry.getRegistry(regport);
        } catch (RemoteException e) {
            throw new InternalError ("Can't find registry after starting it.");
        }
        System.err.println ("Connecting to registry...");
        String[] junk = reg.list();
        System.err.println("Killing registry...");
        DeadCachedConnection.killRegistry();
        System.err.println("Restarting registry...");
        DeadCachedConnection.makeRegistry(regport);
        System.err.println("Trying to use registry in spite of stale cache...");
        junk = reg.list();
        System.err.println("Test succeeded.");
        try {
            DeadCachedConnection.killRegistry();
        } catch (Exception foo) {
        }
    }
    public static void makeRegistry(int p) {
        try {
            JavaVM jvm =
                new JavaVM("sun.rmi.registry.RegistryImpl", "", Integer.toString(p));
            jvm.start();
            DeadCachedConnection.subreg = jvm.getVM();
        } catch (IOException e) {
            System.out.println ("Test setup failed - cannot run rmiregistry");
            TestLibrary.bomb("Test setup failed - cannot run test", e);
        }
        try {
            Thread.sleep (5000);
        } catch (Exception whatever) {
        }
    }
    private static Process subreg = null;
    public static void killRegistry() {
        if (DeadCachedConnection.subreg != null) {
            DeadCachedConnection.subreg.destroy();
            try { Thread.sleep(2000); } catch (InterruptedException ie) {}
        }
        DeadCachedConnection.subreg = null;
    }
}

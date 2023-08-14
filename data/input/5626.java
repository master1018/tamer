public class Reexport {
    static public final int regport = TestLibrary.REGISTRY_PORT;
    static public void main(String[] argv) {
        Registry reg = null;
        try {
            System.err.println("\nregression test for 4120329\n");
            System.err.println("Starting registry on port " + regport);
            Reexport.makeRegistry(regport);
            System.err.println("Creating duplicate registry, this should fail...");
            reg = createReg(true);
            if (reg != null) {
                TestLibrary.bomb("failed was able to duplicate the registry?!?");
            }
            System.err.println("Bringing down the first registry");
            try {
                Reexport.killRegistry();
            } catch (Exception foo) {
            }
            System.err.println("Trying again to start our own " +
                               "registry... this should work");
            reg = createReg(false);
            if (reg == null) {
                TestLibrary.bomb("Could not create registry on second try");
            }
            System.err.println("Test passed");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            killRegistry();
            reg = null;
        }
    }
    static Registry createReg(boolean remoteOk) {
        Registry reg = null;
        try {
            reg = LocateRegistry.createRegistry(regport);
        } catch (Throwable e) {
            if (remoteOk) {
                System.err.println("EXPECTING PORT IN USE EXCEPTION:");
                System.err.println(e.getMessage());
                e.printStackTrace();
            } else {
                TestLibrary.bomb((Exception) e);
            }
        }
        return reg;
    }
    public static void makeRegistry(int p) {
        try {
            JavaVM jvm = new JavaVM("RegistryRunner", "", Integer.toString(p));
            jvm.start();
            Reexport.subreg = jvm.getVM();
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
        if (Reexport.subreg != null) {
            RegistryRunner.requestExit();
            try {
                Reexport.subreg.waitFor();
            } catch (InterruptedException ie) {
            }
        }
        Reexport.subreg = null;
    }
}

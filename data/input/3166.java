public class AltSecurityManager implements Runnable {
    static JavaVM vm = null;
    static String utilityToStart = null;
    static String registry = "sun.rmi.registry.RegistryImpl";
    static String rmid = "sun.rmi.server.Activation";
    static long TIME_OUT = 15000;
    public void run() {
        try {
            vm = new JavaVM(utilityToStart,
                            " -Djava.security.manager=TestSecurityManager",
                            "");
            System.err.println("starting " + utilityToStart);
            vm.start();
            vm.getVM().waitFor();
        } catch (Exception e) {
            TestLibrary.bomb(e);
        }
    }
    public static void ensureExit(String utility) throws Exception {
        utilityToStart = utility;
        try {
            Thread thread = new Thread(new AltSecurityManager());
            System.err.println("expecting RuntimeException for " +
                               "checkListen in child process");
            long start = System.currentTimeMillis();
            thread.start();
            thread.join(TIME_OUT);
            long time = System.currentTimeMillis() - start;
            System.err.println("waited " + time + " millis for " +
                               utilityToStart + " to die");
            if (time >= TIME_OUT) {
                if (utility.equals(rmid)) {
                    RMID.shutdown();
                }
                TestLibrary.bomb(utilityToStart +
                                 " took too long to die...");
            } else {
                System.err.println(utilityToStart +
                                   " terminated on time");
            }
        } finally {
            vm.destroy();
            vm = null;
        }
    }
    public static void main(String[] args) {
        try {
            System.err.println("\nRegression test for bug 4183202\n");
            ensureExit(registry);
            ensureExit(rmid);
            System.err.println("test passed");
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            RMID.removeLog();
        }
    }
}

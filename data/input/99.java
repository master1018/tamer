public class CheckUsage {
    public static void main(String[] args) {
        System.err.println("\nregression test for 4151966\n");
        JavaVM registryVM = null;
        try {
            ByteArrayOutputStream berr = new ByteArrayOutputStream();
            registryVM = new JavaVM("sun.rmi.registry.RegistryImpl",
                                    "", "foo",
                                    System.out, berr);
            System.err.println("starting registry");
            registryVM.start();
            System.err.println(" registry exited with status: " +
                               registryVM.getVM().waitFor());
            try {
                Thread.sleep(7000);
            } catch (InterruptedException ie) {
            }
            String usage = new String(berr.toByteArray());
            System.err.println("rmiregistry usage: " + usage);
            if (usage.indexOf("-J") < 0) {
                TestLibrary.bomb("rmiregistry has incorrect usage statement");
            } else {
                System.err.println("test passed");
            }
        } catch (Exception e) {
            TestLibrary.bomb(e);
        } finally {
            registryVM.destroy();
            registryVM = null;
        }
    }
}

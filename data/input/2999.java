public class Test4504153 {
    private final static String DONE = "Done!";
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4504153\n");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        JavaVM vm = new JavaVM(StartRegistry.class.getName(),
                               "-Dsun.rmi.transport.logLevel=v", "", out, err);
        vm.start();
        vm.getVM().waitFor();
        String errString = err.toString();
        System.err.println(
            "child process's standard error output:\n\n" + err + "\n");
        if (errString.indexOf(DONE) < 0) {
            throw new RuntimeException("TEST FAILED: " +
                "failed to collect expected child process output");
        }
        if (errString.indexOf("TCPEndpoint") >= 0) {
            throw new RuntimeException("TEST FAILED: " +
                "unrequested logging output detected");
        }
        System.err.println("TEST PASSED");
    }
    public static class StartRegistry {
        public static void main(String[] args) throws Exception {
            LocateRegistry.createRegistry(0);
            System.err.println(DONE);
        }
    }
}

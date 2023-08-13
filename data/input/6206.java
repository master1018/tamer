public class NoConsoleOutput {
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 6409194\n");
        String loggingPropertiesFile =
            System.getProperty("test.src", ".") +
            File.separatorChar + "logging.properties";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        JavaVM vm = new JavaVM(DoRMIStuff.class.getName(),
            "-Djava.util.logging.config.file=" + loggingPropertiesFile,
                               "", out, err);
        vm.start();
        vm.getVM().waitFor();
        String outString = out.toString();
        String errString = err.toString();
        System.err.println("-------- subprocess standard output: --------");
        System.err.print(out);
        System.err.println("-------- subprocess standard error:  --------");
        System.err.print(err);
        System.err.println("---------------------------------------------");
        if (outString.length() > 0 || errString.length() > 0) {
            throw new Error("TEST FAILED: unexpected subprocess output");
        }
        System.err.println("TEST PASSED");
    }
    public static class DoRMIStuff {
        private static final int PORT = 2020;
        private interface Foo extends Remote {
            Object echo(Object obj) throws RemoteException;
        }
        private static class FooImpl implements Foo {
            FooImpl() { }
            public Object echo(Object obj) { return obj; }
        }
        public static void main(String[] args) throws Exception {
            LocateRegistry.createRegistry(PORT);
            Registry reg = LocateRegistry.getRegistry("", PORT);
            FooImpl fooimpl = new FooImpl();
            UnicastRemoteObject.exportObject(fooimpl, 0);
            reg.rebind("foo", fooimpl);
            Foo foostub = (Foo) reg.lookup("foo");
            FooImpl fooimpl2 = new FooImpl();
            UnicastRemoteObject.exportObject(fooimpl2, 0);
            foostub.echo(fooimpl2);
            UnicastRemoteObject.unexportObject(fooimpl, true);
            UnicastRemoteObject.unexportObject(fooimpl2, true);
        }
    }
}

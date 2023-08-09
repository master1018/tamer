public class ClassPathCodebase {
    private final static long REGISTRY_WAIT = 15000;
    private final static String dummyClassName = "Dummy";
    private final static String dummyBinding = "DummyObject";
    private final static String importCodebase = "codebase_IMPORT_";
    private final static String exportCodebase = "codebase_EXPORT_";
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4242317\n");
        TestLibrary.suggestSecurityManager("java.lang.SecurityManager");
        Process rmiregistry = null;
        try {
            URL importCodebaseURL = TestLibrary.installClassInCodebase(
                dummyClassName, importCodebase, false);
            URL exportCodebaseURL = TestLibrary.installClassInCodebase(
                dummyClassName, exportCodebase, true);
            File rmiregistryDir =
                new File(System.getProperty("user.dir", "."), importCodebase);
            String rmiregistryCommand =
                System.getProperty("java.home") + File.separator +
                "bin" + File.separator + "rmiregistry";
            String cmdarray[] = new String[] {
                rmiregistryCommand,
                "-J-Denv.class.path=.",
                "-J-Djava.rmi.server.codebase=" + exportCodebaseURL,
                Integer.toString(TestLibrary.REGISTRY_PORT) };
            System.err.println("\nCommand used to spawn rmiregistry process:");
            System.err.println("\t" + Arrays.asList(cmdarray).toString());
            rmiregistry = Runtime.getRuntime().exec(cmdarray, null, rmiregistryDir);
            StreamPipe.plugTogether(rmiregistry.getInputStream(), System.err);
            StreamPipe.plugTogether(rmiregistry.getErrorStream(), System.err);
            Thread.sleep(REGISTRY_WAIT);
            System.err.println();
            ClassLoader loader = URLClassLoader.newInstance(
                new URL[] { importCodebaseURL });
            Class dummyClass = Class.forName(dummyClassName, false, loader);
            Remote dummyObject = (Remote) dummyClass.newInstance();
            Registry registry = LocateRegistry.getRegistry(
                "localhost", TestLibrary.REGISTRY_PORT);
            try {
                registry.bind(dummyBinding, dummyObject);
                System.err.println("Bound dummy object in registry");
            } catch (java.rmi.ConnectException e) {
                System.err.println("Error: rmiregistry not started in time");
                throw e;
            } catch (ServerException e) {
                if (e.detail instanceof UnmarshalException &&
                    ((UnmarshalException) e.detail).detail instanceof
                        ClassNotFoundException)
                {
                    System.err.println(
                        "Error: another registry running on port " +
                        TestLibrary.REGISTRY_PORT + "?");
                }
                throw e;
            }
            Remote dummyLookup = registry.lookup(dummyBinding);
            System.err.println(
                "Looked up dummy object from registry: " + dummyLookup);
            Class dummyLookupClass = dummyLookup.getClass();
            String dummyLookupAnnotation =
                RMIClassLoader.getClassAnnotation(dummyLookupClass);
            System.err.println(
                "Class annotation from registry: " + dummyLookupAnnotation);
            System.err.println();
            if (dummyLookupAnnotation.indexOf(exportCodebase) >= 0) {
                System.err.println("TEST PASSED");
            } else if (dummyLookupAnnotation.indexOf(importCodebase) >= 0) {
                throw new RuntimeException(
                    "rmiregistry annotated with CLASSPATH element URL");
            } else {
                throw new RuntimeException(
                    "rmiregistry used unexpected annotation: \"" +
                    dummyLookupAnnotation + "\"");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("TEST FAILED: " + e.toString());
        } finally {
            if (rmiregistry != null) {
                rmiregistry.destroy();
            }
        }
    }
}

public final class ApkBuilder {
    public final static class WrongOptionException extends Exception {
        private static final long serialVersionUID = 1L;
        public WrongOptionException(String message) {
            super(message);
        }
    }
    public final static class ApkCreationException extends Exception {
        private static final long serialVersionUID = 1L;
        public ApkCreationException(String message) {
            super(message);
        }
        public ApkCreationException(Throwable throwable) {
            super(throwable);
        }
    }
    public static void main(String[] args) {
        try {
            new ApkBuilderImpl().run(args);
        } catch (WrongOptionException e) {
            printUsageAndQuit();
        } catch (FileNotFoundException e) {
            printAndExit(e.getMessage());
        } catch (ApkCreationException e) {
            printAndExit(e.getMessage());
        }
    }
    public static void createApk(String[] args) throws FileNotFoundException, WrongOptionException,
            ApkCreationException {
        new ApkBuilderImpl().run(args);
    }
    private static void printUsageAndQuit() {
        System.err.println("A command line tool to package an Android application from various sources.");
        System.err.println("Usage: apkbuilder <out archive> [-v][-u][-storetype STORE_TYPE] [-z inputzip]");
        System.err.println("            [-f inputfile] [-rf input-folder] [-rj -input-path]");
        System.err.println("");
        System.err.println("    -v      Verbose.");
        System.err.println("    -d      Debug Mode: Includes debug files in the APK file.");
        System.err.println("    -u      Creates an unsigned package.");
        System.err.println("    -storetype Forces the KeyStore type. If ommited the default is used.");
        System.err.println("");
        System.err.println("    -z      Followed by the path to a zip archive.");
        System.err.println("            Adds the content of the application package.");
        System.err.println("");
        System.err.println("    -f      Followed by the path to a file.");
        System.err.println("            Adds the file to the application package.");
        System.err.println("");
        System.err.println("    -rf     Followed by the path to a source folder.");
        System.err.println("            Adds the java resources found in that folder to the application");
        System.err.println("            package, while keeping their path relative to the source folder.");
        System.err.println("");
        System.err.println("    -rj     Followed by the path to a jar file or a folder containing");
        System.err.println("            jar files.");
        System.err.println("            Adds the java resources found in the jar file(s) to the application");
        System.err.println("            package.");
        System.err.println("");
        System.err.println("    -nf     Followed by the root folder containing native libraries to");
        System.err.println("            include in the application package.");
        System.exit(1);
    }
    private static void printAndExit(String... messages) {
        for (String message : messages) {
            System.err.println(message);
        }
        System.exit(1);
    }
}

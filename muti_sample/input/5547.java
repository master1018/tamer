public class UpdateManifest {
    static PrintStream out = System.out;
    static PrintStream err = System.err;
    static boolean debug = true;
    public static void realMain(String[] args) throws Throwable {
        if (args.length == 0) {
            debug = false;
            File tmp = File.createTempFile("system-out-err", ".txt");
            tmp.deleteOnExit();
            out = new PrintStream(new FileOutputStream(tmp));
            err = out;
            Logger.getLogger("java.util.jar").setLevel(Level.OFF);
        }
        try { testManifestExistence(); } catch (Throwable t) { unexpected(t); }
        try { testManifestContents(); } catch (Throwable t) { unexpected(t); }
    }
    static void testManifestExistence() throws Throwable {
        File existence = createTextFile("existence");
        final String jarFileName = "um-existence.jar";
        new File(jarFileName).delete(); 
        Main jartool = new Main(out, err, "jar");
        boolean status = jartool.run(
            new String[] { "cfe", jarFileName, "Hello", existence.getPath() });
        check(status);
        checkManifest(jarFileName, "Hello");
        jartool = new Main(out, err, "jar");
        status = jartool.run(
            new String[] { "ufe", jarFileName, "Bye" });
        check(status);
        checkManifest(jarFileName, "Bye");
    }
    static void testManifestContents() throws Throwable {
        final String animal =
            "Name: animal/marsupial";
        final String specTitle =
            "Specification-Title: Wombat";
        File manifestOrig = File.createTempFile("manifestOrig", ".txt");
        if (!debug) manifestOrig.deleteOnExit();
        PrintWriter pw = new PrintWriter(manifestOrig);
        pw.println("Manifest-Version: 1.0");
        pw.println("Created-By: 1.7.0-internal (Oracle Corporation)");
        pw.println("");
        pw.println(animal);
        pw.println(specTitle);
        pw.close();
        File hello = createTextFile("hello");
        final String jarFileName = "um-test.jar";
        new File(jarFileName).delete(); 
        Main jartool = new Main(out, err, "jar");
        boolean status = jartool.run(
                                new String[] {"cfm", jarFileName,
                                    manifestOrig.getPath(), hello.getPath() });
        check(status);
        File manifestUpdate = File.createTempFile("manifestUpdate", ".txt");
        if (!debug) manifestUpdate.deleteOnExit();
        pw = new PrintWriter(manifestUpdate);
        final String createdBy =
            "Created-By: 1.7.0-special (Oracle Corporation)";
        final String specVersion =
            "Specification-Version: 1.0.0.0";
        pw.println(createdBy); 
        pw.println("");
        pw.println(animal);
        pw.println(specVersion); 
        pw.close();
        jartool = new Main(out, err, "jar");
        status = jartool.run(
            new String[] { "ufm", jarFileName, manifestUpdate.getPath() });
        check(status);
        File f = new File(jarFileName);
        if (!debug) f.deleteOnExit();
        ZipFile zf = new ZipFile(f);
        ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
        BufferedReader r = new BufferedReader(
            new InputStreamReader(zf.getInputStream(ze)));
        r.readLine(); 
        check(r.readLine().equals(createdBy));
        r.readLine(); 
        check(r.readLine().equals(animal));
        String s = r.readLine();
        if (s.equals(specVersion)) {
            check(r.readLine().equals(specTitle));
        } else if (s.equals(specTitle)) {
            check(r.readLine().equals(specVersion));
        } else {
            fail("did not match specVersion nor specTitle");
        }
        zf.close();
    }
    static File createTextFile(String name) throws Throwable {
        File rc = File.createTempFile(name, ".txt");
        if (!debug) rc.deleteOnExit();
        PrintWriter pw = new PrintWriter(rc);
        pw.println("hello, world");
        pw.close();
        return rc;
    }
    static void checkManifest(String jarFileName, String mainClass)
                throws Throwable {
        File f = new File(jarFileName);
        if (!debug) f.deleteOnExit();
        ZipFile zf = new ZipFile(f);
        ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
        BufferedReader r = new BufferedReader(
            new InputStreamReader(zf.getInputStream(ze)));
        String line = r.readLine();
        while (line != null && !(line.startsWith("Main-Class:"))) {
            line = r.readLine();
        }
        if (line == null) {
            fail("Didn't find Main-Class in manifest");
        } else {
            check(line.equals("Main-Class: " + mainClass));
        }
        zf.close();
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {
        passed++;
    }
    static void fail() {
        failed++;
        Thread.dumpStack();
    }
    static void fail(String msg) {
        System.out.println(msg);
        fail();
    }
    static void unexpected(Throwable t) {
        failed++;
        t.printStackTrace();
    }
    static void check(boolean cond) {
        if (cond)
            pass();
        else
            fail();
    }
    static void equal(Object x, Object y) {
        if ((x == null) ? (y == null) : x.equals(y))
            pass();
        else
            fail(x + " not equal to " + y);
    }
    public static void main(String[] args) throws Throwable {
        try {
            realMain(args);
        } catch (Throwable t) {
            unexpected(t);
        }
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0)
            throw new AssertionError("Some tests failed");
    }
}

public class FinalizeZipFile {
    private final static CountDownLatch finalizersDone = new CountDownLatch(3);
    private static class InstrumentedZipFile extends ZipFile {
        public InstrumentedZipFile(File f) throws Exception {
            super(f);
            System.out.printf("Using %s%n", f.getPath());
        }
        protected void finalize() throws IOException {
            System.out.printf("Killing %s%n", getName());
            super.finalize();
            finalizersDone.countDown();
        }
    }
    private static void makeGarbage() throws Throwable {
        final Random rnd = new Random();
        final String javaHome = System.getProperty("java.home");
        final File lib = new File(javaHome, "lib");
        check(lib.isDirectory());
        final File[] jars = lib.listFiles(
            new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".jar");}});
        check(jars.length > 1);
        new InstrumentedZipFile(jars[rnd.nextInt(jars.length)]).close();
        new InstrumentedZipFile(jars[rnd.nextInt(jars.length)]).close();
        ZipFile zf = new InstrumentedZipFile(jars[rnd.nextInt(jars.length)]);
        ZipEntry ze = zf.getEntry("META-INF/MANIFEST.MF");
        InputStream is = zf.getInputStream(ze);
    }
    public static void realMain(String[] args) throws Throwable {
        makeGarbage();
        System.gc();
        finalizersDone.await(5, TimeUnit.SECONDS);
        equal(finalizersDone.getCount(), 0L);
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}

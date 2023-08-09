public class JarEntryTime {
    static boolean cleanup(File dir) throws Throwable {
        boolean rc = true;
        File[] x = dir.listFiles();
        if (x != null) {
            for (int i = 0; i < x.length; i++) {
                rc &= x[i].delete();
            }
        }
        return rc & dir.delete();
    }
    static void extractJar(File jarFile, boolean useExtractionTime) throws Throwable {
        String javahome = System.getProperty("java.home");
        if (javahome.endsWith("jre")) {
            javahome = javahome.substring(0, javahome.length() - 4);
        }
        String jarcmd = javahome + File.separator + "bin" + File.separator + "jar";
        String[] args;
        if (useExtractionTime) {
            args = new String[] {
                jarcmd,
                "-J-Dsun.tools.jar.useExtractionTime=true",
                "xf",
                jarFile.getName() };
        } else {
            args = new String[] {
                jarcmd,
                "xf",
                jarFile.getName() };
        }
        Process p = Runtime.getRuntime().exec(args);
        check(p != null && (p.waitFor() == 0));
    }
    public static void realMain(String[] args) throws Throwable {
        File dirOuter = new File("outer");
        File dirInner = new File(dirOuter, "inner");
        File jarFile = new File("JarEntryTime.jar");
        cleanup(dirInner);
        cleanup(dirOuter);
        jarFile.delete();
        check(dirOuter.mkdir());
        check(dirInner.mkdir());
        File fileInner = new File(dirInner, "foo.txt");
        PrintWriter pw = new PrintWriter(fileInner);
        pw.println("hello, world");
        pw.close();
        final long now = fileInner.lastModified();
        final long earlier = now - (60L * 60L * 6L * 1000L);
        final long yesterday = now - (60L * 60L * 24L * 1000L);
        final long PRECISION = 10000L;
        dirOuter.setLastModified(now);
        dirInner.setLastModified(yesterday);
        fileInner.setLastModified(earlier);
        Main jartool = new Main(System.out, System.err, "jar");
        check(jartool.run(new String[] {
                "cf",
                jarFile.getName(), dirOuter.getName() } ));
        check(jarFile.exists());
        check(cleanup(dirInner));
        check(cleanup(dirOuter));
        extractJar(jarFile, false);
        check(dirOuter.exists());
        check(dirInner.exists());
        check(fileInner.exists());
        check(Math.abs(dirOuter.lastModified() - now) <= PRECISION);
        check(Math.abs(dirInner.lastModified() - yesterday) <= PRECISION);
        check(Math.abs(fileInner.lastModified() - earlier) <= PRECISION);
        check(cleanup(dirInner));
        check(cleanup(dirOuter));
        extractJar(jarFile, true);
        check(dirOuter.exists());
        check(dirInner.exists());
        check(fileInner.exists());
        check(Math.abs(dirOuter.lastModified() - now) <= PRECISION);
        check(Math.abs(dirInner.lastModified() - now) <= PRECISION);
        check(Math.abs(fileInner.lastModified() - now) <= PRECISION);
        check(cleanup(dirInner));
        check(cleanup(dirOuter));
        check(jarFile.delete());
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
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}

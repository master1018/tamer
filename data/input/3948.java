public class ChangeDir {
    private final static String jarName = "test.jar";
    private final static String fileName = "hello.txt";
    private static void cleanup(File dir) throws Throwable {
        if (dir != null && dir.exists()) {
            for (File ff : dir.listFiles()) {
                check(ff.delete());
            }
            check(dir.delete());
            check(new File(jarName).delete());
        }
    }
    public static void realMain(String[] args) throws Throwable {
        doTest("/");
        doTest("
        doTest("
        doTest("
        if (System.getProperty("os.name").startsWith("Windows")) {
            doTest("\\");
            doTest("\\\\");
            doTest("\\\\\\");
            doTest("\\\\\\\\");
            doTest("\\/");
        }
    }
    static void doTest(String sep) throws Throwable {
        File testDir = null;
        JarFile jf = null;
        try {
            File f = File.createTempFile("delete", ".me");
            String dirName = f.getParent();
            testDir = new File(dirName + sep + "a" + sep + "b");
            cleanup(testDir);
            check(testDir.mkdirs());
            File testFile = new File(testDir, fileName);
            check(testFile.createNewFile());
            List<String> argList = new ArrayList<String>();
            argList.add("cf");
            argList.add(jarName);
            argList.add("-C");
            argList.add(dirName + sep + "a" + sep + sep + "b"); 
            argList.add(fileName);
            String jarArgs[] = new String[argList.size()];
            jarArgs = argList.toArray(jarArgs);
            Main jarTool = new Main(System.out, System.err, "jar");
            if (!jarTool.run(jarArgs)) {
                fail("Could not create jar file.");
            }
            jf = new JarFile(jarName);
            for (Enumeration<JarEntry> i = jf.entries(); i.hasMoreElements();) {
                JarEntry je = i.nextElement();
                String name = je.getName();
                if (name.indexOf(fileName) != -1) {
                    if (name.indexOf(fileName) != 0) {
                        fail(String.format(
                                 "Expected '%s' but got '%s'%n", fileName, name));
                    }
                }
            }
        } finally {
            if (jf != null) {
                jf.close();
            }
            cleanup(testDir);
        }
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

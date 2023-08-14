public class T6942366 {
    public static void main(String... args) throws Exception {
        new T6942366().run();
    }
    File testSrc;
    File testClasses;
    int count;
    int errors;
    void run() throws Exception {
        testSrc = new File(System.getProperty("test.src"));
        testClasses = new File(System.getProperty("test.classes"));
        test(true,  false);
        test(false, true);
        test(true,  true);
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }
    void test(boolean useSourcePath, boolean useClassPath) throws Exception {
        System.out.println("test " + (++count) + " sp:" + useSourcePath + " cp:" + useClassPath);
        File testDir = new File("test" + count);
        testDir.mkdirs();
        List<String> args = new ArrayList<String>();
        args.add("-d");
        args.add(testDir.getPath());
        if (useSourcePath) {
            args.add("-sourcepath");
            args.add(testSrc.getPath());
        }
        if (useClassPath) {
            args.add("-classpath");
            args.add(testClasses.getPath());
        } else {
            args.add("-classpath");
            args.add(".");
        }
        File javaHome = new File(System.getProperty("java.home"));
        File rt_jar = new File(javaHome, "lib/rt.jar");
        if (!rt_jar.exists())
            throw new Exception("rt.jar not found");
        args.add("-bootclasspath");
        args.add(rt_jar.getPath());
        args.add(new File(testSrc, "Test.java").getPath());
        System.out.println("javadoc: " + args);
        int rc = com.sun.tools.javadoc.Main.execute(args.toArray(new String[args.size()]));
        if (rc != 0)
            throw new Exception("unexpected exit from javadoc, rc=" + rc);
        if (useSourcePath && useClassPath) {
            long srcLastMod = new File(testSrc, "Test.java").lastModified();
            long classLastMod = new File(testClasses, "Test.class").lastModified();
            System.out.println("Test.java last modified:  " + new Date(srcLastMod));
            System.out.println("Test.class last modified: " + new Date(classLastMod));
            System.out.println((srcLastMod > classLastMod ? "source" : "class") + " is newer");
        }
        String s = "javadoc-for-Base.m";
        boolean expect = useSourcePath;
        boolean found = contains(new File(testDir, "Test.html"), s);
        if (found) {
            if (expect)
                System.out.println("javadoc content \"" + s + "\" found, as expected");
            else
                error("javadoc content \"" + s + "\" found unexpectedly");
        } else {
            if (expect)
                error("javadoc content \"" + s + "\" not found");
            else
                System.out.println("javadoc content \"" + s + "\" not found, as expected");
        }
        System.out.println();
    }
    boolean contains(File f, String s) throws Exception {
        byte[] buf = new byte[(int) f.length()];
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            in.readFully(buf);
        }
        return new String(buf).contains(s);
    }
    void error(String msg) {
        System.out.println("Error: " + msg);
        errors++;
    }
}

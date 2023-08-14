public class TestUserDoclet extends Doclet {
    public static void main(String... args) throws Exception {
        new TestUserDoclet().run();
    }
    static final String docletWarning = "warning from test doclet";
    public static boolean start(RootDoc root) {
        root.printWarning(null, docletWarning);
        return true;
    }
    void run() throws Exception {
        File javaHome = new File(System.getProperty("java.home"));
        if (javaHome.getName().equals("jre"))
            javaHome = javaHome.getParentFile();
        File javadoc = new File(new File(javaHome, "bin"), "javadoc");
        File testSrc = new File(System.getProperty("test.src"));
        File testClasses = new File(System.getProperty("test.classes"));
        String thisClassName = TestUserDoclet.class.getName();
        Process p = new ProcessBuilder()
            .command(javadoc.getPath(),
                "-J-Xbootclasspath:" + System.getProperty("sun.boot.class.path"),
                "-doclet", thisClassName,
                "-docletpath", testClasses.getPath(),
                new File(testSrc, thisClassName + ".java").getPath())
            .redirectErrorStream(true)
            .start();
        int actualDocletWarnCount = 0;
        int reportedDocletWarnCount = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.err.println(line);
                if (line.contains(docletWarning))
                    actualDocletWarnCount++;
                if (line.matches("[0-9]+ warning(s)?"))
                    reportedDocletWarnCount =
                            Integer.valueOf(line.substring(0, line.indexOf(" ")));
            }
        } finally {
            in.close();
        }
        int rc = p.waitFor();
        if (rc != 0)
            System.err.println("javadoc failed, rc:" + rc);
        int expectedDocletWarnCount = 1;
        checkEqual("actual", actualDocletWarnCount, "expected", expectedDocletWarnCount);
        checkEqual("actual", actualDocletWarnCount, "reported", reportedDocletWarnCount);
    }
    void checkEqual(String l1, int i1, String l2, int i2) throws Exception {
        if (i1 != i2)
            throw new Exception(l1 + " warn count, " + i1 + ", does not match "
                        + l2 + " warn count, " + i2);
    }
}

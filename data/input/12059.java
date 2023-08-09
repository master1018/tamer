public class VersionCheck {
    private static String javaBin;
    private static String[] programs = new String[]{
        "appletviewer",
        "extcheck",
        "idlj",
        "jar",
        "jarsigner",
        "javac",
        "javadoc",
        "javah",
        "javap",
        "jconsole",
        "jdb",
        "jhat",
        "jinfo",
        "jmap",
        "jps",
        "jstack",
        "jstat",
        "jstatd",
        "keytool",
        "native2ascii",
        "orbd",
        "pack200",
        "policytool",
        "rmic",
        "rmid",
        "rmiregistry",
        "schemagen",
        "serialver",
        "servertool",
        "tnameserv",
        "wsgen",
        "wsimport",
        "xjc"
    };
    static String refVersion;
    static String refFullVersion;
    private static List<String> getProcessStreamAsList(boolean javaDebug,
                                                       String... argv) {
        List<String> out = new ArrayList<String>();
        List<String> javaCmds = new ArrayList<String>();
        String prog = javaBin + File.separator + argv[0];
        if (System.getProperty("os.name").startsWith("Windows")) {
            prog = prog.concat(".exe");
        }
        javaCmds.add(prog);
        for (int i = 1; i < argv.length; i++) {
            javaCmds.add(argv[i]);
        }
        ProcessBuilder pb = new ProcessBuilder(javaCmds);
        Map<String, String> env = pb.environment();
        if (javaDebug) {
            env.put("_JAVA_LAUNCHER_DEBUG", "true");
        }
        try {
            Process p = pb.start();
            BufferedReader r = (javaDebug) ?
                new BufferedReader(new InputStreamReader(p.getInputStream())) :
                new BufferedReader(new InputStreamReader(p.getErrorStream())) ;
            String s = r.readLine();
            while (s != null) {
                out.add(s.trim());
                s = r.readLine();
            }
            p.waitFor();
            p.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        return out;
    }
    static String getVersion(String... argv) {
        List<String> alist = getProcessStreamAsList(false, argv);
        if (alist.size() == 0) {
            throw new AssertionError("unexpected process returned null");
        }
        StringBuilder out = new StringBuilder();
        for (String x : alist) {
            if (!x.contains("HotSpot")) {
                out = out.append(x + "\n");
            }
        }
        return out.toString();
    }
    static boolean compareVersionStrings() {
        int failcount = 0;
        for (String x : programs) {
            String testStr;
            testStr = getVersion(x, "-J-version");
            if (refVersion.compareTo(testStr) != 0) {
                failcount++;
                System.out.println("Error: " + x +
                                   " fails -J-version comparison");
                System.out.println("Expected:");
                System.out.print(refVersion);
                System.out.println("Actual:");
                System.out.print(testStr);
            }
            testStr = getVersion(x, "-J-fullversion");
            if (refFullVersion.compareTo(testStr) != 0) {
                failcount++;
                System.out.println("Error: " + x +
                                   " fails -J-fullversion comparison");
                System.out.println("Expected:");
                System.out.print(refFullVersion);
                System.out.println("Actual:");
                System.out.print(testStr);
            }
        }
        System.out.println("Version Test: " + failcount);
        return failcount == 0;
    }
    static boolean compareInternalStrings() {
        int failcount = 0;
        String bStr = refVersion.substring(refVersion.lastIndexOf("build") +
                                           "build".length() + 1,
                                           refVersion.lastIndexOf(")"));
        String[] vStr = bStr.split("\\.|-|_");
        String jdkMajor = vStr[0];
        String jdkMinor = vStr[1];
        String jdkMicro = vStr[2];
        String jdkBuild = vStr[vStr.length - 1];
        String expectedDotVersion = "dotversion:" + jdkMajor + "." + jdkMinor;
        String expectedFullVersion = "fullversion:" + bStr;
        List<String> alist = getProcessStreamAsList(true, "java", "-version");
        if (!alist.contains(expectedDotVersion)) {
            System.out.println("Error: could not find " + expectedDotVersion);
            failcount++;
        }
        if (!alist.contains(expectedFullVersion)) {
            System.out.println("Error: could not find " + expectedFullVersion);
            failcount++;
        }
        System.out.println("Internal Strings Test: " + failcount);
        return failcount == 0;
    }
    static void init() {
        String javaHome = System.getProperty("java.home");
        if (javaHome.endsWith("jre")) {
            javaHome = new File(javaHome).getParent();
        }
        javaBin = javaHome + File.separator + "bin";
        refVersion = getVersion("java", "-version");
        refFullVersion = getVersion("java", "-fullversion");
    }
    public static void main(String[] args) {
        init();
        if (compareVersionStrings() && compareInternalStrings()) {
            System.out.println("All Version string comparisons: PASS");
        } else {
            throw new AssertionError("Some tests failed");
        }
    }
}

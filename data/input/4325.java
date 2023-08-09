public class CommandLineTests {
    private static final File CWD = new File(".");
    private static final File EXP_SDK = new File(CWD, "exp-sdk-image");
    private static final File EXP_SDK_LIB_DIR = new File(EXP_SDK, "lib");
    private static final File EXP_SDK_BIN_DIR = new File(EXP_SDK, "bin");
    private static final File EXP_JRE_DIR = new File(EXP_SDK, "jre");
    private static final File EXP_JRE_LIB_DIR = new File(EXP_JRE_DIR, "lib");
    private static final File RtJar = new File(EXP_JRE_LIB_DIR, "rt.jar");
    private static final File CharsetsJar = new File(EXP_JRE_LIB_DIR, "charsets.jar");
    private static final File JsseJar = new File(EXP_JRE_LIB_DIR, "jsse.jar");
    private static final File ToolsJar = new File(EXP_SDK_LIB_DIR, "tools.jar");
    private static final File javaCmd;
    private static final File javacCmd;
    private static final File ConfigFile = new File("pack.conf");
    private static final List<File> jarList;
    static {
        javaCmd = Utils.IsWindows
                    ? new File(EXP_SDK_BIN_DIR, "java.exe")
                    : new File(EXP_SDK_BIN_DIR, "java");
        javacCmd = Utils.IsWindows
                    ? new File(EXP_SDK_BIN_DIR, "javac.exe")
                    : new File(EXP_SDK_BIN_DIR, "javac");
        jarList = new ArrayList<File>();
        jarList.add(RtJar);
        jarList.add(CharsetsJar);
        jarList.add(JsseJar);
        jarList.add(ToolsJar);
    }
    static void init() throws IOException {
            Utils.recursiveCopy(Utils.JavaSDK, EXP_SDK);
            creatConfigFile();
    }
    static void creatConfigFile() throws IOException {
        FileOutputStream fos = null;
        PrintStream ps = null;
        try {
            fos = new FileOutputStream(ConfigFile);
            ps = new PrintStream(fos);
            ps.println("com.sun.java.util.jar.pack.debug.verbose=0");
            ps.println("pack.modification.time=keep");
            ps.println("pack.keep.class.order=true");
            ps.println("pack.deflate.hint=false");
            ps.println("pack.unknown.attribute=error");
            ps.println("pack.segment.limit=-1");
            ps.println("pack.pass.file.0=java/lang/Error.class");
            ps.println("pack.pass.file.1=java/lang/LinkageError.class");
            ps.println("pack.pass.file.2=java/lang/Object.class");
            ps.println("pack.pass.file.3=java/lang/Throwable.class");
            ps.println("pack.pass.file.4=java/lang/VerifyError.class");
            ps.println("pack.pass.file.5=com/sun/demo/jvmti/hprof/Tracker.class");
        } finally {
            Utils.close(ps);
            Utils.close(fos);
        }
    }
    static void runPack200(boolean jre) throws IOException {
        List<String> cmdsList = new ArrayList<String>();
        for (File f : jarList) {
            if (jre && f.getName().equals("tools.jar")) {
                continue;  
            }
            File bakFile = new File(f.getName() + ".bak");
            if (!bakFile.exists()) {  
                Utils.copyFile(f, bakFile);
            } else {  
                Utils.copyFile(bakFile, f);
            }
            cmdsList.clear();
            cmdsList.add(Utils.getPack200Cmd());
            cmdsList.add("-J-esa");
            cmdsList.add("-J-ea");
            cmdsList.add(Utils.Is64Bit ? "-J-Xmx1g" : "-J-Xmx512m");
            cmdsList.add("--repack");
            cmdsList.add("--config-file=" + ConfigFile.getAbsolutePath());
            if (jre) {
                cmdsList.add("--strip-debug");
            }
            cmdsList.add(f.getAbsolutePath());
            Utils.runExec(cmdsList);
        }
    }
    static void testJRE() throws IOException {
        runPack200(true);
        List<String> cmdsList = new ArrayList<String>();
        cmdsList.add(javaCmd.getAbsolutePath());
        cmdsList.add("-verify");
        cmdsList.add("-version");
        Utils.runExec(cmdsList);
    }
    static void testJDK() throws IOException {
        runPack200(false);
        List<String> cmdsList = new ArrayList<String>();
        cmdsList.add(javaCmd.getAbsolutePath());
        cmdsList.add("-verify");
        cmdsList.add("-version");
        Utils.runExec(cmdsList);
        cmdsList.clear();
        cmdsList.add(javacCmd.getAbsolutePath());
        cmdsList.add("-J-verify");
        cmdsList.add("-help");
        Utils.runExec(cmdsList);
    }
    public static void main(String... args) {
        try {
            init();
            testJRE();
            testJDK();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}

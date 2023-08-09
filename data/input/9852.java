public class ExecutionEnvironment {
    static final String LD_LIBRARY_PATH    = "LD_LIBRARY_PATH";
    static final String LD_LIBRARY_PATH_32 = LD_LIBRARY_PATH + "_32";
    static final String LD_LIBRARY_PATH_64 = LD_LIBRARY_PATH + "_64";
    static final String LD_LIBRARY_PATH_VALUE    = "/Bridge/On/The/River/Kwai";
    static final String LD_LIBRARY_PATH_32_VALUE = "/Lawrence/Of/Arabia";
    static final String LD_LIBRARY_PATH_64_VALUE = "/A/Passage/To/India";
    static final String JLDEBUG_KEY = "_JAVA_LAUNCHER_DEBUG";
    static final String EXPECTED_MARKER = "TRACER_MARKER:About to EXEC";
    static final String[] LD_PATH_STRINGS = {
        LD_LIBRARY_PATH + "=" + LD_LIBRARY_PATH_VALUE,
        LD_LIBRARY_PATH_32 + "=" + LD_LIBRARY_PATH_32_VALUE,
        LD_LIBRARY_PATH_64 + "=" + LD_LIBRARY_PATH_64_VALUE
    };
    static final File testJarFile = new File("EcoFriendly.jar");
    static int errors = 0;
    static int passes = 0;
    static final String LIBJVM = TestHelper.isWindows ? "jvm.dll" : "libjvm.so";
    static void createTestJar() {
        try {
            List<String> codeList = new ArrayList<String>();
            codeList.add("static void printValue(String name, boolean property) {\n");
            codeList.add("    String value = (property) ? System.getProperty(name) : System.getenv(name);\n");
            codeList.add("    System.out.println(name + \"=\" + value);\n");
            codeList.add("}\n");
            codeList.add("public static void main(String... args) {\n");
            codeList.add("    System.out.println(\"Execute test:\");\n");
            codeList.add("    printValue(\"os.name\", true);\n");
            codeList.add("    printValue(\"os.arch\", true);\n");
            codeList.add("    printValue(\"os.version\", true);\n");
            codeList.add("    printValue(\"sun.arch.data.model\", true);\n");
            codeList.add("    printValue(\"java.library.path\", true);\n");
            codeList.add("    printValue(\"" + LD_LIBRARY_PATH + "\", false);\n");
            codeList.add("    printValue(\"" + LD_LIBRARY_PATH_32 + "\", false);\n");
            codeList.add("    printValue(\"" + LD_LIBRARY_PATH_64 + "\", false);\n");
            codeList.add("}\n");
            String[] clist = new String[codeList.size()];
            TestHelper.createJar(testJarFile, codeList.toArray(clist));
        } catch (FileNotFoundException fnfe) {
            throw new RuntimeException(fnfe);
        }
    }
    private static void ensureEcoFriendly() {
        TestHelper.TestResult tr = null;
        Map<String, String> env = new HashMap<String, String>();
        for (String x : LD_PATH_STRINGS) {
            String pairs[] = x.split("=");
            env.put(pairs[0], pairs[1]);
        }
        tr = TestHelper.doExec(env, TestHelper.javaCmd, "-jar",
                testJarFile.getAbsolutePath());
        if (!tr.isNotZeroOutput()) {
            System.out.println(tr);
            throw new RuntimeException("Error: No output at all. Did the test execute ?");
        }
        for (String x : LD_PATH_STRINGS) {
            if (!tr.contains(x)) {
                System.out.println("FAIL: did not get <" + x + ">");
                System.out.println(tr);
                errors++;
            } else {
                passes++;
            }
        }
    }
    static void ensureNoExec() {
        Map<String, String> env = new HashMap<String, String>();
        env.put(JLDEBUG_KEY, "true");
        TestHelper.TestResult tr =
                TestHelper.doExec(env, TestHelper.javaCmd, "-version");
        if (tr.testOutput.contains(EXPECTED_MARKER)) {
            System.out.println("FAIL: EnsureNoExecs: found expected warning <" +
                    EXPECTED_MARKER +
                    "> the process execing ?");
            errors++;
        } else {
            passes++;
        }
        return;
    }
    static void verifyJavaLibraryPath() {
        TestHelper.TestResult tr = null;
        Map<String, String> env = new HashMap<String, String>();
        if (TestHelper.isLinux) {
            for (String x : LD_PATH_STRINGS) {
                String pairs[] = x.split("=");
                env.put(pairs[0], pairs[1]);
            }
            tr = TestHelper.doExec(env, TestHelper.javaCmd, "-jar",
                    testJarFile.getAbsolutePath());
            verifyJavaLibraryPathGeneric(tr);
        } else {
            env.clear();
            env.put(LD_LIBRARY_PATH, LD_LIBRARY_PATH_VALUE);
            tr = TestHelper.doExec(env, TestHelper.javaCmd, "-jar",
                    testJarFile.getAbsolutePath());
            verifyJavaLibraryPathGeneric(tr);
            env.clear();
            for (String x : LD_PATH_STRINGS) {
                String pairs[] = x.split("=");
                env.put(pairs[0], pairs[1]);
            }
            tr = TestHelper.doExec(env, TestHelper.javaCmd, "-jar",
                    testJarFile.getAbsolutePath());
            verifyJavaLibraryPathOverride(tr, true);
            if (TestHelper.dualModePresent() && TestHelper.is32Bit) {
                env.clear();
                for (String x : LD_PATH_STRINGS) {
                    String pairs[] = x.split("=");
                    env.put(pairs[0], pairs[1]);
                }
                tr = TestHelper.doExec(env, TestHelper.javaCmd, "-d64", "-jar",
                    testJarFile.getAbsolutePath());
                verifyJavaLibraryPathOverride(tr, false);
                env.clear();
                env.put(LD_LIBRARY_PATH, LD_LIBRARY_PATH_VALUE);
                tr = TestHelper.doExec(env, TestHelper.javaCmd, "-jar",
                        testJarFile.getAbsolutePath());
                verifyJavaLibraryPathGeneric(tr);
            }
            if (TestHelper.java64Cmd != null && TestHelper.is64Bit) {
                env.clear();
                for (String x : LD_PATH_STRINGS) {
                    String pairs[] = x.split("=");
                    env.put(pairs[0], pairs[1]);
                }
                tr = TestHelper.doExec(env, TestHelper.java64Cmd, "-d32", "-jar",
                    testJarFile.getAbsolutePath());
                verifyJavaLibraryPathOverride(tr, true);
                env.clear();
                env.put(LD_LIBRARY_PATH, LD_LIBRARY_PATH_VALUE);
                tr = TestHelper.doExec(env, TestHelper.java64Cmd, "-d32", "-jar",
                        testJarFile.getAbsolutePath());
                verifyJavaLibraryPathGeneric(tr);
            }
        }
    }
    private static void verifyJavaLibraryPathGeneric(TestHelper.TestResult tr) {
        if (!tr.matches("java.library.path=.*" + LD_LIBRARY_PATH_VALUE + ".*")) {
            System.out.print("FAIL: verifyJavaLibraryPath: ");
            System.out.println(" java.library.path does not contain " +
                    LD_LIBRARY_PATH_VALUE);
            System.out.println(tr);
            errors++;
        } else {
            passes++;
        }
    }
    private static void verifyJavaLibraryPathOverride(TestHelper.TestResult tr,
            boolean is32Bit) {
        if (!tr.matches("java.library.path=.*" +
                (is32Bit ? LD_LIBRARY_PATH_32_VALUE : LD_LIBRARY_PATH_64_VALUE) + ".*")) {
            System.out.print("FAIL: verifyJavaLibraryPathOverride: ");
            System.out.println(" java.library.path does not contain " +
                    (is32Bit ? LD_LIBRARY_PATH_32_VALUE : LD_LIBRARY_PATH_64_VALUE));
            System.out.println(tr);
            errors++;
        } else {
            passes++;
        }
        if (tr.matches("java.library.path=.*" + LD_LIBRARY_PATH_VALUE + ".*")) {
            System.out.print("FAIL: verifyJavaLibraryPathOverride: ");
            System.out.println(" java.library.path contains " +
                    LD_LIBRARY_PATH_VALUE);
            System.out.println(tr);
            errors++;
        } else {
            passes++;
        }
    }
    static void verifyVmSelection() {
        TestHelper.TestResult tr = null;
        if (TestHelper.is32Bit) {
            tr = TestHelper.doExec(TestHelper.javaCmd, "-client", "-version");
            if (!tr.matches("Java.*Client VM.*")) {
                System.out.println("FAIL: the expected vm -client did launch");
                System.out.println(tr);
                errors++;
            } else {
                passes++;
            }
        }
        tr = TestHelper.doExec(TestHelper.javaCmd, "-server", "-version");
        if (!tr.matches("Java.*Server VM.*")) {
            System.out.println("FAIL: the expected vm -server did launch");
            System.out.println(tr);
            errors++;
        } else {
            passes++;
        }
    }
    static void verifyNoSymLink() {
        if (TestHelper.is64Bit) {
            return;
        }
        File symLink = null;
        String libPathPrefix = TestHelper.isSDK ? "jre/lib" : "/lib";
        symLink = new File(TestHelper.JAVAHOME, libPathPrefix +
                TestHelper.getJreArch() + "/" + LIBJVM);
        if (symLink.exists()) {
            System.out.println("FAIL: The symlink exists " +
                    symLink.getAbsolutePath());
            errors++;
        } else {
            passes++;
        }
    }
    public static void main(String... args) throws Exception {
        if (TestHelper.isWindows) {
            System.out.println("Warning: noop on windows");
            return;
        }
        createTestJar();
        ensureNoExec();
        verifyVmSelection();
        ensureEcoFriendly();
        verifyJavaLibraryPath();
        verifyNoSymLink();
        if (errors > 0) {
            throw new Exception("ExecutionEnvironment: FAIL: with " +
                    errors + " errors and passes " + passes );
        } else {
            System.out.println("ExecutionEnvironment: PASS " + passes);
        }
    }
}

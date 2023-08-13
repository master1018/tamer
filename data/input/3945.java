public class Arrrghs {
    private Arrrghs(){}
    final static String VersionStr = "-version:1.1+";
    final static String Cookie = "ReExec Args: ";
    private static String removeExtraQuotes(String in) {
        if (TestHelper.isWindows) {
            in = in.trim();
            if (in.startsWith("\"") && in.endsWith("\"")) {
                return in.substring(1, in.length()-1);
            }
        }
        return in;
    }
    private static boolean detectCookie(InputStream istream,
            String expectedArguments) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(istream));
        boolean retval = false;
        String in = rd.readLine();
        while (in != null) {
            if (TestHelper.debug) System.out.println(in);
            if (in.startsWith(Cookie)) {
                String detectedArgument = removeExtraQuotes(in.substring(Cookie.length()));
                if (expectedArguments.equals(detectedArgument)) {
                    retval = true;
                } else {
                    System.out.println("Error: Expected Arguments\t:'" +
                            expectedArguments + "'");
                    System.out.println(" Detected Arguments\t:'" +
                            detectedArgument + "'");
                }
                if (!TestHelper.debug) {
                    rd.close();
                    istream.close();
                    return retval;
                }
            }
            in = rd.readLine();
        }
        return retval;
    }
    private static boolean doTest0(ProcessBuilder pb, String expectedArguments) {
        boolean retval = false;
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            retval = detectCookie(p.getInputStream(), expectedArguments);
            p.waitFor();
            p.destroy();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        return retval;
    }
    static int doTest(String testArguments, String expectedPattern) {
        ProcessBuilder pb = new ProcessBuilder(TestHelper.javaCmd,
                VersionStr, testArguments);
        Map<String, String> env = pb.environment();
        env.put("_JAVA_LAUNCHER_DEBUG", "true");
        return doTest0(pb, testArguments) ? 0 : 1;
    }
    static int doTest(String testPattern) {
        return doTest(testPattern, testPattern);
    }
    static void quoteParsingTests() {
        TestHelper.TestResult tr =
                TestHelper.doExec(TestHelper.javaCmd, VersionStr, "-version");
        if (!tr.isOK()) {
            System.err.println("Warning:Argument Passing Tests were skipped, " +
                    "no java found in system registry.");
            return;
        }
        TestHelper.testExitValue += doTest("-a -b -c -d");
        TestHelper.testExitValue += doTest("-a    -b      -c       -d");
        TestHelper.testExitValue += doTest("-a \"\"-b      -c\"\" -d");
        TestHelper.testExitValue += doTest("-a \\\"-b -c\\\" -d");
        TestHelper.testExitValue += doTest("-a \"-b \\\"stuff\\\"\" -c -d");
        TestHelper.testExitValue += doTest("-a -b\\\\\\\" -c -d");
        TestHelper.testExitValue += doTest("-a -b\\\\\\\\\" -c -d");
        TestHelper.testExitValue += doTest("-a -b  \t -jre-restrict-search -c -d","-a -b -c -d");
        TestHelper.testExitValue += doTest("foo -version:1.1+");
        System.out.println("Completed arguments quoting tests with " +
                TestHelper.testExitValue + " errors");
    }
    static void runBasicErrorMessageTests() {
        TestHelper.TestResult tr = TestHelper.doExec(TestHelper.javaCmd, "-cp");
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-classpath");
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar");
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javacCmd, "-cp");
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-X");
        tr.checkPositive();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-help");
        tr.checkPositive();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd);
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-Xcomp");
        tr.checkNegative();
        tr.isNotZeroOutput();
        System.out.println(tr);
    }
    static void runMainMethodTests() throws FileNotFoundException {
        TestHelper.TestResult tr = null;
        TestHelper.createJar("MIA", new File("some.jar"), new File("Foo"),
                (String[])null);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("Error: Could not find or load main class MIA");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "MIA");
        tr.contains("Error: Could not find or load main class MIA");
        System.out.println(tr);
        TestHelper.createJar(new File("some.jar"), new File("Foo"),
                "private static void main(String[] args){}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("Error: Main method not found in class Foo");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "Foo");
        tr.contains("Error: Main method not found in class Foo");
        System.out.println(tr);
        TestHelper.createJar(new File("some.jar"), new File("Foo"),
                "public static int main(String[] args){return 1;}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("Error: Main method must return a value of type void in class Foo");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "Foo");
        tr.contains("Error: Main method must return a value of type void in class Foo");
        System.out.println(tr);
        TestHelper.createJar(new File("some.jar"), new File("Foo"),
                "public static void main(Object[] args){}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("Error: Main method not found in class Foo");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "Foo");
        tr.contains("Error: Main method not found in class Foo");
        System.out.println(tr);
         TestHelper.createJar(new File("some.jar"), new File("Foo"),
                "public void main(String[] args){}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("Error: Main method is not static in class Foo");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "Foo");
        tr.contains("Error: Main method is not static in class Foo");
        System.out.println(tr);
        TestHelper.createJar(new File("some.jar"), new File("Foo"),
            "void main(Object[] args){}",
            "int  main(Float[] args){return 1;}",
            "private void main() {}",
            "private static void main(int x) {}",
            "public int main(int argc, String[] argv) {return 1;}",
            "public static void main(String[] args) {System.out.println(\"THE_CHOSEN_ONE\");}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.contains("THE_CHOSEN_ONE");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-cp", "some.jar", "Foo");
        tr.contains("THE_CHOSEN_ONE");
        System.out.println(tr);
        TestHelper.createJar(" Foo ", new File("some.jar"), new File("Foo"),
                "public static void main(String... args){}");
        tr = TestHelper.doExec(TestHelper.javaCmd, "-jar", "some.jar");
        tr.checkPositive();
        System.out.println(tr);
    }
    static void runDiagOptionTests() throws FileNotFoundException {
        TestHelper.TestResult tr = null;
        TestHelper.createJar("MIA", new File("some.jar"), new File("Foo"),
                (String[])null);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-Xdiag", "-jar", "some.jar");
        tr.contains("Error: Could not find or load main class MIA");
        tr.contains("java.lang.ClassNotFoundException: MIA");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd,  "-Xdiag", "-cp", "some.jar", "MIA");
        tr.contains("Error: Could not find or load main class MIA");
        tr.contains("java.lang.ClassNotFoundException: MIA");
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd, "-Xdiag", "NonExistentClass");
        tr.contains("Error: Could not find or load main class NonExistentClass");
        tr.contains("java.lang.ClassNotFoundException: NonExistentClass");
        System.out.println(tr);
    }
    static void test6894719() {
        TestHelper.TestResult tr = null;
        tr = TestHelper.doExec(TestHelper.javaCmd,
                "-no-jre-restrict-search", "-version");
        tr.checkPositive();
        System.out.println(tr);
        tr = TestHelper.doExec(TestHelper.javaCmd,
                "-jre-restrict-search", "-version");
        tr.checkPositive();
        System.out.println(tr);
    }
    public static void main(String[] args) throws FileNotFoundException {
        if (TestHelper.debug) {
            System.out.println("Starting Arrrghs tests");
        }
        quoteParsingTests();
        runBasicErrorMessageTests();
        runMainMethodTests();
        test6894719();
        runDiagOptionTests();
        if (TestHelper.testExitValue > 0) {
            System.out.println("Total of " + TestHelper.testExitValue + " failed");
            System.exit(1);
        } else {
            System.out.println("All tests pass");
        }
    }
}

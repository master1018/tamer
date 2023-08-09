public class TestBootNativeLibraryPath {
    private static final String TESTFILE = "Test6";
    static void createTestClass() throws IOException {
        FileOutputStream fos = new FileOutputStream(TESTFILE + ".java");
        PrintStream ps = new PrintStream(fos);
        ps.println("public class " + TESTFILE + "{");
        ps.println("public static void main(String[] args) {\n");
        ps.println("System.out.println(System.getProperty(\"sun.boot.library.path\"));\n");
        ps.println("}}\n");
        ps.close();
        fos.close();
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        String javacOpts[] = {TESTFILE + ".java"};
        if (javac.run(null, null, null,  javacOpts) != 0) {
            throw new RuntimeException("compilation of " + TESTFILE + ".java Failed");
        }
    }
    static List<String> doExec(String... args) {
        String javaCmd = System.getProperty("java.home") + "/bin/java";
        if (!new File(javaCmd).exists()) {
            javaCmd = System.getProperty("java.home") + "/bin/java.exe";
        }
        ArrayList<String> cmds = new ArrayList<String>();
        cmds.add(javaCmd);
        for (String x : args) {
            cmds.add(x);
        }
        System.out.println("cmds=" + cmds);
        ProcessBuilder pb = new ProcessBuilder(cmds);
        Map<String, String> env = pb.environment();
        pb.directory(new File("."));
        List<String> out = new ArrayList<String>();
        try {
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader rd = new BufferedReader(new InputStreamReader(p.getInputStream()),8192);
            String in = rd.readLine();
            while (in != null) {
                out.add(in);
                System.out.println(in);
                in = rd.readLine();
            }
            int retval = p.waitFor();
            p.destroy();
            if (retval != 0) {
                throw new RuntimeException("Error: test returned non-zero value");
            }
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
    }
    public static void main(String[] args) {
        try {
            if (!System.getProperty("sun.arch.data.model").equals("32")) {
                System.out.println("Warning: test skipped for 64-bit systems\n");
                return;
            }
            String osname = System.getProperty("os.name");
            if (osname.startsWith("Windows")) {
                osname = "Windows";
            }
            createTestClass();
            String libpath = File.pathSeparator + "tmp" + File.pathSeparator + "foobar";
            List<String> processOut = null;
            String sunbootlibrarypath = "-Dsun.boot.library.path=" + libpath;
            processOut = doExec(sunbootlibrarypath, "-cp", ".", TESTFILE);
            if (processOut == null || !processOut.get(0).endsWith(libpath)) {
                throw new RuntimeException("Error: did not get expected error string");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Unexpected error " + ex);
        }
    }
}

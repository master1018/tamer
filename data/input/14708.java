public class T5070898
{
    public static void main(String... args) throws Exception {
        new T5070898().run();
    }
    public void run() throws Exception {
        writeFile();
        compileFile();
        int rc = runJavah();
        System.err.println("exit code: " + rc);
        if (rc == 0)
            throw new Exception("unexpected exit code: " + rc);
    }
    void writeFile() throws Exception {
        String content =
            "package test;\n" +
            "public class JavahTest{\n" +
            "    public static void main(String args){\n" +
            "        System.out.println(\"Test Message\");" +
            "    }\n" +
            "    private static native Object nativeTest();\n" +
            "}\n";
        FileWriter out = new FileWriter("JavahTest.java");
        try {
            out.write(content);
        } finally {
            out.close();
        }
    }
    void compileFile() throws Exception {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        int rc = javac.run(null, null, null, "JavahTest.java");
        if (rc != 0)
            throw new Exception("compilation failed");
    }
    int runJavah() throws Exception {
        List<String> cmd = new ArrayList<String>();
        File java_home = new File(System.getProperty("java.home"));
        if (java_home.getName().equals("jre"))
            java_home = java_home.getParentFile();
        cmd.add(new File(new File(java_home, "bin"), "javah").getPath());
        cmd.add("-J-Xbootclasspath:" + System.getProperty("sun.boot.class.path"));
        cmd.add("JavahTest");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        pb.environment().remove("CLASSPATH");
        Process p = pb.start();
        p.getOutputStream().close();
        String line;
        DataInputStream in = new DataInputStream(p.getInputStream());
        try {
        while ((line = in.readLine()) != null)
            System.err.println(line);
        } finally {
            in.close();
        }
        return p.waitFor();
    }
}

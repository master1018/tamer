public class T6729471
{
    public static void main(String... args) {
        new T6729471().run();
    }
    void run() {
        File testClasses = new File(System.getProperty("test.classes"));
        verify("java.util.Map",
                "public abstract boolean containsKey(java.lang.Object)");
        verify("java.util.Map.Entry",
                "public abstract K getKey()");
        verify(new File(testClasses, "T6729471.class").getPath(),
                "public static void main(java.lang.String...)");
        verify(new File(testClasses, "T6729471.class").toURI().toString(),
                "public static void main(java.lang.String...)");
        File java_home = new File(System.getProperty("java.home"));
        if (java_home.getName().equals("jre"))
            java_home = java_home.getParentFile();
        File rt_jar = new File(new File(new File(java_home, "jre"), "lib"), "rt.jar");
        try {
            verify("jar:" + rt_jar.toURL() + "!/java/util/Map.class",
                "public abstract boolean containsKey(java.lang.Object)");
        } catch (MalformedURLException e) {
            error(e.toString());
        }
        File ct_sym = new File(new File(java_home, "lib"), "ct.sym");
        if (ct_sym.exists()) {
            try {
                verify("jar:" + ct_sym.toURL() + "!/META-INF/sym/rt.jar/java/util/Map.class",
                    "public abstract boolean containsKey(java.lang.Object)");
            } catch (MalformedURLException e) {
                error(e.toString());
            }
        } else
            System.err.println("warning: ct.sym not found");
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    void verify(String className, String... expects) {
        String output = javap(className);
        for (String expect: expects) {
            if (output.indexOf(expect)< 0)
                error(expect + " not found");
        }
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
    String javap(String className) {
        String testClasses = System.getProperty("test.classes", ".");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        String[] args = { "-classpath", testClasses, className };
        int rc = com.sun.tools.javap.Main.run(args, out);
        out.close();
        String output = sw.toString();
        System.out.println("class " + className);
        System.out.println(output);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        if (output.indexOf("Error:") != -1)
            throw new Error("javap reported error.");
        return output;
    }
}

public class T6587786 {
    public static void main(String[] args) throws Exception {
        new T6587786().run();
    }
    public void run() throws IOException {
        javap("com.sun.javadoc.Doc", "com.sun.crypto.provider.ai");
        javap("com.sun.crypto.provider.ai", "com.sun.javadoc.ClassDoc");
    }
    void javap(String... args) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        System.out.println(sw.toString());
    }
}

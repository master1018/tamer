public class T4884240 {
    public static void main(String... args) throws Exception {
        new T4884240().run();
    }
    public void run() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { "-sysinfo", "java.lang.Object" };
        int rc = com.sun.tools.javap.Main.run(args, pw);
        if (rc != 0)
            throw new Exception("unexpected return code: " + rc);
        pw.close();
        String[] lines = sw.toString().split("\n");
        if (lines.length < 3
            || !lines[0].trim().startsWith("Classfile")
            || !lines[1].trim().startsWith("Last modified")
            || !lines[2].trim().startsWith("MD5")) {
            System.out.println(sw);
            throw new Exception("unexpected output");
        }
    }
}

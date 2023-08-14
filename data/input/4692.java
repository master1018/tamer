public class T6863746 {
    public static void main(String... args) throws Exception{
        new T6863746().run();
    }
    public void run() throws Exception {
        String[] args = { "-c", "java.lang.Object" };
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        System.out.println(out);
        String[] lines = out.split("\n");
        if (lines.length < 50 || out.indexOf("Code:") == -1)
            throw new Exception("unexpected output from javap");
    }
}

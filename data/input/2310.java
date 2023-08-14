public class T6937244 {
    public static void main(String[] args) throws Exception {
        new T6937244().run();
    }
    void run() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { "java.lang.String" };
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        System.err.println(out);
        if (rc != 0)
            throw new Exception("unexpected exit from javap: " + rc);
        for (String line: out.split("[\r\n]+")) {
            if (line.contains("CASE_INSENSITIVE_ORDER")) {
                if (line.matches("\\s*\\Qpublic static final java.util.Comparator<java.lang.String> CASE_INSENSITIVE_ORDER;\\E\\s*"))
                    return;
                throw new Exception("declaration not shown as expected");
            }
        }
        throw new Exception("declaration of CASE_INSENSITIVE_ORDER not found");
    }
}

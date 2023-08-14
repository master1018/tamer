public class T6937244A {
    public static void main(String[] args) throws Exception {
        new T6937244A().run();
    }
    void run() throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { "Test" };
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        System.err.println(out);
        if (rc != 0)
            throw new Exception("unexpected exit from javap: " + rc);
        int count = 0;
        for (String line: out.split("[\r\n]+")) {
            if (line.contains("implements")) {
                verify(line, "implements java.util.List<java.lang.String>");
                count++;
            }
            if (line.contains("field")) {
                verify(line, "java.util.List<java.lang.String> field");
                count++;
            }
            if (line.contains("method")) {
                verify(line, "java.util.List<java.lang.String> method(java.util.List<java.lang.String>) throws java.lang.Exception");
                count++;
            }
        }
        if (out.contains("/"))
            throw new Exception("unexpected \"/\" in output");
        if (count != 3)
            throw new Exception("wrong number of matches found: " + count);
    }
    void verify(String line, String expect) throws Exception {
        if (!line.contains(expect)) {
            System.err.println("line:   " + line);
            System.err.println("expect: " + expect);
            throw new Exception("expected string not found in line");
        }
    }
}
abstract class Test implements List<String> {
    public List<String> field;
    public List<String> method(List<String> arg) throws Exception { return null; }
}

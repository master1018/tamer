public class T4880672
{
    public static void main(String... args) {
        new T4880672().run();
    }
    void run() {
        verify("java.util.Map", "public interface java.util.Map$Entry");
        verify("T4880672", "class T4880672$A$B");
        verify("C", ""); 
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
        String[] args = { "-XDinner", "-classpath", testClasses, className };
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
    class A {
        class B { }
    }
}
class C { }

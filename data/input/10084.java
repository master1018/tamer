public class T6868539
{
    public static void main(String... args) {
        new T6868539().run();
    }
    void run() {
        String output = javap("T6868539");
        verify(output, "Utf8 +java/lang/String");                                   
        verify(output, "Integer +123456");                                          
        verify(output, "Float +123456.0f");                                         
        verify(output, "Long +123456l");                                            
        verify(output, "Double +123456.0d");                                        
        verify(output, "Class +#[0-9]+ +
        verify(output, "String +#[0-9]+ +
        verify(output, "Fieldref +#[0-9]+\\.#[0-9]+ +
        verify(output, "Methodref +#[0-9]+\\.#[0-9]+ +
        verify(output, "InterfaceMethodref +#[0-9]+\\.#[0-9]+ +
        verify(output, "NameAndType +#[0-9]+:#[0-9]+ +
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    void verify(String output, String... expects) {
        for (String expect: expects) {
            if (!output.matches("(?s).*" + expect + ".*"))
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
        String[] args = { "-v", "-classpath", testClasses, className };
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        String output = sw.toString();
        System.out.println("class " + className);
        System.out.println(output);
        return output;
    }
    int i = 123456;
    float f = 123456.f;
    double d = 123456.;
    long l = 123456L;
    void m(Runnable r) { r.run(); }
}

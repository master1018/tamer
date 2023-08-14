import com.sun.tools.classfile.*;
public class T6716452 {
    public static void main(String[] args) throws Exception {
        new T6716452().run();
    }
    public void run() throws Exception {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        ClassFile cf = ClassFile.read(classFile);
        for (Method m: cf.methods) {
            test(cf, m);
        }
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }
    void test(ClassFile cf, Method m) {
        test(cf, m, Attribute.Code, Code_attribute.class);
        test(cf, m, Attribute.Exceptions, Exceptions_attribute.class);
    }
    void test(ClassFile cf, Method m, String name, Class<?> c) {
        int index = m.attributes.getIndex(cf.constant_pool, name);
        try {
            String m_name = m.getName(cf.constant_pool);
            System.err.println("Method " + m_name + " name:" + name + " index:" + index + " class: " + c);
            boolean expect = (m_name.equals("<init>") && name.equals("Code"))
                || (m_name.indexOf(name) != -1);
            boolean found = (index != -1);
            if (expect) {
                if (found) {
                    Attribute attr = m.attributes.get(index);
                    if (!c.isAssignableFrom(attr.getClass())) {
                        error(m + ": unexpected attribute found,"
                              + " expected " + c.getName()
                              + " found " + attr.getClass().getName());
                    }
                } else {
                    error(m + ": expected attribute " + name + " not found");
                }
            } else {
                if (found) {
                    error(m + ": unexpected attribute " + name);
                }
            }
        } catch (ConstantPoolException e) {
            error(m + ": " + e);
        }
    }
    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("abstract class Test { ");
        out.println("  abstract void m();");
        out.println("  void m_Code() { }");
        out.println("  abstract void m_Exceptions() throws Exception;");
        out.println("  void m_Code_Exceptions() throws Exception { }");
        out.println("}");
        out.close();
        return f;
    }
    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { "-g", f.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
        String path = f.getPath();
        return new File(path.substring(0, path.length() - 5) + ".class");
    }
    void error(String msg) {
        System.err.println("error: " + msg);
        errors++;
    }
    int errors;
}

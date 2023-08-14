public class T7004698 {
    public static void main(String... args) throws Exception {
        new T7004698().run();
    }
    void run() throws Exception {
        File srcDir = new File(System.getProperty("test.src"));
        File srcFile = new File(srcDir, T7004698.class.getSimpleName() + ".java");
        File classesDir = new File(".");
        compile("-Xjcov", "-d", classesDir.getPath(), srcFile.getPath());
        File classFile = new File(classesDir, T7004698.class.getSimpleName() + ".class");
        String out = javap("-v", classFile.getPath());
        Pattern attrBody = Pattern.compile("[0-9a-f, ]+
        Pattern endOfAttr = Pattern.compile("(^$|[A-Z][A-Za-z0-9_]+:.*|})");
        boolean inAttr = false;
        int count = 0;
        int errors = 0;
        for (String line: out.split(System.getProperty("line.separator"))) {
            line = line.trim();
            if (line.equals("CharacterRangeTable:")) {
                inAttr = true;
                count++;
            } else if (inAttr) {
                if (endOfAttr.matcher(line).matches()) {
                    inAttr = false;
                } else if (!attrBody.matcher(line).matches()) {
                    System.err.println("unexpected line found: " + line);
                    errors++;
                }
            }
        }
        if (count == 0)
            throw new Exception("no attribute instances found");
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }
    void compile(String... args) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javac.Main.compile(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            throw new Exception("javac failed unexpectedly; rc=" + rc);
    }
    String javap(String... args) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            throw new Exception("javap failed unexpectedly; rc=" + rc);
        return out;
    }
}

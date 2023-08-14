public class T4994049 extends Doclet {
    public static boolean start(RootDoc root) {
        for (ClassDoc klass : root.classes()) {
            for (MethodDoc method : klass.methods()) {
                if (method.name().equals("tabbedMethod")) {
                    if (method.position().column() == 21) {
                        System.out.println(method.position().column() + ": OK!");
                        return true;
                    } else {
                        System.err.println(method.position() + ": wrong tab expansion");
                        return false;
                    }
                }
            }
        }
        return false;
    }
    public static void main(String... args) throws Exception {
        File testSrc = new File(System.getProperty("test.src"));
        File tmpSrc = new File("tmpSrc");
        initTabs(testSrc, tmpSrc);
        for (String file : args) {
            File source = new File(tmpSrc, file);
            int rc = execute("javadoc", "T4994049", T4994049.class.getClassLoader(),
                        new String[]{ source.getPath() } );
            if (rc != 0)
                throw new Error("Unexpected return code from javadoc: " + rc);
        }
    }
    static void initTabs(File from, File to) throws IOException {
        for (File f: from.listFiles()) {
            File t = new File(to, f.getName());
            if (f.isDirectory()) {
                initTabs(f, t);
            } else if (f.getName().endsWith(".java")) {
                write(t, read(f).replace("\\t", "\t"));
            }
        }
    }
    static String read(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    static void write(File f, String s) throws IOException {
        f.getParentFile().mkdirs();
        try (Writer out = new FileWriter(f)) {
            out.write(s);
        }
    }
}

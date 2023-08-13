public class T4111861 {
    public static void main(String... args) throws Exception {
        new T4111861().run();
    }
    void run() throws Exception {
        File testSrc = new File(System.getProperty("test.src", "."));
        File a_java = new File(testSrc, "A.java");
        javac("-d", ".", a_java.getPath());
        String out = javap("-classpath", ".", "-constants", "A");
        String a = read(a_java);
        if (!filter(out).equals(filter(read(a_java)))) {
            System.out.println(out);
            throw new Exception("unexpected output");
        }
    }
    String javac(String... args) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javac.Main.compile(args, pw);
        if (rc != 0)
            throw new Exception("javac failed, rc=" + rc);
        return sw.toString();
    }
    String javap(String... args) throws Exception {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        if (rc != 0)
            throw new Exception("javap failed, rc=" + rc);
        return sw.toString();
    }
    String read(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new FileReader(f));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } finally {
            in.close();
        }
        return sb.toString();
    }
    String filter(String s) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new StringReader(s));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.indexOf("public static final") > 0) {
                    sb.append(line.trim());
                    sb.append('\n');
                }
            }
        } finally {
            in.close();
        }
        return sb.toString();
    }
}

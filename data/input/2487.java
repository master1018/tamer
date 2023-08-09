public class T6907575 {
    public static void main(String... args) throws Exception {
        new T6907575().run();
    }
    void run() throws Exception {
        String testSrc = System.getProperty("test.src");
        String testClasses = System.getProperty("test.classes");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        GetDeps gd = new GetDeps();
        gd.run(pw, "-classpath", testClasses, "-t", "-p", "p", "p/C1");
        pw.close();
        System.out.println(sw);
        String ref = readFile(new File(testSrc, "T6907575.out"));
        diff(sw.toString().replaceAll("[\r\n]+", "\n"), ref);
    }
    void diff(String actual, String ref) throws Exception {
        System.out.println("EXPECT:>>>" + ref + "<<<");
        System.out.println("ACTUAL:>>>" + actual + "<<<");
        if (!actual.equals(ref))
            throw new Exception("output not as expected");
    }
    String readFile(File f) throws IOException {
        Reader r = new FileReader(f);
        char[] buf = new char[(int) f.length()];
        int offset = 0;
        int n;
        while (offset < buf.length && (n = r.read(buf, offset, buf.length - offset)) != -1)
            offset += n;
        return new String(buf, 0, offset);
    }
}

public class T6994608 {
    public static void main(String... args) throws Exception {
        new T6994608().run();
    }
    void run() throws Exception {
        Locale prev = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            File f = writeFile(new File("classList"), "java.lang.Object");
            test(Arrays.asList("@" + f.getPath()), 0, null);
            test(Arrays.asList("@badfile"), 1, "Can't find file badfile");
            if (errors > 0)
                throw new Exception(errors + " errors occurred");
        } finally {
            Locale.setDefault(prev);
        }
    }
    void test(List<String> args, int expectRC, String expectOut) {
        System.err.println("Test: " + args
                + " rc:" + expectRC
                + ((expectOut != null) ? " out:" + expectOut : ""));
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javah.Main.run(args.toArray(new String[args.size()]), pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != expectRC)
            error("Unexpected exit code: " + rc + "; expected: " + expectRC);
        if (expectOut != null && !out.contains(expectOut))
            error("Expected string not found: " + expectOut);
        System.err.println();
    }
    File writeFile(File f, String s) throws IOException {
        if (f.getParentFile() != null)
            f.getParentFile().mkdirs();
        try (FileWriter out = new FileWriter(f)) {
            out.write(s);
        }
        return f;
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}

public class TestCRLineSeparator extends JavadocTester {
    private static final String BUG_ID = "4979486";
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", ".", "pkg"
    };
    private static final String[][] TEST = {
        {BUG_ID + FS + "pkg" + FS + "MyClass.html", "Line 1\n Line 2"}
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    public static void main(String[] args) throws Exception {
        initFiles(new File(SRC_DIR), new File("."), "pkg");
        TestCRLineSeparator tester = new TestCRLineSeparator();
        run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
    static void initFiles(File fromDir, File toDir, String f) throws IOException {
        File from_f = new File(fromDir, f);
        File to_f = new File(toDir, f);
        if (from_f.isDirectory()) {
            to_f.mkdirs();
            for (String child: from_f.list()) {
                initFiles(from_f, to_f, child);
            }
        } else {
            List<String> lines = new ArrayList<String>();
            BufferedReader in = new BufferedReader(new FileReader(from_f));
            try {
                String line;
                while ((line = in.readLine()) != null)
                    lines.add(line);
            } finally {
                in.close();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(to_f));
            try {
                for (String line: lines) {
                    out.write(line);
                    out.write("\r");
                }
            } finally {
                out.close();
            }
        }
    }
}

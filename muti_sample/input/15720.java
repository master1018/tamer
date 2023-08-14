public class TestSourceTab extends JavadocTester {
    private static final String BUG_ID = "4510979";
    private static final String TMP_SRC_DIR = "tmpSrc";
    private static final String OUTPUT_DIR1 = BUG_ID + "-tabLengthEight";
    private static final String OUTPUT_DIR2 = BUG_ID + "-tabLengthFour";
    private static final String[][] TEST = NO_TEST;
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS1 =
        new String[] {
            "-d", OUTPUT_DIR1, "-sourcepath", TMP_SRC_DIR,
            "-notimestamp", "-linksource", TMP_SRC_DIR + FS + "SingleTab" + FS + "C.java"
        };
    private static final String[] ARGS2 =
        new String[] {
            "-d", OUTPUT_DIR2, "-sourcepath", TMP_SRC_DIR,
            "-notimestamp", "-sourcetab", "4", TMP_SRC_DIR + FS + "DoubleTab" + FS + "C.java"
        };
    private static final String[][] FILES_TO_DIFF = {
        {OUTPUT_DIR1 + FS + "src-html" + FS + "C.html",
         OUTPUT_DIR2 + FS + "src-html" + FS + "C.html"
        },
        {OUTPUT_DIR1 + FS + "C.html",
         OUTPUT_DIR2 + FS + "C.html"
        }
    };
    public static void main(String[] args) throws IOException {
        TestSourceTab tester = new TestSourceTab();
        run(tester, ARGS1, TEST, NEGATED_TEST);
        run(tester, ARGS2, TEST, NEGATED_TEST);
        tester.runDiffs(FILES_TO_DIFF);
    }
    TestSourceTab() throws IOException {
        initTabs(new File(SRC_DIR), new File(TMP_SRC_DIR));
    }
    void initTabs(File from, File to) throws IOException {
        for (File f: from.listFiles()) {
            File t = new File(to, f.getName());
            if (f.isDirectory()) {
                initTabs(f, t);
            } else if (f.getName().endsWith(".java")) {
                write(t, read(f).replace("\\t", "\t"));
            }
        }
    }
    String read(File f) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new FileReader(f));
        try {
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } finally {
            in.close();
        }
        return sb.toString();
    }
    void write(File f, String s) throws IOException {
        f.getParentFile().mkdirs();
        Writer out = new FileWriter(f);
        try {
            out.write(s);
        } finally {
            out.close();
        }
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}

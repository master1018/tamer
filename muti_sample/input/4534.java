public class VersionTest {
    public static void main(String... args) {
        Locale prev = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            System.err.println(Locale.getDefault());
            test("-version", "\\S+ version \"\\S+\"");
            test("-fullversion", "\\S+ full version \"\\S+\"");
        } finally {
            Locale.setDefault(prev);
        }
    }
    static void test(String option, String regex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        String[] args = { option };
        int rc = com.sun.tools.javah.Main.run(args, pw);
        pw.close();
        if (rc != 0)
            throw new Error("javah failed: rc=" + rc);
        String out = sw.toString().trim();
        System.err.println(out);
        if (!out.matches(regex))
            throw new Error("output does not match pattern: " + regex);
    }
}

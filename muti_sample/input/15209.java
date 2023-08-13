public class ToURI {
    static PrintStream log = System.err;
    static int failures = 0;
    static void go(String fn) throws Exception {
        File f = new File(fn);
        log.println();
        log.println(f);
        URI u = f.toURI();
        log.println("  --> " + u);
        File g = new File(u);
        log.println("  --> " + g);
        if (!f.getAbsoluteFile().equals(g)) {
            log.println("ERROR: Expected " + f + ", got " + g);
            failures++;
        }
    }
    public static void main(String[] args) throws Exception {
        go("foo");
        go("foo/bar/baz");
        go("/cdrom/#2");
        go("My Computer");
        go("/tmp");
        go("/");
        go("");
        go("!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`"
           + "abcdefghijklmnopqrstuvwxyz{|}~\u00D0");
        if (File.separatorChar == '\\') {
            go("c:");
            go("c:\\");
            go("c:\\a\\b");
            go("\\\\foo");
            go("\\\\foo\\bar");
        }
        if (failures > 0)
            throw new Exception("Tests failed: " + failures);
    }
}

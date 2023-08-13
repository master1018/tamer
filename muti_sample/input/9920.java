public class GeneralWin32 extends General {
    private static final String EXISTENT_UNC_HOST = "pc-cup01";
    private static final String EXISTENT_UNC_SHARE = "pcdist";
    private static final String NONEXISTENT_UNC_HOST = "non-existent-unc-host";
    private static final String NONEXISTENT_UNC_SHARE = "bogus-share";
    private static void checkCaseLookup(String ud) throws IOException {
        File d = new File("XyZzY0123", "FOO_bar_BAZ");
        File f = new File(d, "GLORPified");
        if (!f.exists()) {
            if (!d.exists()) {
                if (!d.mkdirs()) {
                    throw new RuntimeException("Can't create directory " + d);
                }
            }
            OutputStream o = new FileOutputStream(f);
            o.close();
        }
        File f2 = new File(d.getParent(), "mumble"); 
        if (!f2.exists()) {
            OutputStream o = new FileOutputStream(f2);
            o.close();
        }
        File y = new File(ud, f.getPath());
        String ans = y.getPath();
        check(ans, "XyZzY0123\\FOO_bar_BAZ\\GLORPified");
        check(ans, "xyzzy0123\\foo_bar_baz\\glorpified");
        check(ans, "XYZZY0123\\FOO_BAR_BAZ\\GLORPIFIED");
    }
    private static void checkWild(File f) throws Exception {
        try {
            f.getCanonicalPath();
        } catch (IOException x) {
            return;
        }
        throw new Exception("Wildcard path not rejected: " + f);
    }
    private static void checkWildCards(String ud) throws Exception {
        File d = new File(ud).getCanonicalFile();
        checkWild(new File(d, "*.*"));
        checkWild(new File(d, "*.???"));
        checkWild(new File(new File(d, "*.*"), "foo"));
    }
    private static void checkRelativePaths() throws Exception {
        String ud = System.getProperty("user.dir").replace('/', '\\');
        checkCaseLookup(ud);
        checkWildCards(ud);
        checkNames(3, true, ud + "\\", "");
    }
    private static char findInactiveDrive() {
        for (char d = 'Z'; d >= 'E'; d--) {
            File df = new File(d + ":\\");
            if (!df.exists()) {
                return d;
            }
        }
        throw new RuntimeException("Can't find an inactive drive");
    }
    private static char findActiveDrive() {
        for (char d = 'C'; d <= 'Z'; d--) {
            File df = new File(d + ":\\");
            if (df.exists()) return d;
        }
        throw new RuntimeException("Can't find an active drive");
    }
    private static void checkDrive(int depth, char drive, boolean exists)
        throws Exception
    {
        String d = drive + ":";
        File df = new File(d);
        String ans = exists ? df.getAbsolutePath() : d;
        if (!ans.endsWith("\\"))
            ans = ans + "\\";
        checkNames(depth, false, ans, d);
    }
    private static void checkDrivePaths() throws Exception {
        checkDrive(2, findActiveDrive(), true);
        checkDrive(2, findInactiveDrive(), false);
    }
    private static void checkUncPaths() throws Exception {
        String s = ("\\\\" + NONEXISTENT_UNC_HOST
                    + "\\" + NONEXISTENT_UNC_SHARE);
        ensureNon(s);
        checkSlashes(2, false, s, s);
        s = "\\\\" + EXISTENT_UNC_HOST + "\\" + EXISTENT_UNC_SHARE;
        if (!(new File(s)).exists()) {
            System.err.println("WARNING: " + s +
                               " does not exist, unable to test UNC pathnames");
            return;
        }
        checkSlashes(2, false, s, s);
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar != '\\') {
            return;
        }
        if (args.length > 0) debug = true;
        checkRelativePaths();
        checkDrivePaths();
        checkUncPaths();
    }
}

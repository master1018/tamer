public class DriveRelativePath {
    static void fail(String s) {
        throw new RuntimeException(s);
    }
    public static void main(String[] args) throws IOException {
        if (File.separatorChar != '\\') {
            return;
        }
        File f = new File("foo");
        String c = f.getCanonicalPath();
        System.err.println(c);
        int di = c.indexOf(':');
        if (di == -1) fail("No drive in canonical path");
        String drive = c.substring(0, di + 1);
        File f2 = new File(drive + "foo");
        System.err.println(f2);
        String c2 = f2.getCanonicalPath();
        System.err.println(c2);
        if (!c2.equals(c)) fail("Canonical path mismatch: \""
                                + f2 + "\" maps to \""
                                + c2 + "\"; it should map to \""
                                + c + "\"");
    }
}

public class IsAbsolute {
    private static void ck(String path, boolean ans) throws Exception {
        File f = new File(path);
        boolean x = f.isAbsolute();
        if (x != ans)
            throw new Exception(path + ": expected " + ans + ", got " + x);
        System.err.println(path + " ==> " + x);
    }
    private static void testWin32() throws Exception {
        ck("/foo/bar", false);
        ck("\\foo\\bar", false);
        ck("c:\\foo\\bar", true);
        ck("c:/foo/bar", true);
        ck("c:foo\\bar", false);
        ck("\\\\foo\\bar", true);
    }
    private static void testUnix() throws Exception {
        ck("foo", false);
        ck("foo/bar", false);
        ck("/foo", true);
        ck("/foo/bar", true);
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') testWin32();
        if (File.separatorChar == '/') testUnix();
    }
}

public class GetAbsolutePath {
    private static boolean ignoreCase = false;
    private static void ck(String path, String ans) throws Exception {
        File f = new File(path);
        String p = f.getAbsolutePath();
        if ((ignoreCase && p.equalsIgnoreCase(ans)) || p.equals(ans))
            System.err.println(path + " ==> " + p);
        else
            throw new Exception(path + ": expected " + ans + ", got " + p);
    }
    private static void testWin32() throws Exception {
        String wd = System.getProperty("user.dir");
        char d;
        if ((wd.length() > 2) && (wd.charAt(1) == ':')
            && (wd.charAt(2) == '\\'))
            d = wd.charAt(0);
        else
            throw new Exception("Current directory has no drive");
        ck("/foo/bar", d + ":\\foo\\bar");
        ck("\\foo\\bar", d + ":\\foo\\bar");
        ck("c:\\foo\\bar", "c:\\foo\\bar");
        ck("c:/foo/bar", "c:\\foo\\bar");
        ck("\\\\foo\\bar", "\\\\foo\\bar");
        d = Character.toLowerCase(d);
        char z = 0;
        if (d != 'c') z = 'c';
        else if (d != 'd') z = 'd';
        if (z != 0) {
            File f = new File(z + ":.");
            if (f.exists()) {
                String zwd = f.getCanonicalPath();
                ck(z + ":foo", zwd + "\\foo");
            }
        }
        ck("", wd);
    }
    private static void testUnix() throws Exception {
        String wd = System.getProperty("user.dir");
        ck("foo", wd + "/foo");
        ck("foo/bar", wd + "/foo/bar");
        ck("/foo", "/foo");
        ck("/foo/bar", "/foo/bar");
        ck("", wd);
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') {
            ignoreCase = true;
            testWin32();
        }
        if (File.separatorChar == '/') testUnix();
    }
}

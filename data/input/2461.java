public class GetParent {
    static void check(String path, String[] parents) throws Exception {
        File f = new File(path);
        String p;
        System.err.print(path + ":");
        for (int i = 0; i < parents.length; i++) {
            p = f.getParent();
            System.err.print(" (" + p + ")");
            if (p == null)
                throw new Exception("No parent for " + f);
            if (! p.equals(parents[i]))
                throw new Exception("Wrong parent for " + f
                                    + ": Expected " + parents[i]
                                    + ", got " + p);
            f = new File(p);
        }
        if (f.getParent() != null)
            throw new Exception("Too many parents for " + path);
        System.err.println();
    }
    static void testUnix() throws Exception {
        check("foo", new String[] { });
        check("./foo", new String[] { "." });
        check("foo/bar/baz", new String[] { "foo/bar", "foo" });
        check("../../foo", new String[] { "../..", ".." });
        check("foo
        check("/foo/bar/baz.gorp",
              new String[] { "/foo/bar", "/foo", "/" });
    }
    static void testWin32() throws Exception {
        System.err.println("Win32");
        check("foo", new String[] { });
        check(".\\foo", new String[] { "." });
        check("foo\\bar\\baz", new String[] { "foo\\bar", "foo" });
        check("..\\..\\foo", new String[] { "..\\..", ".." });
        check("c:\\foo\\bar\\baz.xxx",
              new String[] { "c:\\foo\\bar", "c:\\foo", "c:\\" });
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '/') testUnix();
        if (File.separatorChar == '\\') testWin32();
    }
}

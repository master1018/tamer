public class HashCodeEquals {
    static void test(String fn1, String fn2) throws Exception {
        File f1 = new File(fn1);
        File f2 = new File(fn2);
        if (!f1.equals(f2))
            throw new Exception("Instances with equal paths are not equal");
        int h1 = f1.hashCode();
        int h2 = f2.hashCode();
        if (h1 != h2)
            throw new Exception("Hashcodes of equal instances are not equal");
    }
    static void testWin32() throws Exception {
        test("a:/foo/bar/baz", "a:/foo/bar/baz");
        test("A:/Foo/Bar/BAZ", "a:/foo/bar/baz");
    }
    static void testUnix() throws Exception {
        test("foo/bar/baz", "foo/bar/baz");
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') testWin32();
        if (File.separatorChar == '/') testUnix();
    }
}

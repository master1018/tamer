public class FileMethods {
    private static void ck(String op, File got, File ans) throws Exception {
        if (!got.equals(ans))
            throw new Exception(op + " incorrect");
    }
    private static void ck(String op, File f, String[] ls, File[] lf)
        throws Exception
    {
        System.err.println("--- " + op);
        int n = lf.length;
        if (ls.length != n)
            throw new Exception("listFiles returned incorrect count");
        for (int i = 0; i < n; i++) {
            if (ls[i].equals(lf[i].getName())
                && lf[i].getParentFile().equals(f)) {
                System.err.println(ls[i] + " ==> " + lf[i]);
            } else {
                throw new Exception("list mismatch: " + ls[i] + ", " + lf[i]);
            }
        }
    }
    public static void main(String[] args) throws Exception {
        File f;
        f = new File("foo/bar");
        ck("getParentFile", f.getParentFile(), new File(f.getParent()));
        f = new File(".");
        ck("getAbsoluteFile",
           f.getAbsoluteFile(), new File(f.getAbsolutePath()));
        ck("getCanonicalFile",
           f.getCanonicalFile(), new File(f.getCanonicalPath()));
        f = f.getCanonicalFile();
        ck("listFiles", f, f.list(), f.listFiles());
        FilenameFilter ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }};
        ck("listFiles/filtered", f, f.list(ff), f.listFiles(ff));
        FileFilter ff2 = new FileFilter() {
            public boolean accept(File f) {
                return f.getPath().endsWith(".class");
            }};
        ck("listFiles/filtered2", f, f.list(ff), f.listFiles(ff2));
    }
}

public class ListNull {
    static void go(String what, Object[] fs) throws Exception {
        if (fs == null)
            throw new Exception(what + " returned null");
        System.err.println("-- " + what);
        for (int i = 0; i < fs.length; i++)
            System.err.println(fs[i]);
    }
    public static void main(String[] args) throws Exception {
        File d = new File(".");
        go("list()", d.list());
        go("listFiles()", d.listFiles());
        go("list(null)", d.list(null));
        go("listFiles((FilenameFilter)null)", d.listFiles((FilenameFilter)null));
        go("listFiles((FileFilter)null)", d.listFiles((FileFilter)null));
    }
}

public class ToURL {
    static void go(String fn) throws Exception {
        File f = new File(fn);
        URL u = f.toURL();
        String ufn = u.getFile();
        if (!ufn.endsWith("/"))
            throw new Exception(u + " does not end with slash");
        if (ufn.endsWith("
            throw new Exception(u + " ends with two slashes");
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '/') {
            go("/");
        } else if (File.separatorChar == '\\') {
            go("\\");
            go("c:\\");
        }
    }
}

public class Patterns {
    static File dir = new File(".");
    static void ckn(String prefix, String suffix) throws Exception {
        try {
            File f = File.createTempFile(prefix, suffix, dir);
            f.deleteOnExit();
        } catch (Exception x) {
            if ((x instanceof IOException)
                || (x instanceof NullPointerException)
                || (x instanceof IllegalArgumentException)) {
                System.err.println("\"" + prefix + "\", \"" + suffix
                                   + "\" failed as expected: " + x.getMessage());
                return;
            }
            throw x;
        }
        throw new Exception("\"" + prefix + "\", \"" + suffix
                            + "\" should have failed");
    }
    static void cky(String prefix, String suffix) throws Exception {
        File f = File.createTempFile(prefix, suffix, dir);
        f.deleteOnExit();
        System.err.println("\"" + prefix + "\", \"" + suffix
                           + "\" --> " + f.getPath());
    }
    public static void main(String[] args) throws Exception {
        ckn(null, null);
        ckn("", null);
        ckn("x", null);
        ckn("xx", null);
        cky("xxx", null);
        cky("xxx", "");
        cky("xxx", "y");
        cky("xxx", ".y");
    }
}

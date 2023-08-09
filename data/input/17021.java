public class IsHidden {
    private static String dir = System.getProperty("test.dir", ".");
    private static void ck(String path, boolean ans) throws Exception {
        File f = new File(path);
        boolean x = f.isHidden();
        if (x != ans)
            throw new Exception(path + ": expected " + ans + ", got " + x);
        System.err.println(path + " ==> " + x);
    }
    private static void setHidden(File f, boolean value) throws IOException {
        Files.getFileAttributeView(f.toPath(), DosFileAttributeView.class).setHidden(value);
    }
    private static void testWin32() throws Exception {
        File f = new File(dir, "test");
        f.deleteOnExit();
        f.createNewFile();
        setHidden(f, true);
        try {
            ck(f.getPath(), true);
        } finally {
            setHidden(f, false);
        }
        ck(".foo", false);
        ck("foo", false);
    }
    private static void testUnix() throws Exception {
        ck(dir + "/IsHidden.java", false);
        ck(dir + "/.", true);
        ck(".", true);
        ck("..", true);
        ck(".foo", true);
        ck("foo", false);
        ck("", false);
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') testWin32();
        if (File.separatorChar == '/') testUnix();
    }
}

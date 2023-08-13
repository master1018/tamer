public class CompareTo {
    private static void testWin32() throws Exception {
        File f1 = new File("a");
        File f2 = new File("B");
        if (!(f1.compareTo(f2) < 0))
            throw new Exception("compareTo incorrect");
    }
    private static void testUnix() throws Exception {
        File f1 = new File("a");
        File f2 = new File("B");
        if (!(f1.compareTo(f2) > 0))
            throw new Exception("compareTo incorrect");
    }
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') testWin32();
        if (File.separatorChar == '/') testUnix();
    }
}

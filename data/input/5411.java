public class DriveSlash {
    public static void main(String[] args) throws Exception {
        if (File.separatorChar != '\\') return;
        File f = new File("c:\\");
        System.err.println(f.getCanonicalPath());
        String[] fs = f.list();
        if (fs == null) {
            throw new Exception("File.list returned null");
        }
        for (int i = 0; i < fs.length; i++) {
            System.err.print(" " + fs[i]);
        }
        System.err.println();
    }
}

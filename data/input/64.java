public class CheckTempDir {
    public static void main (String[] args) {
        String tmpdir = null;
        if ((tmpdir = System.getProperty("java.io.tmpdir")) == null) {
            throw new RuntimeException("java.io.tmpdir is not initialized");
        } else {
            System.out.println("checked tmpdir is not null: " + tmpdir);
        }
    }
}

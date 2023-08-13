public class GetDirEntry {
    public static void main(String args[]) throws Exception {
        try (ZipFile zf = new ZipFile(new File(System.getProperty("test.src", "."),
                                               "input.jar"))) {
            ZipEntry ze = zf.getEntry("META-INF");
            if (ze == null) {
                throw new Exception("failed to find a directory entry");
            }
        }
    }
}

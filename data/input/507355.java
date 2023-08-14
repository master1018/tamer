public class TemporaryDirectory {
    private static final String PROPERTY = "java.io.tmpdir";
    private static final String PATH_NAME = "tmp";
    private static boolean configured = false;
    public static void setUpDirectory(String baseDir) {
        setUpDirectory(new File(baseDir));
    }
    public static synchronized void setUpDirectory(File baseDir) {
        if (configured) {
            Logger.global.info("Already set to: " +
                    System.getProperty(PROPERTY));
            return;
        }
        File dir = new File(baseDir, PATH_NAME);
        String absolute = dir.getAbsolutePath();
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new UnsupportedOperationException(
                        "Name is used by a non-directory file: " +
                        absolute);
            } else if (!(dir.canRead() && dir.canWrite())) {
                throw new UnsupportedOperationException(
                        "Existing directory is not readable and writable: " +
                        absolute);
            }
        } else {
            if (!dir.mkdirs()) {
                throw new UnsupportedOperationException(
                        "Failed to create directory: " + absolute);
            }
        }
        System.setProperty(PROPERTY, absolute);
        configured = true;
    }
}

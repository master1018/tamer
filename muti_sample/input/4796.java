public class OpenFileInputStreamAction
        implements PrivilegedExceptionAction<FileInputStream> {
    private final File file;
    public OpenFileInputStreamAction(File file) {
        this.file = file;
    }
    public OpenFileInputStreamAction(String filename) {
        this.file = new File(filename);
    }
    public FileInputStream run() throws Exception {
        return new FileInputStream(file);
    }
}

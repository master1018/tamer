public class DefaultFileTypeDetector {
    private DefaultFileTypeDetector() { }
    public static FileTypeDetector create() {
        return new GnomeFileTypeDetector();
    }
}

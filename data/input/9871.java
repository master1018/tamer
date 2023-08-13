public class SimpleFileTypeDetector extends FileTypeDetector {
    public SimpleFileTypeDetector() {
    }
    public String probeContentType(Path file) throws IOException {
        System.out.println("probe " + file + "...");
        String name = file.toString();
        return name.endsWith(".grape") ? "grape/unknown" : null;
    }
}

public class ForceLoad {
    public static void main(String[] args) throws IOException {
        Files.probeContentType(Paths.get("."));
    }
}

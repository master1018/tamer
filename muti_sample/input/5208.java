public class BadDriveLetter {
    public static void main(String[] args) {
        System.err.println(new java.io.File(".:").getAbsolutePath());
    }
}

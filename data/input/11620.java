public class GetCanonicalPath {
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\') {
            testDriveLetter();
        }
    }
    private static void testDriveLetter() throws Exception {
        String path = new File("c:/").getCanonicalPath();
        if (path.length() > 3)
            throw new RuntimeException("Drive letter incorrectly represented");
    }
}

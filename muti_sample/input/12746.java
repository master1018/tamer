public class ParseUtil_6306697 {
    public static void main(String[] args) throws Exception {
        URI uri = ParseUtil.toURI(new URL("http:
        if (!uri.getPath().equals("/")) {
            throw new RuntimeException("Path should be '/' even if given URI string has a empty path");
        }
    }
}

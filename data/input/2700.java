public class ParseUtil_6478588 {
    public static void main(String[] args) throws Exception {
        try {
            URI uri = ParseUtil.toURI(new URL("http:
        } catch ( StringIndexOutOfBoundsException e ) {
             throw new RuntimeException("Test failed: " + e);
        }
    }
}

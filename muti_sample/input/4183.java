public class ParseUtil_6380332 {
    public static void main(String[] args) throws Exception {
        URI uri = ParseUtil.toURI(new URL("http:
        if (uri == null) {
            throw new RuntimeException("Test failed: port number -1 should not fail method toURI()");
        }
    }
}

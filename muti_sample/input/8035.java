public class RelativeURLTest {
    public static void main(String[] args) throws Exception {
        URL base = new URL("file:./");
        URL url = new URL(base, "../images/dummy/../././foo.gif");
        if (!url.toString().equals("file:../images/foo.gif")) {
            throw new RuntimeException("relative URL resolution failed " +
                                       "expect: file:../images/foo.gif " +
                                       "got: "+url.toString());
        }
    }
}

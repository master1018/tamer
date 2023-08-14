public class Test6338070 {
    private static final String DATA
            = "<java>\n"
            + " <void property=\"owner\">\n"
            + "  <void method=\"init\">\n"
            + "   <string>Hello, world</string>\n"
            + "  </void>\n"
            + " </void>\n"
            + "</java> ";
    public static void main(String[] args) {
        Test6338070 test = new Test6338070();
        InputStream stream = new ByteArrayInputStream(DATA.getBytes());
        new XMLDecoder(stream, test).close();
        if (test.message == null) {
            throw new Error("owner's method is not called");
        }
    }
    private String message;
    public void init(String message) {
        this.message = message;
    }
}

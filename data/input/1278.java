public final class TestJava extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <void id=\"owner\" method=\"getOwner\">\n"
            + "  <void method=\"init\">\n"
            + "   <string>Hello, world!</string>\n"
            + "  </void>\n"
            + " </void>\n"
            + " <object idref=\"owner\"/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestJava().test(true);
    }
    private String message;
    public void init(String message) {
        this.message = message;
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        decoder.setOwner(this);
        if (this != decoder.readObject()) {
            throw new Error("owner should be the same");
        }
        if (this.message == null) {
            throw new Error("owner's method is not called");
        }
    }
}

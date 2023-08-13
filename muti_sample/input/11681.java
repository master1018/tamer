public final class TestNull extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <null/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestNull().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        if (null != decoder.readObject()) {
            throw new Error("null value expected");
        }
    }
}

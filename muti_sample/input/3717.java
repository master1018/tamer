public final class TestFalse extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <false/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestFalse().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        if (!Boolean.FALSE.equals(decoder.readObject())) {
            throw new Error("false expected");
        }
    }
}

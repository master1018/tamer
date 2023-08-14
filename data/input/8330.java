public final class TestTrue extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <true/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestTrue().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        if (!Boolean.TRUE.equals(decoder.readObject())) {
            throw new Error("true expected");
        }
    }
}

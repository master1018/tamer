public final class TestBoolean extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <boolean>true</boolean>\n"
            + " <boolean>false</boolean>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestBoolean().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        if (!Boolean.TRUE.equals(decoder.readObject())) {
            throw new Error("true expected");
        }
        if (!Boolean.FALSE.equals(decoder.readObject())) {
            throw new Error("false expected");
        }
    }
}

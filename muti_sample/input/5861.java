public final class TestChar extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <char>X</char>\n"
            + " <char code=\"#20\"/>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestChar().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        if (!decoder.readObject().equals(Character.valueOf('X'))) {
            throw new Error("unexpected character");
        }
        if (!decoder.readObject().equals(Character.valueOf((char) 0x20))) {
            throw new Error("unexpected character code");
        }
    }
}

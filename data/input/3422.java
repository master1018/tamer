public final class TestString extends AbstractTest {
    public static final String PREFIX = " prefix ";
    public static final String POSTFIX = " postfix ";
    public static final String XML
            = "<java>\n"
            + " <string>" + PREFIX + "</string>\n"
            + " <string>" + POSTFIX + "</string>\n"
            + " <string>" + PREFIX + POSTFIX + "</string>\n"
            + " <string>" + PREFIX + "<char code=\"0\"/>" + POSTFIX + "</string>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestString().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate(PREFIX, decoder.readObject());
        validate(POSTFIX, decoder.readObject());
        validate(PREFIX + POSTFIX, decoder.readObject());
        validate(PREFIX + '\u0000' + POSTFIX, decoder.readObject());
    }
    private static void validate(String name, Object object) {
        if (!object.equals(name)) {
            throw new Error(name + " expected");
        }
    }
}

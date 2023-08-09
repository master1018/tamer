public final class TestInt extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <int>0</int>\n"
            + " <int>127</int>\n"
            + " <int>-128</int>\n"
            + " <int>32767</int>\n"
            + " <int>-32768</int>\n"
            + " <int>2147483647</int>\n"
            + " <int>-2147483648</int>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestInt().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate(0, decoder.readObject());
        validate((int) Byte.MAX_VALUE, decoder.readObject());
        validate((int) Byte.MIN_VALUE, decoder.readObject());
        validate((int) Short.MAX_VALUE, decoder.readObject());
        validate((int) Short.MIN_VALUE, decoder.readObject());
        validate(Integer.MAX_VALUE, decoder.readObject());
        validate(Integer.MIN_VALUE, decoder.readObject());
    }
    private static void validate(int value, Object object) {
        if (!object.equals(Integer.valueOf(value))) {
            throw new Error("int " + value + " expected");
        }
    }
}

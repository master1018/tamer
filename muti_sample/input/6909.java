public final class TestShort extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <short>0</short>\n"
            + " <short>127</short>\n"
            + " <short>-128</short>\n"
            + " <short>32767</short>\n"
            + " <short>-32768</short>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestShort().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate((short) 0, decoder.readObject());
        validate((short) Byte.MAX_VALUE, decoder.readObject());
        validate((short) Byte.MIN_VALUE, decoder.readObject());
        validate(Short.MAX_VALUE, decoder.readObject());
        validate(Short.MIN_VALUE, decoder.readObject());
    }
    private static void validate(short value, Object object) {
        if (!object.equals(Short.valueOf(value))) {
            throw new Error("short " + value + " expected");
        }
    }
}

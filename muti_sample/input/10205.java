public final class TestLong extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <long>0</long>\n"
            + " <long>127</long>\n"
            + " <long>-128</long>\n"
            + " <long>32767</long>\n"
            + " <long>-32768</long>\n"
            + " <long>2147483647</long>\n"
            + " <long>-2147483648</long>\n"
            + " <long>9223372036854775807</long>\n"
            + " <long>-9223372036854775808</long>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestLong().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate(0L, decoder.readObject());
        validate((long) Byte.MAX_VALUE, decoder.readObject());
        validate((long) Byte.MIN_VALUE, decoder.readObject());
        validate((long) Short.MAX_VALUE, decoder.readObject());
        validate((long) Short.MIN_VALUE, decoder.readObject());
        validate((long) Integer.MAX_VALUE, decoder.readObject());
        validate((long) Integer.MIN_VALUE, decoder.readObject());
        validate(Long.MAX_VALUE, decoder.readObject());
        validate(Long.MIN_VALUE, decoder.readObject());
    }
    private static void validate(long value, Object object) {
        if (!object.equals(Long.valueOf(value))) {
            throw new Error("long " + value + " expected");
        }
    }
}

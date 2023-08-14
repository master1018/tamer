public final class TestDouble extends AbstractTest {
    public static final String XML
            = "<java>\n"
            + " <double>0</double>\n"
            + " <double>1000</double>\n"
            + " <double>-1.1e15</double>\n"
            + " <double>10.11e-123</double>\n"
            + "</java>";
    public static void main(String[] args) {
        new TestDouble().test(true);
    }
    @Override
    protected void validate(XMLDecoder decoder) {
        validate(0.0, decoder.readObject());
        validate(1000.0, decoder.readObject());
        validate(-1.1e15, decoder.readObject());
        validate(10.11e-123, decoder.readObject());
    }
    private static void validate(double value, Object object) {
        if (!object.equals(Double.valueOf(value))) {
            throw new Error("double " + value + " expected");
        }
    }
}

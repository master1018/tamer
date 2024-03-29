public class EqualsWithDelta implements IArgumentMatcher, Serializable {
    private static final long serialVersionUID = -3018631689416120154L;
    private final Number expected;
    private final Number delta;
    public EqualsWithDelta(Number value, Number delta) {
        this.expected = value;
        this.delta = delta;
    }
    public boolean matches(Object actual) {
        Number actualNumber = (Number) actual;
        return expected.doubleValue() - delta.doubleValue() <= actualNumber
                .doubleValue()
                && actualNumber.doubleValue() <= expected.doubleValue()
                        + delta.doubleValue();
    }
    public void appendTo(StringBuffer buffer) {
        buffer.append("eq(" + expected + ", " + delta + ")");
    }
}

public class FooNumberFormat extends NumberFormat {
    private DecimalFormat df;
    public FooNumberFormat(String pattern, DecimalFormatSymbols dfs) {
        df = new DecimalFormat(pattern, dfs);
    }
    @Override
    public StringBuffer format(double number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        return df.format(number, toAppendTo, pos);
    }
    @Override
    public StringBuffer format(long number,
                               StringBuffer toAppendTo,
                               FieldPosition pos) {
        return df.format(number, toAppendTo, pos);
    }
    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return df.parse(source, parsePosition);
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof FooNumberFormat
            && df.equals(((FooNumberFormat)other).df);
    }
    @Override
    public int hashCode() {
        return df.hashCode();
    }
    public String toPattern() {
        return df.toPattern();
    }
    public DecimalFormatSymbols getDecimalFormatSymbols() {
        return df.getDecimalFormatSymbols();
    }
    public void setDecimalSeparatorAlwaysShown(boolean newValue) {
        df.setDecimalSeparatorAlwaysShown(newValue);
    }
}

public class FooDateFormat extends DateFormat {
    private SimpleDateFormat sdf;
    public FooDateFormat(String pattern, Locale loc) {
        sdf = new SimpleDateFormat(pattern, loc);
    }
    @Override
    public StringBuffer format(Date date,
                               StringBuffer toAppendTo,
                               FieldPosition fieldPosition) {
        return sdf.format(date, toAppendTo, fieldPosition);
    }
    @Override
    public Date parse(String source, ParsePosition pos) {
        return sdf.parse(source, pos);
    }
    @Override
    public boolean equals(Object other) {
        return other instanceof FooDateFormat
            && sdf.equals(((FooDateFormat)other).sdf);
    }
    @Override
    public int hashCode() {
        return sdf.hashCode();
    }
}

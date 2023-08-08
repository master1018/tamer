public class TimeType implements DataType {
    public int getTypeId() {
        return UjacTypes.TYPE_TIME;
    }
    public Object convertObject(Object value, FormatHelper formatHelper) throws TypeConverterException {
        if (value == null) {
            return null;
        }
        if (value instanceof Time) {
            return value;
        }
        String strVal = value.toString();
        try {
            return formatHelper.getIsoTimeFormat().parse(strVal);
        } catch (ParseException ex) {
            throw new TypeConverterException("Failed to parse Time value out of given value '" + strVal + "'.", ex);
        }
    }
    public String formatValue(Object value, FormatHelper formatHelper) throws TypeConverterException {
        return formatHelper.getIsoDateFormat().format((Date) convertObject(value, formatHelper));
    }
}

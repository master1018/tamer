public class DateFormatter extends InternationalFormatter {
    public DateFormatter() {
        this(DateFormat.getDateInstance());
    }
    public DateFormatter(DateFormat format) {
        super(format);
        setFormat(format);
    }
    public void setFormat(DateFormat format) {
        super.setFormat(format);
    }
    private Calendar getCalendar() {
        Format f = getFormat();
        if (f instanceof DateFormat) {
            return ((DateFormat)f).getCalendar();
        }
        return Calendar.getInstance();
    }
    boolean getSupportsIncrement() {
        return true;
    }
    Object getAdjustField(int start, Map attributes) {
        Iterator attrs = attributes.keySet().iterator();
        while (attrs.hasNext()) {
            Object key = attrs.next();
            if ((key instanceof DateFormat.Field) &&
                (key == DateFormat.Field.HOUR1 ||
                 ((DateFormat.Field)key).getCalendarField() != -1)) {
                return key;
            }
        }
        return null;
    }
    Object adjustValue(Object value, Map attributes, Object key,
                           int direction) throws
                      BadLocationException, ParseException {
        if (key != null) {
            int field;
            if (key == DateFormat.Field.HOUR1) {
                key = DateFormat.Field.HOUR0;
            }
            field = ((DateFormat.Field)key).getCalendarField();
            Calendar calendar = getCalendar();
            if (calendar != null) {
                calendar.setTime((Date)value);
                int fieldValue = calendar.get(field);
                try {
                    calendar.add(field, direction);
                    value = calendar.getTime();
                } catch (Throwable th) {
                    value = null;
                }
                return value;
            }
        }
        return null;
    }
}

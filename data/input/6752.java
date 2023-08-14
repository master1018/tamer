public class SpinnerDateModel extends AbstractSpinnerModel implements Serializable
{
    private Comparable start, end;
    private Calendar value;
    private int calendarField;
    private boolean calendarFieldOK(int calendarField) {
        switch(calendarField) {
        case Calendar.ERA:
        case Calendar.YEAR:
        case Calendar.MONTH:
        case Calendar.WEEK_OF_YEAR:
        case Calendar.WEEK_OF_MONTH:
        case Calendar.DAY_OF_MONTH:
        case Calendar.DAY_OF_YEAR:
        case Calendar.DAY_OF_WEEK:
        case Calendar.DAY_OF_WEEK_IN_MONTH:
        case Calendar.AM_PM:
        case Calendar.HOUR:
        case Calendar.HOUR_OF_DAY:
        case Calendar.MINUTE:
        case Calendar.SECOND:
        case Calendar.MILLISECOND:
            return true;
        default:
            return false;
        }
    }
    public SpinnerDateModel(Date value, Comparable start, Comparable end, int calendarField) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        if (!calendarFieldOK(calendarField)) {
            throw new IllegalArgumentException("invalid calendarField");
        }
        if (!(((start == null) || (start.compareTo(value) <= 0)) &&
              ((end == null) || (end.compareTo(value) >= 0)))) {
            throw new IllegalArgumentException("(start <= value <= end) is false");
        }
        this.value = Calendar.getInstance();
        this.start = start;
        this.end = end;
        this.calendarField = calendarField;
        this.value.setTime(value);
    }
    public SpinnerDateModel() {
        this(new Date(), null, null, Calendar.DAY_OF_MONTH);
    }
    public void setStart(Comparable start) {
        if ((start == null) ? (this.start != null) : !start.equals(this.start)) {
            this.start = start;
            fireStateChanged();
        }
    }
    public Comparable getStart() {
        return start;
    }
    public void setEnd(Comparable end) {
        if ((end == null) ? (this.end != null) : !end.equals(this.end)) {
            this.end = end;
            fireStateChanged();
        }
    }
    public Comparable getEnd() {
        return end;
    }
    public void setCalendarField(int calendarField) {
        if (!calendarFieldOK(calendarField)) {
            throw new IllegalArgumentException("invalid calendarField");
        }
        if (calendarField != this.calendarField) {
            this.calendarField = calendarField;
            fireStateChanged();
        }
    }
    public int getCalendarField() {
        return calendarField;
    }
    public Object getNextValue() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value.getTime());
        cal.add(calendarField, 1);
        Date next = cal.getTime();
        return ((end == null) || (end.compareTo(next) >= 0)) ? next : null;
    }
    public Object getPreviousValue() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value.getTime());
        cal.add(calendarField, -1);
        Date prev = cal.getTime();
        return ((start == null) || (start.compareTo(prev) <= 0)) ? prev : null;
    }
    public Date getDate() {
        return value.getTime();
    }
    public Object getValue() {
        return value.getTime();
    }
    public void setValue(Object value) {
        if ((value == null) || !(value instanceof Date)) {
            throw new IllegalArgumentException("illegal value");
        }
        if (!value.equals(this.value.getTime())) {
            this.value.setTime((Date)value);
            fireStateChanged();
        }
    }
}

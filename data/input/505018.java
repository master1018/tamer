public class Date extends java.util.Date {
    private static final long serialVersionUID = 1511598038487230103L;
    @Deprecated
    public Date(int theYear, int theMonth, int theDay) {
        super(theYear, theMonth, theDay);
    }
    public Date(long theDate) {
        super(normalizeTime(theDate));
    }
    @Deprecated
    @Override
    public int getHours() {
        throw new IllegalArgumentException();
    }
    @Deprecated
    @Override
    public int getMinutes() {
        throw new IllegalArgumentException();
    }
    @Deprecated
    @Override
    public int getSeconds() {
        throw new IllegalArgumentException();
    }
    @Deprecated
    @Override
    public void setHours(int theHours) {
        throw new IllegalArgumentException();
    }
    @Deprecated
    @Override
    public void setMinutes(int theMinutes) {
        throw new IllegalArgumentException();
    }
    @Deprecated
    @Override
    public void setSeconds(int theSeconds) {
        throw new IllegalArgumentException();
    }
    @Override
    public void setTime(long theTime) {
        super.setTime(normalizeTime(theTime));
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(10);
        format((getYear() + 1900), 4, sb);
        sb.append('-');
        format((getMonth() + 1), 2, sb);
        sb.append('-');
        format(getDate(), 2, sb);
        return sb.toString();
    }
    private static final String PADDING = "0000";  
    private void format(int date, int digits, StringBuilder sb) { 
        String str = String.valueOf(date);
        if (digits - str.length() > 0) {
            sb.append(PADDING.substring(0, digits - str.length()));
        }
        sb.append(str); 
    }
    public static Date valueOf(String dateString) {
        if (dateString == null) {
            throw new IllegalArgumentException();
        }
        int firstIndex = dateString.indexOf('-');
        int secondIndex = dateString.indexOf('-', firstIndex + 1);
        if (secondIndex == -1 || firstIndex == 0
                || secondIndex + 1 == dateString.length()) {
            throw new IllegalArgumentException();
        }
        int year = Integer.parseInt(dateString.substring(0, firstIndex));
        int month = Integer.parseInt(dateString.substring(firstIndex + 1,
                secondIndex));
        int day = Integer.parseInt(dateString.substring(secondIndex + 1,
                dateString.length()));
        return new Date(year - 1900, month - 1, day);
    }
    private static long normalizeTime(long theTime) {
        return theTime;
    }
}

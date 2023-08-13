public class DateTime {
    private final Date date;
    private final int year;
    private final int month;
    private final int day;
    private final int hour;
    private final int minute;
    private final int second;
    private final int timeZone;
    public DateTime(String yearString, int month, int day, int hour, int minute, int second, int timeZone) {
        this.year = convertToYear(yearString);
        this.date = convertToDate(year, month, day, hour, minute, second, timeZone);
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.timeZone = timeZone;
    }
    private int convertToYear(String yearString) {
        int year = Integer.parseInt(yearString);
        switch (yearString.length()) {
            case 1:
            case 2:
                if (year >= 0 && year < 50)
                    return 2000 + year;
                else
                    return 1900 + year;
            case 3:
                return 1900 + year;
            default:
                return year;
        }
    }
    public static Date convertToDate(int year, int month, int day, int hour, int minute, int second, int timeZone) {
        Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
        c.set(year, month - 1, day, hour, minute, second);
        c.set(Calendar.MILLISECOND, 0);
        if (timeZone != Integer.MIN_VALUE) {
            int minutes = ((timeZone / 100) * 60) + timeZone % 100;
            c.add(Calendar.MINUTE, -1 * minutes);
        }
        return c.getTime();
    }
    public Date getDate() {
        return date;
    }
    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }
    public int getHour() {
        return hour;
    }
    public int getMinute() {
        return minute;
    }
    public int getSecond() {
        return second;
    }
    public int getTimeZone() {
        return timeZone;
    }
    public void print() {
        System.out.println(getYear() + " " + getMonth() + " " + getDay() + "; " + getHour() + " " + getMinute() + " " + getSecond() + " " + getTimeZone());
    }
    public static DateTime parse(String dateString) throws ParseException {
        try {
            return new DateTimeParser(new StringReader(dateString)).parseAll();
        }
        catch (TokenMgrError err) {
            throw new ParseException(err.getMessage());
        }
    }
}

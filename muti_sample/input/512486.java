public final class HttpDateTime {
    private static final String HTTP_DATE_RFC_REGEXP =
            "([0-9]{1,2})[- ]([A-Za-z]{3,9})[- ]([0-9]{2,4})[ ]"
            + "([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])";
    private static final String HTTP_DATE_ANSIC_REGEXP =
            "[ ]([A-Za-z]{3,9})[ ]+([0-9]{1,2})[ ]"
            + "([0-9]{1,2}:[0-9][0-9]:[0-9][0-9])[ ]([0-9]{2,4})";
    private static final Pattern HTTP_DATE_RFC_PATTERN =
            Pattern.compile(HTTP_DATE_RFC_REGEXP);
    private static final Pattern HTTP_DATE_ANSIC_PATTERN =
            Pattern.compile(HTTP_DATE_ANSIC_REGEXP);
    private static class TimeOfDay {
        TimeOfDay(int h, int m, int s) {
            this.hour = h;
            this.minute = m;
            this.second = s;
        }
        int hour;
        int minute;
        int second;
    }
    public static long parse(String timeString)
            throws IllegalArgumentException {
        int date = 1;
        int month = Calendar.JANUARY;
        int year = 1970;
        TimeOfDay timeOfDay;
        Matcher rfcMatcher = HTTP_DATE_RFC_PATTERN.matcher(timeString);
        if (rfcMatcher.find()) {
            date = getDate(rfcMatcher.group(1));
            month = getMonth(rfcMatcher.group(2));
            year = getYear(rfcMatcher.group(3));
            timeOfDay = getTime(rfcMatcher.group(4));
        } else {
            Matcher ansicMatcher = HTTP_DATE_ANSIC_PATTERN.matcher(timeString);
            if (ansicMatcher.find()) {
                month = getMonth(ansicMatcher.group(1));
                date = getDate(ansicMatcher.group(2));
                timeOfDay = getTime(ansicMatcher.group(3));
                year = getYear(ansicMatcher.group(4));
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (year >= 2038) {
            year = 2038;
            month = Calendar.JANUARY;
            date = 1;
        }
        Time time = new Time(Time.TIMEZONE_UTC);
        time.set(timeOfDay.second, timeOfDay.minute, timeOfDay.hour, date,
                month, year);
        return time.toMillis(false );
    }
    private static int getDate(String dateString) {
        if (dateString.length() == 2) {
            return (dateString.charAt(0) - '0') * 10
                    + (dateString.charAt(1) - '0');
        } else {
            return (dateString.charAt(0) - '0');
        }
    }
    private static int getMonth(String monthString) {
        int hash = Character.toLowerCase(monthString.charAt(0)) +
                Character.toLowerCase(monthString.charAt(1)) +
                Character.toLowerCase(monthString.charAt(2)) - 3 * 'a';
        switch (hash) {
            case 22:
                return Calendar.JANUARY;
            case 10:
                return Calendar.FEBRUARY;
            case 29:
                return Calendar.MARCH;
            case 32:
                return Calendar.APRIL;
            case 36:
                return Calendar.MAY;
            case 42:
                return Calendar.JUNE;
            case 40:
                return Calendar.JULY;
            case 26:
                return Calendar.AUGUST;
            case 37:
                return Calendar.SEPTEMBER;
            case 35:
                return Calendar.OCTOBER;
            case 48:
                return Calendar.NOVEMBER;
            case 9:
                return Calendar.DECEMBER;
            default:
                throw new IllegalArgumentException();
        }
    }
    private static int getYear(String yearString) {
        if (yearString.length() == 2) {
            int year = (yearString.charAt(0) - '0') * 10
                    + (yearString.charAt(1) - '0');
            if (year >= 70) {
                return year + 1900;
            } else {
                return year + 2000;
            }
        } else if (yearString.length() == 3) {
            int year = (yearString.charAt(0) - '0') * 100
                    + (yearString.charAt(1) - '0') * 10
                    + (yearString.charAt(2) - '0');
            return year + 1900;
        } else if (yearString.length() == 4) {
             return (yearString.charAt(0) - '0') * 1000
                    + (yearString.charAt(1) - '0') * 100
                    + (yearString.charAt(2) - '0') * 10
                    + (yearString.charAt(3) - '0');
        } else {
             return 1970;
        }
    }
    private static TimeOfDay getTime(String timeString) {
        int i = 0;
        int hour = timeString.charAt(i++) - '0';
        if (timeString.charAt(i) != ':')
            hour = hour * 10 + (timeString.charAt(i++) - '0');
        i++;
        int minute = (timeString.charAt(i++) - '0') * 10
                    + (timeString.charAt(i++) - '0');
        i++;
        int second = (timeString.charAt(i++) - '0') * 10
                  + (timeString.charAt(i++) - '0');
        return new TimeOfDay(hour, minute, second);        
    }
}

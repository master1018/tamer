class RuleDay {
    private static final Map<String,DayOfWeek> abbreviations = new HashMap<String,DayOfWeek>(7);
    static {
        for (DayOfWeek day : DayOfWeek.values()) {
            abbreviations.put(day.getAbbr(), day);
        }
    }
    private String dayName = null;
    private DayOfWeek dow;
    private boolean lastOne = false;
    private int soonerOrLater = 0;
    private int thanDayOfMonth; 
    RuleDay() {
    }
    RuleDay(int day) {
        thanDayOfMonth = day;
    }
    int getDay() {
        return thanDayOfMonth;
    }
    int getDayOfWeekNum() {
        return dow.value();
    }
    boolean isLast() {
        return lastOne;
    }
    boolean isLater() {
        return soonerOrLater > 0;
    }
    boolean isEarlier() {
        return soonerOrLater < 0;
    }
    boolean isExact() {
        return soonerOrLater == 0;
    }
    static RuleDay parse(String day) {
        RuleDay d = new RuleDay();
        if (day.startsWith("last")) {
            d.lastOne = true;
            d.dayName = day.substring(4);
            d.dow = getDOW(d.dayName);
        } else {
            int index;
            if ((index = day.indexOf(">=")) != -1) {
                d.dayName = day.substring(0, index);
                d.dow = getDOW(d.dayName);
                d.soonerOrLater = 1; 
                d.thanDayOfMonth = Integer.parseInt(day.substring(index+2));
            } else if ((index = day.indexOf("<=")) != -1) {
                d.dayName = day.substring(0, index);
                d.dow = getDOW(d.dayName);
                d.soonerOrLater = -1; 
                d.thanDayOfMonth = Integer.parseInt(day.substring(index+2));
            } else {
                d.thanDayOfMonth = Integer.parseInt(day);
            }
        }
        return d;
    }
    int getDayForSimpleTimeZone() {
        if (isLast()) {
            return -1;
        }
        return isEarlier() ? -getDay() : getDay();
    }
    int getDayOfWeekForSimpleTimeZoneInt() {
        if (isEarlier() || isLater()) {
            return -getDayOfWeekNum();
        }
        return isLast() ? getDayOfWeekNum() : 0;
    }
    String getDayOfWeekForSimpleTimeZone() {
        int d = getDayOfWeekForSimpleTimeZoneInt();
        if (d == 0) {
            return "0";
        }
        String sign = "";
        if (d < 0) {
            sign = "-";
            d = -d;
        }
        return sign + toString(d);
    }
    private static DayOfWeek getDOW(String abbr) {
        return abbreviations.get(abbr);
    }
    static String toString(int dow) {
        if (dow >= DayOfWeek.SUNDAY.value() && dow <= DayOfWeek.SATURDAY.value()) {
            return "Calendar." + DayOfWeek.values()[dow - 1];
        }
        throw new IllegalArgumentException("wrong Day_of_Week number: " + dow);
    }
}

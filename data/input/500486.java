public abstract class MonkeyUtils {
    private static final java.util.Date DATE = new java.util.Date();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS ");
    private MonkeyUtils() {
    }
    public static synchronized String toCalendarTime(long time) {
        DATE.setTime(time);
        return DATE_FORMATTER.format(DATE);
    }
}

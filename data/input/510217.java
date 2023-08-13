public class DateUtils {
    private DateUtils() {
    }
    public static String getKMLTimestamp(long when) {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Calendar c = Calendar.getInstance(tz);
        c.setTimeInMillis(when);
        return String.format("%tY-%tm-%tdT%tH:%tM:%tSZ", c, c, c, c, c, c);
    }
    public static String getCurrentKMLTimestamp() {
        return getKMLTimestamp(System.currentTimeMillis());
    }
    public static String getCurrentTimestamp() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        return String.format("%tY-%tm-%td-%tH-%tM-%tS", c, c, c, c, c, c);
    }
}

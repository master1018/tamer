public class WeekDateTest {
    static SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
    static SimpleDateFormat ywdFormat = new SimpleDateFormat("YYYY-'W'ww-u");
    static {
        ymdFormat.setCalendar(newCalendar());
        ywdFormat.setCalendar(newCalendar());
    }
    static final String[][] roundTripData = {
        { "2005-01-01", "2004-W53-6" },
        { "2005-01-02", "2004-W53-7" },
        { "2005-12-31", "2005-W52-6" },
        { "2007-01-01", "2007-W01-1" },
        { "2007-12-30", "2007-W52-7" },
        { "2007-12-31", "2008-W01-1" },
        { "2008-01-01", "2008-W01-2" },
        { "2008-12-29", "2009-W01-1" },
        { "2008-12-31", "2009-W01-3" },
        { "2009-01-01", "2009-W01-4" },
        { "2009-12-31", "2009-W53-4" },
        { "2010-01-03", "2009-W53-7" },
        { "2009-12-31", "2009-W53-4" },
        { "2010-01-01", "2009-W53-5" },
        { "2010-01-02", "2009-W53-6" },
        { "2010-01-03", "2009-W53-7" },
        { "2008-12-28", "2008-W52-7" },
        { "2008-12-29", "2009-W01-1" },
        { "2008-12-30", "2009-W01-2" },
        { "2008-12-31", "2009-W01-3" },
        { "2009-01-01", "2009-W01-4" },
        { "2009-01-01", "2009-W01-4" },
    };
    static final String[][] leniencyData = {
        { "2008-12-28", "2009-W01-0" },
        { "2010-01-04", "2009-W53-8" },
        { "2008-12-29", "2008-W53-1" },
    };
    static final String[] invalidData = {
        "2010-W00-1",
        "2010-W55-1",
        "2010-W03-0",
        "2010-W04-8",
        "2010-W04-19"
    };
    public static void main(String[] args) throws Exception {
        formatTest(roundTripData);
        parseTest(roundTripData);
        parseTest(leniencyData);
        nonLenientTest(invalidData);
        noWeekDateSupport();
    }
    private static void formatTest(String[][] data) throws Exception {
        for (String[] dates : data) {
            String regularDate = dates[0];
            String weekDate = dates[1];
            Date date = null;
            date = ymdFormat.parse(regularDate);
            String s = ywdFormat.format(date);
            if (!s.equals(weekDate)) {
                throw new RuntimeException("format: got="+s+", expecetd="+weekDate);
            }
        }
    }
    private static void parseTest(String[][] data) throws Exception {
        for (String[] dates : data) {
            String regularDate = dates[0];
            String weekDate = dates[1];
            Date date1 = null, date2 = null;
            date1 = ymdFormat.parse(regularDate);
            date2 = ywdFormat.parse(weekDate);
            if (!date1.equals(date2)) {
                System.err.println(regularDate + ": date1 = " + date1);
                System.err.println(weekDate + ": date2 = " + date2);
                throw new RuntimeException("parse: date1 != date2");
            }
        }
    }
    private static void nonLenientTest(String[] data) {
        ywdFormat.setLenient(false);
        for (String date : data) {
            try {
                Date d = ywdFormat.parse(date);
                throw new RuntimeException("No ParseException thrown with " + date);
            } catch (ParseException e) {
            }
        }
        ywdFormat.setLenient(true);
    }
    private static void noWeekDateSupport() throws Exception {
        Calendar jcal = Calendar.getInstance(TimeZone.getTimeZone("GMT"),
                                             new Locale("ja", "JP", "JP"));
        jcal.setFirstDayOfWeek(MONDAY);
        jcal.setMinimalDaysInFirstWeek(4);
        SimpleDateFormat sdf = new SimpleDateFormat("Y-'W'ww-u");
        sdf.setCalendar(jcal);
        Date d = sdf.parse("21-W01-3"); 
        GregorianCalendar gcal = newCalendar();
        gcal.setTime(d);
        if (gcal.get(YEAR) != 2008
            || gcal.get(MONTH) != DECEMBER
            || gcal.get(DAY_OF_MONTH) != 31) {
            String s = String.format("noWeekDateSupport: got %04d-%02d-%02d, expected 2008-12-31%n",
                                     gcal.get(YEAR),
                                     gcal.get(MONTH)+1,
                                     gcal.get(DAY_OF_MONTH));
            throw new RuntimeException(s);
        }
    }
    private static GregorianCalendar newCalendar() {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.setFirstDayOfWeek(MONDAY);
        cal.setMinimalDaysInFirstWeek(4);
        return cal;
    }
}

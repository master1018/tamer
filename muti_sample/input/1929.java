public class DaylightTimeTest {
    private static final int ONE_HOUR = 60 * 60 * 1000; 
    private static final int INTERVAL = 24 * ONE_HOUR;  
    private static final String[] ZONES = TimeZone.getAvailableIDs();
    private static int errors = 0;
    public static void main(String[] args) {
        for (String id : ZONES) {
            TimeZone tz = TimeZone.getTimeZone(id);
            long now = System.currentTimeMillis();
            boolean observes = tz.observesDaylightTime();
            boolean found = findDSTTransition(tz, now);
            if (observes != found) {
                now = System.currentTimeMillis();
                observes = tz.observesDaylightTime();
                found = findDSTTransition(tz, now);
                if (observes != found) {
                    System.err.printf("%s: observesDaylightTime() should return %s at %d%n",
                                      tz.getID(), found, now);
                    errors++;
                }
            }
        }
        testSimpleTimeZone(new SimpleTimeZone(-8*ONE_HOUR, "X",
                                              APRIL, 1, -SUNDAY, 2*ONE_HOUR,
                                              OCTOBER, -1, SUNDAY, 2*ONE_HOUR,
                                              1*ONE_HOUR));
        testSimpleTimeZone(new SimpleTimeZone(-8*ONE_HOUR, "Y"));
        if (errors > 0) {
            throw new RuntimeException("DaylightTimeTest: failed");
        }
    }
    private static boolean findDSTTransition(TimeZone tz, long now) {
        GregorianCalendar cal = new GregorianCalendar(tz, Locale.US);
        cal.setTimeInMillis(now);
        cal.add(YEAR, 50);
        long end = cal.getTimeInMillis();
        for (long t = now; t < end; t += INTERVAL) {
            cal.setTimeInMillis(t);
            if (cal.get(DST_OFFSET) > 0) {
                return true;
            }
        }
        return false;
    }
    private static void testSimpleTimeZone(SimpleTimeZone stz) {
        if (stz.useDaylightTime() != stz.observesDaylightTime()) {
            System.err.printf("Failed: useDaylightTime=%b, observesDaylightTime()=%b%n\t%s%n",
                              stz.useDaylightTime(),stz.observesDaylightTime(), stz);
            errors++;
        }
    }
}

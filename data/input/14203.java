public class Bug6772689 {
    private static final int BEGIN_YEAR = 2035;
    private static final int END_YEAR = BEGIN_YEAR + 28;
    public static void main(String[] args) {
        TimeZone defaultTimeZone = TimeZone.getDefault();
        int errors = 0;
        Calendar cal = new GregorianCalendar(BEGIN_YEAR, MARCH, 1);
        String[] tzids = TimeZone.getAvailableIDs();
        try {
            for (String id : tzids) {
                TimeZone tz = TimeZone.getTimeZone(id);
                if (!tz.useDaylightTime()) {
                    continue;
                }
                TimeZone.setDefault(tz);
              dateloop:
                for (int year = BEGIN_YEAR; year < END_YEAR; year++) {
                    for (int month = MARCH; month <= NOVEMBER; month++) {
                        cal.set(year, month, 1, 15, 0, 0);
                        int maxDom = cal.getActualMaximum(DAY_OF_MONTH);
                        for (int dom = 1; dom <= maxDom; dom++) {
                            Date date = new Date(year - 1900, month, dom);
                            if (date.getYear()+1900 != year
                                || date.getMonth() != month
                                || date.getDate() != dom) {
                                System.err.printf("%s: got %04d-%02d-%02d, expected %04d-%02d-%02d%n",
                                                  id,
                                                  date.getYear() + 1900,
                                                  date.getMonth() + 1,
                                                  date.getDate(),
                                                  year,
                                                  month + 1,
                                                  dom);
                                errors++;
                                break dateloop;
                            }
                        }
                    }
                }
            }
        } finally {
            TimeZone.setDefault(defaultTimeZone);
        }
        if (errors > 0) {
            throw new RuntimeException("Transition test failed");
        }
    }
}

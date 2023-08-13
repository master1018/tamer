public class TimeZoneNameProviderTest extends ProviderTest {
    com.bar.TimeZoneNameProviderImpl tznp = new com.bar.TimeZoneNameProviderImpl();
    public static void main(String[] s) {
        new TimeZoneNameProviderTest();
    }
    TimeZoneNameProviderTest() {
        test1();
        test2();
        aliasTest();
    }
    void test1() {
        Locale[] available = Locale.getAvailableLocales();
        List<Locale> providerLocales = Arrays.asList(tznp.getAvailableLocales());
        String[] ids = TimeZone.getAvailableIDs();
        for (Locale target: available) {
            OpenListResourceBundle rb = LocaleData.getTimeZoneNames(target);
            boolean jreHasBundle = rb.getLocale().equals(target);
            for (String id: ids) {
                TimeZone tz = TimeZone.getTimeZone(id);
                String[] jrearray = null;
                if (jreHasBundle) {
                    try {
                        jrearray = rb.getStringArray(id);
                    } catch (MissingResourceException mre) {}
                }
                for (int i = 1; i <=(tz.useDaylightTime()?4:2); i++) {
                    String name = tz.getDisplayName(i>=3, i%2, target);
                    String providersname = null;
                    if (providerLocales.contains(target)) {
                        providersname = tznp.getDisplayName(id, i>=3, i%2, target);
                    }
                    String jresname = null;
                    if (jrearray != null) {
                        jresname = jrearray[i];
                    }
                    checkValidity(target, jresname, providersname, name,
                        jreHasBundle && rb.handleGetKeys().contains(id));
                }
            }
        }
    }
    final String pattern = "z";
    final Locale OSAKA = new Locale("ja", "JP", "osaka");
    final Locale KYOTO = new Locale("ja", "JP", "kyoto");
    final String[] TIMEZONES = {
        "GMT", "America/Los_Angeles", "SystemV/PST8",
        "SystemV/PST8PDT", "PST8PDT",
    };
    final String[] DISPLAY_NAMES_OSAKA = {
        tznp.getDisplayName(TIMEZONES[0], false, TimeZone.SHORT, OSAKA),
        tznp.getDisplayName(TIMEZONES[1], false, TimeZone.SHORT, OSAKA),
        tznp.getDisplayName(TIMEZONES[2], false, TimeZone.SHORT, OSAKA),
        tznp.getDisplayName(TIMEZONES[3], false, TimeZone.SHORT, OSAKA),
        tznp.getDisplayName(TIMEZONES[4], false, TimeZone.SHORT, OSAKA)
    };
    final String[] DISPLAY_NAMES_KYOTO = {
        tznp.getDisplayName(TIMEZONES[0], false, TimeZone.SHORT, KYOTO),
        tznp.getDisplayName(TIMEZONES[1], false, TimeZone.SHORT, KYOTO),
        tznp.getDisplayName(TIMEZONES[2], false, TimeZone.SHORT, KYOTO),
        tznp.getDisplayName(TIMEZONES[3], false, TimeZone.SHORT, KYOTO),
        tznp.getDisplayName(TIMEZONES[4], false, TimeZone.SHORT, KYOTO)
    };
    void test2() {
        Locale defaultLocale = Locale.getDefault();
        Date d = new Date(2005-1900, Calendar.DECEMBER, 22);
        String formatted;
        TimeZone tz;
        SimpleDateFormat df;
        try {
            for (int i = 0; i < TIMEZONES.length; i++) {
                tz = TimeZone.getTimeZone(TIMEZONES[i]);
                TimeZone.setDefault(tz);
                df = new SimpleDateFormat(pattern, DateFormatSymbols.getInstance(OSAKA));
                Locale.setDefault(defaultLocale);
                System.out.println(formatted = df.format(d));
                if(!formatted.equals(DISPLAY_NAMES_OSAKA[i])) {
                    throw new RuntimeException("TimeZone " + TIMEZONES[i] +
                        ": formatted zone names mismatch. " +
                        formatted + " should match with " +
                        DISPLAY_NAMES_OSAKA[i]);
                }
                df.parse(DISPLAY_NAMES_OSAKA[i]);
                Locale.setDefault(KYOTO);
                df = new SimpleDateFormat(pattern, DateFormatSymbols.getInstance());
                System.out.println(formatted = df.format(d));
                if(!formatted.equals(DISPLAY_NAMES_KYOTO[i])) {
                    Locale.setDefault(defaultLocale);
                    throw new RuntimeException("Timezone " + TIMEZONES[i] +
                        ": formatted zone names mismatch. " +
                        formatted + " should match with " +
                        DISPLAY_NAMES_KYOTO[i]);
                }
                df.parse(DISPLAY_NAMES_KYOTO[i]);
            }
        } catch (ParseException pe) {
            Locale.setDefault(defaultLocale);
            throw new RuntimeException("parse error occured" + pe);
        }
        Locale.setDefault(defaultLocale);
    }
    final String LATIME = "America/Los_Angeles";
    final String PST = "PST";
    final String PST8PDT = "PST8PDT";
    final String US_PACIFIC = "US/Pacific";
    final String LATIME_IN_OSAKA =
        tznp.getDisplayName(LATIME, false, TimeZone.LONG, OSAKA);
    final String TOKYOTIME = "Asia/Tokyo";
    final String JST = "JST";
    final String JAPAN = "Japan";
    final String JST_IN_OSAKA =
        tznp.getDisplayName(JST, false, TimeZone.LONG, OSAKA);
    void aliasTest() {
        String latime = TimeZone.getTimeZone(LATIME).getDisplayName(OSAKA);
        if (!LATIME_IN_OSAKA.equals(latime)) {
            throw new RuntimeException("Could not get provider's localized name.  result: "+latime+" expected: "+LATIME_IN_OSAKA);
        }
        String pst = TimeZone.getTimeZone(PST).getDisplayName(OSAKA);
        if (!LATIME_IN_OSAKA.equals(pst)) {
            throw new RuntimeException("Provider's localized name is not available for an alias ID: "+PST+".  result: "+pst+" expected: "+LATIME_IN_OSAKA);
        }
        String us_pacific = TimeZone.getTimeZone(US_PACIFIC).getDisplayName(OSAKA);
        if (!LATIME_IN_OSAKA.equals(us_pacific)) {
            throw new RuntimeException("Provider's localized name is not available for an alias ID: "+US_PACIFIC+".  result: "+us_pacific+" expected: "+LATIME_IN_OSAKA);
        }
        String jstime = TimeZone.getTimeZone(JST).getDisplayName(OSAKA);
        if (!JST_IN_OSAKA.equals(jstime)) {
            throw new RuntimeException("Could not get provider's localized name.  result: "+jstime+" expected: "+JST_IN_OSAKA);
        }
        String tokyotime = TimeZone.getTimeZone(TOKYOTIME).getDisplayName(OSAKA);
        if (!JST_IN_OSAKA.equals(tokyotime)) {
            throw new RuntimeException("Provider's localized name is not available for a standard ID: "+TOKYOTIME+".  result: "+tokyotime+" expected: "+JST_IN_OSAKA);
        }
        String japan = TimeZone.getTimeZone(JAPAN).getDisplayName(OSAKA);
        if (!JST_IN_OSAKA.equals(japan)) {
            throw new RuntimeException("Provider's localized name is not available for an alias ID: "+JAPAN+".  result: "+japan+" expected: "+JST_IN_OSAKA);
        }
    }
}

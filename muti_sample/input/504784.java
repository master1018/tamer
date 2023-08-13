public class TimeTest extends TestCase {
    @SmallTest
    public void testNormalize0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.parse("20060432T010203");
        t.normalize(false );
    }
    private static class DateTest {
        public int year1;
        public int month1;
        public int day1;
        public int hour1;
        public int minute1;
        public int dst1;
        public int offset;
        public int year2;
        public int month2;
        public int day2;
        public int hour2;
        public int minute2;
        public int dst2;
        public DateTest(int year1, int month1, int day1, int hour1, int minute1, int dst1,
                int offset, int year2, int month2, int day2, int hour2, int minute2,
                int dst2) {
            this.year1 = year1;
            this.month1 = month1;
            this.day1 = day1;
            this.hour1 = hour1;
            this.minute1 = minute1;
            this.dst1 = dst1;
            this.offset = offset;
            this.year2 = year2;
            this.month2 = month2;
            this.day2 = day2;
            this.hour2 = hour2;
            this.minute2 = minute2;
            this.dst2 = dst2;
        }
        public DateTest(int year1, int month1, int day1, int hour1, int minute1,
                int offset, int year2, int month2, int day2, int hour2, int minute2) {
            this.year1 = year1;
            this.month1 = month1;
            this.day1 = day1;
            this.hour1 = hour1;
            this.minute1 = minute1;
            this.dst1 = -1;
            this.offset = offset;
            this.year2 = year2;
            this.month2 = month2;
            this.day2 = day2;
            this.hour2 = hour2;
            this.minute2 = minute2;
            this.dst2 = -1;
        }
    }
    private DateTest[] dayTests = {
            new DateTest(2007, 10, 4, 0, 0, 0, 2007, 10, 4, 0, 0),
            new DateTest(2007, 10, 5, 0, 0, 0, 2007, 10, 5, 0, 0),
            new DateTest(2007, 10, 3, 0, 0, 1, 2007, 10, 4, 0, 0),
            new DateTest(2007, 10, 4, 0, 0, 1, 2007, 10, 5, 0, 0),
            new DateTest(2007, 10, 5, 0, 0, 1, 2007, 10, 6, 0, 0),
            new DateTest(2007, 10, 3, 1, 0, 1, 2007, 10, 4, 1, 0),
            new DateTest(2007, 10, 4, 1, 0, 1, 2007, 10, 5, 1, 0),
            new DateTest(2007, 10, 5, 1, 0, 1, 2007, 10, 6, 1, 0),
            new DateTest(2007, 10, 3, 2, 0, 1, 2007, 10, 4, 2, 0),
            new DateTest(2007, 10, 4, 2, 0, 1, 2007, 10, 5, 2, 0),
            new DateTest(2007, 10, 5, 2, 0, 1, 2007, 10, 6, 2, 0),
    };
    private DateTest[] minuteTests = {
            new DateTest(2007, 10, 4, 0, 0, 0, 2007, 10, 4, 0, 0),
            new DateTest(2007, 10, 5, 0, 0, 0, 2007, 10, 5, 0, 0),
            new DateTest(2007, 10, 3, 0, 0, 60, 2007, 10, 3, 1, 0),
            new DateTest(2007, 10, 4, 0, 0, 60, 2007, 10, 4, 1, 0),
            new DateTest(2007, 10, 5, 0, 0, 60, 2007, 10, 5, 1, 0),
            new DateTest(2007, 10, 3, 1, 0, 60, 2007, 10, 3, 2, 0),
            new DateTest(2007, 10, 4, 1, 0, 1, 30, 2007, 10, 4, 1, 30, 1),
            new DateTest(2007, 10, 4, 1, 0, 1, 60, 2007, 10, 4, 1, 0, 0),
            new DateTest(2007, 10, 4, 1, 30, 1, 15, 2007, 10, 4, 1, 45, 1),
            new DateTest(2007, 10, 4, 1, 30, 1, 30, 2007, 10, 4, 1, 0, 0),
            new DateTest(2007, 10, 4, 1, 30, 1, 60, 2007, 10, 4, 1, 30, 0),
            new DateTest(2007, 10, 4, 1, 30, 0, 15, 2007, 10, 4, 1, 45, 0),
            new DateTest(2007, 10, 4, 1, 30, 0, 30, 2007, 10, 4, 2, 0, 0),
            new DateTest(2007, 10, 5, 1, 0, 60, 2007, 10, 5, 2, 0),
            new DateTest(2007, 10, 3, 2, 0, 60, 2007, 10, 3, 3, 0),
            new DateTest(2007, 10, 4, 2, 0, 30, 2007, 10, 4, 2, 30),
            new DateTest(2007, 10, 4, 2, 0, 60, 2007, 10, 4, 3, 0),
            new DateTest(2007, 10, 5, 2, 0, 60, 2007, 10, 5, 3, 0),
    };
    @SmallTest
    public void testNormalize1() throws Exception {
        Time local = new Time("America/Los_Angeles");
        int len = dayTests.length;
        for (int index = 0; index < len; index++) {
            DateTest test = dayTests[index];
            local.set(0, test.minute1, test.hour1, test.day1, test.month1, test.year1);
            local.normalize(false );
            local.monthDay += test.offset;
            local.normalize(true );
            if (local.year != test.year2 || local.month != test.month2
                    || local.monthDay != test.day2 || local.hour != test.hour2
                    || local.minute != test.minute2) {
                String expectedTime = String.format("%d-%02d-%02d %02d:%02d",
                        test.year2, test.month2, test.day2, test.hour2, test.minute2);
                String actualTime = String.format("%d-%02d-%02d %02d:%02d",
                        local.year, local.month, local.monthDay, local.hour, local.minute);
                throw new RuntimeException(
                        "day test index " + index + ", normalize(): expected local " + expectedTime
                                + " got: " + actualTime);
            }
            local.set(0, test.minute1, test.hour1, test.day1, test.month1, test.year1);
            local.normalize(false );
            local.monthDay += test.offset;
            long millis = local.toMillis(true );
            local.set(millis);
            if (local.year != test.year2 || local.month != test.month2
                    || local.monthDay != test.day2 || local.hour != test.hour2
                    || local.minute != test.minute2) {
                String expectedTime = String.format("%d-%02d-%02d %02d:%02d",
                        test.year2, test.month2, test.day2, test.hour2, test.minute2);
                String actualTime = String.format("%d-%02d-%02d %02d:%02d",
                        local.year, local.month, local.monthDay, local.hour, local.minute);
                throw new RuntimeException(
                        "day test index " + index + ", toMillis(): expected local " + expectedTime
                                + " got: " + actualTime);
            }
        }
        len = minuteTests.length;
        for (int index = 0; index < len; index++) {
            DateTest test = minuteTests[index];
            local.set(0, test.minute1, test.hour1, test.day1, test.month1, test.year1);
            local.isDst = test.dst1;
            local.normalize(false );
            if (test.dst2 == -1) test.dst2 = local.isDst;
            local.minute += test.offset;
            local.normalize(false );
            if (local.year != test.year2 || local.month != test.month2
                    || local.monthDay != test.day2 || local.hour != test.hour2
                    || local.minute != test.minute2 || local.isDst != test.dst2) {
                String expectedTime = String.format("%d-%02d-%02d %02d:%02d isDst: %d",
                        test.year2, test.month2, test.day2, test.hour2, test.minute2,
                        test.dst2);
                String actualTime = String.format("%d-%02d-%02d %02d:%02d isDst: %d",
                        local.year, local.month, local.monthDay, local.hour, local.minute,
                        local.isDst);
                throw new RuntimeException(
                        "minute test index " + index + ", normalize(): expected local " + expectedTime
                                + " got: " + actualTime);
            }
            local.set(0, test.minute1, test.hour1, test.day1, test.month1, test.year1);
            local.isDst = test.dst1;
            local.normalize(false );
            if (test.dst2 == -1) test.dst2 = local.isDst;
            local.minute += test.offset;
            long millis = local.toMillis(false );
            local.set(millis);
            if (local.year != test.year2 || local.month != test.month2
                    || local.monthDay != test.day2 || local.hour != test.hour2
                    || local.minute != test.minute2 || local.isDst != test.dst2) {
                String expectedTime = String.format("%d-%02d-%02d %02d:%02d isDst: %d",
                        test.year2, test.month2, test.day2, test.hour2, test.minute2,
                        test.dst2);
                String actualTime = String.format("%d-%02d-%02d %02d:%02d isDst: %d",
                        local.year, local.month, local.monthDay, local.hour, local.minute,
                        local.isDst);
                throw new RuntimeException(
                        "minute test index " + index + ", toMillis(): expected local " + expectedTime
                                + " got: " + actualTime);
            }
        }
    }
    @SmallTest
    public void testSwitchTimezone0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.parse("20061005T120000");
        t.switchTimezone("America/Los_Angeles");
    }
    @SmallTest
    public void testCtor0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        assertEquals(Time.TIMEZONE_UTC, t.timezone);
    }
    @SmallTest
    public void testGetActualMaximum0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        int r = t.getActualMaximum(Time.SECOND);
    }
    @SmallTest
    public void testClear0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.clear(Time.TIMEZONE_UTC);
    }
    @SmallTest
    public void testCompare0() throws Exception {
        Time a = new Time(Time.TIMEZONE_UTC);
        Time b = new Time("America/Los_Angeles");
        int r = Time.compare(a, b);
    }
    @SmallTest
    public void testFormat0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        String r = t.format("%Y%m%dT%H%M%S");
    }
    @SmallTest
    public void testToString0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        String r = t.toString();
    }
    @SmallTest
    public void testGetCurrentTimezone0() throws Exception {
        String r = Time.getCurrentTimezone();
    }
    @SmallTest
    public void testSetToNow0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.setToNow();
    }
    @SmallTest
    public void testMillis0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.set(0, 0, 0, 1, 1, 2006);
        long r = t.toMillis(true );
        t.set(1, 0, 0, 1, 1, 2006);
        r = t.toMillis(true );
    }
    @SmallTest
    public void testMillis1() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.set(1, 0, 0, 1, 0, 1970);
        long r = t.toMillis(true );
    }
    @SmallTest
    public void testParse0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.parse("12345678T901234");
    }
    @SmallTest
    public void testParse33390() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.parse3339("1980-05-23");
        if (!t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23) {
            fail("Did not parse all-day date correctly");
        }
        t.parse3339("1980-05-23T09:50:50");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 9 || t.minute != 50 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse timezone-offset-less date correctly");
        }
        t.parse3339("1980-05-23T09:50:50Z");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 9 || t.minute != 50 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse UTC date correctly");
        }
        t.parse3339("1980-05-23T09:50:50.0Z");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 9 || t.minute != 50 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse UTC date correctly");
        }
        t.parse3339("1980-05-23T09:50:50.12Z");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 9 || t.minute != 50 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse UTC date correctly");
        }
        t.parse3339("1980-05-23T09:50:50.123Z");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 9 || t.minute != 50 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse UTC date correctly");
        }
        t.parse3339("1980-05-23T09:50:50-01:05");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 10 || t.minute != 55 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse timezone-offset date correctly");
        }
        t.parse3339("1980-05-23T09:50:50.123-01:05");
        if (t.allDay || t.year != 1980 || t.month != 04 || t.monthDay != 23 ||
                t.hour != 10 || t.minute != 55 || t.second != 50 ||
                t.gmtoff != 0) {
            fail("Did not parse timezone-offset date correctly");
        }
        try {
            t.parse3339("1980");
            fail("Did not throw error on truncated input length");
        } catch (TimeFormatException e) {
        }
        try {
            t.parse3339("1980-05-23T09:50:50.123+");
            fail("Did not throw error on truncated timezone offset");
        } catch (TimeFormatException e1) {
        }
        try {
            t.parse3339("1980-05-23T09:50:50.123+05:0");
            fail("Did not throw error on truncated timezone offset");
        } catch (TimeFormatException e1) {
        }
    }
    @SmallTest
    public void testSet0() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.set(1000L);
        t.set(2000L);
        t.set(1000L * 60);
        t.set((1000L * 60 * 60 * 24) + 1000L);
    }
    @SmallTest
    public void testSet1() throws Exception {
        Time t = new Time(Time.TIMEZONE_UTC);
        t.set(1, 2, 3, 4, 5, 6);
    }
    private static final String[] mTimeZones = {
        "Pacific/Kiritimati",
        "Pacific/Enderbury",
        "Pacific/Fiji",
        "Antarctica/South_Pole",
        "Pacific/Norfolk",
        "Pacific/Ponape",
        "Asia/Magadan",
        "Australia/Lord_Howe",
        "Australia/Sydney",
        "Australia/Adelaide",
        "Asia/Tokyo",
        "Asia/Seoul",
        "Asia/Taipei",
        "Asia/Singapore",
        "Asia/Hong_Kong",
        "Asia/Saigon",
        "Asia/Bangkok",
        "Indian/Cocos",
        "Asia/Rangoon",
        "Asia/Omsk",
        "Antarctica/Mawson",
        "Asia/Colombo",
        "Asia/Calcutta",
        "Asia/Oral",
        "Asia/Kabul",
        "Asia/Dubai",
        "Asia/Tehran",
        "Europe/Moscow",
        "Asia/Baghdad",
        "Africa/Mogadishu",
        "Europe/Athens",
        "Africa/Cairo",
        "Europe/Rome",
        "Europe/Berlin",
        "Europe/Amsterdam",
        "Africa/Tunis",
        "Europe/London",
        "Europe/Dublin",
        "Atlantic/St_Helena",
        "Africa/Monrovia",
        "Africa/Accra",
        "Atlantic/Azores",
        "Atlantic/South_Georgia",
        "America/Noronha",
        "America/Sao_Paulo",
        "America/Cayenne",
        "America/St_Johns",
        "America/Puerto_Rico",
        "America/Aruba",
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "America/Anchorage",
        "Pacific/Marquesas",
        "America/Adak",
        "Pacific/Honolulu",
        "Pacific/Midway",
    };
    @Suppress
    public void disableTestGetJulianDay() throws Exception {
        Time time = new Time();
        for (int monthDay = 1; monthDay <= 366; monthDay++) {
            for (int zoneIndex = 0; zoneIndex < mTimeZones.length; zoneIndex++) {
                time.set(0, 0, 0, monthDay, 0, 2008);
                time.timezone = mTimeZones[zoneIndex];
                long millis = time.normalize(true);
                if (zoneIndex == 0) {
                    Log.i("TimeTest", time.format("%B %d, %Y"));
                }
                int julianDay = Time.getJulianDay(millis, time.gmtoff);
                for (int hour = 0; hour < 24; hour++) {
                    for (int minute = 0; minute < 60; minute += 15) {
                        time.set(0, minute, hour, monthDay, 0, 2008);
                        millis = time.normalize(true);
                        int day = Time.getJulianDay(millis, time.gmtoff);
                        if (day != julianDay) {
                            Log.e("TimeTest", "Julian day: " + day + " at time "
                                    + time.hour + ":" + time.minute
                                    + " != today's Julian day: " + julianDay
                                    + " timezone: " + time.timezone);
                        }
                        assertEquals(day, julianDay);
                    }
                }
            }
        }
    }
    @Suppress
    public void disableTestSetJulianDay() throws Exception {
        Time time = new Time();
        for (int monthDay = 1; monthDay <= 366; monthDay++) {
            for (int zoneIndex = 0; zoneIndex < mTimeZones.length; zoneIndex++) {
                time.set(0, 0, 0, monthDay, 0, 2008);
                time.timezone = mTimeZones[zoneIndex];
                long millis = time.normalize(true);
                if (zoneIndex == 0) {
                    Log.i("TimeTest", time.format("%B %d, %Y"));
                }
                int julianDay = Time.getJulianDay(millis, time.gmtoff);
                time.setJulianDay(julianDay);
                assertTrue(time.hour == 0 || time.hour == 1);
                assertEquals(0, time.minute);
                assertEquals(0, time.second);
                millis = time.toMillis(false);
                int day = Time.getJulianDay(millis, time.gmtoff);
                if (day != julianDay) {
                    Log.i("TimeTest", "Error: gmtoff " + (time.gmtoff / 3600.0)
                            + " day " + julianDay
                            + " millis " + millis
                            + " " + time.format("%B %d, %Y") + " " + time.timezone);
                }
                assertEquals(day, julianDay);
            }
        }
    }
}

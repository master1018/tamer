public abstract class BaseCalendar extends AbstractCalendar {
    public static final int JANUARY = 1;
    public static final int FEBRUARY = 2;
    public static final int MARCH = 3;
    public static final int APRIL = 4;
    public static final int MAY = 5;
    public static final int JUNE = 6;
    public static final int JULY = 7;
    public static final int AUGUST = 8;
    public static final int SEPTEMBER = 9;
    public static final int OCTOBER = 10;
    public static final int NOVEMBER = 11;
    public static final int DECEMBER = 12;
    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;
    private static final int BASE_YEAR = 1970;
    private static final int[] FIXED_DATES = {
        719163, 
        719528, 
        719893, 
        720259, 
        720624, 
        720989, 
        721354, 
        721720, 
        722085, 
        722450, 
        722815, 
        723181, 
        723546, 
        723911, 
        724276, 
        724642, 
        725007, 
        725372, 
        725737, 
        726103, 
        726468, 
        726833, 
        727198, 
        727564, 
        727929, 
        728294, 
        728659, 
        729025, 
        729390, 
        729755, 
        730120, 
        730486, 
        730851, 
        731216, 
        731581, 
        731947, 
        732312, 
        732677, 
        733042, 
        733408, 
        733773, 
        734138, 
        734503, 
        734869, 
        735234, 
        735599, 
        735964, 
        736330, 
        736695, 
        737060, 
        737425, 
        737791, 
        738156, 
        738521, 
        738886, 
        739252, 
        739617, 
        739982, 
        740347, 
        740713, 
        741078, 
        741443, 
        741808, 
        742174, 
        742539, 
        742904, 
        743269, 
        743635, 
        744000, 
        744365, 
    };
    public abstract static class Date extends CalendarDate {
        protected Date() {
            super();
        }
        protected Date(TimeZone zone) {
            super(zone);
        }
        public Date setNormalizedDate(int normalizedYear, int month, int dayOfMonth) {
            setNormalizedYear(normalizedYear);
            setMonth(month).setDayOfMonth(dayOfMonth);
            return this;
        }
        public abstract int getNormalizedYear();
        public abstract void setNormalizedYear(int normalizedYear);
        int cachedYear = 2004;
        long cachedFixedDateJan1 = 731581L;
        long cachedFixedDateNextJan1 = cachedFixedDateJan1 + 366;
        protected final boolean hit(int year) {
            return year == cachedYear;
        }
        protected final boolean hit(long fixedDate) {
            return (fixedDate >= cachedFixedDateJan1 &&
                    fixedDate < cachedFixedDateNextJan1);
        }
        protected int getCachedYear() {
            return cachedYear;
        }
        protected long getCachedJan1() {
            return cachedFixedDateJan1;
        }
        protected void setCache(int year, long jan1, int len) {
            cachedYear = year;
            cachedFixedDateJan1 = jan1;
            cachedFixedDateNextJan1 = jan1 + len;
        }
    }
    public boolean validate(CalendarDate date) {
        Date bdate = (Date) date;
        if (bdate.isNormalized()) {
            return true;
        }
        int month = bdate.getMonth();
        if (month < JANUARY || month > DECEMBER) {
            return false;
        }
        int d = bdate.getDayOfMonth();
        if (d <= 0 || d > getMonthLength(bdate.getNormalizedYear(), month)) {
            return false;
        }
        int dow = bdate.getDayOfWeek();
        if (dow != bdate.FIELD_UNDEFINED && dow != getDayOfWeek(bdate)) {
            return false;
        }
        if (!validateTime(date)) {
            return false;
        }
        bdate.setNormalized(true);
        return true;
    }
    public boolean normalize(CalendarDate date) {
        if (date.isNormalized()) {
            return true;
        }
        Date bdate = (Date) date;
        TimeZone zi = bdate.getZone();
        if (zi != null) {
            getTime(date);
            return true;
        }
        int days = normalizeTime(bdate);
        normalizeMonth(bdate);
        long d = (long)bdate.getDayOfMonth() + days;
        int m = bdate.getMonth();
        int y = bdate.getNormalizedYear();
        int ml = getMonthLength(y, m);
        if (!(d > 0 && d <= ml)) {
            if (d <= 0 && d > -28) {
                ml = getMonthLength(y, --m);
                d += ml;
                bdate.setDayOfMonth((int) d);
                if (m == 0) {
                    m = DECEMBER;
                    bdate.setNormalizedYear(y - 1);
                }
                bdate.setMonth(m);
            } else if (d > ml && d < (ml + 28)) {
                d -= ml;
                ++m;
                bdate.setDayOfMonth((int)d);
                if (m > DECEMBER) {
                    bdate.setNormalizedYear(y + 1);
                    m = JANUARY;
                }
                bdate.setMonth(m);
            } else {
                long fixedDate = d + getFixedDate(y, m, 1, bdate) - 1L;
                getCalendarDateFromFixedDate(bdate, fixedDate);
            }
        } else {
            bdate.setDayOfWeek(getDayOfWeek(bdate));
        }
        date.setLeapYear(isLeapYear(bdate.getNormalizedYear()));
        date.setZoneOffset(0);
        date.setDaylightSaving(0);
        bdate.setNormalized(true);
        return true;
    }
    void normalizeMonth(CalendarDate date) {
        Date bdate = (Date) date;
        int year = bdate.getNormalizedYear();
        long month = bdate.getMonth();
        if (month <= 0) {
            long xm = 1L - month;
            year -= (int)((xm / 12) + 1);
            month = 13 - (xm % 12);
            bdate.setNormalizedYear(year);
            bdate.setMonth((int) month);
        } else if (month > DECEMBER) {
            year += (int)((month - 1) / 12);
            month = ((month - 1)) % 12 + 1;
            bdate.setNormalizedYear(year);
            bdate.setMonth((int) month);
        }
    }
    public int getYearLength(CalendarDate date) {
        return isLeapYear(((Date)date).getNormalizedYear()) ? 366 : 365;
    }
    public int getYearLengthInMonths(CalendarDate date) {
        return 12;
    }
    static final int[] DAYS_IN_MONTH
        = { 31, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    static final int[] ACCUMULATED_DAYS_IN_MONTH
        = {  -30,  0, 31, 59, 90,120,151,181,212,243, 273, 304, 334};
    static final int[] ACCUMULATED_DAYS_IN_MONTH_LEAP
        = {  -30,  0, 31, 59+1, 90+1,120+1,151+1,181+1,212+1,243+1, 273+1, 304+1, 334+1};
    public int getMonthLength(CalendarDate date) {
        Date gdate = (Date) date;
        int month = gdate.getMonth();
        if (month < JANUARY || month > DECEMBER) {
            throw new IllegalArgumentException("Illegal month value: " + month);
        }
        return getMonthLength(gdate.getNormalizedYear(), month);
    }
    private final int getMonthLength(int year, int month) {
        int days = DAYS_IN_MONTH[month];
        if (month == FEBRUARY && isLeapYear(year)) {
            days++;
        }
        return days;
    }
    public long getDayOfYear(CalendarDate date) {
        return getDayOfYear(((Date)date).getNormalizedYear(),
                            date.getMonth(),
                            date.getDayOfMonth());
    }
    final long getDayOfYear(int year, int month, int dayOfMonth) {
        return (long) dayOfMonth
            + (isLeapYear(year) ?
               ACCUMULATED_DAYS_IN_MONTH_LEAP[month] : ACCUMULATED_DAYS_IN_MONTH[month]);
    }
    public long getFixedDate(CalendarDate date) {
        if (!date.isNormalized()) {
            normalizeMonth(date);
        }
        return getFixedDate(((Date)date).getNormalizedYear(),
                            date.getMonth(),
                            date.getDayOfMonth(),
                            (BaseCalendar.Date) date);
    }
    public long getFixedDate(int year, int month, int dayOfMonth, BaseCalendar.Date cache) {
        boolean isJan1 = month == JANUARY && dayOfMonth == 1;
        if (cache != null && cache.hit(year)) {
            if (isJan1) {
                return cache.getCachedJan1();
            }
            return cache.getCachedJan1() + getDayOfYear(year, month, dayOfMonth) - 1;
        }
        int n = year - BASE_YEAR;
        if (n >= 0 && n < FIXED_DATES.length) {
            long jan1 = FIXED_DATES[n];
            if (cache != null) {
                cache.setCache(year, jan1, isLeapYear(year) ? 366 : 365);
            }
            return isJan1 ? jan1 : jan1 + getDayOfYear(year, month, dayOfMonth) - 1;
        }
        long prevyear = (long)year - 1;
        long days = dayOfMonth;
        if (prevyear >= 0) {
            days += (365 * prevyear)
                   + (prevyear / 4)
                   - (prevyear / 100)
                   + (prevyear / 400)
                   + ((367 * month - 362) / 12);
        } else {
            days += (365 * prevyear)
                   + CalendarUtils.floorDivide(prevyear, 4)
                   - CalendarUtils.floorDivide(prevyear, 100)
                   + CalendarUtils.floorDivide(prevyear, 400)
                   + CalendarUtils.floorDivide((367 * month - 362), 12);
        }
        if (month > FEBRUARY) {
            days -=  isLeapYear(year) ? 1 : 2;
        }
        if (cache != null && isJan1) {
            cache.setCache(year, days, isLeapYear(year) ? 366 : 365);
        }
        return days;
    }
    public void getCalendarDateFromFixedDate(CalendarDate date,
                                             long fixedDate) {
        Date gdate = (Date) date;
        int year;
        long jan1;
        boolean isLeap;
        if (gdate.hit(fixedDate)) {
            year = gdate.getCachedYear();
            jan1 = gdate.getCachedJan1();
            isLeap = isLeapYear(year);
        } else {
            year = getGregorianYearFromFixedDate(fixedDate);
            jan1 = getFixedDate(year, JANUARY, 1, null);
            isLeap = isLeapYear(year);
            gdate.setCache (year, jan1, isLeap ? 366 : 365);
        }
        int priorDays = (int)(fixedDate - jan1);
        long mar1 = jan1 + 31 + 28;
        if (isLeap) {
            ++mar1;
        }
        if (fixedDate >= mar1) {
            priorDays += isLeap ? 1 : 2;
        }
        int month = 12 * priorDays + 373;
        if (month > 0) {
            month /= 367;
        } else {
            month = CalendarUtils.floorDivide(month, 367);
        }
        long month1 = jan1 + ACCUMULATED_DAYS_IN_MONTH[month];
        if (isLeap && month >= MARCH) {
            ++month1;
        }
        int dayOfMonth = (int)(fixedDate - month1) + 1;
        int dayOfWeek = getDayOfWeekFromFixedDate(fixedDate);
        assert dayOfWeek > 0 : "negative day of week " + dayOfWeek;
        gdate.setNormalizedYear(year);
        gdate.setMonth(month);
        gdate.setDayOfMonth(dayOfMonth);
        gdate.setDayOfWeek(dayOfWeek);
        gdate.setLeapYear(isLeap);
        gdate.setNormalized(true);
    }
    public int getDayOfWeek(CalendarDate date) {
        long fixedDate = getFixedDate(date);
        return getDayOfWeekFromFixedDate(fixedDate);
    }
    public static final int getDayOfWeekFromFixedDate(long fixedDate) {
        if (fixedDate >= 0) {
            return (int)(fixedDate % 7) + SUNDAY;
        }
        return (int)CalendarUtils.mod(fixedDate, 7) + SUNDAY;
    }
    public int getYearFromFixedDate(long fixedDate) {
        return getGregorianYearFromFixedDate(fixedDate);
    }
    final int getGregorianYearFromFixedDate(long fixedDate) {
        long d0;
        int  d1, d2, d3, d4;
        int  n400, n100, n4, n1;
        int  year;
        if (fixedDate > 0) {
            d0 = fixedDate - 1;
            n400 = (int)(d0 / 146097);
            d1 = (int)(d0 % 146097);
            n100 = d1 / 36524;
            d2 = d1 % 36524;
            n4 = d2 / 1461;
            d3 = d2 % 1461;
            n1 = d3 / 365;
            d4 = (d3 % 365) + 1;
        } else {
            d0 = fixedDate - 1;
            n400 = (int)CalendarUtils.floorDivide(d0, 146097L);
            d1 = (int)CalendarUtils.mod(d0, 146097L);
            n100 = CalendarUtils.floorDivide(d1, 36524);
            d2 = CalendarUtils.mod(d1, 36524);
            n4 = CalendarUtils.floorDivide(d2, 1461);
            d3 = CalendarUtils.mod(d2, 1461);
            n1 = CalendarUtils.floorDivide(d3, 365);
            d4 = CalendarUtils.mod(d3, 365) + 1;
        }
        year = 400 * n400 + 100 * n100 + 4 * n4 + n1;
        if (!(n100 == 4 || n1 == 4)) {
            ++year;
        }
        return year;
    }
    protected boolean isLeapYear(CalendarDate date) {
        return isLeapYear(((Date)date).getNormalizedYear());
    }
    boolean isLeapYear(int normalizedYear) {
        return CalendarUtils.isGregorianLeapYear(normalizedYear);
    }
}

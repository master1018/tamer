public class RecurrenceProcessor
{
    private Time mIterator = new Time(Time.TIMEZONE_UTC);
    private Time mUntil = new Time(Time.TIMEZONE_UTC);
    private StringBuilder mStringBuilder = new StringBuilder();
    private Time mGenerated = new Time(Time.TIMEZONE_UTC);
    private DaySet mDays = new DaySet(false);
    private static final int MAX_ALLOWED_ITERATIONS = 2000;
    public RecurrenceProcessor()
    {
    }
    private static final String TAG = "RecurrenceProcessor";
    private static final boolean SPEW = false;
    public long getLastOccurence(Time dtstart,
                                 RecurrenceSet recur) throws DateException {
        long lastTime = -1;
        boolean hasCount = false;
        if (recur.rrules != null) {
            for (EventRecurrence rrule : recur.rrules) {
                if (rrule.count != 0) {
                    hasCount = true;
                } else if (rrule.until != null) {
                    mIterator.parse(rrule.until);
                    long untilTime = mIterator.toMillis(false );
                    if (untilTime > lastTime) {
                        lastTime = untilTime;
                    }
                } else {
                    return -1;
                }
            }
            if (lastTime != -1 && recur.rdates != null) {
                for (long dt : recur.rdates) {
                    if (dt > lastTime) {
                        lastTime = dt;
                    }
                }
            }
            if (lastTime != -1 && !hasCount) {
                return lastTime;
            }
        } else if (recur.rdates != null &&
                   recur.exrules == null && recur.exdates == null) {
            for (long dt : recur.rdates) {
                if (dt > lastTime) {
                    lastTime = dt;
                }
            }
            return lastTime;
        }
        if (hasCount || recur.rdates != null) {
            long[] dates = expand(dtstart, recur,
                    dtstart.toMillis(false ) ,
                    -1 );
            if (dates.length == 0) {
                return 0;
            }
            return dates[dates.length - 1];
        }
        return -1;
    }
    private static boolean listContains(int[] a, int N, int v)
    {
        for (int i=0; i<N; i++) {
            if (a[i] == v) {
                return true;
            }
        }
        return false;
    }
    private static boolean listContains(int[] a, int N, int v, int max)
    {
        for (int i=0; i<N; i++) {
            int w = a[i];
            if (w > 0) {
                if (w == v) {
                    return true;
                }
            } else {
                max += w; 
                if (max == v) {
                    return true;
                }
            }
        }
        return false;
    }
    private static int filter(EventRecurrence r, Time iterator)
    {
        boolean found;
        int freq = r.freq;
        if (EventRecurrence.MONTHLY >= freq) {
            if (r.bymonthCount > 0) {
                found = listContains(r.bymonth, r.bymonthCount,
                        iterator.month + 1);
                if (!found) {
                    return 1;
                }
            }
        }
        if (EventRecurrence.WEEKLY >= freq) {
            if (r.byweeknoCount > 0) {
                found = listContains(r.byweekno, r.byweeknoCount,
                                iterator.getWeekNumber(),
                                iterator.getActualMaximum(Time.WEEK_NUM));
                if (!found) {
                    return 2;
                }
            }
        }
        if (EventRecurrence.DAILY >= freq) {
            if (r.byyeardayCount > 0) {
                found = listContains(r.byyearday, r.byyeardayCount,
                                iterator.yearDay, iterator.getActualMaximum(Time.YEAR_DAY));
                if (!found) {
                    return 3;
                }
            }
            if (r.bymonthdayCount > 0 ) {
                found = listContains(r.bymonthday, r.bymonthdayCount,
                                iterator.monthDay,
                                iterator.getActualMaximum(Time.MONTH_DAY));
                if (!found) {
                    return 4;
                }
            }
byday:
            if (r.bydayCount > 0) {
                int a[] = r.byday;
                int N = r.bydayCount;
                int v = EventRecurrence.timeDay2Day(iterator.weekDay);
                for (int i=0; i<N; i++) {
                    if (a[i] == v) {
                        break byday;
                    }
                }
                return 5;
            }
        }
        if (EventRecurrence.HOURLY >= freq) {
            found = listContains(r.byhour, r.byhourCount,
                            iterator.hour,
                            iterator.getActualMaximum(Time.HOUR));
            if (!found) {
                return 6;
            }
        }
        if (EventRecurrence.MINUTELY >= freq) {
            found = listContains(r.byminute, r.byminuteCount,
                            iterator.minute,
                            iterator.getActualMaximum(Time.MINUTE));
            if (!found) {
                return 7;
            }
        }
        if (EventRecurrence.SECONDLY >= freq) {
            found = listContains(r.bysecond, r.bysecondCount,
                            iterator.second,
                            iterator.getActualMaximum(Time.SECOND));
            if (!found) {
                return 8;
            }
        }
        return 0;
    }
    private static final int USE_ITERATOR = 0;
    private static final int USE_BYLIST = 1;
    int generateByList(int count, int freq, int byFreq)
    {
        if (byFreq >= freq) {
            return USE_ITERATOR;
        } else {
            if (count == 0) {
                return USE_ITERATOR;
            } else {
                return USE_BYLIST;
            }
        }
    }
    private static boolean useBYX(int freq, int freqConstant, int count)
    {
        return freq > freqConstant && count > 0;
    }
    public static class DaySet
    {
        public DaySet(boolean zulu)
        {
            mTime = new Time(Time.TIMEZONE_UTC);
        }
        void setRecurrence(EventRecurrence r)
        {
            mYear = 0;
            mMonth = -1;
            mR = r;
        }
        boolean get(Time iterator, int day)
        {
            int realYear = iterator.year;
            int realMonth = iterator.month;
            Time t = null;
            if (SPEW) {
                Log.i(TAG, "get called with iterator=" + iterator
                        + " " + iterator.month
                        + "/" + iterator.monthDay
                        + "/" + iterator.year + " day=" + day);
            }
            if (day < 1 || day > 28) {
                t = mTime;
                t.set(day, realMonth, realYear);
                unsafeNormalize(t);
                realYear = t.year;
                realMonth = t.month;
                day = t.monthDay;
                if (SPEW) {
                    Log.i(TAG, "normalized t=" + t + " " + t.month
                            + "/" + t.monthDay
                            + "/" + t.year);
                }
            }
            if (realYear != mYear || realMonth != mMonth) {
                if (t == null) {
                    t = mTime;
                    t.set(day, realMonth, realYear);
                    unsafeNormalize(t);
                    if (SPEW) {
                        Log.i(TAG, "set t=" + t + " " + t.month
                                + "/" + t.monthDay
                                + "/" + t.year
                                + " realMonth=" + realMonth + " mMonth=" + mMonth);
                    }
                }
                mYear = realYear;
                mMonth = realMonth;
                mDays = generateDaysList(t, mR);
                if (SPEW) {
                    Log.i(TAG, "generated days list");
                }
            }
            return (mDays & (1<<day)) != 0;
        }
        private static int generateDaysList(Time generated, EventRecurrence r)
        {
            int days = 0;
            int i, count, v;
            int[] byday, bydayNum, bymonthday;
            int j, lastDayThisMonth;
            int first; 
            int k;
            lastDayThisMonth = generated.getActualMaximum(Time.MONTH_DAY);
            count = r.bydayCount;
            if (count > 0) {
                j = generated.monthDay;
                while (j >= 8) {
                    j -= 7;
                }
                first = generated.weekDay;
                if (first >= j) {
                    first = first - j + 1;
                } else {
                    first = first - j + 8;
                }
                byday = r.byday;
                bydayNum = r.bydayNum;
                for (i=0; i<count; i++) {
                    v = bydayNum[i];
                    j = EventRecurrence.day2TimeDay(byday[i]) - first + 1;
                    if (j <= 0) {
                        j += 7;
                    }
                    if (v == 0) {
                        for (; j<=lastDayThisMonth; j+=7) {
                            if (SPEW) Log.i(TAG, "setting " + j + " for rule "
                                    + v + "/" + EventRecurrence.day2TimeDay(byday[i]));
                            days |= 1 << j;
                        }
                    }
                    else if (v > 0) {
                        j += 7*(v-1);
                        if (j <= lastDayThisMonth) {
                            if (SPEW) Log.i(TAG, "setting " + j + " for rule "
                                    + v + "/" + EventRecurrence.day2TimeDay(byday[i]));
                            days |= 1 << j;
                        }
                    }
                    else {
                        for (; j<=lastDayThisMonth; j+=7) {
                        }
                        j += 7*v;
                        if (j >= 1) {
                            if (SPEW) Log.i(TAG, "setting " + j + " for rule "
                                    + v + "/" + EventRecurrence.day2TimeDay(byday[i]));
                            days |= 1 << j;
                        }
                    }
                }
            }
            if (r.freq > EventRecurrence.WEEKLY) {
                count = r.bymonthdayCount;
                if (count != 0) {
                    bymonthday = r.bymonthday;
                    if (r.bydayCount == 0) {
                        for (i=0; i<count; i++) {
                            v = bymonthday[i];
                            if (v >= 0) {
                                days |= 1 << v;
                            } else {
                                j = lastDayThisMonth + v + 1; 
                                if (j >= 1 && j <= lastDayThisMonth) {
                                    days |= 1 << j;
                                }
                            }
                        }
                    } else {
                        for (j=1; j<=lastDayThisMonth; j++) {
                            next_day : { 
                                if ((days&(1<<j)) != 0) {
                                    for (i=0; i<count; i++) {
                                        if (bymonthday[i] == j) {
                                            break next_day;
                                        }
                                    }
                                    days &= ~(1<<j);
                                }
                            }
                        }
                    }
                }
            }
            return days;
        }
        private EventRecurrence mR;
        private int mDays;
        private Time mTime;
        private int mYear;
        private int mMonth;
    }
    public long[] expand(Time dtstart,
            RecurrenceSet recur,
            long rangeStartMillis,
            long rangeEndMillis) throws DateException {
        String timezone = dtstart.timezone;
        mIterator.clear(timezone);
        mGenerated.clear(timezone);
        mIterator.set(rangeStartMillis);
        long rangeStartDateValue = normDateTimeComparisonValue(mIterator);
        long rangeEndDateValue;
        if (rangeEndMillis != -1) {
            mIterator.set(rangeEndMillis);
            rangeEndDateValue = normDateTimeComparisonValue(mIterator);
        } else {
            rangeEndDateValue = Long.MAX_VALUE;
        }
        TreeSet<Long> dtSet = new TreeSet<Long>();
        if (recur.rrules != null) {
            for (EventRecurrence rrule : recur.rrules) {
                expand(dtstart, rrule, rangeStartDateValue,
                        rangeEndDateValue, true , dtSet);
            }
        }
        if (recur.rdates != null) {
            for (long dt : recur.rdates) {
                mIterator.set(dt);
                long dtvalue = normDateTimeComparisonValue(mIterator);
                dtSet.add(dtvalue);
            }
        }
        if (recur.exrules != null) {
            for (EventRecurrence exrule : recur.exrules) {
                expand(dtstart, exrule, rangeStartDateValue,
                        rangeEndDateValue, false , dtSet);
            }
        }
        if (recur.exdates != null) {
            for (long dt : recur.exdates) {
                mIterator.set(dt);
                long dtvalue = normDateTimeComparisonValue(mIterator);
                dtSet.remove(dtvalue);
            }
        }
        if (dtSet.isEmpty()) {
            return new long[0];
        }
        int len = dtSet.size();
        long[] dates = new long[len];
        int i = 0;
        for (Long val: dtSet) {
            setTimeFromLongValue(mIterator, val);
            dates[i++] = mIterator.toMillis(true );
        }
        return dates;
    }
    public void expand(Time dtstart,
            EventRecurrence r,
            long rangeStartDateValue,
            long rangeEndDateValue,
            boolean add,
            TreeSet<Long> out) throws DateException {
        unsafeNormalize(dtstart);
        long dtstartDateValue = normDateTimeComparisonValue(dtstart);
        int count = 0;
        if (add && dtstartDateValue >= rangeStartDateValue
                && dtstartDateValue < rangeEndDateValue) {
            out.add(dtstartDateValue);
            ++count;
        }
        Time iterator = mIterator;
        Time until = mUntil;
        StringBuilder sb = mStringBuilder;
        Time generated = mGenerated;
        DaySet days = mDays;
        try {
            days.setRecurrence(r);
            if (rangeEndDateValue == Long.MAX_VALUE && r.until == null && r.count == 0) {
                throw new DateException(
                        "No range end provided for a recurrence that has no UNTIL or COUNT.");
            }
            int freqField;
            int freqAmount = r.interval;
            int freq = r.freq;
            switch (freq)
            {
                case EventRecurrence.SECONDLY:
                    freqField = Time.SECOND;
                    break;
                case EventRecurrence.MINUTELY:
                    freqField = Time.MINUTE;
                    break;
                case EventRecurrence.HOURLY:
                    freqField = Time.HOUR;
                    break;
                case EventRecurrence.DAILY:
                    freqField = Time.MONTH_DAY;
                    break;
                case EventRecurrence.WEEKLY:
                    freqField = Time.MONTH_DAY;
                    freqAmount = 7 * r.interval;
                    if (freqAmount <= 0) {
                        freqAmount = 7;
                    }
                    break;
                case EventRecurrence.MONTHLY:
                    freqField = Time.MONTH;
                    break;
                case EventRecurrence.YEARLY:
                    freqField = Time.YEAR;
                    break;
                default:
                    throw new DateException("bad freq=" + freq);
            }
            if (freqAmount <= 0) {
                freqAmount = 1;
            }
            int bymonthCount = r.bymonthCount;
            boolean usebymonth = useBYX(freq, EventRecurrence.MONTHLY, bymonthCount);
            boolean useDays = freq >= EventRecurrence.WEEKLY &&
                                 (r.bydayCount > 0 || r.bymonthdayCount > 0);
            int byhourCount = r.byhourCount;
            boolean usebyhour = useBYX(freq, EventRecurrence.HOURLY, byhourCount);
            int byminuteCount = r.byminuteCount;
            boolean usebyminute = useBYX(freq, EventRecurrence.MINUTELY, byminuteCount);
            int bysecondCount = r.bysecondCount;
            boolean usebysecond = useBYX(freq, EventRecurrence.SECONDLY, bysecondCount);
            iterator.set(dtstart);
            if (freqField == Time.MONTH) {
                if (useDays) {
                    iterator.monthDay = 1;
                }
            }
            long untilDateValue;
            if (r.until != null) {
                String untilStr = r.until;
                if (untilStr.length() == 15) {
                    untilStr = untilStr + 'Z';
                }
                until.parse(untilStr);
                until.switchTimezone(dtstart.timezone);
                untilDateValue = normDateTimeComparisonValue(until);
            } else {
                untilDateValue = Long.MAX_VALUE;
            }
            sb.ensureCapacity(15);
            sb.setLength(15); 
            if (SPEW) {
                Log.i(TAG, "expand called w/ rangeStart=" + rangeStartDateValue
                        + " rangeEnd=" + rangeEndDateValue);
            }
            boolean eventEnded = false;
            int failsafe = 0; 
            events: {
                while (true) {
                    int monthIndex = 0;
                    if (failsafe++ > MAX_ALLOWED_ITERATIONS) { 
                        throw new DateException("Recurrence processing stuck: " + r.toString());
                    }
                    unsafeNormalize(iterator);
                    int iteratorYear = iterator.year;
                    int iteratorMonth = iterator.month + 1;
                    int iteratorDay = iterator.monthDay;
                    int iteratorHour = iterator.hour;
                    int iteratorMinute = iterator.minute;
                    int iteratorSecond = iterator.second;
                    generated.set(iterator);
                    if (SPEW) Log.i(TAG, "year=" + generated.year);
                    do { 
                        int month = usebymonth
                                        ? r.bymonth[monthIndex]
                                        : iteratorMonth;
                        month--;
                        if (SPEW) Log.i(TAG, "  month=" + month);
                        int dayIndex = 1;
                        int lastDayToExamine = 0;
                        if (useDays) {
                            if (freq == EventRecurrence.WEEKLY) {
                                int dow = iterator.weekDay;
                                dayIndex = iterator.monthDay - dow;
                                lastDayToExamine = dayIndex + 6;
                            } else {
                                lastDayToExamine = generated
                                    .getActualMaximum(Time.MONTH_DAY);
                            }
                            if (SPEW) Log.i(TAG, "dayIndex=" + dayIndex
                                    + " lastDayToExamine=" + lastDayToExamine
                                    + " days=" + days);
                        }
                        do { 
                            int day;
                            if (useDays) {
                                if (!days.get(iterator, dayIndex)) {
                                    dayIndex++;
                                    continue;
                                } else {
                                    day = dayIndex;
                                }
                            } else {
                                day = iteratorDay;
                            }
                            if (SPEW) Log.i(TAG, "    day=" + day);
                            int hourIndex = 0;
                            do {
                                int hour = usebyhour
                                                ? r.byhour[hourIndex]
                                                : iteratorHour;
                                if (SPEW) Log.i(TAG, "      hour=" + hour + " usebyhour=" + usebyhour);
                                int minuteIndex = 0;
                                do {
                                    int minute = usebyminute
                                                    ? r.byminute[minuteIndex]
                                                    : iteratorMinute;
                                    if (SPEW) Log.i(TAG, "        minute=" + minute);
                                    int secondIndex = 0;
                                    do {
                                        int second = usebysecond
                                                        ? r.bysecond[secondIndex]
                                                        : iteratorSecond;
                                        if (SPEW) Log.i(TAG, "          second=" + second);
                                        generated.set(second, minute, hour, day, month, iteratorYear);
                                        unsafeNormalize(generated);
                                        long genDateValue = normDateTimeComparisonValue(generated);
                                        if (genDateValue >= dtstartDateValue) {
                                            int filtered = filter(r, generated);
                                            if (0 == filtered) {
                                                if (!(dtstartDateValue == genDateValue
                                                        && add
                                                        && dtstartDateValue >= rangeStartDateValue
                                                        && dtstartDateValue < rangeEndDateValue)) {
                                                    ++count;
                                                }
                                                if (genDateValue > untilDateValue) {
                                                    if (SPEW) {
                                                        Log.i(TAG, "stopping b/c until="
                                                            + untilDateValue
                                                            + " generated="
                                                            + genDateValue);
                                                    }
                                                    break events;
                                                }
                                                if (genDateValue >= rangeEndDateValue) {
                                                    if (SPEW) {
                                                        Log.i(TAG, "stopping b/c rangeEnd="
                                                                + rangeEndDateValue
                                                                + " generated=" + generated);
                                                    }
                                                    break events;
                                                }
                                                if (genDateValue >= rangeStartDateValue) {
                                                    if (SPEW) {
                                                        Log.i(TAG, "adding date=" + generated + " filtered=" + filtered);
                                                    }
                                                    if (add) {
                                                        out.add(genDateValue);
                                                    } else {
                                                        out.remove(genDateValue);
                                                    }
                                                }
                                                if (r.count > 0 && r.count == count) {
                                                    break events;
                                                }
                                            }
                                        }
                                        secondIndex++;
                                    } while (usebysecond && secondIndex < bysecondCount);
                                    minuteIndex++;
                                } while (usebyminute && minuteIndex < byminuteCount);
                                hourIndex++;
                            } while (usebyhour && hourIndex < byhourCount);
                            dayIndex++;
                        } while (useDays && dayIndex <= lastDayToExamine);
                        monthIndex++;
                    } while (usebymonth && monthIndex < bymonthCount);
                    int oldDay = iterator.monthDay;
                    generated.set(iterator);  
                    int n = 1;
                    while (true) {
                        int value = freqAmount * n;
                        switch (freqField) {
                            case Time.SECOND:
                                iterator.second += value;
                                break;
                            case Time.MINUTE:
                                iterator.minute += value;
                                break;
                            case Time.HOUR:
                                iterator.hour += value;
                                break;
                            case Time.MONTH_DAY:
                                iterator.monthDay += value;
                                break;
                            case Time.MONTH:
                                iterator.month += value;
                                break;
                            case Time.YEAR:
                                iterator.year += value;
                                break;
                            case Time.WEEK_DAY:
                                iterator.monthDay += value;
                                break;
                            case Time.YEAR_DAY:
                                iterator.monthDay += value;
                                break;
                            default:
                                throw new RuntimeException("bad field=" + freqField);
                        }
                        unsafeNormalize(iterator);
                        if (freqField != Time.YEAR && freqField != Time.MONTH) {
                            break;
                        }
                        if (iterator.monthDay == oldDay) {
                            break;
                        }
                        n++;
                        iterator.set(generated);
                    }
                }
            }
        }
        catch (DateException e) {
            Log.w(TAG, "DateException with r=" + r + " rangeStart=" + rangeStartDateValue
                    + " rangeEnd=" + rangeEndDateValue);
            throw e;
        }
        catch (RuntimeException t) {
            Log.w(TAG, "RuntimeException with r=" + r + " rangeStart=" + rangeStartDateValue
                    + " rangeEnd=" + rangeEndDateValue);
            throw t;
        }
    }
    static void unsafeNormalize(Time date) {
        int second = date.second;
        int minute = date.minute;
        int hour = date.hour;
        int monthDay = date.monthDay;
        int month = date.month;
        int year = date.year;
        int addMinutes = ((second < 0) ? (second - 59) : second) / 60;
        second -= addMinutes * 60;
        minute += addMinutes;
        int addHours = ((minute < 0) ? (minute - 59) : minute) / 60;
        minute -= addHours * 60;
        hour += addHours;
        int addDays = ((hour < 0) ? (hour - 23) : hour) / 24;
        hour -= addDays * 24;
        monthDay += addDays;
        while (monthDay <= 0) {
            int days = month > 1 ? yearLength(year) : yearLength(year - 1);
            monthDay += days;
            year -= 1;
        }
        if (month < 0) {
            int years = (month + 1) / 12 - 1;
            year += years;
            month -= 12 * years;
        } else if (month >= 12) {
            int years = month / 12;
            year += years;
            month -= 12 * years;
        }
        while (true) {
            if (month == 0) {
                int yearLength = yearLength(year);
                if (monthDay > yearLength) {
                    year++;
                    monthDay -= yearLength;
                }
            }
            int monthLength = monthLength(year, month);
            if (monthDay > monthLength) {
                monthDay -= monthLength;
                month++;
                if (month >= 12) {
                    month -= 12;
                    year++;
                }
            } else break;
        }
        date.second = second;
        date.minute = minute;
        date.hour = hour;
        date.monthDay = monthDay;
        date.month = month;
        date.year = year;
        date.weekDay = weekDay(year, month, monthDay);
        date.yearDay = yearDay(year, month, monthDay);
    }
    static boolean isLeapYear(int year) {
        return (year % 4 == 0) && ((year % 100 != 0) || (year % 400 == 0));
    }
    static int yearLength(int year) {
        return isLeapYear(year) ? 366 : 365;
    }
    private static final int[] DAYS_PER_MONTH = { 31, 28, 31, 30, 31, 30, 31,
            31, 30, 31, 30, 31 };
    private static final int[] DAYS_IN_YEAR_PRECEDING_MONTH = { 0, 31, 59, 90,
        120, 151, 180, 212, 243, 273, 304, 334 };
    static int monthLength(int year, int month) {
        int n = DAYS_PER_MONTH[month];
        if (n != 28) {
            return n;
        }
        return isLeapYear(year) ? 29 : 28;
    }
    static int weekDay(int year, int month, int day) {
        if (month <= 1) {
            month += 12;
            year -= 1;
        }
        return (day + (13 * month - 14) / 5 + year + year/4 - year/100 + year/400) % 7;
    }
    static int yearDay(int year, int month, int day) {
        int yearDay = DAYS_IN_YEAR_PRECEDING_MONTH[month] + day - 1;
        if (month >= 2 && isLeapYear(year)) {
            yearDay += 1;
        }
        return yearDay;
    }
    private static final long normDateTimeComparisonValue(Time normalized) {
        return ((long)normalized.year << 26) + (normalized.month << 22)
                + (normalized.monthDay << 17) + (normalized.hour << 12)
                + (normalized.minute << 6) + normalized.second;
    }
    private static final void setTimeFromLongValue(Time date, long val) {
        date.year = (int) (val >> 26);
        date.month = (int) (val >> 22) & 0xf;
        date.monthDay = (int) (val >> 17) & 0x1f;
        date.hour = (int) (val >> 12) & 0x1f;
        date.minute = (int) (val >> 6) & 0x3f;
        date.second = (int) (val & 0x3f);
    }
}

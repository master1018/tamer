public class CalendarUtilities {
    private static final String TAG = "CalendarUtility";
    static final int SECONDS = 1000;
    static final int MINUTES = SECONDS*60;
    static final int HOURS = MINUTES*60;
    static final long DAYS = HOURS*24;
    static final int MSFT_LONG_SIZE = 4;
    static final int MSFT_WCHAR_SIZE = 2;
    static final int MSFT_WORD_SIZE = 2;
    static final int MSFT_SYSTEMTIME_YEAR = 0 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_MONTH = 1 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_DAY_OF_WEEK = 2 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_DAY = 3 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_HOUR = 4 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_MINUTE = 5 * MSFT_WORD_SIZE;
    static final int MSFT_SYSTEMTIME_SIZE = 8*MSFT_WORD_SIZE;
    static final int MSFT_TIME_ZONE_BIAS_OFFSET = 0;
    static final int MSFT_TIME_ZONE_STANDARD_NAME_OFFSET =
        MSFT_TIME_ZONE_BIAS_OFFSET + MSFT_LONG_SIZE;
    static final int MSFT_TIME_ZONE_STANDARD_DATE_OFFSET =
        MSFT_TIME_ZONE_STANDARD_NAME_OFFSET + (MSFT_WCHAR_SIZE*32);
    static final int MSFT_TIME_ZONE_STANDARD_BIAS_OFFSET =
        MSFT_TIME_ZONE_STANDARD_DATE_OFFSET + MSFT_SYSTEMTIME_SIZE;
    static final int MSFT_TIME_ZONE_DAYLIGHT_NAME_OFFSET =
        MSFT_TIME_ZONE_STANDARD_BIAS_OFFSET + MSFT_LONG_SIZE;
    static final int MSFT_TIME_ZONE_DAYLIGHT_DATE_OFFSET =
        MSFT_TIME_ZONE_DAYLIGHT_NAME_OFFSET + (MSFT_WCHAR_SIZE*32);
    static final int MSFT_TIME_ZONE_DAYLIGHT_BIAS_OFFSET =
        MSFT_TIME_ZONE_DAYLIGHT_DATE_OFFSET + MSFT_SYSTEMTIME_SIZE;
    static final int MSFT_TIME_ZONE_SIZE =
        MSFT_TIME_ZONE_DAYLIGHT_BIAS_OFFSET + MSFT_LONG_SIZE;
    private static HashMap<String, TimeZone> sTimeZoneCache = new HashMap<String, TimeZone>();
    private static HashMap<TimeZone, String> sTziStringCache = new HashMap<TimeZone, String>();
    private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
    static final String[] sTypeToFreq =
        new String[] {"DAILY", "WEEKLY", "MONTHLY", "MONTHLY", "", "YEARLY", "YEARLY"};
    static final String[] sDayTokens =
        new String[] {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};
    static final String[] sTwoCharacterNumbers =
        new String[] {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    static final int sCurrentYear = new GregorianCalendar().get(Calendar.YEAR);
    static final TimeZone sGmtTimeZone = TimeZone.getTimeZone("GMT");
    private static final String ICALENDAR_ATTENDEE = "ATTENDEE;ROLE=REQ-PARTICIPANT";
    static final String ICALENDAR_ATTENDEE_CANCEL = ICALENDAR_ATTENDEE;
    static final String ICALENDAR_ATTENDEE_INVITE =
        ICALENDAR_ATTENDEE + ";PARTSTAT=NEEDS-ACTION;RSVP=TRUE";
    static final String ICALENDAR_ATTENDEE_ACCEPT =
        ICALENDAR_ATTENDEE + ";PARTSTAT=ACCEPTED";
    static final String ICALENDAR_ATTENDEE_DECLINE =
        ICALENDAR_ATTENDEE + ";PARTSTAT=DECLINED";
    static final String ICALENDAR_ATTENDEE_TENTATIVE =
        ICALENDAR_ATTENDEE + ";PARTSTAT=TENTATIVE";
    public static final int BUSY_STATUS_FREE = 0;
    public static final int BUSY_STATUS_TENTATIVE = 1;
    public static final int BUSY_STATUS_BUSY = 2;
    public static final int BUSY_STATUS_OUT_OF_OFFICE = 3;
    static int getLong(byte[] bytes, int offset) {
        return (bytes[offset++] & 0xFF) | ((bytes[offset++] & 0xFF) << 8) |
        ((bytes[offset++] & 0xFF) << 16) | ((bytes[offset] & 0xFF) << 24);
    }
    static void setLong(byte[] bytes, int offset, int value) {
        bytes[offset++] = (byte) (value & 0xFF);
        bytes[offset++] = (byte) ((value >> 8) & 0xFF);
        bytes[offset++] = (byte) ((value >> 16) & 0xFF);
        bytes[offset] = (byte) ((value >> 24) & 0xFF);
    }
    static int getWord(byte[] bytes, int offset) {
        return (bytes[offset++] & 0xFF) | ((bytes[offset] & 0xFF) << 8);
    }
    static void setWord(byte[] bytes, int offset, int value) {
        bytes[offset++] = (byte) (value & 0xFF);
        bytes[offset] = (byte) ((value >> 8) & 0xFF);
    }
    static class TimeZoneDate {
        String year;
        int month;
        int dayOfWeek;
        int day;
        int time;
        int hour;
        int minute;
    }
    static void putRuleIntoTimeZoneInformation(byte[] bytes, int offset, RRule rrule, int hour,
            int minute) {
        setWord(bytes, offset + MSFT_SYSTEMTIME_MONTH, rrule.month);
        setWord(bytes, offset + MSFT_SYSTEMTIME_DAY_OF_WEEK, rrule.dayOfWeek - 1);
        setWord(bytes, offset + MSFT_SYSTEMTIME_DAY, rrule.week < 0 ? 5 : rrule.week);
        setWord(bytes, offset + MSFT_SYSTEMTIME_HOUR, hour);
        setWord(bytes, offset + MSFT_SYSTEMTIME_MINUTE, minute);
    }
    static void putTransitionMillisIntoSystemTime(byte[] bytes, int offset, long millis) {
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTimeInMillis(millis + 30*SECONDS);
        setWord(bytes, offset + MSFT_SYSTEMTIME_MONTH, cal.get(Calendar.MONTH) + 1);
        setWord(bytes, offset + MSFT_SYSTEMTIME_DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) - 1);
        int wom = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        setWord(bytes, offset + MSFT_SYSTEMTIME_DAY, wom < 0 ? 5 : wom);
        setWord(bytes, offset + MSFT_SYSTEMTIME_HOUR, getTrueTransitionHour(cal));
        setWord(bytes, offset + MSFT_SYSTEMTIME_MINUTE, getTrueTransitionMinute(cal));
     }
    static TimeZoneDate getTimeZoneDateFromSystemTime(byte[] bytes, int offset) {
        TimeZoneDate tzd = new TimeZoneDate();
        int num = getWord(bytes, offset + MSFT_SYSTEMTIME_YEAR);
        tzd.year = Integer.toString(num);
        num = getWord(bytes, offset + MSFT_SYSTEMTIME_MONTH);
        if (num == 0) {
            return null;
        } else {
            tzd.month = num -1;
        }
        tzd.dayOfWeek = getWord(bytes, offset + MSFT_SYSTEMTIME_DAY_OF_WEEK) + 1;
        num = getWord(bytes, offset + MSFT_SYSTEMTIME_DAY);
        if (num == 5) {
            tzd.day = -1;
        } else {
            tzd.day = num;
        }
        int hour = getWord(bytes, offset + MSFT_SYSTEMTIME_HOUR);
        tzd.hour = hour;
        int minute = getWord(bytes, offset + MSFT_SYSTEMTIME_MINUTE);
        tzd.minute = minute;
        tzd.time = (hour*HOURS) + (minute*MINUTES);
        return tzd;
    }
    static long getMillisAtTimeZoneDateTransition(TimeZone timeZone, TimeZoneDate tzd) {
        GregorianCalendar testCalendar = new GregorianCalendar(timeZone);
        testCalendar.set(GregorianCalendar.YEAR, sCurrentYear);
        testCalendar.set(GregorianCalendar.MONTH, tzd.month);
        testCalendar.set(GregorianCalendar.DAY_OF_WEEK, tzd.dayOfWeek);
        testCalendar.set(GregorianCalendar.DAY_OF_WEEK_IN_MONTH, tzd.day);
        testCalendar.set(GregorianCalendar.HOUR_OF_DAY, tzd.hour);
        testCalendar.set(GregorianCalendar.MINUTE, tzd.minute);
        testCalendar.set(GregorianCalendar.SECOND, 0);
        return testCalendar.getTimeInMillis();
    }
    static GregorianCalendar findTransitionDate(TimeZone tz, long startTime,
            long endTime, boolean startInDaylightTime) {
        long startingEndTime = endTime;
        Date date = null;
        while ((endTime - startTime) > MINUTES) {
            long checkTime = ((startTime + endTime) / 2) + 1;
            date = new Date(checkTime);
            boolean inDaylightTime = tz.inDaylightTime(date);
            if (inDaylightTime != startInDaylightTime) {
                endTime = checkTime;
            } else {
                startTime = checkTime;
            }
        }
        if (endTime == startingEndTime) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar(tz);
        calendar.setTimeInMillis(startTime);
        return calendar;
    }
    static public String timeZoneToTziString(TimeZone tz) {
        String tziString = sTziStringCache.get(tz);
        if (tziString != null) {
            if (Eas.USER_LOG) {
                SyncManager.log(TAG, "TZI string for " + tz.getDisplayName() + " found in cache.");
            }
            return tziString;
        }
        tziString = timeZoneToTziStringImpl(tz);
        sTziStringCache.put(tz, tziString);
        return tziString;
    }
    static class RRule {
        static final int RRULE_NONE = 0;
        static final int RRULE_DAY_WEEK = 1;
        static final int RRULE_DATE = 2;
        int type;
        int dayOfWeek;
        int week;
        int month;
        int date;
        RRule(int _month, int _date) {
            type = RRULE_DATE;
            month = _month;
            date = _date;
        }
        RRule(int _month, int _dayOfWeek, int _week) {
            type = RRULE_DAY_WEEK;
            month = _month;
            dayOfWeek = _dayOfWeek;
            week = _week;
        }
        @Override
        public String toString() {
            if (type == RRULE_DAY_WEEK) {
                return "FREQ=YEARLY;BYMONTH=" + month + ";BYDAY=" + week +
                    sDayTokens[dayOfWeek - 1];
            } else {
                return "FREQ=YEARLY;BYMONTH=" + month + ";BYMONTHDAY=" + date;
            }
       }
    }
    static RRule inferRRuleFromCalendars(GregorianCalendar[] calendars) {
        GregorianCalendar calendar = calendars[0];
        if (calendar == null) return null;
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int week = calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
        int maxWeek = calendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH);
        boolean dateRule = false;
        boolean dayOfWeekRule = false;
        for (int i = 1; i < calendars.length; i++) {
            GregorianCalendar cal = calendars[i];
            if (cal == null) return null;
            if (cal.get(Calendar.MONTH) != month) {
                return null;
            } else if (dayOfWeek == cal.get(Calendar.DAY_OF_WEEK)) {
                if (dateRule) {
                    return null;
                }
                dayOfWeekRule = true;
                int thisWeek = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (week != thisWeek) {
                    if (week < 0 || week == maxWeek) {
                        int thisMaxWeek = cal.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH);
                        if (thisWeek == thisMaxWeek) {
                            week = -1;
                            continue;
                        }
                    }
                    return null;
                }
            } else if (date == cal.get(Calendar.DAY_OF_MONTH)) {
                if (dayOfWeekRule) {
                    return null;
                }
                dateRule = true;
            } else {
                return null;
            }
        }
        if (dateRule) {
            return new RRule(month + 1, date);
        }
        return new RRule(month + 1, dayOfWeek, week);
    }
    static String utcOffsetString(int offsetMinutes) {
        StringBuilder sb = new StringBuilder();
        int hours = offsetMinutes / 60;
        if (hours < 0) {
            sb.append('-');
            hours = 0 - hours;
        } else {
            sb.append('+');
        }
        int minutes = offsetMinutes % 60;
        if (hours < 10) {
            sb.append('0');
        }
        sb.append(hours);
        if (minutes < 10) {
            sb.append('0');
        }
        sb.append(minutes);
        return sb.toString();
    }
    static boolean getDSTCalendars(TimeZone tz, GregorianCalendar[] toDaylightCalendars,
            GregorianCalendar[] toStandardCalendars) {
        int maxYears = toDaylightCalendars.length;
        if (toStandardCalendars.length != maxYears) {
            return false;
        }
        for (int i = 0; i < maxYears; i++) {
            GregorianCalendar cal = new GregorianCalendar(tz);
            cal.set(sCurrentYear + i, Calendar.JANUARY, 1, 0, 0, 0);
            long startTime = cal.getTimeInMillis();
            long endOfYearTime = startTime + (365*DAYS) + (DAYS>>2);
            Date date = new Date(startTime);
            boolean startInDaylightTime = tz.inDaylightTime(date);
            cal = findTransitionDate(tz, startTime, endOfYearTime, startInDaylightTime);
            if (cal == null) {
                return false;
            } else if (startInDaylightTime) {
                toStandardCalendars[i] = cal;
            } else {
                toDaylightCalendars[i] = cal;
            }
            cal = findTransitionDate(tz, startTime, endOfYearTime, !startInDaylightTime);
            if (cal == null) {
                return false;
            } else if (startInDaylightTime) {
                toDaylightCalendars[i] = cal;
            } else {
                toStandardCalendars[i] = cal;
            }
        }
        return true;
    }
    static private void writeNoDST(SimpleIcsWriter writer, TimeZone tz, String offsetString)
            throws IOException {
        writer.writeTag("BEGIN", "STANDARD");
        writer.writeTag("TZOFFSETFROM", offsetString);
        writer.writeTag("TZOFFSETTO", offsetString);
        writer.writeTag("DTSTART", millisToEasDateTime(0L));
        writer.writeTag("END", "STANDARD");
        writer.writeTag("END", "VTIMEZONE");
    }
    static void timeZoneToVTimezone(TimeZone tz, SimpleIcsWriter writer)
            throws IOException {
        int rawOffsetMinutes = tz.getRawOffset() / MINUTES;
        String standardOffsetString = utcOffsetString(rawOffsetMinutes);
        writer.writeTag("BEGIN", "VTIMEZONE");
        writer.writeTag("TZID", tz.getID());
        writer.writeTag("X-LIC-LOCATION", tz.getDisplayName());
        if (!tz.useDaylightTime()) {
            writeNoDST(writer, tz, standardOffsetString);
            return;
        }
        int maxYears = 3;
        GregorianCalendar[] toDaylightCalendars = new GregorianCalendar[maxYears];
        GregorianCalendar[] toStandardCalendars = new GregorianCalendar[maxYears];
        if (!getDSTCalendars(tz, toDaylightCalendars, toStandardCalendars)) {
            writeNoDST(writer, tz, standardOffsetString);
            return;
        }
        RRule daylightRule = inferRRuleFromCalendars(toDaylightCalendars);
        RRule standardRule = inferRRuleFromCalendars(toStandardCalendars);
        String daylightOffsetString =
            utcOffsetString(rawOffsetMinutes + (tz.getDSTSavings() / MINUTES));
        boolean hasRule = daylightRule != null && standardRule != null;
        writer.writeTag("BEGIN", "DAYLIGHT");
        writer.writeTag("TZOFFSETFROM", standardOffsetString);
        writer.writeTag("TZOFFSETTO", daylightOffsetString);
        writer.writeTag("DTSTART",
                transitionMillisToVCalendarTime(
                        toDaylightCalendars[0].getTimeInMillis(), tz, true));
        if (hasRule) {
            writer.writeTag("RRULE", daylightRule.toString());
        } else {
            for (int i = 1; i < maxYears; i++) {
                writer.writeTag("RDATE", transitionMillisToVCalendarTime(
                        toDaylightCalendars[i].getTimeInMillis(), tz, true));
            }
        }
        writer.writeTag("END", "DAYLIGHT");
        writer.writeTag("BEGIN", "STANDARD");
        writer.writeTag("TZOFFSETFROM", daylightOffsetString);
        writer.writeTag("TZOFFSETTO", standardOffsetString);
        writer.writeTag("DTSTART",
                transitionMillisToVCalendarTime(
                        toStandardCalendars[0].getTimeInMillis(), tz, false));
        if (hasRule) {
            writer.writeTag("RRULE", standardRule.toString());
        } else {
            for (int i = 1; i < maxYears; i++) {
                writer.writeTag("RDATE", transitionMillisToVCalendarTime(
                        toStandardCalendars[i].getTimeInMillis(), tz, true));
            }
        }
        writer.writeTag("END", "STANDARD");
        writer.writeTag("END", "VTIMEZONE");
    }
    static long findNextTransition(long startingMillis, GregorianCalendar[] transitions) {
        for (GregorianCalendar transition: transitions) {
            long transitionMillis = transition.getTimeInMillis();
            if (transitionMillis > startingMillis) {
                return transitionMillis;
            }
        }
        return 0;
    }
    static String timeZoneToTziStringImpl(TimeZone tz) {
        String tziString;
        byte[] tziBytes = new byte[MSFT_TIME_ZONE_SIZE];
        int standardBias = - tz.getRawOffset();
        standardBias /= 60*SECONDS;
        setLong(tziBytes, MSFT_TIME_ZONE_BIAS_OFFSET, standardBias);
        if (tz.useDaylightTime()) {
            GregorianCalendar[] toDaylightCalendars = new GregorianCalendar[3];
            GregorianCalendar[] toStandardCalendars = new GregorianCalendar[3];
            if (getDSTCalendars(tz, toDaylightCalendars, toStandardCalendars)) {
                RRule daylightRule = inferRRuleFromCalendars(toDaylightCalendars);
                RRule standardRule = inferRRuleFromCalendars(toStandardCalendars);
                if ((daylightRule != null) && (daylightRule.type == RRule.RRULE_DAY_WEEK) &&
                        (standardRule != null) && (standardRule.type == RRule.RRULE_DAY_WEEK)) {
                    putRuleIntoTimeZoneInformation(tziBytes, MSFT_TIME_ZONE_STANDARD_DATE_OFFSET,
                            standardRule,
                            getTrueTransitionHour(toStandardCalendars[0]),
                            getTrueTransitionMinute(toStandardCalendars[0]));
                    putRuleIntoTimeZoneInformation(tziBytes, MSFT_TIME_ZONE_DAYLIGHT_DATE_OFFSET,
                            daylightRule,
                            getTrueTransitionHour(toDaylightCalendars[0]),
                            getTrueTransitionMinute(toDaylightCalendars[0]));
                } else {
                    long now = System.currentTimeMillis();
                    long standardTransition = findNextTransition(now, toStandardCalendars);
                    long daylightTransition = findNextTransition(now, toDaylightCalendars);
                    if (standardTransition != 0 && daylightTransition != 0) {
                        putTransitionMillisIntoSystemTime(tziBytes,
                                MSFT_TIME_ZONE_STANDARD_DATE_OFFSET, standardTransition);
                        putTransitionMillisIntoSystemTime(tziBytes,
                                MSFT_TIME_ZONE_DAYLIGHT_DATE_OFFSET, daylightTransition);
                    }
                }
            }
            int dstOffset = tz.getDSTSavings();
            setLong(tziBytes, MSFT_TIME_ZONE_DAYLIGHT_BIAS_OFFSET, - dstOffset / MINUTES);
        }
        byte[] tziEncodedBytes = Base64.encode(tziBytes, Base64.NO_WRAP);
        tziString = new String(tziEncodedBytes);
        return tziString;
    }
    static public TimeZone tziStringToTimeZone(String timeZoneString) {
        TimeZone timeZone = sTimeZoneCache.get(timeZoneString);
        if (timeZone != null) {
            if (Eas.USER_LOG) {
                SyncManager.log(TAG, " Using cached TimeZone " + timeZone.getDisplayName());
            }
        } else {
            timeZone = tziStringToTimeZoneImpl(timeZoneString);
            if (timeZone == null) {
                SyncManager.alwaysLog("TimeZone not found using default: " + timeZoneString);
                timeZone = TimeZone.getDefault();
            }
            sTimeZoneCache.put(timeZoneString, timeZone);
        }
        return timeZone;
    }
    static TimeZone tziStringToTimeZoneImpl(String timeZoneString) {
        TimeZone timeZone = null;
        byte[] timeZoneBytes = Base64.decode(timeZoneString, Base64.DEFAULT);
        int bias = -1 * getLong(timeZoneBytes, MSFT_TIME_ZONE_BIAS_OFFSET) * MINUTES;
        String[] zoneIds = TimeZone.getAvailableIDs(bias);
        if (zoneIds.length > 0) {
            TimeZoneDate dstEnd =
                getTimeZoneDateFromSystemTime(timeZoneBytes, MSFT_TIME_ZONE_STANDARD_DATE_OFFSET);
            if (dstEnd == null) {
                timeZone = TimeZone.getTimeZone(zoneIds[0]);
                if (Eas.USER_LOG) {
                    SyncManager.log(TAG, "TimeZone without DST found by offset: " +
                            timeZone.getDisplayName());
                }
                return timeZone;
            } else {
                TimeZoneDate dstStart = getTimeZoneDateFromSystemTime(timeZoneBytes,
                        MSFT_TIME_ZONE_DAYLIGHT_DATE_OFFSET);
                long dstSavings =
                    -1 * getLong(timeZoneBytes, MSFT_TIME_ZONE_DAYLIGHT_BIAS_OFFSET) * MINUTES;
                for (String zoneId: zoneIds) {
                    timeZone = TimeZone.getTimeZone(zoneId);
                    long millisAtTransition = getMillisAtTimeZoneDateTransition(timeZone, dstStart);
                    Date before = new Date(millisAtTransition - MINUTES);
                    Date after = new Date(millisAtTransition + MINUTES);
                    if (timeZone.inDaylightTime(before)) continue;
                    if (!timeZone.inDaylightTime(after)) continue;
                    millisAtTransition = getMillisAtTimeZoneDateTransition(timeZone, dstEnd);
                    before = new Date(millisAtTransition - (dstSavings + MINUTES));
                    after = new Date(millisAtTransition + MINUTES);
                    if (!timeZone.inDaylightTime(before)) continue;
                    if (timeZone.inDaylightTime(after)) continue;
                    if (dstSavings != timeZone.getDSTSavings()) continue;
                    return timeZone;
                }
                timeZone = TimeZone.getTimeZone(zoneIds[0]);
                if (Eas.USER_LOG) {
                    SyncManager.log(TAG, "No TimeZone with correct DST settings; using first: " +
                            timeZone.getDisplayName());
                }
                return timeZone;
            }
        }
        return null;
    }
    static public String convertEmailDateTimeToCalendarDateTime(String date) {
       return date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 13) +
           date.substring(14, 16) + date.substring(17, 19) + 'Z';
    }
    static String formatTwo(int num) {
        if (num <= 12) {
            return sTwoCharacterNumbers[num];
        } else
            return Integer.toString(num);
    }
    static public String millisToEasDateTime(long millis) {
        return millisToEasDateTime(millis, sGmtTimeZone, true);
    }
    static public String millisToEasDateTime(long millis, TimeZone tz, boolean withTime) {
        StringBuilder sb = new StringBuilder();
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTimeInMillis(millis);
        sb.append(cal.get(Calendar.YEAR));
        sb.append(formatTwo(cal.get(Calendar.MONTH) + 1));
        sb.append(formatTwo(cal.get(Calendar.DAY_OF_MONTH)));
        if (withTime) {
            sb.append('T');
            sb.append(formatTwo(cal.get(Calendar.HOUR_OF_DAY)));
            sb.append(formatTwo(cal.get(Calendar.MINUTE)));
            sb.append(formatTwo(cal.get(Calendar.SECOND)));
            if (tz == sGmtTimeZone) {
                sb.append('Z');
            }
        }
        return sb.toString();
    }
    static int getTrueTransitionMinute(GregorianCalendar calendar) {
        int minute = calendar.get(Calendar.MINUTE);
        if (minute == 59) {
            minute = 0;
        }
        return minute;
    }
    static int getTrueTransitionHour(GregorianCalendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        hour++;
        if (hour == 24) {
            hour = 0;
        }
        return hour;
    }
    static String transitionMillisToVCalendarTime(long millis, TimeZone tz, boolean dst) {
        StringBuilder sb = new StringBuilder();
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTimeInMillis(millis);
        sb.append(cal.get(Calendar.YEAR));
        sb.append(formatTwo(cal.get(Calendar.MONTH) + 1));
        sb.append(formatTwo(cal.get(Calendar.DAY_OF_MONTH)));
        sb.append('T');
        sb.append(formatTwo(getTrueTransitionHour(cal)));
        sb.append(formatTwo(getTrueTransitionMinute(cal)));
        sb.append(formatTwo(0));
        return sb.toString();
    }
    static public long getUtcAllDayCalendarTime(long time, TimeZone localTimeZone) {
        return transposeAllDayTime(time, localTimeZone, UTC_TIMEZONE);
    }
    static public long getLocalAllDayCalendarTime(long time, TimeZone localTimeZone) {
        return transposeAllDayTime(time, UTC_TIMEZONE, localTimeZone);
    }
    static private long transposeAllDayTime(long time, TimeZone fromTimeZone,
            TimeZone toTimeZone) {
        GregorianCalendar fromCalendar = new GregorianCalendar(fromTimeZone);
        fromCalendar.setTimeInMillis(time);
        GregorianCalendar toCalendar = new GregorianCalendar(toTimeZone);
        toCalendar.set(fromCalendar.get(GregorianCalendar.YEAR),
                fromCalendar.get(GregorianCalendar.MONTH),
                fromCalendar.get(GregorianCalendar.DATE), 0, 0, 0);
        return toCalendar.getTimeInMillis();
    }
    static void addByDay(StringBuilder rrule, int dow, int wom) {
        rrule.append(";BYDAY=");
        boolean addComma = false;
        for (int i = 0; i < 7; i++) {
            if ((dow & 1) == 1) {
                if (addComma) {
                    rrule.append(',');
                }
                if (wom > 0) {
                    rrule.append(wom == 5 ? -1 : wom);
                }
                rrule.append(sDayTokens[i]);
                addComma = true;
            }
            dow >>= 1;
        }
    }
    static void addByMonthDay(StringBuilder rrule, int dom) {
        if (dom == 127) {
            dom = -1;
        }
        rrule.append(";BYMONTHDAY=" + dom);
    }
    static String generateEasDayOfWeek(String dow) {
        int bits = 0;
        int bit = 1;
        for (String token: sDayTokens) {
            if (dow.indexOf(token) >= 0) {
                bits |= bit;
            }
            bit <<= 1;
        }
        return Integer.toString(bits);
    }
    static String tokenFromRrule(String rrule, String token) {
        int start = rrule.indexOf(token);
        if (start < 0) return null;
        int len = rrule.length();
        start += token.length();
        int end = start;
        char c;
        do {
            c = rrule.charAt(end++);
            if ((c == ';') || (end == len)) {
                if (end == len) end++;
                return rrule.substring(start, end -1);
            }
        } while (true);
    }
    static String recurrenceUntilToEasUntil(String until) {
        StringBuilder sb = new StringBuilder();
        sb.append(until.substring(0, 4));
        sb.append(until.substring(4, 6));
        sb.append(until.substring(6, 8));
        sb.append("T000000Z");
        return sb.toString();
    }
    static void addUntil(String rrule, Serializer s) throws IOException {
        String until = tokenFromRrule(rrule, "UNTIL=");
        if (until != null) {
            s.data(Tags.CALENDAR_RECURRENCE_UNTIL, recurrenceUntilToEasUntil(until));
        }
    }
    static public void recurrenceFromRrule(String rrule, long startTime, Serializer s)
            throws IOException {
        if (Eas.USER_LOG) {
            SyncManager.log(TAG, "RRULE: " + rrule);
        }
        String freq = tokenFromRrule(rrule, "FREQ=");
        if (freq != null) {
            if (freq.equals("DAILY")) {
                s.start(Tags.CALENDAR_RECURRENCE);
                s.data(Tags.CALENDAR_RECURRENCE_TYPE, "0");
                s.data(Tags.CALENDAR_RECURRENCE_INTERVAL, "1");
                addUntil(rrule, s);
                s.end();
            } else if (freq.equals("WEEKLY")) {
                s.start(Tags.CALENDAR_RECURRENCE);
                s.data(Tags.CALENDAR_RECURRENCE_TYPE, "1");
                s.data(Tags.CALENDAR_RECURRENCE_INTERVAL, "1");
                String byDay = tokenFromRrule(rrule, "BYDAY=");
                if (byDay != null) {
                    s.data(Tags.CALENDAR_RECURRENCE_DAYOFWEEK, generateEasDayOfWeek(byDay));
                }
                addUntil(rrule, s);
                s.end();
            } else if (freq.equals("MONTHLY")) {
                String byMonthDay = tokenFromRrule(rrule, "BYMONTHDAY=");
                if (byMonthDay != null) {
                    s.start(Tags.CALENDAR_RECURRENCE);
                    s.data(Tags.CALENDAR_RECURRENCE_TYPE, "2");
                    s.data(Tags.CALENDAR_RECURRENCE_DAYOFMONTH, byMonthDay);
                    addUntil(rrule, s);
                    s.end();
                } else {
                    String byDay = tokenFromRrule(rrule, "BYDAY=");
                    String bareByDay;
                    if (byDay != null) {
                        int wom = byDay.charAt(0);
                        if (wom == '-') {
                            wom = 5;
                            bareByDay = byDay.substring(2);
                        } else {
                            wom = wom - '0';
                            bareByDay = byDay.substring(1);
                        }
                        s.start(Tags.CALENDAR_RECURRENCE);
                        s.data(Tags.CALENDAR_RECURRENCE_TYPE, "3");
                        s.data(Tags.CALENDAR_RECURRENCE_WEEKOFMONTH, Integer.toString(wom));
                        s.data(Tags.CALENDAR_RECURRENCE_DAYOFWEEK, generateEasDayOfWeek(bareByDay));
                        addUntil(rrule, s);
                        s.end();
                    }
                }
            } else if (freq.equals("YEARLY")) {
                String byMonth = tokenFromRrule(rrule, "BYMONTH=");
                String byMonthDay = tokenFromRrule(rrule, "BYMONTHDAY=");
                if (byMonth == null || byMonthDay == null) {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(startTime);
                    cal.setTimeZone(TimeZone.getDefault());
                    byMonth = Integer.toString(cal.get(Calendar.MONTH) + 1);
                    byMonthDay = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
                }
                s.start(Tags.CALENDAR_RECURRENCE);
                s.data(Tags.CALENDAR_RECURRENCE_TYPE, "5");
                s.data(Tags.CALENDAR_RECURRENCE_DAYOFMONTH, byMonthDay);
                s.data(Tags.CALENDAR_RECURRENCE_MONTHOFYEAR, byMonth);
                addUntil(rrule, s);
                s.end();
            }
        }
    }
    static public String rruleFromRecurrence(int type, int occurrences, int interval, int dow,
            int dom, int wom, int moy, String until) {
        StringBuilder rrule = new StringBuilder("FREQ=" + sTypeToFreq[type]);
        if (interval > 0) {
            rrule.append(";INTERVAL=" + interval);
        }
        if (occurrences > 0) {
            rrule.append(";COUNT=" + occurrences);
        }
        switch(type) {
            case 0: 
            case 1: 
                if (dow > 0) addByDay(rrule, dow, -1);
                break;
            case 2: 
                if (dom > 0) addByMonthDay(rrule, dom);
                break;
            case 3: 
                if (dow > 0) addByDay(rrule, dow, wom);
                break;
            case 5: 
                if (dom > 0) addByMonthDay(rrule, dom);
                if (moy > 0) {
                    rrule.append(";BYMONTH=" + moy);
                }
                break;
            case 6: 
                if (dow > 0) addByDay(rrule, dow, wom);
                if (dom > 0) addByMonthDay(rrule, dom);
                if (moy > 0) {
                    rrule.append(";BYMONTH=" + moy);
                }
                break;
            default:
                break;
        }
        if (until != null) {
            rrule.append(";UNTIL=" + until);
        }
        return rrule.toString();
    }
    static public long createCalendar(EasSyncService service, Account account, Mailbox mailbox) {
        ContentValues cv = new ContentValues();
        cv.put(Calendars.DISPLAY_NAME, account.mDisplayName);
        cv.put(Calendars._SYNC_ACCOUNT, account.mEmailAddress);
        cv.put(Calendars._SYNC_ACCOUNT_TYPE, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        cv.put(Calendars.SYNC_EVENTS, 1);
        cv.put(Calendars.SELECTED, 1);
        cv.put(Calendars.HIDDEN, 0);
        cv.put(Calendars.ORGANIZER_CAN_RESPOND, 0);
        cv.put(Calendars.COLOR, 0xFF000000 | Email.getAccountColor(account.mId));
        cv.put(Calendars.TIMEZONE, Time.getCurrentTimezone());
        cv.put(Calendars.ACCESS_LEVEL, Calendars.OWNER_ACCESS);
        cv.put(Calendars.OWNER_ACCOUNT, account.mEmailAddress);
        Uri uri = service.mContentResolver.insert(Calendars.CONTENT_URI, cv);
        if (uri != null) {
            String stringId = uri.getPathSegments().get(1);
            mailbox.mSyncStatus = stringId;
            return Long.parseLong(stringId);
        }
        return -1;
    }
    static public String getUidFromGlobalObjId(String globalObjId) {
        StringBuilder sb = new StringBuilder();
        try {
            byte[] idBytes = Base64.decode(globalObjId, Base64.DEFAULT);
            String idString = new String(idBytes);
            int index = idString.indexOf("vCal-Uid");
            if (index > 0) {
                return idString.substring(index + 12, idString.length() - 1);
            } else {
                for (byte b: idBytes) {
                    Utility.byteToHex(sb, b);
                }
                return sb.toString();
            }
        } catch (RuntimeException e) {
            return globalObjId;
        }
    }
    static public int attendeeStatusFromBusyStatus(int busyStatus) {
        int attendeeStatus;
        switch (busyStatus) {
            case BUSY_STATUS_BUSY:
                attendeeStatus = Attendees.ATTENDEE_STATUS_ACCEPTED;
                break;
            case BUSY_STATUS_TENTATIVE:
                attendeeStatus = Attendees.ATTENDEE_STATUS_TENTATIVE;
                break;
            case BUSY_STATUS_FREE:
            case BUSY_STATUS_OUT_OF_OFFICE:
            default:
                attendeeStatus = Attendees.ATTENDEE_STATUS_NONE;
        }
        return attendeeStatus;
    }
    static public int busyStatusFromAttendeeStatus(int selfAttendeeStatus) {
        int busyStatus;
        switch (selfAttendeeStatus) {
            case Attendees.ATTENDEE_STATUS_DECLINED:
            case Attendees.ATTENDEE_STATUS_NONE:
            case Attendees.ATTENDEE_STATUS_INVITED:
                busyStatus = BUSY_STATUS_FREE;
                break;
            case Attendees.ATTENDEE_STATUS_TENTATIVE:
                busyStatus = BUSY_STATUS_TENTATIVE;
                break;
            case Attendees.ATTENDEE_STATUS_ACCEPTED:
            default:
                busyStatus = BUSY_STATUS_BUSY;
                break;
        }
        return busyStatus;
    }
    static public String buildMessageTextFromEntityValues(Context context,
            ContentValues entityValues, StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        Resources resources = context.getResources();
        Date date = new Date(entityValues.getAsLong(Events.DTSTART));
        String dateTimeString = DateFormat.getDateTimeInstance().format(date);
        if (!entityValues.containsKey(Events.ORIGINAL_EVENT) &&
                entityValues.containsKey(Events.RRULE)) {
            sb.append(resources.getString(R.string.meeting_recurring, dateTimeString));
        } else {
            sb.append(resources.getString(R.string.meeting_when, dateTimeString));
        }
        String location = null;
        if (entityValues.containsKey(Events.EVENT_LOCATION)) {
            location = entityValues.getAsString(Events.EVENT_LOCATION);
            if (!TextUtils.isEmpty(location)) {
                sb.append("\n");
                sb.append(resources.getString(R.string.meeting_where, location));
            }
        }
        String desc = entityValues.getAsString(Events.DESCRIPTION);
        if (desc != null) {
            sb.append("\n--\n");
            sb.append(desc);
        }
        return sb.toString();
    }
    static private void addAttendeeToMessage(SimpleIcsWriter ics, ArrayList<Address> toList,
            String attendeeName, String attendeeEmail, int messageFlag, Account account) {
        if ((messageFlag & Message.FLAG_OUTGOING_MEETING_REQUEST_MASK) != 0) {
            String icalTag = ICALENDAR_ATTENDEE_INVITE;
            if ((messageFlag & Message.FLAG_OUTGOING_MEETING_CANCEL) != 0) {
                icalTag = ICALENDAR_ATTENDEE_CANCEL;
            }
            if (attendeeName != null) {
                icalTag += ";CN=" + SimpleIcsWriter.quoteParamValue(attendeeName);
            }
            ics.writeTag(icalTag, "MAILTO:" + attendeeEmail);
            toList.add(attendeeName == null ? new Address(attendeeEmail) :
                new Address(attendeeEmail, attendeeName));
        } else if (attendeeEmail.equalsIgnoreCase(account.mEmailAddress)) {
            String icalTag = null;
            switch (messageFlag) {
                case Message.FLAG_OUTGOING_MEETING_ACCEPT:
                    icalTag = ICALENDAR_ATTENDEE_ACCEPT;
                    break;
                case Message.FLAG_OUTGOING_MEETING_DECLINE:
                    icalTag = ICALENDAR_ATTENDEE_DECLINE;
                    break;
                case Message.FLAG_OUTGOING_MEETING_TENTATIVE:
                    icalTag = ICALENDAR_ATTENDEE_TENTATIVE;
                    break;
            }
            if (icalTag != null) {
                if (attendeeName != null) {
                    icalTag += ";CN="
                            + SimpleIcsWriter.quoteParamValue(attendeeName);
                }
                ics.writeTag(icalTag, "MAILTO:" + attendeeEmail);
            }
        }
    }
    static public EmailContent.Message createMessageForEntity(Context context, Entity entity,
            int messageFlag, String uid, Account account) {
        return createMessageForEntity(context, entity, messageFlag, uid, account,
                null );
    }
    static public EmailContent.Message createMessageForEntity(Context context, Entity entity,
            int messageFlag, String uid, Account account, String specifiedAttendee) {
        ContentValues entityValues = entity.getEntityValues();
        ArrayList<NamedContentValues> subValues = entity.getSubValues();
        boolean isException = entityValues.containsKey(Events.ORIGINAL_EVENT);
        boolean isReply = false;
        EmailContent.Message msg = new EmailContent.Message();
        msg.mFlags = messageFlag;
        msg.mTimeStamp = System.currentTimeMillis();
        String method;
        if ((messageFlag & EmailContent.Message.FLAG_OUTGOING_MEETING_INVITE) != 0) {
            method = "REQUEST";
        } else if ((messageFlag & EmailContent.Message.FLAG_OUTGOING_MEETING_CANCEL) != 0) {
            method = "CANCEL";
        } else {
            method = "REPLY";
            isReply = true;
        }
        try {
            SimpleIcsWriter ics = new SimpleIcsWriter();
            ics.writeTag("BEGIN", "VCALENDAR");
            ics.writeTag("METHOD", method);
            ics.writeTag("PRODID", "AndroidEmail");
            ics.writeTag("VERSION", "2.0");
            TimeZone vCalendarTimeZone = sGmtTimeZone;
            String vCalendarDateSuffix = "";
            boolean allDayEvent = false;
            if (entityValues.containsKey(Events.ALL_DAY)) {
                Integer ade = entityValues.getAsInteger(Events.ALL_DAY);
                allDayEvent = (ade != null) && (ade == 1);
                if (allDayEvent) {
                    vCalendarDateSuffix = ";VALUE=DATE";
                }
            }
            if (!isReply && !allDayEvent &&
                    (entityValues.containsKey(Events.RRULE) ||
                            entityValues.containsKey(Events.ORIGINAL_EVENT))) {
                vCalendarTimeZone = TimeZone.getDefault();
                timeZoneToVTimezone(vCalendarTimeZone, ics);
                vCalendarDateSuffix = ";TZID=" + vCalendarTimeZone.getID();
            }
            ics.writeTag("BEGIN", "VEVENT");
            if (uid == null) {
                uid = entityValues.getAsString(Events._SYNC_DATA);
            }
            if (uid != null) {
                ics.writeTag("UID", uid);
            }
            if (entityValues.containsKey("DTSTAMP")) {
                ics.writeTag("DTSTAMP", entityValues.getAsString("DTSTAMP"));
            } else {
                ics.writeTag("DTSTAMP", millisToEasDateTime(System.currentTimeMillis()));
            }
            long startTime = entityValues.getAsLong(Events.DTSTART);
            if (startTime != 0) {
                ics.writeTag("DTSTART" + vCalendarDateSuffix,
                        millisToEasDateTime(startTime, vCalendarTimeZone, !allDayEvent));
            }
            if (isException) {
                long originalTime = entityValues.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
                ics.writeTag("RECURRENCE-ID" + vCalendarDateSuffix,
                        millisToEasDateTime(originalTime, vCalendarTimeZone, !allDayEvent));
            }
            if (!entityValues.containsKey(Events.DURATION)) {
                if (entityValues.containsKey(Events.DTEND)) {
                    ics.writeTag("DTEND" + vCalendarDateSuffix,
                            millisToEasDateTime(
                                    entityValues.getAsLong(Events.DTEND), vCalendarTimeZone,
                                    !allDayEvent));
                }
            } else {
                long durationMillis = HOURS;
                Duration duration = new Duration();
                try {
                    duration.parse(entityValues.getAsString(Events.DURATION));
                } catch (ParseException e) {
                }
                ics.writeTag("DTEND" + vCalendarDateSuffix,
                        millisToEasDateTime(
                                startTime + durationMillis, vCalendarTimeZone, !allDayEvent));
            }
            String location = null;
            if (entityValues.containsKey(Events.EVENT_LOCATION)) {
                location = entityValues.getAsString(Events.EVENT_LOCATION);
                ics.writeTag("LOCATION", location);
            }
            String sequence = entityValues.getAsString(Events._SYNC_VERSION);
            if (sequence == null) {
                sequence = "0";
            }
            int titleId = 0;
            switch (messageFlag) {
                case Message.FLAG_OUTGOING_MEETING_INVITE:
                    if (!sequence.equals("0")) {
                        titleId = R.string.meeting_updated;
                    }
                    break;
                case Message.FLAG_OUTGOING_MEETING_ACCEPT:
                    titleId = R.string.meeting_accepted;
                    break;
                case Message.FLAG_OUTGOING_MEETING_DECLINE:
                    titleId = R.string.meeting_declined;
                    break;
                case Message.FLAG_OUTGOING_MEETING_TENTATIVE:
                    titleId = R.string.meeting_tentative;
                    break;
                case Message.FLAG_OUTGOING_MEETING_CANCEL:
                    titleId = R.string.meeting_canceled;
                    break;
            }
            Resources resources = context.getResources();
            String title = entityValues.getAsString(Events.TITLE);
            if (title == null) {
                title = "";
            }
            ics.writeTag("SUMMARY", title);
            if (titleId == 0) {
                msg.mSubject = title;
            } else {
                msg.mSubject = resources.getString(titleId, title);
            }
            StringBuilder sb = new StringBuilder();
            if (isException && !isReply) {
                Date date = new Date(entityValues.getAsLong(Events.ORIGINAL_INSTANCE_TIME));
                String dateString = DateFormat.getDateInstance().format(date);
                if (titleId == R.string.meeting_canceled) {
                    sb.append(resources.getString(R.string.exception_cancel, dateString));
                } else {
                    sb.append(resources.getString(R.string.exception_updated, dateString));
                }
                sb.append("\n\n");
            }
            String text =
                CalendarUtilities.buildMessageTextFromEntityValues(context, entityValues, sb);
            if (text.length() > 0) {
                ics.writeTag("DESCRIPTION", text);
            }
            msg.mText = text;
            if (!isReply) {
                if (entityValues.containsKey(Events.ALL_DAY)) {
                    Integer ade = entityValues.getAsInteger(Events.ALL_DAY);
                    ics.writeTag("X-MICROSOFT-CDO-ALLDAYEVENT", ade == 0 ? "FALSE" : "TRUE");
                }
                String rrule = entityValues.getAsString(Events.RRULE);
                if (rrule != null) {
                    ics.writeTag("RRULE", rrule);
                }
            }
            String organizerName = null;
            String organizerEmail = null;
            ArrayList<Address> toList = new ArrayList<Address>();
            for (NamedContentValues ncv: subValues) {
                Uri ncvUri = ncv.uri;
                ContentValues ncvValues = ncv.values;
                if (ncvUri.equals(Attendees.CONTENT_URI)) {
                    Integer relationship =
                        ncvValues.getAsInteger(Attendees.ATTENDEE_RELATIONSHIP);
                    if (relationship != null &&
                            ncvValues.containsKey(Attendees.ATTENDEE_EMAIL)) {
                        if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
                            organizerName = ncvValues.getAsString(Attendees.ATTENDEE_NAME);
                            organizerEmail = ncvValues.getAsString(Attendees.ATTENDEE_EMAIL);
                            continue;
                        }
                        String attendeeEmail = ncvValues.getAsString(Attendees.ATTENDEE_EMAIL);
                        String attendeeName = ncvValues.getAsString(Attendees.ATTENDEE_NAME);
                        if (attendeeEmail == null) continue;
                        if ((specifiedAttendee != null) &&
                                !attendeeEmail.equalsIgnoreCase(specifiedAttendee)) {
                            continue;
                        }
                        addAttendeeToMessage(ics, toList, attendeeName, attendeeEmail, messageFlag,
                                account);
                    }
                }
            }
            if (toList.isEmpty() && (specifiedAttendee != null)) {
                addAttendeeToMessage(ics, toList, null, specifiedAttendee, messageFlag, account);
            }
            if (organizerEmail != null) {
                String icalTag = "ORGANIZER";
                if (organizerName != null) {
                    icalTag += ";CN=" + SimpleIcsWriter.quoteParamValue(organizerName);
                }
                ics.writeTag(icalTag, "MAILTO:" + organizerEmail);
                if (isReply) {
                    toList.add(organizerName == null ? new Address(organizerEmail) :
                        new Address(organizerEmail, organizerName));
                }
            }
            if (toList.isEmpty()) return null;
            Address[] toArray = new Address[toList.size()];
            int i = 0;
            for (Address address: toList) {
                toArray[i++] = address;
            }
            msg.mTo = Address.pack(toArray);
            ics.writeTag("CLASS", "PUBLIC");
            ics.writeTag("STATUS", (messageFlag == Message.FLAG_OUTGOING_MEETING_CANCEL) ?
                    "CANCELLED" : "CONFIRMED");
            ics.writeTag("TRANSP", "OPAQUE"); 
            ics.writeTag("PRIORITY", "5");  
            ics.writeTag("SEQUENCE", sequence);
            ics.writeTag("END", "VEVENT");
            ics.writeTag("END", "VCALENDAR");
            Attachment att = new Attachment();
            att.mContentBytes = ics.getBytes();
            att.mMimeType = "text/calendar; method=" + method;
            att.mFileName = "invite.ics";
            att.mSize = att.mContentBytes.length;
            att.mFlags = Attachment.FLAG_ICS_ALTERNATIVE_PART;
            msg.mAttachments = new ArrayList<Attachment>();
            msg.mAttachments.add(att);
        } catch (IOException e) {
            Log.w(TAG, "IOException in createMessageForEntity");
            return null;
        }
        return msg;
    }
    static public EmailContent.Message createMessageForEventId(Context context, long eventId,
            int messageFlag, String uid, Account account) throws RemoteException {
        return createMessageForEventId(context, eventId, messageFlag, uid, account,
                null );
    }
    static public EmailContent.Message createMessageForEventId(Context context, long eventId,
            int messageFlag, String uid, Account account, String specifiedAttendee)
            throws RemoteException {
        ContentResolver cr = context.getContentResolver();
        EntityIterator eventIterator =
            EventsEntity.newEntityIterator(
                    cr.query(ContentUris.withAppendedId(Events.CONTENT_URI.buildUpon()
                            .appendQueryParameter(android.provider.Calendar.CALLER_IS_SYNCADAPTER,
                            "true").build(), eventId), null, null, null, null), cr);
        try {
            while (eventIterator.hasNext()) {
                Entity entity = eventIterator.next();
                return createMessageForEntity(context, entity, messageFlag, uid, account,
                        specifiedAttendee);
            }
        } finally {
            eventIterator.close();
        }
        return null;
    }
}

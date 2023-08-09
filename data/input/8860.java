public abstract class AbstractCalendar extends CalendarSystem {
    static final int SECOND_IN_MILLIS = 1000;
    static final int MINUTE_IN_MILLIS = SECOND_IN_MILLIS * 60;
    static final int HOUR_IN_MILLIS = MINUTE_IN_MILLIS * 60;
    static final int DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
    static final int EPOCH_OFFSET = 719163;
    private Era[] eras;
    protected AbstractCalendar() {
    }
    public Era getEra(String eraName) {
        if (eras != null) {
            for (int i = 0; i < eras.length; i++) {
                if (eras[i].equals(eraName)) {
                    return eras[i];
                }
            }
        }
        return null;
    }
    public Era[] getEras() {
        Era[] e = null;
        if (eras != null) {
            e = new Era[eras.length];
            System.arraycopy(eras, 0, e, 0, eras.length);
        }
        return e;
    }
    public void setEra(CalendarDate date, String eraName) {
        if (eras == null) {
            return; 
        }
        for (int i = 0; i < eras.length; i++) {
            Era e = eras[i];
            if (e != null && e.getName().equals(eraName)) {
                date.setEra(e);
                return;
            }
        }
        throw new IllegalArgumentException("unknown era name: " + eraName);
    }
    protected void setEras(Era[] eras) {
        this.eras = eras;
    }
    public CalendarDate getCalendarDate() {
        return getCalendarDate(System.currentTimeMillis(), newCalendarDate());
    }
    public CalendarDate getCalendarDate(long millis) {
        return getCalendarDate(millis, newCalendarDate());
    }
    public CalendarDate getCalendarDate(long millis, TimeZone zone) {
        CalendarDate date = newCalendarDate(zone);
        return getCalendarDate(millis, date);
    }
    public CalendarDate getCalendarDate(long millis, CalendarDate date) {
        int ms = 0;             
        int zoneOffset = 0;
        int saving = 0;
        long days = 0;          
        TimeZone zi = date.getZone();
        if (zi != null) {
            int[] offsets = new int[2];
            if (zi instanceof ZoneInfo) {
                zoneOffset = ((ZoneInfo)zi).getOffsets(millis, offsets);
            } else {
                zoneOffset = zi.getOffset(millis);
                offsets[0] = zi.getRawOffset();
                offsets[1] = zoneOffset - offsets[0];
            }
            days = zoneOffset / DAY_IN_MILLIS;
            ms = zoneOffset % DAY_IN_MILLIS;
            saving = offsets[1];
        }
        date.setZoneOffset(zoneOffset);
        date.setDaylightSaving(saving);
        days += millis / DAY_IN_MILLIS;
        ms += (int) (millis % DAY_IN_MILLIS);
        if (ms >= DAY_IN_MILLIS) {
            ms -= DAY_IN_MILLIS;
            ++days;
        } else {
            while (ms < 0) {
                ms += DAY_IN_MILLIS;
                --days;
            }
        }
        days += EPOCH_OFFSET;
        getCalendarDateFromFixedDate(date, days);
        setTimeOfDay(date, ms);
        date.setLeapYear(isLeapYear(date));
        date.setNormalized(true);
        return date;
    }
    public long getTime(CalendarDate date) {
        long gd = getFixedDate(date);
        long ms = (gd - EPOCH_OFFSET) * DAY_IN_MILLIS + getTimeOfDay(date);
        int zoneOffset = 0;
        TimeZone zi = date.getZone();
        if (zi != null) {
            if (date.isNormalized()) {
                return ms - date.getZoneOffset();
            }
            int[] offsets = new int[2];
            if (date.isStandardTime()) {
                if (zi instanceof ZoneInfo) {
                    ((ZoneInfo)zi).getOffsetsByStandard(ms, offsets);
                    zoneOffset = offsets[0];
                } else {
                    zoneOffset = zi.getOffset(ms - zi.getRawOffset());
                }
            } else {
                if (zi instanceof ZoneInfo) {
                    zoneOffset = ((ZoneInfo)zi).getOffsetsByWall(ms, offsets);
                } else {
                    zoneOffset = zi.getOffset(ms - zi.getRawOffset());
                }
            }
        }
        ms -= zoneOffset;
        getCalendarDate(ms, date);
        return ms;
    }
    protected long getTimeOfDay(CalendarDate date) {
        long fraction = date.getTimeOfDay();
        if (fraction != CalendarDate.TIME_UNDEFINED) {
            return fraction;
        }
        fraction = getTimeOfDayValue(date);
        date.setTimeOfDay(fraction);
        return fraction;
    }
    public long getTimeOfDayValue(CalendarDate date) {
        long fraction = date.getHours();
        fraction *= 60;
        fraction += date.getMinutes();
        fraction *= 60;
        fraction += date.getSeconds();
        fraction *= 1000;
        fraction += date.getMillis();
        return fraction;
    }
    public CalendarDate setTimeOfDay(CalendarDate cdate, int fraction) {
        if (fraction < 0) {
            throw new IllegalArgumentException();
        }
        boolean normalizedState = cdate.isNormalized();
        int time = fraction;
        int hours = time / HOUR_IN_MILLIS;
        time %= HOUR_IN_MILLIS;
        int minutes = time / MINUTE_IN_MILLIS;
        time %= MINUTE_IN_MILLIS;
        int seconds = time / SECOND_IN_MILLIS;
        time %= SECOND_IN_MILLIS;
        cdate.setHours(hours);
        cdate.setMinutes(minutes);
        cdate.setSeconds(seconds);
        cdate.setMillis(time);
        cdate.setTimeOfDay(fraction);
        if (hours < 24 && normalizedState) {
            cdate.setNormalized(normalizedState);
        }
        return cdate;
    }
    public int getWeekLength() {
        return 7;
    }
    protected abstract boolean isLeapYear(CalendarDate date);
    public CalendarDate getNthDayOfWeek(int nth, int dayOfWeek, CalendarDate date) {
        CalendarDate ndate = (CalendarDate) date.clone();
        normalize(ndate);
        long fd = getFixedDate(ndate);
        long nfd;
        if (nth > 0) {
            nfd = 7 * nth + getDayOfWeekDateBefore(fd, dayOfWeek);
        } else {
            nfd = 7 * nth + getDayOfWeekDateAfter(fd, dayOfWeek);
        }
        getCalendarDateFromFixedDate(ndate, nfd);
        return ndate;
    }
    static long getDayOfWeekDateBefore(long fixedDate, int dayOfWeek) {
        return getDayOfWeekDateOnOrBefore(fixedDate - 1, dayOfWeek);
    }
    static long getDayOfWeekDateAfter(long fixedDate, int dayOfWeek) {
        return getDayOfWeekDateOnOrBefore(fixedDate + 7, dayOfWeek);
    }
    public static long getDayOfWeekDateOnOrBefore(long fixedDate, int dayOfWeek) {
        long fd = fixedDate - (dayOfWeek - 1);
        if (fd >= 0) {
            return fixedDate - (fd % 7);
        }
        return fixedDate - CalendarUtils.mod(fd, 7);
    }
    protected abstract long getFixedDate(CalendarDate date);
    protected abstract void getCalendarDateFromFixedDate(CalendarDate date,
                                                         long fixedDate);
    public boolean validateTime(CalendarDate date) {
        int t = date.getHours();
        if (t < 0 || t >= 24) {
            return false;
        }
        t = date.getMinutes();
        if (t < 0 || t >= 60) {
            return false;
        }
        t = date.getSeconds();
        if (t < 0 || t >= 60) {
            return false;
        }
        t = date.getMillis();
        if (t < 0 || t >= 1000) {
            return false;
        }
        return true;
    }
    int normalizeTime(CalendarDate date) {
        long fraction = getTimeOfDay(date);
        long days = 0;
        if (fraction >= DAY_IN_MILLIS) {
            days = fraction / DAY_IN_MILLIS;
            fraction %= DAY_IN_MILLIS;
        } else if (fraction < 0) {
            days = CalendarUtils.floorDivide(fraction, DAY_IN_MILLIS);
            if (days != 0) {
                fraction -= DAY_IN_MILLIS * days; 
            }
        }
        if (days != 0) {
            date.setTimeOfDay(fraction);
        }
        date.setMillis((int)(fraction % 1000));
        fraction /= 1000;
        date.setSeconds((int)(fraction % 60));
        fraction /= 60;
        date.setMinutes((int)(fraction % 60));
        date.setHours((int)(fraction / 60));
        return (int)days;
    }
}

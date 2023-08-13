public class SimpleTimeZone extends TimeZone {
    private static final long serialVersionUID = -403250971215465050L;
    private int rawOffset;
    private int startYear, startMonth, startDay, startDayOfWeek, startTime;
    private int endMonth, endDay, endDayOfWeek, endTime;
    private int startMode, endMode;
    private static final int DOM_MODE = 1, DOW_IN_MONTH_MODE = 2,
            DOW_GE_DOM_MODE = 3, DOW_LE_DOM_MODE = 4;
    public static final int UTC_TIME = 2;
    public static final int STANDARD_TIME = 1;
    public static final int WALL_TIME = 0;
    private boolean useDaylight;
    private GregorianCalendar daylightSavings;
    private int dstSavings = 3600000;
    public SimpleTimeZone(int offset, final String name) {
        setID(name);
        rawOffset = offset;
    }
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime) {
        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, 3600000);
    }
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int endMonth,
            int endDay, int endDayOfWeek, int endTime, int daylightSavings) {
        this(offset, name);
        if (daylightSavings <= 0) {
            throw new IllegalArgumentException(Msg.getString(
                    "K00e9", daylightSavings)); 
        }
        dstSavings = daylightSavings;
        setStartRule(startMonth, startDay, startDayOfWeek, startTime);
        setEndRule(endMonth, endDay, endDayOfWeek, endTime);
    }
    public SimpleTimeZone(int offset, String name, int startMonth,
            int startDay, int startDayOfWeek, int startTime, int startTimeMode,
            int endMonth, int endDay, int endDayOfWeek, int endTime,
            int endTimeMode, int daylightSavings) {
        this(offset, name, startMonth, startDay, startDayOfWeek, startTime,
                endMonth, endDay, endDayOfWeek, endTime, daylightSavings);
        startMode = startTimeMode;
        endMode = endTimeMode;
    }
    @Override
    public Object clone() {
        SimpleTimeZone zone = (SimpleTimeZone) super.clone();
        if (daylightSavings != null) {
            zone.daylightSavings = (GregorianCalendar) daylightSavings.clone();
        }
        return zone;
    }
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) object;
        return getID().equals(tz.getID())
                && rawOffset == tz.rawOffset
                && useDaylight == tz.useDaylight
                && (!useDaylight || (startYear == tz.startYear
                        && startMonth == tz.startMonth
                        && startDay == tz.startDay && startMode == tz.startMode
                        && startDayOfWeek == tz.startDayOfWeek
                        && startTime == tz.startTime && endMonth == tz.endMonth
                        && endDay == tz.endDay
                        && endDayOfWeek == tz.endDayOfWeek
                        && endTime == tz.endTime && endMode == tz.endMode && dstSavings == tz.dstSavings));
    }
    @Override
    public int getDSTSavings() {
        if (!useDaylight) {
            return 0;
        }
        return dstSavings;
    }
    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int time) {
        if (era != GregorianCalendar.BC && era != GregorianCalendar.AD) {
            throw new IllegalArgumentException(Msg.getString("K00ea", era)); 
        }
        checkRange(month, dayOfWeek, time);
        if (month != Calendar.FEBRUARY || day != 29 || !isLeapYear(year)) {
            checkDay(month, day);
        }
        if (!useDaylightTime() || era != GregorianCalendar.AD
                || year < startYear) {
            return rawOffset;
        }
        if (endMonth < startMonth) {
            if (month > endMonth && month < startMonth) {
                return rawOffset;
            }
        } else {
            if (month < startMonth || month > endMonth) {
                return rawOffset;
            }
        }
        int ruleDay = 0, daysInMonth, firstDayOfMonth = mod7(dayOfWeek - day);
        if (month == startMonth) {
            switch (startMode) {
                case DOM_MODE:
                    ruleDay = startDay;
                    break;
                case DOW_IN_MONTH_MODE:
                    if (startDay >= 0) {
                        ruleDay = mod7(startDayOfWeek - firstDayOfMonth) + 1
                                + (startDay - 1) * 7;
                    } else {
                        daysInMonth = GregorianCalendar.DaysInMonth[startMonth];
                        if (startMonth == Calendar.FEBRUARY && isLeapYear(
                                year)) {
                            daysInMonth += 1;
                        }
                        ruleDay = daysInMonth
                                + 1
                                + mod7(startDayOfWeek
                                - (firstDayOfMonth + daysInMonth))
                                + startDay * 7;
                    }
                    break;
                case DOW_GE_DOM_MODE:
                    ruleDay = startDay
                            + mod7(startDayOfWeek
                            - (firstDayOfMonth + startDay - 1));
                    break;
                case DOW_LE_DOM_MODE:
                    ruleDay = startDay
                            + mod7(startDayOfWeek
                            - (firstDayOfMonth + startDay - 1));
                    if (ruleDay != startDay) {
                        ruleDay -= 7;
                    }
                    break;
            }
            if (ruleDay > day || ruleDay == day && time < startTime) {
                return rawOffset;
            }
        }
        int ruleTime = endTime - dstSavings;
        int nextMonth = (month + 1) % 12;
        if (month == endMonth || (ruleTime < 0 && nextMonth == endMonth)) {
            switch (endMode) {
                case DOM_MODE:
                    ruleDay = endDay;
                    break;
                case DOW_IN_MONTH_MODE:
                    if (endDay >= 0) {
                        ruleDay = mod7(endDayOfWeek - firstDayOfMonth) + 1
                                + (endDay - 1) * 7;
                    } else {
                        daysInMonth = GregorianCalendar.DaysInMonth[endMonth];
                        if (endMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                            daysInMonth++;
                        }
                        ruleDay = daysInMonth
                                + 1
                                + mod7(endDayOfWeek
                                - (firstDayOfMonth + daysInMonth)) + endDay
                                * 7;
                    }
                    break;
                case DOW_GE_DOM_MODE:
                    ruleDay = endDay
                            + mod7(
                            endDayOfWeek - (firstDayOfMonth + endDay - 1));
                    break;
                case DOW_LE_DOM_MODE:
                    ruleDay = endDay
                            + mod7(
                            endDayOfWeek - (firstDayOfMonth + endDay - 1));
                    if (ruleDay != endDay) {
                        ruleDay -= 7;
                    }
                    break;
            }
            int ruleMonth = endMonth;
            if (ruleTime < 0) {
                int changeDays = 1 - (ruleTime / 86400000);
                ruleTime = (ruleTime % 86400000) + 86400000;
                ruleDay -= changeDays;
                if (ruleDay <= 0) {
                    if (--ruleMonth < Calendar.JANUARY) {
                        ruleMonth = Calendar.DECEMBER;
                    }
                    ruleDay += GregorianCalendar.DaysInMonth[ruleMonth];
                    if (ruleMonth == Calendar.FEBRUARY && isLeapYear(year)) {
                        ruleDay++;
                    }
                }
            }
            if (month == ruleMonth) {
                if (ruleDay < day || ruleDay == day && time >= ruleTime) {
                    return rawOffset;
                }
            } else if (nextMonth != ruleMonth) {
                return rawOffset;
            }
        }
        return rawOffset + dstSavings;
    }
    @Override
    public int getOffset(long time) {
        if (!useDaylightTime()) {
            return rawOffset;
        }
        if (daylightSavings == null) {
            daylightSavings = new GregorianCalendar(this);
        }
        return daylightSavings.getOffset(time + rawOffset);
    }
    @Override
    public int getRawOffset() {
        return rawOffset;
    }
    @Override
    public synchronized int hashCode() {
        int hashCode = getID().hashCode() + rawOffset;
        if (useDaylight) {
            hashCode += startYear + startMonth + startDay + startDayOfWeek
                    + startTime + startMode + endMonth + endDay + endDayOfWeek
                    + endTime + endMode + dstSavings;
        }
        return hashCode;
    }
    @Override
    public boolean hasSameRules(TimeZone zone) {
        if (!(zone instanceof SimpleTimeZone)) {
            return false;
        }
        SimpleTimeZone tz = (SimpleTimeZone) zone;
        if (useDaylight != tz.useDaylight) {
            return false;
        }
        if (!useDaylight) {
            return rawOffset == tz.rawOffset;
        }
        return rawOffset == tz.rawOffset && dstSavings == tz.dstSavings
                && startYear == tz.startYear && startMonth == tz.startMonth
                && startDay == tz.startDay && startMode == tz.startMode
                && startDayOfWeek == tz.startDayOfWeek
                && startTime == tz.startTime && endMonth == tz.endMonth
                && endDay == tz.endDay && endDayOfWeek == tz.endDayOfWeek
                && endTime == tz.endTime && endMode == tz.endMode;
    }
    @Override
    public boolean inDaylightTime(Date time) {
        long millis = time.getTime();
        if (!useDaylightTime()) {
            return false;
        }
        if (daylightSavings == null) {
            daylightSavings = new GregorianCalendar(this);
        }
        return daylightSavings.getOffset(millis + rawOffset) != rawOffset;
    }
    private boolean isLeapYear(int year) {
        if (year > 1582) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }
        return year % 4 == 0;
    }
    private int mod7(int num1) {
        int rem = num1 % 7;
        return (num1 < 0 && rem < 0) ? 7 + rem : rem;
    }
    public void setDSTSavings(int milliseconds) {
        if (milliseconds > 0) {
            dstSavings = milliseconds;
        } else {
            throw new IllegalArgumentException();
        }
    }
    private void checkRange(int month, int dayOfWeek, int time) {
        if (month < Calendar.JANUARY || month > Calendar.DECEMBER) {
            throw new IllegalArgumentException(Msg.getString("K00e5", month)); 
        }
        if (dayOfWeek < Calendar.SUNDAY || dayOfWeek > Calendar.SATURDAY) {
            throw new IllegalArgumentException(Msg
                    .getString("K00e7", dayOfWeek)); 
        }
        if (time < 0 || time >= 24 * 3600000) {
            throw new IllegalArgumentException(Msg.getString("K00e8", time)); 
        }
    }
    private void checkDay(int month, int day) {
        if (day <= 0 || day > GregorianCalendar.DaysInMonth[month]) {
            throw new IllegalArgumentException(Msg.getString("K00e6", day)); 
        }
    }
    private void setEndMode() {
        if (endDayOfWeek == 0) {
            endMode = DOM_MODE;
        } else if (endDayOfWeek < 0) {
            endDayOfWeek = -endDayOfWeek;
            if (endDay < 0) {
                endDay = -endDay;
                endMode = DOW_LE_DOM_MODE;
            } else {
                endMode = DOW_GE_DOM_MODE;
            }
        } else {
            endMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (endDay != 0) {
            checkRange(endMonth, endMode == DOM_MODE ? 1 : endDayOfWeek,
                    endTime);
            if (endMode != DOW_IN_MONTH_MODE) {
                checkDay(endMonth, endDay);
            } else {
                if (endDay < -5 || endDay > 5) {
                    throw new IllegalArgumentException(Msg.getString(
                            "K00f8", endDay)); 
                }
            }
        }
        if (endMode != DOM_MODE) {
            endDayOfWeek--;
        }
    }
    public void setEndRule(int month, int dayOfMonth, int time) {
        endMonth = month;
        endDay = dayOfMonth;
        endDayOfWeek = 0; 
        endTime = time;
        setEndMode();
    }
    public void setEndRule(int month, int day, int dayOfWeek, int time) {
        endMonth = month;
        endDay = day;
        endDayOfWeek = dayOfWeek;
        endTime = time;
        setEndMode();
    }
    public void setEndRule(int month, int day, int dayOfWeek, int time,
            boolean after) {
        endMonth = month;
        endDay = after ? day : -day;
        endDayOfWeek = -dayOfWeek;
        endTime = time;
        setEndMode();
    }
    @Override
    public void setRawOffset(int offset) {
        rawOffset = offset;
    }
    private void setStartMode() {
        if (startDayOfWeek == 0) {
            startMode = DOM_MODE;
        } else if (startDayOfWeek < 0) {
            startDayOfWeek = -startDayOfWeek;
            if (startDay < 0) {
                startDay = -startDay;
                startMode = DOW_LE_DOM_MODE;
            } else {
                startMode = DOW_GE_DOM_MODE;
            }
        } else {
            startMode = DOW_IN_MONTH_MODE;
        }
        useDaylight = startDay != 0 && endDay != 0;
        if (startDay != 0) {
            checkRange(startMonth, startMode == DOM_MODE ? 1 : startDayOfWeek,
                    startTime);
            if (startMode != DOW_IN_MONTH_MODE) {
                checkDay(startMonth, startDay);
            } else {
                if (startDay < -5 || startDay > 5) {
                    throw new IllegalArgumentException(Msg.getString(
                            "K00f8", startDay)); 
                }
            }
        }
        if (startMode != DOM_MODE) {
            startDayOfWeek--;
        }
    }
    public void setStartRule(int month, int dayOfMonth, int time) {
        startMonth = month;
        startDay = dayOfMonth;
        startDayOfWeek = 0; 
        startTime = time;
        setStartMode();
    }
    public void setStartRule(int month, int day, int dayOfWeek, int time) {
        startMonth = month;
        startDay = day;
        startDayOfWeek = dayOfWeek;
        startTime = time;
        setStartMode();
    }
    public void setStartRule(int month, int day, int dayOfWeek, int time,
            boolean after) {
        startMonth = month;
        startDay = after ? day : -day;
        startDayOfWeek = -dayOfWeek;
        startTime = time;
        setStartMode();
    }
    public void setStartYear(int year) {
        startYear = year;
        useDaylight = true;
    }
    @Override
    public String toString() {
        return getClass().getName()
                + "[id=" 
                + getID()
                + ",offset=" 
                + rawOffset
                + ",dstSavings=" 
                + dstSavings
                + ",useDaylight=" 
                + useDaylight
                + ",startYear=" 
                + startYear
                + ",startMode=" 
                + startMode
                + ",startMonth=" 
                + startMonth
                + ",startDay=" 
                + startDay
                + ",startDayOfWeek=" 
                + (useDaylight && (startMode != DOM_MODE) ? startDayOfWeek + 1
                        : 0) + ",startTime=" + startTime + ",endMode=" 
                + endMode + ",endMonth=" + endMonth + ",endDay=" + endDay 
                + ",endDayOfWeek=" 
                + (useDaylight && (endMode != DOM_MODE) ? endDayOfWeek + 1 : 0)
                + ",endTime=" + endTime + "]"; 
    }
    @Override
    public boolean useDaylightTime() {
        return useDaylight;
    }
    private static final ObjectStreamField[] serialPersistentFields = {
            new ObjectStreamField("dstSavings", Integer.TYPE), 
            new ObjectStreamField("endDay", Integer.TYPE), 
            new ObjectStreamField("endDayOfWeek", Integer.TYPE), 
            new ObjectStreamField("endMode", Integer.TYPE), 
            new ObjectStreamField("endMonth", Integer.TYPE), 
            new ObjectStreamField("endTime", Integer.TYPE), 
            new ObjectStreamField("monthLength", byte[].class), 
            new ObjectStreamField("rawOffset", Integer.TYPE), 
            new ObjectStreamField("serialVersionOnStream", Integer.TYPE), 
            new ObjectStreamField("startDay", Integer.TYPE), 
            new ObjectStreamField("startDayOfWeek", Integer.TYPE), 
            new ObjectStreamField("startMode", Integer.TYPE), 
            new ObjectStreamField("startMonth", Integer.TYPE), 
            new ObjectStreamField("startTime", Integer.TYPE), 
            new ObjectStreamField("startYear", Integer.TYPE), 
            new ObjectStreamField("useDaylight", Boolean.TYPE), }; 
    private void writeObject(ObjectOutputStream stream) throws IOException {
        int sEndDay = endDay, sEndDayOfWeek = endDayOfWeek + 1, sStartDay = startDay, sStartDayOfWeek = startDayOfWeek + 1;
        if (useDaylight
                && (startMode != DOW_IN_MONTH_MODE || endMode != DOW_IN_MONTH_MODE)) {
            Calendar cal = new GregorianCalendar(this);
            if (endMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, endMonth);
                cal.set(Calendar.DATE, endDay);
                sEndDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (endMode == DOM_MODE) {
                    sEndDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
            if (startMode != DOW_IN_MONTH_MODE) {
                cal.set(Calendar.MONTH, startMonth);
                cal.set(Calendar.DATE, startDay);
                sStartDay = cal.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                if (startMode == DOM_MODE) {
                    sStartDayOfWeek = cal.getFirstDayOfWeek();
                }
            }
        }
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("dstSavings", dstSavings); 
        fields.put("endDay", sEndDay); 
        fields.put("endDayOfWeek", sEndDayOfWeek); 
        fields.put("endMode", endMode); 
        fields.put("endMonth", endMonth); 
        fields.put("endTime", endTime); 
        fields.put("monthLength", GregorianCalendar.DaysInMonth); 
        fields.put("rawOffset", rawOffset); 
        fields.put("serialVersionOnStream", 1); 
        fields.put("startDay", sStartDay); 
        fields.put("startDayOfWeek", sStartDayOfWeek); 
        fields.put("startMode", startMode); 
        fields.put("startMonth", startMonth); 
        fields.put("startTime", startTime); 
        fields.put("startYear", startYear); 
        fields.put("useDaylight", useDaylight); 
        stream.writeFields();
        stream.writeInt(4);
        byte[] values = new byte[4];
        values[0] = (byte) startDay;
        values[1] = (byte) (startMode == DOM_MODE ? 0 : startDayOfWeek + 1);
        values[2] = (byte) endDay;
        values[3] = (byte) (endMode == DOM_MODE ? 0 : endDayOfWeek + 1);
        stream.write(values);
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        rawOffset = fields.get("rawOffset", 0); 
        useDaylight = fields.get("useDaylight", false); 
        if (useDaylight) {
            endMonth = fields.get("endMonth", 0); 
            endTime = fields.get("endTime", 0); 
            startMonth = fields.get("startMonth", 0); 
            startTime = fields.get("startTime", 0); 
            startYear = fields.get("startYear", 0); 
        }
        if (fields.get("serialVersionOnStream", 0) == 0) { 
            if (useDaylight) {
                startMode = endMode = DOW_IN_MONTH_MODE;
                endDay = fields.get("endDay", 0); 
                endDayOfWeek = fields.get("endDayOfWeek", 0) - 1; 
                startDay = fields.get("startDay", 0); 
                startDayOfWeek = fields.get("startDayOfWeek", 0) - 1; 
            }
        } else {
            dstSavings = fields.get("dstSavings", 0); 
            if (useDaylight) {
                endMode = fields.get("endMode", 0); 
                startMode = fields.get("startMode", 0); 
                int length = stream.readInt();
                byte[] values = new byte[length];
                stream.readFully(values);
                if (length >= 4) {
                    startDay = values[0];
                    startDayOfWeek = values[1];
                    if (startMode != DOM_MODE) {
                        startDayOfWeek--;
                    }
                    endDay = values[2];
                    endDayOfWeek = values[3];
                    if (endMode != DOM_MODE) {
                        endDayOfWeek--;
                    }
                }
            }
        }
    }
}

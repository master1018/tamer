public abstract class TimeZone implements Serializable, Cloneable {
    private static final long serialVersionUID = 3581463369166924961L;
    public static final int SHORT = 0;
    public static final int LONG = 1;
    private static TimeZone Default;
    static TimeZone GMT = new SimpleTimeZone(0, "GMT"); 
    private String ID;
    public TimeZone() {
    }
    @Override
    public Object clone() {
        try {
            TimeZone zone = (TimeZone) super.clone();
            return zone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    public static synchronized String[] getAvailableIDs() {
        return ZoneInfoDB.getAvailableIDs();
    }
    public static synchronized String[] getAvailableIDs(int offset) {
        return ZoneInfoDB.getAvailableIDs(offset);
    }
    public static synchronized TimeZone getDefault() {
        if (Default == null) {
            Default = ZoneInfoDB.getDefault();
        }
        return (TimeZone) Default.clone();
    }
    public final String getDisplayName() {
        return getDisplayName(false, LONG, Locale.getDefault());
    }
    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }
    public final String getDisplayName(boolean daylightTime, int style) {
        return getDisplayName(daylightTime, style, Locale.getDefault());
    }
    public String getDisplayName(boolean daylightTime, int style, Locale locale) {
        if (style == SHORT || style == LONG) {
            boolean useDaylight = daylightTime && useDaylightTime();
            String result = Resources.getDisplayTimeZone(getID(), daylightTime, style, locale.toString());
            if (result != null) {
                return result;
            }
            int offset = getRawOffset();
            if (useDaylight && this instanceof SimpleTimeZone) {
                offset += ((SimpleTimeZone) this).getDSTSavings();
            }
            offset /= 60000;
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            StringBuffer buffer = new StringBuffer(9);
            buffer.append("GMT");
            buffer.append(sign);
            appendNumber(buffer, 2, offset / 60);
            buffer.append(':');
            appendNumber(buffer, 2, offset % 60);
            return buffer.toString();
        }
        throw new IllegalArgumentException();
    }
    private void appendNumber(StringBuffer buffer, int count, int value) {
        String string = Integer.toString(value);
        if (count > string.length()) {
            for (int i = 0; i < count - string.length(); i++) {
                buffer.append('0');
            }
        }
        buffer.append(string);
    }
    public String getID() {
        return ID;
    }
    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }
    public int getOffset(long time) {
        if (inDaylightTime(new Date(time))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }
    abstract public int getOffset(int era, int year, int month, int day,
            int dayOfWeek, int time);
    abstract public int getRawOffset();
    public static synchronized TimeZone getTimeZone(String name) {
        TimeZone zone = ZoneInfo.getTimeZone(name);
        if (zone == null) {
            if (name.startsWith("GMT") && name.length() > 3) {
                char sign = name.charAt(3);
                if (sign == '+' || sign == '-') {
                    int[] position = new int[1];
                    String formattedName = formatTimeZoneName(name, 4);
                    int hour = parseNumber(formattedName, 4, position);
                    if (hour < 0 || hour > 23) {
                        return (TimeZone) GMT.clone();
                    }
                    int index = position[0];
                    if (index != -1) {
                        int raw = hour * 3600000;
                        if (index < formattedName.length()
                                && formattedName.charAt(index) == ':') {
                            int minute = parseNumber(formattedName, index + 1,
                                    position);
                            if (position[0] == -1 || minute < 0 || minute > 59) {
                                return (TimeZone) GMT.clone();
                            }
                            raw += minute * 60000;
                        } else if (hour >= 30 || index > 6) {
                            raw = (hour / 100 * 3600000) + (hour % 100 * 60000);
                        }
                        if (sign == '-') {
                            raw = -raw;
                        }
                        return new SimpleTimeZone(raw, formattedName);
                    }
                }
            }
            zone = GMT;
        }
        return (TimeZone) zone.clone();
    }
    private static String formatTimeZoneName(String name, int offset) {
        StringBuilder buf = new StringBuilder();
        int index = offset, length = name.length();
        buf.append(name.substring(0, offset));
        while (index < length) {
            if (Character.digit(name.charAt(index), 10) != -1) {
                buf.append(name.charAt(index));
                if ((length - (index + 1)) == 2) {
                    buf.append(':');
                }
            } else if (name.charAt(index) == ':') {
                buf.append(':');
            }
            index++;
        }
        if (buf.toString().indexOf(":") == -1) {
            buf.append(':');
            buf.append("00");
        }
        if (buf.toString().indexOf(":") == 5) {
            buf.insert(4, '0');
        }
        return buf.toString();
    }
    public boolean hasSameRules(TimeZone zone) {
        if (zone == null) {
            return false;
        }
        return getRawOffset() == zone.getRawOffset();
    }
    abstract public boolean inDaylightTime(Date time);
    private static int parseNumber(String string, int offset, int[] position) {
        int index = offset, length = string.length(), digit, result = 0;
        while (index < length
                && (digit = Character.digit(string.charAt(index), 10)) != -1) {
            index++;
            result = result * 10 + digit;
        }
        position[0] = index == offset ? -1 : index;
        return result;
    }
    public static synchronized void setDefault(TimeZone timezone) {
        Default = timezone;
        ZoneInfoDB.setDefault(timezone);
    }
    public void setID(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        ID = name;
    }
    abstract public void setRawOffset(int offset);
    abstract public boolean useDaylightTime();
    private static native String getCustomTimeZone(int[] tzinfo,
            boolean[] isCustomTimeZone);
}

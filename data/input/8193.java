abstract public class TimeZone implements Serializable, Cloneable {
    public TimeZone() {
    }
    public static final int SHORT = 0;
    public static final int LONG  = 1;
    private static final int ONE_MINUTE = 60*1000;
    private static final int ONE_HOUR   = 60*ONE_MINUTE;
    private static final int ONE_DAY    = 24*ONE_HOUR;
    static final long serialVersionUID = 3581463369166924961L;
    public abstract int getOffset(int era, int year, int month, int day,
                                  int dayOfWeek, int milliseconds);
    public int getOffset(long date) {
        if (inDaylightTime(new Date(date))) {
            return getRawOffset() + getDSTSavings();
        }
        return getRawOffset();
    }
    int getOffsets(long date, int[] offsets) {
        int rawoffset = getRawOffset();
        int dstoffset = 0;
        if (inDaylightTime(new Date(date))) {
            dstoffset = getDSTSavings();
        }
        if (offsets != null) {
            offsets[0] = rawoffset;
            offsets[1] = dstoffset;
        }
        return rawoffset + dstoffset;
    }
    abstract public void setRawOffset(int offsetMillis);
    public abstract int getRawOffset();
    public String getID()
    {
        return ID;
    }
    public void setID(String ID)
    {
        if (ID == null) {
            throw new NullPointerException();
        }
        this.ID = ID;
    }
    public final String getDisplayName() {
        return getDisplayName(false, LONG,
                              Locale.getDefault(Locale.Category.DISPLAY));
    }
    public final String getDisplayName(Locale locale) {
        return getDisplayName(false, LONG, locale);
    }
    public final String getDisplayName(boolean daylight, int style) {
        return getDisplayName(daylight, style,
                              Locale.getDefault(Locale.Category.DISPLAY));
    }
    public String getDisplayName(boolean daylight, int style, Locale locale) {
        if (style != SHORT && style != LONG) {
            throw new IllegalArgumentException("Illegal style: " + style);
        }
        String id = getID();
        String[] names = getDisplayNames(id, locale);
        if (names == null) {
            if (id.startsWith("GMT")) {
                char sign = id.charAt(3);
                if (sign == '+' || sign == '-') {
                    return id;
                }
            }
            int offset = getRawOffset();
            if (daylight) {
                offset += getDSTSavings();
            }
            return ZoneInfoFile.toCustomID(offset);
        }
        int index = daylight ? 3 : 1;
        if (style == SHORT) {
            index++;
        }
        return names[index];
    }
    private static class DisplayNames {
        private static final Map<String, SoftReference<Map<Locale, String[]>>> CACHE =
            new ConcurrentHashMap<String, SoftReference<Map<Locale, String[]>>>();
    }
    private static final String[] getDisplayNames(String id, Locale locale) {
        Map<String, SoftReference<Map<Locale, String[]>>> displayNames = DisplayNames.CACHE;
        SoftReference<Map<Locale, String[]>> ref = displayNames.get(id);
        if (ref != null) {
            Map<Locale, String[]> perLocale = ref.get();
            if (perLocale != null) {
                String[] names = perLocale.get(locale);
                if (names != null) {
                    return names;
                }
                names = TimeZoneNameUtility.retrieveDisplayNames(id, locale);
                if (names != null) {
                    perLocale.put(locale, names);
                }
                return names;
            }
        }
        String[] names = TimeZoneNameUtility.retrieveDisplayNames(id, locale);
        if (names != null) {
            Map<Locale, String[]> perLocale = new ConcurrentHashMap<Locale, String[]>();
            perLocale.put(locale, names);
            ref = new SoftReference<Map<Locale, String[]>>(perLocale);
            displayNames.put(id, ref);
        }
        return names;
    }
    public int getDSTSavings() {
        if (useDaylightTime()) {
            return 3600000;
        }
        return 0;
    }
    public abstract boolean useDaylightTime();
    public boolean observesDaylightTime() {
        return useDaylightTime() || inDaylightTime(new Date());
    }
    abstract public boolean inDaylightTime(Date date);
    public static synchronized TimeZone getTimeZone(String ID) {
        return getTimeZone(ID, true);
    }
    private static TimeZone getTimeZone(String ID, boolean fallback) {
        TimeZone tz = ZoneInfo.getTimeZone(ID);
        if (tz == null) {
            tz = parseCustomTimeZone(ID);
            if (tz == null && fallback) {
                tz = new ZoneInfo(GMT_ID, 0);
            }
        }
        return tz;
    }
    public static synchronized String[] getAvailableIDs(int rawOffset) {
        return ZoneInfo.getAvailableIDs(rawOffset);
    }
    public static synchronized String[] getAvailableIDs() {
        return ZoneInfo.getAvailableIDs();
    }
    private static native String getSystemTimeZoneID(String javaHome,
                                                     String country);
    private static native String getSystemGMTOffsetID();
    public static TimeZone getDefault() {
        return (TimeZone) getDefaultRef().clone();
    }
    static TimeZone getDefaultRef() {
        TimeZone defaultZone = defaultZoneTL.get();
        if (defaultZone == null) {
            defaultZone = defaultTimeZone;
            if (defaultZone == null) {
                defaultZone = setDefaultZone();
                assert defaultZone != null;
            }
        }
        return defaultZone;
    }
    private static synchronized TimeZone setDefaultZone() {
        TimeZone tz = null;
        String zoneID = AccessController.doPrivileged(
                new GetPropertyAction("user.timezone"));
        if (zoneID == null || zoneID.equals("")) {
            String country = AccessController.doPrivileged(
                    new GetPropertyAction("user.country"));
            String javaHome = AccessController.doPrivileged(
                    new GetPropertyAction("java.home"));
            try {
                zoneID = getSystemTimeZoneID(javaHome, country);
                if (zoneID == null) {
                    zoneID = GMT_ID;
                }
            } catch (NullPointerException e) {
                zoneID = GMT_ID;
            }
        }
        tz = getTimeZone(zoneID, false);
        if (tz == null) {
            String gmtOffsetID = getSystemGMTOffsetID();
            if (gmtOffsetID != null) {
                zoneID = gmtOffsetID;
            }
            tz = getTimeZone(zoneID, true);
        }
        assert tz != null;
        final String id = zoneID;
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    System.setProperty("user.timezone", id);
                    return null;
                }
            });
        defaultTimeZone = tz;
        return tz;
    }
    private static boolean hasPermission() {
        boolean hasPermission = true;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkPermission(new PropertyPermission
                                   ("user.timezone", "write"));
            } catch (SecurityException e) {
                hasPermission = false;
            }
        }
        return hasPermission;
    }
    public static void setDefault(TimeZone zone)
    {
        if (hasPermission()) {
            synchronized (TimeZone.class) {
                defaultTimeZone = zone;
                defaultZoneTL.set(null);
            }
        } else {
            defaultZoneTL.set(zone);
        }
    }
    public boolean hasSameRules(TimeZone other) {
        return other != null && getRawOffset() == other.getRawOffset() &&
            useDaylightTime() == other.useDaylightTime();
    }
    public Object clone()
    {
        try {
            TimeZone other = (TimeZone) super.clone();
            other.ID = ID;
            return other;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
    static final TimeZone NO_TIMEZONE = null;
    private String           ID;
    private static volatile TimeZone defaultTimeZone;
    private static final InheritableThreadLocal<TimeZone> defaultZoneTL
                                        = new InheritableThreadLocal<TimeZone>();
    static final String         GMT_ID        = "GMT";
    private static final int    GMT_ID_LENGTH = 3;
    private static final TimeZone parseCustomTimeZone(String id) {
        int length;
        if ((length = id.length()) < (GMT_ID_LENGTH + 2) ||
            id.indexOf(GMT_ID) != 0) {
            return null;
        }
        ZoneInfo zi;
        zi = ZoneInfoFile.getZoneInfo(id);
        if (zi != null) {
            return zi;
        }
        int index = GMT_ID_LENGTH;
        boolean negative = false;
        char c = id.charAt(index++);
        if (c == '-') {
            negative = true;
        } else if (c != '+') {
            return null;
        }
        int hours = 0;
        int num = 0;
        int countDelim = 0;
        int len = 0;
        while (index < length) {
            c = id.charAt(index++);
            if (c == ':') {
                if (countDelim > 0) {
                    return null;
                }
                if (len > 2) {
                    return null;
                }
                hours = num;
                countDelim++;
                num = 0;
                len = 0;
                continue;
            }
            if (c < '0' || c > '9') {
                return null;
            }
            num = num * 10 + (c - '0');
            len++;
        }
        if (index != length) {
            return null;
        }
        if (countDelim == 0) {
            if (len <= 2) {
                hours = num;
                num = 0;
            } else {
                hours = num / 100;
                num %= 100;
            }
        } else {
            if (len != 2) {
                return null;
            }
        }
        if (hours > 23 || num > 59) {
            return null;
        }
        int gmtOffset =  (hours * 60 + num) * 60 * 1000;
        if (gmtOffset == 0) {
            zi = ZoneInfoFile.getZoneInfo(GMT_ID);
            if (negative) {
                zi.setID("GMT-00:00");
            } else {
                zi.setID("GMT+00:00");
            }
        } else {
            zi = ZoneInfoFile.getCustomTimeZone(id, negative ? -gmtOffset : gmtOffset);
        }
        return zi;
    }
}

public abstract class CalendarSystem {
    private volatile static boolean initialized = false;
    private static ConcurrentMap<String, String> names;
    private static ConcurrentMap<String,CalendarSystem> calendars;
    private static final String PACKAGE_NAME = "sun.util.calendar.";
    private static final String[] namePairs = {
        "gregorian", "Gregorian",
        "japanese", "LocalGregorianCalendar",
        "julian", "JulianCalendar",
    };
    private static void initNames() {
        ConcurrentMap<String,String> nameMap = new ConcurrentHashMap<String,String>();
        StringBuilder clName = new StringBuilder();
        for (int i = 0; i < namePairs.length; i += 2) {
            clName.setLength(0);
            String cl = clName.append(PACKAGE_NAME).append(namePairs[i+1]).toString();
            nameMap.put(namePairs[i], cl);
        }
        synchronized (CalendarSystem.class) {
            if (!initialized) {
                names = nameMap;
                calendars = new ConcurrentHashMap<String,CalendarSystem>();
                initialized = true;
            }
        }
    }
    private final static Gregorian GREGORIAN_INSTANCE = new Gregorian();
    public static Gregorian getGregorianCalendar() {
        return GREGORIAN_INSTANCE;
    }
    public static CalendarSystem forName(String calendarName) {
        if ("gregorian".equals(calendarName)) {
            return GREGORIAN_INSTANCE;
        }
        if (!initialized) {
            initNames();
        }
        CalendarSystem cal = calendars.get(calendarName);
        if (cal != null) {
            return cal;
        }
        String className = names.get(calendarName);
        if (className == null) {
            return null; 
        }
        if (className.endsWith("LocalGregorianCalendar")) {
            cal = LocalGregorianCalendar.getLocalGregorianCalendar(calendarName);
        } else {
            try {
                Class cl = Class.forName(className);
                cal = (CalendarSystem) cl.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("internal error", e);
            }
        }
        if (cal == null) {
            return null;
        }
        CalendarSystem cs =  calendars.putIfAbsent(calendarName, cal);
        return (cs == null) ? cal : cs;
    }
    public abstract String getName();
    public abstract CalendarDate getCalendarDate();
    public abstract CalendarDate getCalendarDate(long millis);
    public abstract CalendarDate getCalendarDate(long millis, CalendarDate date);
    public abstract CalendarDate getCalendarDate(long millis, TimeZone zone);
    public abstract CalendarDate newCalendarDate();
    public abstract CalendarDate newCalendarDate(TimeZone zone);
    public abstract long getTime(CalendarDate date);
    public abstract int getYearLength(CalendarDate date);
    public abstract int getYearLengthInMonths(CalendarDate date);
    public abstract int getMonthLength(CalendarDate date); 
    public abstract int getWeekLength();
    public abstract Era getEra(String eraName);
    public abstract Era[] getEras();
    public abstract void setEra(CalendarDate date, String eraName);
    public abstract CalendarDate getNthDayOfWeek(int nth, int dayOfWeek,
                                                 CalendarDate date);
    public abstract CalendarDate setTimeOfDay(CalendarDate date, int timeOfDay);
    public abstract boolean validate(CalendarDate date);
    public abstract boolean normalize(CalendarDate date);
}
